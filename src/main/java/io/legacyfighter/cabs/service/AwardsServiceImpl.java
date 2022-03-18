package io.legacyfighter.cabs.service;

import io.legacyfighter.cabs.config.AppProperties;
import io.legacyfighter.cabs.dto.AwardsAccountDTO;
import io.legacyfighter.cabs.entity.Client;
import io.legacyfighter.cabs.entity.Transit;
import io.legacyfighter.cabs.entity.miles.AwardedMiles;
import io.legacyfighter.cabs.entity.miles.AwardsAccount;
import io.legacyfighter.cabs.repository.AwardedMilesRepository;
import io.legacyfighter.cabs.repository.AwardsAccountRepository;
import io.legacyfighter.cabs.repository.ClientRepository;
import io.legacyfighter.cabs.repository.TransitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;

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

        Instant createdAt = Instant.now(clock);

        AwardsAccount account = AwardsAccount.notActiveAccount(client, createdAt);

        accountRepository.save(account);
    }

    @Override
    @Transactional
    public void activateAccount(Long clientId) {
        AwardsAccount account = accountRepository.findByClient(clientRepository.getOne(clientId));

        if (account == null) {
            throw new IllegalArgumentException("Account does not exists, id = " + clientId);
        }

        account.activate();

        accountRepository.save(account);
    }

    @Override
    @Transactional
    public void deactivateAccount(Long clientId) {
        AwardsAccount account = accountRepository.findByClient(clientRepository.getOne(clientId));

        if (account == null) {
            throw new IllegalArgumentException("Account does not exists, id = " + clientId);
        }

        account.deactivate();

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
        }

        Instant at = Instant.now(clock);
        Integer milesAmount = appProperties.getDefaultMilesBonus();
        Instant expiresAt = at.plus(appProperties.getMilesExpirationInDays(), ChronoUnit.DAYS);

        AwardedMiles awardedMiles = account.addExpiringMiles(milesAmount, at, expiresAt, transit);

        accountRepository.save(account);

        return awardedMiles;
    }

    @Override
    public AwardedMiles registerNonExpiringMiles(Long clientId, Integer milesAmount) {
        AwardsAccount account = accountRepository.findByClient(clientRepository.getOne(clientId));

        if (account == null) {
            throw new IllegalArgumentException("Account does not exists, id = " + clientId);
        }

        Instant at = Instant.now(clock);

        AwardedMiles awardedMiles = account.addNonExpiringMiles(milesAmount, at);

        accountRepository.save(account);

        return awardedMiles;
    }

    @Override
    @Transactional
    public void removeMiles(Long clientId, Integer milesAmountToRemove) {
        Client client = clientRepository.getOne(clientId);
        AwardsAccount account = accountRepository.findByClient(client);

        if (account == null) {
            throw new IllegalArgumentException("Account does not exists, id = " + clientId);
        }

        Instant at = Instant.now(clock);
        Integer transitCount = transitRepository.countByClient(client);
        int claimCount = client.getClaims().size();
        Client.Type clientType = client.getType();

        account.remove(milesAmountToRemove, at, transitCount, claimCount, clientType, isSunday());
    }

    @Override
    public Integer calculateBalance(Long clientId) {
        Client client = clientRepository.getOne(clientId);
        AwardsAccount awardsAccount = accountRepository.findByClient(client);
        Instant at = Instant.now(clock);

        return awardsAccount.calculateBalance(at);
    }

    @Override
    public void transferMiles(Long fromClientId, Long toClientId, Integer milesAmountToTransfer) {
        Client fromClient = clientRepository.getOne(fromClientId);
        Client toClient = clientRepository.getOne(toClientId);
        AwardsAccount accountFrom = accountRepository.findByClient(fromClient);
        AwardsAccount accountTo = accountRepository.findByClient(toClient);

        if (accountFrom == null) {
            throw new IllegalArgumentException("Account does not exists, id = " + fromClientId);
        }
        if (accountTo == null) {
            throw new IllegalArgumentException("Account does not exists, id = " + toClientId);
        }

        Instant at = Instant.now(clock);

        accountFrom.moveMilesTo(accountTo, milesAmountToTransfer, at);

        accountRepository.save(accountFrom);
        accountRepository.save(accountTo);
    }

    private boolean isSunday() {
        return Instant.now(clock)
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .getDayOfWeek()
                .equals(DayOfWeek.SUNDAY);
    }
}
