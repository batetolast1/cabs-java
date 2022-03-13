package io.legacyfighter.cabs.service;


import io.legacyfighter.cabs.config.AppProperties;
import io.legacyfighter.cabs.dto.ClaimDTO;
import io.legacyfighter.cabs.entity.Claim;
import io.legacyfighter.cabs.entity.ClaimsResolver;
import io.legacyfighter.cabs.entity.Client;
import io.legacyfighter.cabs.entity.Transit;
import io.legacyfighter.cabs.repository.ClaimRepository;
import io.legacyfighter.cabs.repository.ClaimsResolverRepository;
import io.legacyfighter.cabs.repository.ClientRepository;
import io.legacyfighter.cabs.repository.TransitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

import static io.legacyfighter.cabs.entity.Claim.Status.ESCALATED;

@Service
public class ClaimService {

    private final Clock clock;
    private final ClientRepository clientRepository;
    private final TransitRepository transitRepository;
    private final ClaimRepository claimRepository;
    private final ClaimNumberGenerator claimNumberGenerator;
    private final AppProperties appProperties;
    private final AwardsService awardsService;
    private final ClientNotificationService clientNotificationService;
    private final DriverNotificationService driverNotificationService;
    private final ClaimsResolverRepository claimsResolverRepository;

    public ClaimService(Clock clock,
                        ClientRepository clientRepository,
                        TransitRepository transitRepository,
                        ClaimRepository claimRepository,
                        ClaimNumberGenerator claimNumberGenerator,
                        AppProperties appProperties,
                        AwardsService awardsService,
                        ClientNotificationService clientNotificationService,
                        DriverNotificationService driverNotificationService,
                        ClaimsResolverRepository claimsResolverRepository) {
        this.clock = clock;
        this.clientRepository = clientRepository;
        this.transitRepository = transitRepository;
        this.claimRepository = claimRepository;
        this.claimNumberGenerator = claimNumberGenerator;
        this.appProperties = appProperties;
        this.awardsService = awardsService;
        this.clientNotificationService = clientNotificationService;
        this.driverNotificationService = driverNotificationService;
        this.claimsResolverRepository = claimsResolverRepository;
    }

    public Claim create(ClaimDTO claimDTO) {
        Claim claim = new Claim();
        claim.setCreationDate(Instant.now(clock));
        claim.setClaimNo(claimNumberGenerator.generate(claim));
        claim = update(claimDTO, claim);
        return claim;
    }

    public Claim find(Long id) {
        Claim claim = claimRepository.getOne(id);
        if (claim == null) {
            throw new IllegalStateException("Claim does not exists");
        }
        return claim;
    }

    public Claim update(ClaimDTO claimDTO, Claim claim) {
        Client client = clientRepository.getOne(claimDTO.getClientId());
        Transit transit = transitRepository.getOne(claimDTO.getTransitId());
        if (client == null) {
            throw new IllegalStateException("Client does not exists");
        }
        if (transit == null) {
            throw new IllegalStateException("Transit does not exists");
        }
        if (claimDTO.isDraft()) {
            claim.setStatus(Claim.Status.DRAFT);
        } else {
            claim.setStatus(Claim.Status.NEW);
        }
        claim.setOwner(client);
        claim.setTransit(transit);
        claim.setCreationDate(Instant.now(clock));
        claim.setReason(claimDTO.getReason());
        claim.setIncidentDescription(claimDTO.getIncidentDescription());
        return claimRepository.save(claim);
    }

    @Transactional
    public Claim setStatus(Claim.Status newStatus, Long id) {
        Claim claim = find(id);
        claim.setStatus(newStatus);
        return claim;
    }

    @Transactional
    public Claim tryToResolveAutomatically(Long id) {
        Claim claim = find(id);
        ClaimsResolver claimsResolver = findOrCreateClaimsResolver(claim.getOwner());
        Integer numberOfTransitsByClient = transitRepository.countByClient(claim.getOwner());
        Integer automaticRefundForVipThreshold = appProperties.getAutomaticRefundForVipThreshold();
        Integer numberOfTransitsForClaimAutomaticRefund = appProperties.getNoOfTransitsForClaimAutomaticRefund();

        ClaimsResolver.Result result = claimsResolver.resolve(claim, numberOfTransitsByClient, automaticRefundForVipThreshold, numberOfTransitsForClaimAutomaticRefund);

        if (result.getDecision() == Claim.Status.REFUNDED) {
            claim.refund();
            clientNotificationService.notifyClientAboutRefund(claim.getClaimNo(), claim.getOwner().getId());
        }
        if (result.getDecision() == ESCALATED) {
            claim.escalate();
        }

        if (result.getAwardedMiles() == ClaimsResolver.AwardedMiles.EXTRA_MILES) {
            awardsService.registerSpecialMiles(claim.getOwner().getId(), 10);
        }

        if (result.getWhoToAsk() == ClaimsResolver.WhoToAsk.ASK_CLIENT) {
            clientNotificationService.askForMoreInformation(claim.getClaimNo(), claim.getOwner().getId());

        }
        if (result.getWhoToAsk() == ClaimsResolver.WhoToAsk.ASK_DRIVER) {
            driverNotificationService.askDriverForDetailsAboutClaim(claim.getClaimNo(), claim.getTransit().getDriver().getId());
        }

        return claim;
    }

    private ClaimsResolver findOrCreateClaimsResolver(Client client) {
        return claimsResolverRepository.findByClientId(client.getId())
                .orElseGet(() -> claimsResolverRepository.save(new ClaimsResolver(client.getId())));
    }
}
