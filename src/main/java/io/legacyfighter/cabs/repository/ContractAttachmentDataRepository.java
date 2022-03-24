package io.legacyfighter.cabs.repository;

import io.legacyfighter.cabs.entity.contract.ContractAttachmentData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Repository
public interface ContractAttachmentDataRepository extends JpaRepository<ContractAttachmentData, Long> {

    Set<ContractAttachmentData> findByContractAttachmentNoIn(List<UUID> contractAttachmentNos);

    @Modifying
    @Query("DELETE FROM ContractAttachmentData cad " +
            "WHERE cad.contractAttachmentNo = (" +
            "SELECT cad.contractAttachmentNo " +
            "FROM ContractAttachmentDecision cad " +
            "WHERE cad.id = ?1" +
            ")")
    void deleteByContractAttachmentNo(Long contractAttachmentNo);
}
