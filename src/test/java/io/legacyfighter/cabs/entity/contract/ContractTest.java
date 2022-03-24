package io.legacyfighter.cabs.entity.contract;

import org.junit.jupiter.api.Test;

import java.util.Objects;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class ContractTest {

    private static final long CONTRACT_ATTACHMENT_ID = 1L;

    @Test
    void canCreateContract() {
        // given
        String partnerName = "partner name";
        String subject = "subject";
        String contractNo = "contract no";

        // when
        Contract contract = new Contract(partnerName, subject, contractNo);

        // then
        assertThat(contract.getPartnerName()).isEqualTo(partnerName);
        assertThat(contract.getSubject()).isEqualTo(subject);
        assertThat(contract.getContractNo()).isEqualTo(contractNo);
        assertThat(contract.getCreationDate()).isNotNull();
        assertThat(contract.getStatus()).isEqualTo(Contract.Status.NEGOTIATIONS_IN_PROGRESS);
        assertThat(contract.getAttachments()).isEmpty();
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
        contract.proposeAttachment(new byte[]{1, 2, 3});

        // then
        assertThat(contract.getAttachments()).hasSize(1);
        ContractAttachment contractAttachment = getContractAttachment(contract, null);
        assertThat(contractAttachment.getStatus()).isEqualTo(ContractAttachment.Status.PROPOSED);

    }

    @Test
    void canAcceptProposedAttachment() {
        // when
        Contract contract = aContractWithProposedAttachment();

        // when
        contract.acceptAttachment(CONTRACT_ATTACHMENT_ID);

        // then
        assertThat(contract.getAttachments()).hasSize(1);
        ContractAttachment contractAttachment = getContractAttachment(contract, CONTRACT_ATTACHMENT_ID);
        assertThat(contractAttachment.getStatus()).isEqualTo(ContractAttachment.Status.ACCEPTED_BY_ONE_SIDE);
    }

    @Test
    void canAcceptRejectedAttachment() {
        // when
        Contract contract = aContractWithRejectedAttachment();

        // when
        contract.acceptAttachment(CONTRACT_ATTACHMENT_ID);

        // then
        assertThat(contract.getAttachments()).hasSize(1);
        ContractAttachment contractAttachment = getContractAttachment(contract, CONTRACT_ATTACHMENT_ID);
        assertThat(contractAttachment.getStatus()).isEqualTo(ContractAttachment.Status.ACCEPTED_BY_ONE_SIDE);
    }

    @Test
    void canAcceptAttachmentAcceptedByOneSide() {
        // when
        Contract contract = aContractWithAttachmentAcceptedByOneSide();

        // when
        contract.acceptAttachment(CONTRACT_ATTACHMENT_ID);

        // then
        assertThat(contract.getAttachments()).hasSize(1);
        ContractAttachment contractAttachment = getContractAttachment(contract, CONTRACT_ATTACHMENT_ID);
        assertThat(contractAttachment.getStatus()).isEqualTo(ContractAttachment.Status.ACCEPTED_BY_BOTH_SIDES);
    }

    @Test
    void canAcceptAttachmentAcceptedByBothSides() {
        // when
        Contract contract = aContractWithAttachmentAcceptedByBothSides();

        // when
        contract.acceptAttachment(CONTRACT_ATTACHMENT_ID);

        // then
        assertThat(contract.getAttachments()).hasSize(1);
        ContractAttachment contractAttachment = getContractAttachment(contract, CONTRACT_ATTACHMENT_ID);
        assertThat(contractAttachment.getStatus()).isEqualTo(ContractAttachment.Status.ACCEPTED_BY_BOTH_SIDES);
    }

    @Test
    void canRejectProposedAttachment() {
        // when
        Contract contract = aContractWithProposedAttachment();

        // when
        contract.rejectAttachment(CONTRACT_ATTACHMENT_ID);

        // then
        assertThat(contract.getAttachments()).hasSize(1);
        ContractAttachment contractAttachment = getContractAttachment(contract, CONTRACT_ATTACHMENT_ID);
        assertThat(contractAttachment.getStatus()).isEqualTo(ContractAttachment.Status.REJECTED);
    }

    @Test
    void canRejectRejectedAttachment() {
        // when
        Contract contract = aContractWithRejectedAttachment();

        // when
        contract.rejectAttachment(CONTRACT_ATTACHMENT_ID);

        // then
        assertThat(contract.getAttachments()).hasSize(1);
        ContractAttachment contractAttachment = getContractAttachment(contract, CONTRACT_ATTACHMENT_ID);
        assertThat(contractAttachment.getStatus()).isEqualTo(ContractAttachment.Status.REJECTED);
    }

    @Test
    void canRejectAttachmentAcceptedByOneSide() {
        // when
        Contract contract = aContractWithAttachmentAcceptedByOneSide();

        // when
        contract.rejectAttachment(CONTRACT_ATTACHMENT_ID);

        // then
        assertThat(contract.getAttachments()).hasSize(1);
        ContractAttachment contractAttachment = getContractAttachment(contract, CONTRACT_ATTACHMENT_ID);
        assertThat(contractAttachment.getStatus()).isEqualTo(ContractAttachment.Status.REJECTED);
    }

    @Test
    void canRejectAttachmentAcceptedByBothSides() {
        // when
        Contract contract = aContractWithAttachmentAcceptedByBothSides();

        // when
        contract.rejectAttachment(CONTRACT_ATTACHMENT_ID);

        // then
        assertThat(contract.getAttachments()).hasSize(1);
        ContractAttachment contractAttachment = getContractAttachment(contract, CONTRACT_ATTACHMENT_ID);
        assertThat(contractAttachment.getStatus()).isEqualTo(ContractAttachment.Status.REJECTED);
    }

    private ContractAttachment getContractAttachment(Contract contract, Long id) {
        Optional<ContractAttachment> optionalLoadedAttachment = contract.getAttachments()
                .stream()
                .filter(a -> Objects.equals(a.getId(), id))
                .findFirst();
        assertThat(optionalLoadedAttachment).isPresent();
        return optionalLoadedAttachment.get();
    }

    private Contract aContract() {
        String partnerName = "partner name";
        String subject = "subject";
        String contractNo = "contract no";
        return new Contract(partnerName, subject, contractNo);
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
        contract.proposeAttachment(null);
        ContractAttachment attachment = contract.findAttachment(null);
        attachment.setId(CONTRACT_ATTACHMENT_ID);
        return contract;
    }

    private Contract aContractWithRejectedAttachment() {
        Contract contract = aContractWithProposedAttachment();
        contract.rejectAttachment(CONTRACT_ATTACHMENT_ID);
        return contract;
    }

    private Contract aContractWithAttachmentAcceptedByOneSide() {
        Contract contract = aContractWithProposedAttachment();
        contract.acceptAttachment(CONTRACT_ATTACHMENT_ID);
        return contract;
    }

    private Contract aContractWithAttachmentAcceptedByBothSides() {
        Contract contract = aContractWithProposedAttachment();
        contract.acceptAttachment(CONTRACT_ATTACHMENT_ID);
        contract.acceptAttachment(CONTRACT_ATTACHMENT_ID);
        return contract;
    }
}
