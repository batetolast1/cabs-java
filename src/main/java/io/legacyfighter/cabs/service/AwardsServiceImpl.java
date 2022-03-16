package io.legacyfighter.cabs.service;

import io.legacyfighter.cabs.config.AppProperties;
import io.legacyfighter.cabs.dto.AwardsAccountDTO;
import io.legacyfighter.cabs.entity.AwardedMiles;
import io.legacyfighter.cabs.entity.AwardsAccount;
import io.legacyfighter.cabs.entity.Client;
import io.legacyfighter.cabs.entity.Transit;
import io.legacyfighter.cabs.repository.AwardedMilesRepository;
import io.legacyfighter.cabs.repository.AwardsAccountRepository;
import io.legacyfighter.cabs.repository.ClientRepository;
import io.legacyfighter.cabs.repository.TransitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.comparator.Comparators;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.List;

@Service
public class AwardsServiceImpl implements AwardsService {

    private final AwardsAccountRepository accountRepository;
    private final AwardedMilesRepository milesRepository;
    private final ClientRepository clientRepository;
    private final TransitRepository transitRepository;
    private final Clock clock;
    private final AppProperties appProperties;

    public AwardsServiceImpl(AwardsAccountRepository accountRepository,
                             AwardedMilesRepository milesRepository,
                             ClientRepository clientRepository,
                             TransitRepository transitRepository,
                             Clock clock,
                             AppProperties appProperties) {
        this.accountRepository = accountRepository;
        this.milesRepository = milesRepository;
        this.clientRepository = clientRepository;
        this.transitRepository = transitRepository;
        this.clock = clock;
        this.appProperties = appProperties;
    }

    @Override
    public AwardsAccountDTO findBy(Long clientId) {
        return new AwardsAccountDTO(accountRepository.findByClient(clientRepository.getOne(clientId)));
    }

    @Override
    public void registerToProgram(Long clientId) {
        Client client = clientRepository.getOne(clientId);

        if (client == null) {
            throw new IllegalArgumentException("Client does not exists, id = " + clientId);
        }

        AwardsAccount account = new AwardsAccount();

        account.setClient(client);
        account.setActive(false);
        account.setDate(Instant.now(clock));

        accountRepository.save(account);
    }

    @Override
    @Transactional
    public void activateAccount(Long clientId) {
        AwardsAccount account = accountRepository.findByClient(clientRepository.getOne(clientId));

        if (account == null) {
            throw new IllegalArgumentException("Account does not exists, id = " + clientId);
        }

        account.setActive(true);

        accountRepository.save(account);
    }

    @Override
    @Transactional
    public void deactivateAccount(Long clientId) {
        AwardsAccount account = accountRepository.findByClient(clientRepository.getOne(clientId));

        if (account == null) {
            throw new IllegalArgumentException("Account does not exists, id = " + clientId);
        }

        account.setActive(false);

        accountRepository.save(account);
    }

    @Override
    public AwardedMiles registerMiles(Long clientId, Long transitId) {
        AwardsAccount account = accountRepository.findByClient(clientRepository.getOne(clientId));
        Transit transit = transitRepository.getOne(transitId);
        if (transit == null) {
            throw new IllegalArgumentException("transit does not exists, id = " + transitId);
        }

        Instant now = Instant.now(clock);
        if (account == null || !account.isActive()) {
            return null;
        } else {
            AwardedMiles miles = new AwardedMiles();
            miles.setTransit(transit);
            miles.setDate(Instant.now(clock));
            miles.setClient(account.getClient());
            miles.setMiles(appProperties.getDefaultMilesBonus());
            miles.setExpirationDate(now.plus(appProperties.getMilesExpirationInDays(), ChronoUnit.DAYS));
            miles.setCantExpire(false);
            account.increaseTransactions();

            milesRepository.save(miles);
            accountRepository.save(account);
            return miles;
        }
    }

    @Override
    public AwardedMiles registerNonExpiringMiles(Long clientId, Integer milesAmount) {
        AwardsAccount account = accountRepository.findByClient(clientRepository.getOne(clientId));

        if (account == null) {
            throw new IllegalArgumentException("Account does not exists, id = " + clientId);
        } else {
            AwardedMiles miles = new AwardedMiles();
            miles.setTransit(null);
            miles.setClient(account.getClient());
            miles.setMiles(milesAmount);
            miles.setDate(Instant.now(clock));
            miles.setCantExpire(true);
            account.increaseTransactions();
            milesRepository.save(miles);
            accountRepository.save(account);
            return miles;
        }
    }

    @Override
    @Transactional
    public void removeMiles(Long clientId, Integer miles) {
        Client client = clientRepository.getOne(clientId);
        AwardsAccount account = accountRepository.findByClient(client);

        if (account == null) {
            throw new IllegalArgumentException("Account does not exists, id = " + clientId);
        } else {
            if (calculateBalance(clientId) >= miles && Boolean.TRUE.equals(account.isActive())) {
                List<AwardedMiles> milesList = milesRepository.findAllByClient(client);
                int transitsCounter = transitRepository.findByClient(client).size();
                if (client.getClaims().size() >= 3) {
                    milesList.sort(Comparator.comparing(AwardedMiles::getExpirationDate, Comparators.nullsHigh()).reversed().thenComparing(Comparators.nullsHigh()));
                } else if (client.getType().equals(Client.Type.VIP)) {
                    milesList.sort(Comparator.comparing(AwardedMiles::cantExpire).thenComparing(AwardedMiles::getExpirationDate, Comparators.nullsLow()));
                } else if (transitsCounter >= 15 && isSunday()) {
                    milesList.sort(Comparator.comparing(AwardedMiles::cantExpire).thenComparing(AwardedMiles::getExpirationDate, Comparators.nullsLow()));
                } else if (transitsCounter >= 15) {
                    milesList.sort(Comparator.comparing(AwardedMiles::cantExpire).thenComparing(AwardedMiles::getDate));
                } else {
                    milesList.sort(Comparator.comparing(AwardedMiles::getDate));
                }
                for (AwardedMiles iter : milesList) {
                    if (miles <= 0) {
                        break;
                    }
                    if (Boolean.TRUE.equals(iter.cantExpire()) || iter.getExpirationDate().isAfter(Instant.now(clock))) {
                        if (iter.getMiles() <= miles) {
                            miles -= iter.getMiles();
                            iter.setMiles(0);
                        } else {
                            iter.setMiles(iter.getMiles() - miles);
                            miles = 0;
                        }
                        milesRepository.save(iter);
                    }
                }
            } else {
                throw new IllegalArgumentException("Insufficient miles, id = " + clientId + ", miles requested = " + miles);
            }
        }

    }

    @Override
    public Integer calculateBalance(Long clientId) {
        Client client = clientRepository.getOne(clientId);
        List<AwardedMiles> milesList = milesRepository.findAllByClient(client);

        return milesList.stream()
                .filter(t -> t.getExpirationDate() != null && t.getExpirationDate().isAfter(Instant.now(clock)) || t.cantExpire())
                .map(AwardedMiles::getMiles)
                .reduce(0, Integer::sum);
    }

    @Override
    public void transferMiles(Long fromClientId, Long toClientId, Integer milesAmount) {
        Client fromClient = clientRepository.getOne(fromClientId);
        AwardsAccount accountFrom = accountRepository.findByClient(fromClient);
        AwardsAccount accountTo = accountRepository.findByClient(clientRepository.getOne(toClientId));
        if (accountFrom == null) {
            throw new IllegalArgumentException("Account does not exists, id = " + fromClientId);
        }
        if (accountTo == null) {
            throw new IllegalArgumentException("Account does not exists, id = " + toClientId);
        }

        if (calculateBalance(fromClientId) >= milesAmount && Boolean.TRUE.equals(accountFrom.isActive())) {
            List<AwardedMiles> milesList = milesRepository.findAllByClient(fromClient);

            for (AwardedMiles iter : milesList) {
                if (Boolean.TRUE.equals(iter.cantExpire()) || iter.getExpirationDate().isAfter(Instant.now(clock))) {
                    if (iter.getMiles() <= milesAmount) {
                        iter.setClient(accountTo.getClient());
                        milesAmount -= iter.getMiles();
                    } else {
                        iter.setMiles(iter.getMiles() - milesAmount);
                        AwardedMiles miles = new AwardedMiles();

                        miles.setClient(accountTo.getClient());
                        miles.setCantExpire(iter.cantExpire());
                        miles.setExpirationDate(iter.getExpirationDate());
                        miles.setMiles(milesAmount);

                        milesAmount -= iter.getMiles();

                        milesRepository.save(miles);

                    }
                    milesRepository.save(iter);
                }
            }

            accountFrom.increaseTransactions();
            accountTo.increaseTransactions();

            accountRepository.save(accountFrom);
            accountRepository.save(accountTo);
        }
    }

    boolean isSunday() {
        return Instant.now(clock).atZone(ZoneId.systemDefault()).toLocalDate().getDayOfWeek().equals(DayOfWeek.SUNDAY);
    }
}
