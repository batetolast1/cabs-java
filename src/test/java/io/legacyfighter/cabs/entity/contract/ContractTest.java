package io.legacyfighter.cabs.entity.contract;

import org.junit.jupiter.api.Test;

import javax.persistence.EntityNotFoundException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ContractTest {

    @Test
    void canCreateContract() {
        // given
        String partnerName = "partner name";
        String subject = "subject";
        String contractNo = "contract no";
        Instant creationDate = Instant.now();

        // when
        Contract contract = new Contract(partnerName, subject, contractNo, creationDate);

        // then
        assertThat(contract.getPartnerName()).isEqualTo(partnerName);
        assertThat(contract.getSubject()).isEqualTo(subject);
        assertThat(contract.getContractNo()).isEqualTo(contractNo);
        assertThat(contract.getCreationDate()).isEqualTo(creationDate);
        assertThat(contract.getStatus()).isEqualTo(Contract.Status.NEGOTIATIONS_IN_PROGRESS);
        assertThat(contract.getContractAttachmentDecisions()).isEmpty();
        assertThat(contract.getChangeDate()).isNull();
        assertThat(contract.getAcceptedAt()).isNull();
        assertThat(contract.getRejectedAt()).isNull();
    }

    @Test
    void canAcceptContractWithoutAttachments() {
        // when
        Contract contract = aContract();

        // when
        contract.accept();

        // then
        assertThat(contract.getStatus()).isEqualTo(Contract.Status.ACCEPTED);
    }

    @Test
    void cannotAcceptContractWithProposedAttachment() {
        // when
        Contract contract = aContractWithProposedAttachment();

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(contract::accept);
    }

    @Test
    void cannotAcceptContractWithRejectedAttachment() {
        // when
        Contract contract = aContractWithRejectedAttachment();

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(contract::accept);
    }

    @Test
    void cannotAcceptContractWithAttachmentAcceptedByOneSide() {
        // when
        Contract contract = aContractWithAttachmentAcceptedByOneSide();

        // when
        assertThatExceptionOfType(IllegalStateException.class)
                .isThrownBy(contract::accept);
    }

    @Test
    void canAcceptContractWithAttachmentAcceptedByBothSides() {
        // when
        Contract contract = aContractWithAttachmentAcceptedByBothSides();

        // when
        contract.accept();

        // then
        assertThat(contract.getStatus()).isEqualTo(Contract.Status.ACCEPTED);
    }

    @Test
    void canRejectContract() {
        // when
        Contract contract = aContract();

        // when
        contract.reject();

        // then
        assertThat(contract.getStatus()).isEqualTo(Contract.Status.REJECTED);
    }

    @Test
    void canRejectAcceptedContract() {
        // when
        Contract contract = anAcceptedContract();

        // when
        contract.reject();

        // then
        assertThat(contract.getStatus()).isEqualTo(Contract.Status.REJECTED);
    }

    @Test
    void canRejectRejectedContract() {
        // when
        Contract contract = aRejectedContract();

        // when
        contract.reject();

        // then
        assertThat(contract.getStatus()).isEqualTo(Contract.Status.REJECTED);
    }

    @Test
    void canProposeAttachment() {
        // when
        Contract contract = aContract();

        // when
        contract.proposeAttachment();

        // then
        assertThat(contract.getContractAttachmentDecisions()).hasSize(1);
        ContractAttachmentDecision contractAttachmentDecision = getContractAttachment(contract, contract.getContractAttachmentNos().get(0));
        assertThat(contractAttachmentDecision.getStatus()).isEqualTo(ContractAttachmentDecision.Status.PROPOSED);
    }

    @Test
    void cannotAcceptNonExistingAttachment() {
        // given
        UUID contractAttachmentNo = UUID.randomUUID();

        // when
        Contract contract = aContract();

        // when
        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> contract.acceptAttachment(contractAttachmentNo));
    }


    @Test
    void canAcceptProposedAttachment() {
        // when
        Contract contract = aContractWithProposedAttachment();
        // and
        UUID contractAttachmentNo = contract.getContractAttachmentNos().get(0);

        // when
        contract.acceptAttachment(contractAttachmentNo);

        // then
        assertThat(contract.getContractAttachmentDecisions()).hasSize(1);
        ContractAttachmentDecision contractAttachmentDecision = getContractAttachment(contract, contractAttachmentNo);
        assertThat(contractAttachmentDecision.getStatus()).isEqualTo(ContractAttachmentDecision.Status.ACCEPTED_BY_ONE_SIDE);
    }

    @Test
    void canAcceptRejectedAttachment() {
        // when
        Contract contract = aContractWithRejectedAttachment();
        // and
        UUID contractAttachmentNo = contract.getContractAttachmentNos().get(0);

        // when
        contract.acceptAttachment(contractAttachmentNo);

        // then
        assertThat(contract.getContractAttachmentDecisions()).hasSize(1);
        ContractAttachmentDecision contractAttachmentDecision = getContractAttachment(contract, contractAttachmentNo);
        assertThat(contractAttachmentDecision.getStatus()).isEqualTo(ContractAttachmentDecision.Status.ACCEPTED_BY_ONE_SIDE);
    }

    @Test
    void canAcceptAttachmentAcceptedByOneSide() {
        // when
        Contract contract = aContractWithAttachmentAcceptedByOneSide();
        // and
        UUID contractAttachmentNo = contract.getContractAttachmentNos().get(0);

        // when
        contract.acceptAttachment(contractAttachmentNo);

        // then
        assertThat(contract.getContractAttachmentDecisions()).hasSize(1);
        ContractAttachmentDecision contractAttachmentDecision = getContractAttachment(contract, contractAttachmentNo);
        assertThat(contractAttachmentDecision.getStatus()).isEqualTo(ContractAttachmentDecision.Status.ACCEPTED_BY_BOTH_SIDES);
    }

    @Test
    void canAcceptAttachmentAcceptedByBothSides() {
        // when
        Contract contract = aContractWithAttachmentAcceptedByBothSides();
        // and
        UUID contractAttachmentNo = contract.getContractAttachmentNos().get(0);

        // when
        contract.acceptAttachment(contractAttachmentNo);

        // then
        assertThat(contract.getContractAttachmentDecisions()).hasSize(1);
        ContractAttachmentDecision contractAttachmentDecision = getContractAttachment(contract, contractAttachmentNo);
        assertThat(contractAttachmentDecision.getStatus()).isEqualTo(ContractAttachmentDecision.Status.ACCEPTED_BY_BOTH_SIDES);
    }

    @Test
    void cannotRejectNonExistingAttachment() {
        // given
        UUID contractAttachmentNo = UUID.randomUUID();

        // when
        Contract contract = aContract();

        // when
        assertThatExceptionOfType(EntityNotFoundException.class)
                .isThrownBy(() -> contract.rejectAttachment(contractAttachmentNo));
    }

    @Test
    void canRejectProposedAttachment() {
        // when
        Contract contract = aContractWithProposedAttachment();
        // and
        UUID contractAttachmentNo = contract.getContractAttachmentNos().get(0);

        // when
        contract.rejectAttachment(contractAttachmentNo);

        // then
        assertThat(contract.getContractAttachmentDecisions()).hasSize(1);
        ContractAttachmentDecision contractAttachmentDecision = getContractAttachment(contract, contractAttachmentNo);
        assertThat(contractAttachmentDecision.getStatus()).isEqualTo(ContractAttachmentDecision.Status.REJECTED);
    }

    @Test
    void canRejectRejectedAttachment() {
        // when
        Contract contract = aContractWithRejectedAttachment();
        // and
        UUID contractAttachmentNo = contract.getContractAttachmentNos().get(0);

        // when
        contract.rejectAttachment(contractAttachmentNo);

        // then
        assertThat(contract.getContractAttachmentDecisions()).hasSize(1);
        ContractAttachmentDecision contractAttachmentDecision = getContractAttachment(contract, contractAttachmentNo);
        assertThat(contractAttachmentDecision.getStatus()).isEqualTo(ContractAttachmentDecision.Status.REJECTED);
    }

    @Test
    void canRejectAttachmentAcceptedByOneSide() {
        // when
        Contract contract = aContractWithAttachmentAcceptedByOneSide();
        // and
        UUID contractAttachmentNo = contract.getContractAttachmentNos().get(0);

        // when
        contract.rejectAttachment(contractAttachmentNo);

        // then
        assertThat(contract.getContractAttachmentDecisions()).hasSize(1);
        ContractAttachmentDecision contractAttachmentDecision = getContractAttachment(contract, contractAttachmentNo);
        assertThat(contractAttachmentDecision.getStatus()).isEqualTo(ContractAttachmentDecision.Status.REJECTED);
    }

    @Test
    void canRejectAttachmentAcceptedByBothSides() {
        // when
        Contract contract = aContractWithAttachmentAcceptedByBothSides();
        // and
        UUID contractAttachmentNo = contract.getContractAttachmentNos().get(0);

        // when
        contract.rejectAttachment(contractAttachmentNo);

        // then
        assertThat(contract.getContractAttachmentDecisions()).hasSize(1);
        ContractAttachmentDecision contractAttachmentDecision = getContractAttachment(contract, contractAttachmentNo);
        assertThat(contractAttachmentDecision.getStatus()).isEqualTo(ContractAttachmentDecision.Status.REJECTED);
    }

    @Test
    void canRemoveAttachment() {
        // when
        Contract contract = aContractWithProposedAttachment();
        // and
        UUID contractAttachmentNo = contract.getContractAttachmentNos().get(0);

        // when
        contract.removeAttachment(contractAttachmentNo);

        // then
        assertThat(contract.getContractAttachmentDecisions()).isEmpty();
    }

    @Test
    void cannotRemoveNonExistingAttachment() {
        // when
        Contract contract = aContractWithProposedAttachment();

        // when
        contract.removeAttachment(UUID.randomUUID());

        // then
        assertThat(contract.getContractAttachmentDecisions()).hasSize(1);
    }

    private ContractAttachmentDecision getContractAttachment(Contract contract, UUID contractAttachmentNo) {
        Optional<ContractAttachmentDecision> optionalContractAttachmentDecision = contract.getContractAttachmentDecisions()
                .stream()
                .filter(a -> Objects.equals(a.getContractAttachmentNo(), contractAttachmentNo))
                .findFirst();
        assertThat(optionalContractAttachmentDecision).isPresent();
        return optionalContractAttachmentDecision.get();
    }

    private Contract aContract() {
        String partnerName = "partner name";
        String subject = "subject";
        String contractNo = "contract no";
        Instant creationDate = Instant.now();
        return new Contract(partnerName, subject, contractNo, creationDate);
    }

    private Contract anAcceptedContract() {
        Contract contract = aContract();
        contract.accept();
        return contract;
    }

    private Contract aRejectedContract() {
        Contract contract = aContract();
        contract.reject();
        return contract;
    }

    private Contract aContractWithProposedAttachment() {
        Contract contract = aContract();
        contract.proposeAttachment();
        return contract;
    }

    private Contract aContractWithRejectedAttachment() {
        Contract contract = aContractWithProposedAttachment();
        List<UUID> attachmentIds = contract.getContractAttachmentNos();
        contract.rejectAttachment(attachmentIds.get(0));
        return contract;
    }

    private Contract aContractWithAttachmentAcceptedByOneSide() {
        Contract contract = aContractWithProposedAttachment();
        List<UUID> attachmentIds = contract.getContractAttachmentNos();
        contract.acceptAttachment(attachmentIds.get(0));
        return contract;
    }

    private Contract aContractWithAttachmentAcceptedByBothSides() {
        Contract contract = aContractWithProposedAttachment();
        List<UUID> attachmentIds = contract.getContractAttachmentNos();
        contract.acceptAttachment(attachmentIds.get(0));
        contract.acceptAttachment(attachmentIds.get(0));
        return contract;
    }
}
