package io.legacyfighter.cabs.entity.contract;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ContractAttachmentDecisionTest {

    @Test
    void canCreateContractAttachmentDecision() {
        // given
        Contract contract = new Contract();
        byte[] data = {1, 2, 3};

        // when
        ContractAttachmentDecision contractAttachmentDecision = new ContractAttachmentDecision(contract);

        // then
        assertThat(contractAttachmentDecision.getContract()).isEqualTo(contract);
        assertThat(contractAttachmentDecision.getStatus()).isEqualTo(ContractAttachmentDecision.Status.PROPOSED);
        assertThat(contractAttachmentDecision.getAcceptedAt()).isNull();
        assertThat(contractAttachmentDecision.getAcceptedAt()).isNull();
        assertThat(contractAttachmentDecision.getRejectedAt()).isNull();
    }
}
