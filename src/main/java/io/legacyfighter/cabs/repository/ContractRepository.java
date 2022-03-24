package io.legacyfighter.cabs.repository;

import io.legacyfighter.cabs.entity.contract.Contract;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ContractRepository extends JpaRepository<Contract, Long> {

    int countByPartnerName(String partnerName);

    @Query("SELECT c " +
            "FROM Contract c " +
            "JOIN ContractAttachment ca " +
            "ON ca.contract.id = c.id " +
            "WHERE ca.id = ?1")
    Contract findByAttachmentId(Long attachmentId);
}
