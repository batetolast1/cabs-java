package io.legacyfighter.cabs.entity.contract;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ContractAttachmentDataTest {

    @Test
    void canCreateContractAttachmentData() {
        // given
        UUID contractAttachmentNo = UUID.randomUUID();
        byte[] data = {1, 2, 3};
        Instant creationDate = Instant.now();

        // when
        ContractAttachmentData contractAttachmentData = new ContractAttachmentData(contractAttachmentNo, data, creationDate);

        // then
        assertThat(contractAttachmentData.getContractAttachmentNo()).isEqualTo(contractAttachmentNo);
        assertThat(contractAttachmentData.getData()).isEqualTo(data);
        assertThat(contractAttachmentData.getCreationDate()).isEqualTo(creationDate);
    }
}