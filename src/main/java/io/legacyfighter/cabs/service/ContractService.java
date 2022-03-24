package io.legacyfighter.cabs.service;

import io.legacyfighter.cabs.dto.ContractAttachmentDTO;
import io.legacyfighter.cabs.dto.ContractDTO;
import io.legacyfighter.cabs.entity.contract.Contract;
import io.legacyfighter.cabs.entity.contract.ContractAttachment;
import io.legacyfighter.cabs.repository.ContractAttachmentRepository;
import io.legacyfighter.cabs.repository.ContractRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;

@Service
public class ContractService {

    private final ContractRepository contractRepository;
    private final ContractAttachmentRepository contractAttachmentRepository;

    public ContractService(ContractRepository contractRepository,
                           ContractAttachmentRepository contractAttachmentRepository) {
        this.contractRepository = contractRepository;
        this.contractAttachmentRepository = contractAttachmentRepository;
    }

    @Transactional
    public Contract createContract(ContractDTO contractDTO) {
        int partnerContractsCount = contractRepository.countByPartnerName(contractDTO.getPartnerName()) + 1;
        String contractNo = "C/" + partnerContractsCount + "/" + contractDTO.getPartnerName();

        Contract contract = new Contract(contractDTO.getPartnerName(), contractDTO.getSubject(), contractNo);

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
        return new ContractDTO(find(id), contractAttachmentRepository.findByContractId(id));
    }

    @Transactional
    public ContractAttachmentDTO proposeAttachment(Long contractId, ContractAttachmentDTO contractAttachmentDTO) {
        Contract contract = find(contractId);
        ContractAttachment contractAttachment = contract.proposeAttachment(contractAttachmentDTO.getData());
        return new ContractAttachmentDTO(contractAttachmentRepository.save(contractAttachment));
    }

    @Transactional
    public void acceptAttachment(Long attachmentId) {
        Contract contract = contractRepository.findByAttachmentId(attachmentId);

        if (contract == null) {
            throw new EntityNotFoundException();
        }

        contract.acceptAttachment(attachmentId);
    }

    @Transactional
    public void rejectAttachment(Long attachmentId) {
        Contract contract = contractRepository.findByAttachmentId(attachmentId);

        if (contract == null) {
            throw new EntityNotFoundException();
        }

        contract.rejectAttachment(attachmentId);
    }

    @Transactional
    public void removeAttachment(Long contractId, Long attachmentId) {
        //TODO sprawdzenie czy nalezy do kontraktu (JIRA: II-14455)
        contractAttachmentRepository.deleteById(attachmentId);
    }
}
