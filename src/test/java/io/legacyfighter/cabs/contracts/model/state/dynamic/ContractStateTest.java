package io.legacyfighter.cabs.contracts.model.state.dynamic;

import io.legacyfighter.cabs.common.InMemoryDocumentPublisher;
import io.legacyfighter.cabs.contracts.model.ContractHeader;
import io.legacyfighter.cabs.document.model.ContentId;
import io.legacyfighter.cabs.document.model.content.DocumentNumber;
import io.legacyfighter.cabs.document.model.state.dynamic.ChangeCommand;
import io.legacyfighter.cabs.document.model.state.dynamic.State;
import io.legacyfighter.cabs.document.model.state.dynamic.StateConfig;
import io.legacyfighter.cabs.document.model.state.dynamic.config.events.DocumentPublished;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ContractStateTest {

    private static final DocumentNumber ANY_NUMBER = new DocumentNumber("nr: 1");

    private static final Long ANY_USER = 1L;

    private static final Long OTHER_USER = 2L;

    private static final ContentId ANY_VERSION = new ContentId(UUID.randomUUID());

    private static final ContentId OTHER_VERSION = new ContentId(UUID.randomUUID());

    private InMemoryDocumentPublisher publisher;

    @BeforeEach
    void setup() {
        publisher = new InMemoryDocumentPublisher();
    }

    @Test
    void draftCanBeVerifiedByUserOtherThanCreator() {
        // given
        State state = draft().changeContent(ANY_VERSION);

        // when
        state = state.changeState(new ChangeCommand(ContractStateAssembler.VERIFIED).withParam(ContractStateAssembler.PARAM_VERIFIER, OTHER_USER));

        // then
        assertThat(state.getStateDescriptor()).isEqualTo(ContractStateAssembler.VERIFIED);
        assertThat(state.getDocumentHeader()).isInstanceOf(ContractHeader.class);
        assertThat(((ContractHeader) state.getDocumentHeader()).getVerifier()).isEqualTo(OTHER_USER);
    }

    @Test
    void canNotChangePublished() {
        // given
        State state = draft().changeContent(ANY_VERSION)
                .changeState(new ChangeCommand(ContractStateAssembler.VERIFIED).withParam(ContractStateAssembler.PARAM_VERIFIER, OTHER_USER))
                .changeState(new ChangeCommand(ContractStateAssembler.PUBLISHED));
        // and
        publisher.contains(DocumentPublished.class);
        publisher.reset();

        // when
        state = state.changeContent(OTHER_VERSION);

        // then
        publisher.containsNoEvents();

        assertThat(state.getStateDescriptor()).isEqualTo(ContractStateAssembler.PUBLISHED);
        assertThat(state.getDocumentHeader().getContentId()).isEqualTo(ANY_VERSION);
    }

    @Test
    void changingVerifiedMovesToDraft() {
        // given
        State state = draft().changeContent(ANY_VERSION)
                .changeState(new ChangeCommand(ContractStateAssembler.VERIFIED).withParam(ContractStateAssembler.PARAM_VERIFIER, OTHER_USER));

        // when
        state = state.changeContent(OTHER_VERSION);

        // then
        assertThat(state.getStateDescriptor()).isEqualTo(ContractStateAssembler.DRAFT);
        assertThat(state.getDocumentHeader().getContentId()).isEqualTo(OTHER_VERSION);
    }


    @Test
    void canChangeStateToTheSame() {
        State state = draft().changeContent(ANY_VERSION);
        assertThat(state.getStateDescriptor()).isEqualTo(ContractStateAssembler.DRAFT);
        state.changeState(new ChangeCommand(ContractStateAssembler.DRAFT));
        assertThat(state.getStateDescriptor()).isEqualTo(ContractStateAssembler.DRAFT);

        state = state.changeState(new ChangeCommand(ContractStateAssembler.VERIFIED).withParam(ContractStateAssembler.PARAM_VERIFIER, OTHER_USER));
        assertThat(state.getStateDescriptor()).isEqualTo(ContractStateAssembler.VERIFIED);
        state = state.changeState(new ChangeCommand(ContractStateAssembler.VERIFIED).withParam(ContractStateAssembler.PARAM_VERIFIER, OTHER_USER));
        assertThat(state.getStateDescriptor()).isEqualTo(ContractStateAssembler.VERIFIED);

        state = state.changeState(new ChangeCommand(ContractStateAssembler.PUBLISHED));
        assertThat(state.getStateDescriptor()).isEqualTo(ContractStateAssembler.PUBLISHED);
        state = state.changeState(new ChangeCommand(ContractStateAssembler.PUBLISHED));
        assertThat(state.getStateDescriptor()).isEqualTo(ContractStateAssembler.PUBLISHED);

        state = state.changeState(new ChangeCommand(ContractStateAssembler.ARCHIVED));
        assertThat(state.getStateDescriptor()).isEqualTo(ContractStateAssembler.ARCHIVED);
        state = state.changeState(new ChangeCommand(ContractStateAssembler.ARCHIVED));
        assertThat(state.getStateDescriptor()).isEqualTo(ContractStateAssembler.ARCHIVED);
    }

    private State draft() {
        ContractHeader header = new ContractHeader(ANY_USER, ANY_NUMBER);
        header.setStateDescriptor(ContractStateAssembler.DRAFT);

        ContractStateAssembler assembler = new ContractStateAssembler(publisher);
        StateConfig<ContractHeader> config = assembler.assemble();

        return config.recreate(header);
    }
}
