package io.legacyfighter.cabs.service;

import io.legacyfighter.cabs.config.AppProperties;
import io.legacyfighter.cabs.dto.AwardsAccountDTO;
import io.legacyfighter.cabs.entity.Client;
import io.legacyfighter.cabs.entity.Transit;
import io.legacyfighter.cabs.entity.miles.AwardedMiles;
import io.legacyfighter.cabs.entity.miles.AwardsAccount;
import io.legacyfighter.cabs.entity.miles.ConstantUntil;
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

        if (account == null || !account.isActive()) {
            return null;
        } else {
            Instant now = Instant.now(clock);
            Integer defaultMilesBonus = appProperties.getDefaultMilesBonus();
            Instant whenExpires = now.plus(appProperties.getMilesExpirationInDays(), ChronoUnit.DAYS);
            ConstantUntil miles = ConstantUntil.constantUntil(defaultMilesBonus, whenExpires);

            AwardedMiles awardedMiles = new AwardedMiles();
            awardedMiles.setClient(account.getClient());
            awardedMiles.setTransit(transit);
            awardedMiles.setDate(now);
            awardedMiles.setMiles(miles);

            milesRepository.save(awardedMiles);

            account.increaseTransactions();

            accountRepository.save(account);

            return awardedMiles;
        }
    }

    @Override
    public AwardedMiles registerNonExpiringMiles(Long clientId, Integer milesAmount) {
        AwardsAccount account = accountRepository.findByClient(clientRepository.getOne(clientId));

        if (account == null) {
            throw new IllegalArgumentException("Account does not exists, id = " + clientId);
        } else {
            Instant now = Instant.now(clock);
            ConstantUntil miles = ConstantUntil.constantUntilForever(milesAmount);

            AwardedMiles awardedMiles = new AwardedMiles();
            awardedMiles.setClient(account.getClient());
            awardedMiles.setTransit(null);
            awardedMiles.setDate(now);
            awardedMiles.setMiles(miles);

            milesRepository.save(awardedMiles);

            account.increaseTransactions();

            accountRepository.save(account);

            return awardedMiles;
        }
    }

    @Override
    @Transactional
    public void removeMiles(Long clientId, Integer milesAmount) {
        Client client = clientRepository.getOne(clientId);
        AwardsAccount account = accountRepository.findByClient(client);

        if (account == null) {
            throw new IllegalArgumentException("Account does not exists, id = " + clientId);
        } else {
            if (calculateBalance(clientId) >= milesAmount && Boolean.TRUE.equals(account.isActive())) {
                List<AwardedMiles> awardedMilesList = milesRepository.findAllByClient(client);
                int transitsCounter = transitRepository.findByClient(client).size();

                if (client.getClaims().size() >= 3) {
                    awardedMilesList.sort(Comparator.comparing(AwardedMiles::getExpirationDate, Comparators.nullsHigh()).reversed().thenComparing(Comparators.nullsHigh()));
                } else if (client.getType().equals(Client.Type.VIP)) {
                    awardedMilesList.sort(Comparator.comparing(AwardedMiles::cantExpire).thenComparing(AwardedMiles::getExpirationDate, Comparators.nullsLow()));
                } else if (transitsCounter >= 15 && isSunday()) {
                    awardedMilesList.sort(Comparator.comparing(AwardedMiles::cantExpire).thenComparing(AwardedMiles::getExpirationDate, Comparators.nullsLow()));
                } else if (transitsCounter >= 15) {
                    awardedMilesList.sort(Comparator.comparing(AwardedMiles::cantExpire).thenComparing(AwardedMiles::getDate));
                } else {
                    awardedMilesList.sort(Comparator.comparing(AwardedMiles::getDate));
                }

                Instant now = Instant.now(clock);

                for (AwardedMiles awardedMiles : awardedMilesList) {
                    if (milesAmount <= 0) {
                        break;
                    }

                    if (Boolean.TRUE.equals(awardedMiles.cantExpire()) || awardedMiles.getExpirationDate().isAfter(now)) {
                        Integer awardedMilesAmount = awardedMiles.getMilesAmount(now);

                        if (awardedMilesAmount <= milesAmount) {
                            milesAmount -= awardedMilesAmount;
                            awardedMiles.removeAllMiles(now);
                        } else {
                            awardedMiles.subtract(milesAmount, now);
                            milesAmount = 0;
                        }

                        milesRepository.save(awardedMiles);
                    }
                }
            } else {
                throw new IllegalArgumentException("Insufficient milesAmount, id = " + clientId + ", milesAmount requested = " + milesAmount);
            }
        }

    }

    @Override
    public Integer calculateBalance(Long clientId) {
        Client client = clientRepository.getOne(clientId);
        List<AwardedMiles> awardedMilesList = milesRepository.findAllByClient(client);
        Instant now = Instant.now(clock);

        return awardedMilesList.stream()
                .filter(awardedMiles -> (awardedMiles.getExpirationDate() != null && awardedMiles.getExpirationDate().isAfter(now))
                        || Boolean.TRUE.equals(awardedMiles.cantExpire()))
                .map(awardedMiles -> awardedMiles.getMilesAmount(now))
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
            List<AwardedMiles> awardedMilesList = milesRepository.findAllByClient(fromClient);
            Instant now = Instant.now(clock);

            for (AwardedMiles awardedMiles : awardedMilesList) {
                if ((awardedMiles.getExpirationDate() != null && awardedMiles.getExpirationDate().isAfter(now))
                        || Boolean.TRUE.equals(awardedMiles.cantExpire())) {

                    Integer awardedMilesAmount = awardedMiles.getMilesAmount(now);

                    if (awardedMilesAmount <= milesAmount) {
                        awardedMiles.setClient(accountTo.getClient());

                        milesAmount -= awardedMilesAmount;
                    } else {
                        awardedMiles.subtract(milesAmount, now);

                        AwardedMiles transferredAwardedMiles = new AwardedMiles();
                        transferredAwardedMiles.setClient(accountTo.getClient());
                        transferredAwardedMiles.setMiles(awardedMiles.getMiles());

                        milesRepository.save(transferredAwardedMiles);

                        milesAmount -= awardedMilesAmount;
                    }
                    
                    milesRepository.save(awardedMiles);
                }
            }

            accountFrom.increaseTransactions();
            accountTo.increaseTransactions();

            accountRepository.save(accountFrom);
            accountRepository.save(accountTo);
        }
    }

    private boolean isSunday() {
        return Instant.now(clock)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .getDayOfWeek()
                .equals(DayOfWeek.SUNDAY);
    }
}
