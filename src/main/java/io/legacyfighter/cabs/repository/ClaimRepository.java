package io.legacyfighter.cabs.repository;

import io.legacyfighter.cabs.entity.Claim;
import io.legacyfighter.cabs.entity.Client;
import io.legacyfighter.cabs.entity.Transit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClaimRepository extends JpaRepository<Claim, Long> {

    List<Claim> findByOwner(Client owner);

    List<Claim> findByOwnerAndTransit(Client owner, Transit transit);

    Integer countByOwner(Client owner);
}
