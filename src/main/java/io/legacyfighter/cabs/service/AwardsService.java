package io.legacyfighter.cabs.service;

import io.legacyfighter.cabs.dto.AwardsAccountDTO;
import io.legacyfighter.cabs.entity.miles.AwardedMiles;

public interface AwardsService {

    AwardsAccountDTO findBy(Long clientId);

    void registerToProgram(Long clientId);

    void activateAccount(Long clientId);

    void deactivateAccount(Long clientId);

    AwardedMiles registerMiles(Long clientId, Long transitId);

    AwardedMiles registerNonExpiringMiles(Long clientId, Integer milesAmount);

    void removeMiles(Long clientId, Integer milesAmount);

    Integer calculateBalance(Long clientId);

    void transferMiles(Long fromClientId, Long toClientId, Integer milesAmount);
}
