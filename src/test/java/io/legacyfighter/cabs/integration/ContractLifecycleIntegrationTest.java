package io.legacyfighter.cabs.integration;

import io.legacyfighter.cabs.common.Fixtures;
import io.legacyfighter.cabs.dto.ContractAttachmentDTO;
import io.legacyfighter.cabs.dto.ContractDTO;
import io.legacyfighter.cabs.entity.contract.Contract;
import io.legacyfighter.cabs.entity.contract.ContractAttachmentDecision;
import io.legacyfighter.cabs.service.ContractService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.Objects;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@SpringBootTest
class ContractLifecycleIntegrationTest {

    @Autowired
    private Fixtures fixtures;

    @Autowired
    private ContractService contractService;

    @Test
    void canCreateContract() {
        // given
        ContractDTO contractDTO = aContractDTO();

        // when
        Contract contract = contractService.createContract(contractDTO);

        // then
        ContractDTO loaded = loadContract(contract);
        assertThat(loaded.getId()).isEqualTo(contract.getId());
        assertThat(loaded.getPartnerName()).isEqualTo(contractDTO.getPartnerName());
        assertThat(loaded.getSubject()).isEqualTo("subject");
        assertThat(loaded.getContractNo()).isEqualTo("C/1/" + contractDTO.getPartnerName());
        assertThat(loaded.getStatus()).isEqualTo(Contract.Status.NEGOTIATIONS_IN_PROGRESS);
        assertThat(loaded.getCreationDate()).isNotNull();
        assertThat(loaded.getAttachments()).isEmpty();
        assertThat(loaded.getAcceptedAt()).isNull();
        assertThat(loaded.getRejectedAt()).isNull();
        assertThat(loaded.getChangeDate()).isNull();
    }

    @Test
    void canGenerateCorrectContractNo() {
        // given
        fixtures.aContractFor("partner");
        fixtures.aContractFor("partner");
        fixtures.aContractFor("different partner");
        // and
        ContractDTO contractDTO = aContractDTO("partner");

        // when
        Contract contract = contractService.createContract(contractDTO);

        // then
        ContractDTO loaded = loadContract(contract);
        assertThat(loaded.getContractNo()).isEqualTo("C/3/partner");
    }

    @Test
    void cannotAcceptNonExistingContract() {
        // when
        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> contractService.acceptContract(0L));
    }

    @Test
    void canAcceptContractWithoutAttachments() {
        // given
        Contract contract = fixtures.aContractFor("partner name");

        // when
        contractService.acceptContract(contract.getId());

        // then
        ContractDTO loaded = loadContract(contract);
        assertThat(loaded.getStatus()).isEqualTo(Contract.Status.ACCEPTED);
        assertThat(loaded.getAcceptedAt()).isNull(); // TODO should not be?
    }

    @Test
    void cannotAcceptContractWithProposedAttachment() {
        // given
        Contract contract = fixtures.aContractFor("partner name");
        Long contractId = contract.getId();
        // and
        fixtures.aProposedAttachmentFor(contract);

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> contractService.acceptContract(contractId));
    }

    @Test
    void cannotAcceptContractWithAttachmentAcceptedByOneSide() {
        // given
        Contract contract = fixtures.aContractFor("partner name");
        Long contractId = contract.getId();
        // and
        fixtures.anAttachmentAcceptedByOneSideFor(contract);

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> contractService.acceptContract(contractId));
    }

    @Test
    void canAcceptContractWithAttachmentAcceptedByBothSides() {
        // given
        Contract contract = fixtures.aContractFor("partner name");
        // and
        fixtures.anAttachmentAcceptedByBothSidesFor(contract);

        // when
        contractService.acceptContract(contract.getId());

        // then
        ContractDTO loaded = loadContract(contract);
        assertThat(loaded.getStatus()).isEqualTo(Contract.Status.ACCEPTED);
        assertThat(loaded.getAcceptedAt()).isNull(); // TODO should not be?
    }

    @Test
    void cannotAcceptContractWithRejectedAttachment() {
        // given
        Contract contract = fixtures.aContractFor("partner name");
        Long contractId = contract.getId();
        // and
        fixtures.aRejectedAttachmentFor(contract);

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(() -> contractService.acceptContract(contractId));
    }

    @Test
    void canAcceptRejectedContract() {
        // given
        Contract contract = fixtures.aRejectedContractFor("partner name");

        // when
        contractService.acceptContract(contract.getId());

        // then
        ContractDTO loaded = loadContract(contract);
        assertThat(loaded.getStatus()).isEqualTo(Contract.Status.ACCEPTED);
        assertThat(loaded.getAcceptedAt()).isNull(); // TODO should not be?
    }

    @Test
    void canAcceptAcceptedContract() {
        // given
        Contract contract = fixtures.anAcceptedContractFor("partner name");

        // when
        contractService.acceptContract(contract.getId());

        // then
        ContractDTO loaded = loadContract(contract);
        assertThat(loaded.getStatus()).isEqualTo(Contract.Status.ACCEPTED);
        assertThat(loaded.getAcceptedAt()).isNull(); // TODO should not be?
    }

    @Test
    void cannotRejectNonExistingContract() {
        // when
        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> contractService.rejectContract(0L));
    }

    @Test
    void canRejectContract() {
        // given
        Contract contract = fixtures.aContractFor("partner name");

        // when
        contractService.rejectContract(contract.getId());

        // then
        ContractDTO loaded = loadContract(contract);
        assertThat(loaded.getStatus()).isEqualTo(Contract.Status.REJECTED);
        assertThat(loaded.getRejectedAt()).isNull(); // TODO should not be?
    }

    @Test
    void canRejectAcceptedContract() {
        // given
        Contract contract = fixtures.anAcceptedContractFor("partner name");

        // when
        contractService.rejectContract(contract.getId());

        // then
        ContractDTO loaded = loadContract(contract);
        assertThat(loaded.getStatus()).isEqualTo(Contract.Status.REJECTED);
        assertThat(loaded.getAcceptedAt()).isNull(); // TODO should not be?
    }

    @Test
    void canRejectRejectedContract() {
        // given
        Contract contract = fixtures.aRejectedContractFor("partner name");

        // when
        contractService.rejectContract(contract.getId());

        // then
        ContractDTO loaded = loadContract(contract);
        assertThat(loaded.getStatus()).isEqualTo(Contract.Status.REJECTED);
        assertThat(loaded.getAcceptedAt()).isNull(); // TODO should not be?
    }

    @Test
    void cannotProposeAttachmentToNonExistingContract() {
        // given
        ContractAttachmentDTO contractAttachmentDTO = new ContractAttachmentDTO();

        // when
        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> contractService.proposeAttachment(0L, contractAttachmentDTO));
    }

    @Test
    void canProposeAttachment() {
        // given
        Contract contract = fixtures.aContractFor("partner name");
        // and
        ContractAttachmentDTO contractAttachmentDTO = aContractAttachmentDTO();

        // when
        ContractAttachmentDTO result = contractService.proposeAttachment(contract.getId(), contractAttachmentDTO);

        // then
        ContractDTO loadedContract = loadContract(contract);
        assertThat(loadedContract.getAttachments()).hasSize(1);

        ContractAttachmentDTO loadedAttachment = findContractAttachment(loadedContract, result.getId());
        assertThat(loadedAttachment).isNotNull();
        assertThat(loadedAttachment.getId()).isEqualTo(result.getId());
        assertThat(loadedAttachment.getData()).isEqualTo(contractAttachmentDTO.getData());
        assertThat(loadedAttachment.getStatus()).isEqualTo(ContractAttachmentDecision.Status.PROPOSED);
        assertThat(loadedAttachment.getContractId()).isEqualTo(contract.getId());
        assertThat(loadedAttachment.getCreationDate()).isNotNull();
        assertThat(loadedAttachment.getChangeDate()).isNull();
        assertThat(loadedAttachment.getRejectedAt()).isNull();
        assertThat(loadedAttachment.getAcceptedAt()).isNull();
    }

    @Test
    void canProposeAttachmentToAcceptedContract() {
        // given
        Contract contract = fixtures.anAcceptedContractFor("partner name");
        // and
        ContractAttachmentDTO contractAttachmentDTO = aContractAttachmentDTO();

        // when
        ContractAttachmentDTO result = contractService.proposeAttachment(contract.getId(), contractAttachmentDTO);

        // then
        ContractDTO loadedContract = loadContract(contract);
        assertThat(loadedContract.getAttachments()).hasSize(1);
        ContractAttachmentDTO loadedAttachment = findContractAttachment(loadedContract, result.getId());
        assertThat(loadedAttachment).isNotNull();
        assertThat(loadedAttachment.getStatus()).isEqualTo(ContractAttachmentDecision.Status.PROPOSED);
    }

    @Test
    void canProposeAttachmentToRejectedContract() {
        // given
        Contract contract = fixtures.aRejectedContractFor("partner name");
        // and
        ContractAttachmentDTO contractAttachmentDTO = aContractAttachmentDTO();

        // when
        ContractAttachmentDTO result = contractService.proposeAttachment(contract.getId(), contractAttachmentDTO);

        // then
        ContractDTO loadedContract = loadContract(contract);
        assertThat(loadedContract.getAttachments()).hasSize(1);
        ContractAttachmentDTO loadedAttachment = findContractAttachment(loadedContract, result.getId());
        assertThat(loadedAttachment).isNotNull();
        assertThat(loadedAttachment.getStatus()).isEqualTo(ContractAttachmentDecision.Status.PROPOSED);
    }

    @Test
    void cannotRejectNonExistingAttachment() {
        // when
        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> contractService.rejectAttachment(0L));
    }

    @Test
    void canRejectAttachment() {
        // given
        Contract contract = fixtures.aContractFor("partner name");
        // and
        ContractAttachmentDTO contractAttachmentDTO = fixtures.aProposedAttachmentFor(contract);

        // when
        contractService.rejectAttachment(contractAttachmentDTO.getId());

        // then
        ContractDTO loadedContract = loadContract(contract);
        assertThat(loadedContract.getAttachments()).hasSize(1);
        ContractAttachmentDTO loadedAttachment = findContractAttachment(loadedContract, contractAttachmentDTO.getId());
        assertThat(loadedAttachment).isNotNull();
        assertThat(loadedAttachment.getStatus()).isEqualTo(ContractAttachmentDecision.Status.REJECTED);
        assertThat(loadedAttachment.getRejectedAt()).isNull(); // TODO should not be?
    }

    @Test
    void canRejectAttachmentAcceptedByOneSide() {
        // given
        Contract contract = fixtures.aContractFor("partner name");
        // and
        ContractAttachmentDTO contractAttachmentDTO = fixtures.anAttachmentAcceptedByOneSideFor(contract);

        // when
        contractService.rejectAttachment(contractAttachmentDTO.getId());

        // then
        ContractDTO loadedContract = loadContract(contract);
        assertThat(loadedContract.getAttachments()).hasSize(1);
        ContractAttachmentDTO loadedAttachment = findContractAttachment(loadedContract, contractAttachmentDTO.getId());
        assertThat(loadedAttachment).isNotNull();
        assertThat(loadedAttachment.getStatus()).isEqualTo(ContractAttachmentDecision.Status.REJECTED);
        assertThat(loadedAttachment.getRejectedAt()).isNull(); // TODO should not be?
    }

    @Test
    void canRejectAttachmentAcceptedByBothSides() {
        // given
        Contract contract = fixtures.aContractFor("partner name");
        // and
        ContractAttachmentDTO contractAttachmentDTO = fixtures.anAttachmentAcceptedByBothSidesFor(contract);

        // when
        contractService.rejectAttachment(contractAttachmentDTO.getId());

        // then
        ContractDTO loadedContract = loadContract(contract);
        assertThat(loadedContract.getAttachments()).hasSize(1);
        ContractAttachmentDTO loadedAttachment = findContractAttachment(loadedContract, contractAttachmentDTO.getId());
        assertThat(loadedAttachment).isNotNull();
        assertThat(loadedAttachment.getStatus()).isEqualTo(ContractAttachmentDecision.Status.REJECTED);
        assertThat(loadedAttachment.getRejectedAt()).isNull(); // TODO should not be?
    }

    @Test
    void canRejectRejectedAttachment() {
        // given
        Contract contract = fixtures.aContractFor("partner name");
        // and
        ContractAttachmentDTO contractAttachmentDTO = fixtures.aRejectedAttachmentFor(contract);

        // when
        contractService.rejectAttachment(contractAttachmentDTO.getId());

        // then
        ContractDTO loadedContract = loadContract(contract);
        assertThat(loadedContract.getAttachments()).hasSize(1);
        ContractAttachmentDTO loadedAttachment = findContractAttachment(loadedContract, contractAttachmentDTO.getId());
        assertThat(loadedAttachment).isNotNull();
        assertThat(loadedAttachment.getStatus()).isEqualTo(ContractAttachmentDecision.Status.REJECTED);
        assertThat(loadedAttachment.getRejectedAt()).isNull(); // TODO should not be?
    }

    @Test
    void canRejectAttachmentFromAcceptedContract() {
        // given
        Contract contract = fixtures.anAcceptedContractFor("partner name");
        // and
        ContractAttachmentDTO contractAttachmentDTO = fixtures.aProposedAttachmentFor(contract);

        // when
        contractService.rejectAttachment(contractAttachmentDTO.getId());

        // then
        ContractDTO loadedContract = loadContract(contract);
        ContractAttachmentDTO loadedAttachment = findContractAttachment(loadedContract, contractAttachmentDTO.getId());
        assertThat(loadedAttachment).isNotNull();
        assertThat(loadedAttachment.getStatus()).isEqualTo(ContractAttachmentDecision.Status.REJECTED);
        assertThat(loadedAttachment.getRejectedAt()).isNull(); // TODO should not be?
    }

    @Test
    void canRejectAttachmentFromRejectedContract() {
        // given
        Contract contract = fixtures.aRejectedContractFor("partner name");
        // and
        ContractAttachmentDTO contractAttachmentDTO = fixtures.aProposedAttachmentFor(contract);

        // when
        contractService.rejectAttachment(contractAttachmentDTO.getId());

        // then
        ContractDTO loadedContract = loadContract(contract);
        ContractAttachmentDTO loadedAttachment = findContractAttachment(loadedContract, contractAttachmentDTO.getId());
        assertThat(loadedAttachment).isNotNull();
        assertThat(loadedAttachment.getStatus()).isEqualTo(ContractAttachmentDecision.Status.REJECTED);
        assertThat(loadedAttachment.getRejectedAt()).isNull(); // TODO should not be?
    }

    @Test
    void cannotAcceptNonExistingAttachment() {
        // when
        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> contractService.acceptAttachment(0L));
    }

    @Test
    void canAcceptAttachmentByOneSide() {
        // given
        Contract contract = fixtures.aContractFor("partner name");
        // and
        ContractAttachmentDTO contractAttachmentDTO = fixtures.aProposedAttachmentFor(contract);

        // when
        contractService.acceptAttachment(contractAttachmentDTO.getId());

        // then
        ContractDTO loadedContract = loadContract(contract);
        ContractAttachmentDTO loadedAttachment = findContractAttachment(loadedContract, contractAttachmentDTO.getId());
        assertThat(loadedAttachment).isNotNull();
        assertThat(loadedAttachment.getStatus()).isEqualTo(ContractAttachmentDecision.Status.ACCEPTED_BY_ONE_SIDE);
        assertThat(loadedAttachment.getAcceptedAt()).isNull(); // TODO should not be?
    }

    @Test
    void canAcceptAttachmentByBothSides() {
        // given
        Contract contract = fixtures.aContractFor("partner name");
        // and
        ContractAttachmentDTO contractAttachmentDTO = fixtures.anAttachmentAcceptedByOneSideFor(contract);

        // when
        contractService.acceptAttachment(contractAttachmentDTO.getId());

        // then
        ContractDTO loadedContract = loadContract(contract);
        ContractAttachmentDTO loadedAttachment = findContractAttachment(loadedContract, contractAttachmentDTO.getId());
        assertThat(loadedAttachment).isNotNull();
        assertThat(loadedAttachment.getStatus()).isEqualTo(ContractAttachmentDecision.Status.ACCEPTED_BY_BOTH_SIDES);
        assertThat(loadedAttachment.getAcceptedAt()).isNull(); // TODO should not be?
    }

    @Test
    void canAcceptRejectedAttachment() {
        // given
        Contract contract = fixtures.aContractFor("partner name");
        // and
        ContractAttachmentDTO contractAttachmentDTO = fixtures.aRejectedAttachmentFor(contract);

        // when
        contractService.acceptAttachment(contractAttachmentDTO.getId());

        // then
        ContractDTO loadedContract = loadContract(contract);
        ContractAttachmentDTO loadedAttachment = findContractAttachment(loadedContract, contractAttachmentDTO.getId());
        assertThat(loadedAttachment).isNotNull();
        assertThat(loadedAttachment.getStatus()).isEqualTo(ContractAttachmentDecision.Status.ACCEPTED_BY_ONE_SIDE);
        assertThat(loadedAttachment.getAcceptedAt()).isNull(); // TODO should not be?
    }

    @Test
    void canAcceptAttachmentFromAcceptedContract() {
        // given
        Contract contract = fixtures.anAcceptedContractFor("partner name");
        // and
        ContractAttachmentDTO contractAttachmentDTO = fixtures.aProposedAttachmentFor(contract);

        // when
        contractService.acceptAttachment(contractAttachmentDTO.getId());

        // then
        ContractDTO loadedContract = loadContract(contract);
        ContractAttachmentDTO loadedAttachment = findContractAttachment(loadedContract, contractAttachmentDTO.getId());
        assertThat(loadedAttachment).isNotNull();
        assertThat(loadedAttachment.getStatus()).isEqualTo(ContractAttachmentDecision.Status.ACCEPTED_BY_ONE_SIDE);
        assertThat(loadedAttachment.getAcceptedAt()).isNull(); // TODO should not be?
    }

    @Test
    void canAcceptAttachmentFromRejectedContract() {
        // given
        Contract contract = fixtures.aRejectedContractFor("partner name");
        // and
        ContractAttachmentDTO contractAttachmentDTO = fixtures.aProposedAttachmentFor(contract);

        // when
        contractService.acceptAttachment(contractAttachmentDTO.getId());

        // then
        ContractDTO loadedContract = loadContract(contract);
        ContractAttachmentDTO loadedAttachment = findContractAttachment(loadedContract, contractAttachmentDTO.getId());
        assertThat(loadedAttachment).isNotNull();
        assertThat(loadedAttachment.getStatus()).isEqualTo(ContractAttachmentDecision.Status.ACCEPTED_BY_ONE_SIDE);
        assertThat(loadedAttachment.getAcceptedAt()).isNull(); // TODO should not be?
    }

    @Test
    void canRemoveAttachment() {
        // given
        Contract contract = fixtures.aContractFor("partner name");
        // and
        ContractAttachmentDTO contractAttachmentDTO = fixtures.aProposedAttachmentFor(contract);

        // when
        contractService.removeAttachment(contract.getId(), contractAttachmentDTO.getId());

        // then
        ContractDTO loadedContract = loadContract(contract);
        assertThat(loadedContract.getAttachments()).isEmpty();
    }

    @Test
    void canRemoveAttachmentAcceptedByOneSide() {
        // given
        Contract contract = fixtures.aContractFor("partner name");
        // and
        ContractAttachmentDTO contractAttachmentDTO = fixtures.anAttachmentAcceptedByOneSideFor(contract);

        // when
        contractService.removeAttachment(contract.getId(), contractAttachmentDTO.getId());

        // then
        ContractDTO loadedContract = loadContract(contract);
        assertThat(loadedContract.getAttachments()).isEmpty();
    }

    @Test
    void canRemoveAttachmentAcceptedByBothSides() {
        // given
        Contract contract = fixtures.aContractFor("partner name");
        // and
        ContractAttachmentDTO contractAttachmentDTO = fixtures.anAttachmentAcceptedByBothSidesFor(contract);

        // when
        contractService.removeAttachment(contract.getId(), contractAttachmentDTO.getId());

        // then
        ContractDTO loadedContract = loadContract(contract);
        assertThat(loadedContract.getAttachments()).isEmpty();
    }

    @Test
    void canRemoveRejectedAttachment() {
        // given
        Contract contract = fixtures.aContractFor("partner name");
        // and
        ContractAttachmentDTO contractAttachmentDTO = fixtures.aRejectedAttachmentFor(contract);

        // when
        contractService.removeAttachment(contract.getId(), contractAttachmentDTO.getId());

        // then
        ContractDTO loadedContract = loadContract(contract);
        assertThat(loadedContract.getAttachments()).isEmpty();
    }

    @Test
    void canRemoveAttachmentFromRejectedContract() {
        // given
        Contract contract = fixtures.aRejectedContractFor("partner name");
        // and
        ContractAttachmentDTO contractAttachmentDTO = fixtures.aProposedAttachmentFor(contract);

        // when
        contractService.removeAttachment(contract.getId(), contractAttachmentDTO.getId());

        // then
        ContractDTO loadedContract = loadContract(contract);
        assertThat(loadedContract.getAttachments()).isEmpty();
    }

    @Test
    void canRemoveAttachmentFromAcceptedContract() {
        // given
        Contract contract = fixtures.anAcceptedContractFor("partner name");
        // and
        ContractAttachmentDTO contractAttachmentDTO = fixtures.aProposedAttachmentFor(contract);

        // when
        contractService.removeAttachment(contract.getId(), contractAttachmentDTO.getId());

        // then
        ContractDTO loadedContract = loadContract(contract);
        assertThat(loadedContract.getAttachments()).isEmpty();
    }

    @Test
    void canRemoveAttachmentWhenProvidingDifferentContract() {
        // given
        Contract contract = fixtures.aContractFor("partner name");
        fixtures.aContractFor("different partner name");
        // and
        ContractAttachmentDTO contractAttachmentDTO = fixtures.aProposedAttachmentFor(contract);

        // when
        contractService.removeAttachment(contract.getId(), contractAttachmentDTO.getId());

        // then
        ContractDTO loadedContract = loadContract(contract);
        assertThat(loadedContract.getAttachments()).isEmpty();
    }

    private ContractDTO aContractDTO() {
        ContractDTO contractDTO = new ContractDTO();
        contractDTO.setPartnerName("partner name " + LocalDateTime.now());
        contractDTO.setSubject("subject");
        return contractDTO;
    }

    private ContractDTO aContractDTO(String partnerName) {
        ContractDTO contractDTO = new ContractDTO();
        contractDTO.setPartnerName(partnerName);
        contractDTO.setSubject("subject");
        return contractDTO;
    }

    private ContractAttachmentDTO aContractAttachmentDTO() {
        ContractAttachmentDTO contractAttachmentDTO = new ContractAttachmentDTO();
        contractAttachmentDTO.setData(new byte[]{1, 2, 3});
        return contractAttachmentDTO;
    }

    private ContractDTO loadContract(Contract contract) {
        return contractService.findDto(contract.getId());
    }

    private ContractAttachmentDTO findContractAttachment(ContractDTO contract, Long attachmentId) {
        return contract.getAttachments().stream()
                .filter(attachmentDTO -> Objects.equals(attachmentDTO.getId(), attachmentId))
                .findFirst()
                .orElse(null);
    }
}
