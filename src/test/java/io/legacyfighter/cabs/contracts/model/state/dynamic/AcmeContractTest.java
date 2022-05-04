package io.legacyfighter.cabs.contracts.model.state.dynamic;

import io.legacyfighter.cabs.contracts.model.ContentId;
import io.legacyfighter.cabs.contracts.model.DocumentHeader;
import io.legacyfighter.cabs.contracts.model.content.DocumentNumber;
import io.legacyfighter.cabs.contracts.model.state.dynamic.acme.AcmeContractStateAssembler;
import io.legacyfighter.cabs.contracts.model.state.dynamic.config.events.DocumentPublished;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AcmeContractTest {

    private static final DocumentNumber ANY_NUMBER = new DocumentNumber("nr: 1");

    private static final Long ANY_USER = 1L;

    private static final Long OTHER_USER = 2L;

    private static final ContentId ANY_VERSION = new ContentId(UUID.randomUUID());

    private static final ContentId OTHER_VERSION = new ContentId(UUID.randomUUID());

    private InMemoryDocumentPublisher publisher;

    @Test
    void draftCanBeVerifiedByUserOtherThanCreator() {
        // given
        State state = draft().changeContent(ANY_VERSION);

        // when
        state = state.changeState(new ChangeCommand(AcmeContractStateAssembler.VERIFIED).withParam(AcmeContractStateAssembler.PARAM_VERIFIER, OTHER_USER));

        // then
        assertThat(state.getStateDescriptor()).isEqualTo(AcmeContractStateAssembler.VERIFIED);
        assertThat(state.getDocumentHeader().getVerifier()).isEqualTo(OTHER_USER);
    }

    @Test
    void canNotChangePublished() {
        // given
        State state = draft().changeContent(ANY_VERSION)
                .changeState(new ChangeCommand(AcmeContractStateAssembler.VERIFIED).withParam(AcmeContractStateAssembler.PARAM_VERIFIER, OTHER_USER))
                .changeState(new ChangeCommand(AcmeContractStateAssembler.PUBLISHED));
        // and
        publisher.contains(DocumentPublished.class);
        publisher.reset();

        // when
        state = state.changeContent(OTHER_VERSION);

        // then
        publisher.containsNoEvents();

        assertThat(state.getStateDescriptor()).isEqualTo(AcmeContractStateAssembler.PUBLISHED);
        assertThat(state.getDocumentHeader().getContentId()).isEqualTo(ANY_VERSION);
    }

    @Test
    void changingVerifiedMovesToDraft() {
        // given
        State state = draft().changeContent(ANY_VERSION)
                .changeState(new ChangeCommand(AcmeContractStateAssembler.VERIFIED).withParam(AcmeContractStateAssembler.PARAM_VERIFIER, OTHER_USER));

        // when
        state = state.changeContent(OTHER_VERSION);

        // then
        assertThat(state.getStateDescriptor()).isEqualTo(AcmeContractStateAssembler.DRAFT);
        assertThat(state.getDocumentHeader().getContentId()).isEqualTo(OTHER_VERSION);
    }


    @Test
    void canChangeStateToTheSame() {
        State state = draft().changeContent(ANY_VERSION);
        assertThat(state.getStateDescriptor()).isEqualTo(AcmeContractStateAssembler.DRAFT);
        state.changeState(new ChangeCommand(AcmeContractStateAssembler.DRAFT));
        assertThat(state.getStateDescriptor()).isEqualTo(AcmeContractStateAssembler.DRAFT);

        state = state.changeState(new ChangeCommand(AcmeContractStateAssembler.VERIFIED).withParam(AcmeContractStateAssembler.PARAM_VERIFIER, OTHER_USER));
        assertThat(state.getStateDescriptor()).isEqualTo(AcmeContractStateAssembler.VERIFIED);
        state = state.changeState(new ChangeCommand(AcmeContractStateAssembler.VERIFIED).withParam(AcmeContractStateAssembler.PARAM_VERIFIER, OTHER_USER));
        assertThat(state.getStateDescriptor()).isEqualTo(AcmeContractStateAssembler.VERIFIED);

        state = state.changeState(new ChangeCommand(AcmeContractStateAssembler.PUBLISHED));
        assertThat(state.getStateDescriptor()).isEqualTo(AcmeContractStateAssembler.PUBLISHED);
        state = state.changeState(new ChangeCommand(AcmeContractStateAssembler.PUBLISHED));
        assertThat(state.getStateDescriptor()).isEqualTo(AcmeContractStateAssembler.PUBLISHED);

        state = state.changeState(new ChangeCommand(AcmeContractStateAssembler.ARCHIVED));
        assertThat(state.getStateDescriptor()).isEqualTo(AcmeContractStateAssembler.ARCHIVED);
        state = state.changeState(new ChangeCommand(AcmeContractStateAssembler.ARCHIVED));
        assertThat(state.getStateDescriptor()).isEqualTo(AcmeContractStateAssembler.ARCHIVED);
    }

    private State draft() {
        DocumentHeader header = new DocumentHeader(ANY_USER, ANY_NUMBER);
        header.setStateDescriptor(AcmeContractStateAssembler.DRAFT);
        publisher = new InMemoryDocumentPublisher();

        AcmeContractStateAssembler assembler = new AcmeContractStateAssembler(publisher);
        StateConfig config = assembler.assemble();

        return config.recreate(header);
    }
}
