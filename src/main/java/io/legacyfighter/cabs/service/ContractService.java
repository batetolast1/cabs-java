package io.legacyfighter.cabs.service;

import io.legacyfighter.cabs.dto.ContractAttachmentDTO;
import io.legacyfighter.cabs.dto.ContractDTO;
import io.legacyfighter.cabs.entity.contract.Contract;
import io.legacyfighter.cabs.entity.contract.ContractAttachmentData;
import io.legacyfighter.cabs.entity.contract.ContractAttachmentDecision;
import io.legacyfighter.cabs.repository.ContractAttachmentDataRepository;
import io.legacyfighter.cabs.repository.ContractRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.Clock;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;

@Service
public class ContractService {

    private final ContractRepository contractRepository;

    private final ContractAttachmentDataRepository contractAttachmentDataRepository;

    private final Clock clock;

    public ContractService(ContractRepository contractRepository,
                           ContractAttachmentDataRepository contractAttachmentDataRepository,
                           Clock clock) {
        this.contractRepository = contractRepository;
        this.contractAttachmentDataRepository = contractAttachmentDataRepository;
        this.clock = clock;
    }

    @Transactional
    public Contract createContract(ContractDTO contractDTO) {
        int partnerContractsCount = contractRepository.countByPartnerName(contractDTO.getPartnerName()) + 1;
        String contractNo = "C/" + partnerContractsCount + "/" + contractDTO.getPartnerName();

        Contract contract = new Contract(contractDTO.getPartnerName(), contractDTO.getSubject(), contractNo, Instant.now(clock));

        return contractRepository.save(contract);
    }

    @Transactional
    public void acceptContract(Long id) {
        Contract contract = find(id);
        contract.accept();
    }

    @Transactional
    public void rejectContract(Long id) {
        Contract contract = find(id);
        contract.reject();
    }

    @Transactional
    public Contract find(Long id) {
        Contract contract = contractRepository.getOne(id);
        if (contract == null) {
            throw new IllegalStateException("Contract does not exist");
        }
        return contract;
    }

    @Transactional
    public ContractDTO findDto(Long id) {
        Contract contract = find(id);
        Set<ContractAttachmentData> contractAttachmentDataSet = contractAttachmentDataRepository.findByContractAttachmentNoIn(contract.getContractAttachmentNos());
        return new ContractDTO(contract, contractAttachmentDataSet);
    }

    @Transactional
    public ContractAttachmentDTO proposeAttachment(Long contractId, ContractAttachmentDTO contractAttachmentDTO) {
        Contract contract = find(contractId);
        UUID contractAttachmentNo = contract.proposeAttachment();
        contract = contractRepository.save(contract);

        ContractAttachmentDecision contractAttachmentDecision = contract.findContractAttachmentDecision(contractAttachmentNo);

        ContractAttachmentData contractAttachmentData = new ContractAttachmentData(contractAttachmentNo, contractAttachmentDTO.getData(), Instant.now(clock));
        ContractAttachmentData savedContractAttachmentData = contractAttachmentDataRepository.save(contractAttachmentData);

        return new ContractAttachmentDTO(contractAttachmentDecision, savedContractAttachmentData);
    }

    @Transactional
    public void acceptAttachment(Long attachmentId) {
        Contract contract = contractRepository.findByAttachmentId(attachmentId);

        if (contract == null) {
            throw new EntityNotFoundException();
        }

        UUID contractAttachmentNo = contractRepository.findContractAttachmentNoByAttachmentId(attachmentId);

        contract.acceptAttachment(contractAttachmentNo);
    }

    @Transactional
    public void rejectAttachment(Long attachmentId) {
        Contract contract = contractRepository.findByAttachmentId(attachmentId);

        if (contract == null) {
            throw new EntityNotFoundException();
        }

        UUID contractAttachmentNo = contractRepository.findContractAttachmentNoByAttachmentId(attachmentId);

        contract.rejectAttachment(contractAttachmentNo);
    }

    @Transactional
    public void removeAttachment(Long contractId, Long attachmentId) {
        //TODO sprawdzenie czy nalezy do kontraktu (JIRA: II-14455)
        Contract contract = find(contractId);
        UUID contractAttachmentNo = contractRepository.findContractAttachmentNoByAttachmentId(attachmentId);
        contract.removeAttachment(contractAttachmentNo);
        contractAttachmentDataRepository.deleteByContractAttachmentNo(attachmentId);
    }
}
