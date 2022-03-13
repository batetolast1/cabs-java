package io.legacyfighter.cabs.repository;

import io.legacyfighter.cabs.entity.ClaimsResolver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ClaimsResolverRepository extends JpaRepository<ClaimsResolver, Long> {

    Optional<ClaimsResolver> findByClientId(Long clientId);
}
