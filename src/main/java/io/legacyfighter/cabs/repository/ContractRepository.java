package io.legacyfighter.cabs.repository;

import io.legacyfighter.cabs.entity.contract.Contract;
import io.legacyfighter.cabs.entity.contract.ContractAttachmentDecision;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public class ContractRepository {

    private final ContractEntityRepository contractEntityRepository;

    private final ContractAttachmentRepository contractAttachmentRepository;

    public ContractRepository(ContractEntityRepository contractEntityRepository,
                              ContractAttachmentRepository contractAttachmentRepository) {
        this.contractEntityRepository = contractEntityRepository;
        this.contractAttachmentRepository = contractAttachmentRepository;
    }

    public Contract save(Contract contract) {
        return contractEntityRepository.save(contract);
    }

    public Contract getOne(Long id) {
        return contractEntityRepository.getOne(id);
    }

    public int countByPartnerName(String partnerName) {
        return contractEntityRepository.countByPartnerName(partnerName);
    }

    public Contract findByAttachmentId(Long attachmentId) {
        return contractEntityRepository.findByAttachmentId(attachmentId);
    }

    public UUID findContractAttachmentNoByAttachmentId(Long attachmentId) {
        return contractAttachmentRepository.findContractAttachmentNoUsingAttachmentId(attachmentId);
    }
}

interface ContractEntityRepository extends JpaRepository<Contract, Long> {

    int countByPartnerName(String partnerName);

    @Query("SELECT c " +
            "FROM Contract c " +
            "JOIN ContractAttachmentDecision cad " +
            "ON cad.contract.id = c.id " +
            "WHERE cad.id = ?1")
    Contract findByAttachmentId(Long attachmentId);
}

interface ContractAttachmentRepository extends JpaRepository<ContractAttachmentDecision, Long> {

    @Query("SELECT cad.contractAttachmentNo " +
            "FROM ContractAttachmentDecision cad " +
            "WHERE cad.id = ?1")
    UUID findContractAttachmentNoUsingAttachmentId(Long attachmentId);
}
