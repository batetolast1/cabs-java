package io.legacyfighter.cabs.entity.contract;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ContractAttachmentTest {

    @Test
    void canCreateContractAttachment() {
        // given
        Contract contract = new Contract();
        byte[] data = {1, 2, 3};

        // when
        ContractAttachment contractAttachment = new ContractAttachment(contract, data);

        // then
        assertThat(contractAttachment.getContract()).isEqualTo(contract);
        assertThat(contractAttachment.getData()).isEqualTo(data);
        assertThat(contractAttachment.getCreationDate()).isNotNull();
        assertThat(contractAttachment.getStatus()).isEqualTo(ContractAttachment.Status.PROPOSED);
        assertThat(contractAttachment.getAcceptedAt()).isNull();
        assertThat(contractAttachment.getAcceptedAt()).isNull();
        assertThat(contractAttachment.getRejectedAt()).isNull();
    }
}
