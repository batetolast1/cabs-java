package io.legacyfighter.cabs.contracts.model.state.straightforward;

import io.legacyfighter.cabs.contracts.model.ContractHeader;
import io.legacyfighter.cabs.document.model.ContentId;
import io.legacyfighter.cabs.document.model.content.DocumentNumber;
import io.legacyfighter.cabs.document.model.state.straightforward.BaseState;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ContractStateTest {

    private static final DocumentNumber ANY_NUMBER = new DocumentNumber("nr: 1");

    private static final Long ANY_USER = 1L;

    private static final Long OTHER_USER = 2L;

    private static final ContentId ANY_VERSION = new ContentId(UUID.randomUUID());

    private static final ContentId OTHER_VERSION = new ContentId(UUID.randomUUID());

    private BaseState state;

    @Test
    void onlyDraftCanBeVerifiedByUserOtherThanCreator() {
        // given
        state = draft().changeContent(ANY_VERSION);

        // when
        state = state.changeState(new VerifiedState(OTHER_USER));

        // then
        assertThat(state).isInstanceOf(VerifiedState.class);
        assertThat(state.getDocumentHeader()).isInstanceOf(ContractHeader.class);
        assertThat(((ContractHeader) state.getDocumentHeader()).getVerifier()).isEqualTo(OTHER_USER);
    }

    @Test
    void canNotChangePublished() {
        // given
        state = draft().changeContent(ANY_VERSION)
                .changeState(new VerifiedState(OTHER_USER))
                .changeState(new PublishedState());

        // when
        state = state.changeContent(OTHER_VERSION);

        // then
        assertThat(state).isInstanceOf(PublishedState.class);
        assertThat(state.getDocumentHeader().getContentId()).isEqualTo(ANY_VERSION);
    }

    @Test
    void changingVerifiedMovesToDraft() {
        // given
        state = draft().changeContent(ANY_VERSION)
                .changeState(new VerifiedState(OTHER_USER));

        // when
        state = state.changeContent(OTHER_VERSION);

        // then
        assertThat(state).isInstanceOf(DraftState.class);
        assertThat(state.getDocumentHeader().getContentId()).isEqualTo(OTHER_VERSION);
    }

    private BaseState draft() {
        ContractHeader header = new ContractHeader(ANY_USER, ANY_NUMBER);

        BaseState state = new DraftState();
        state.init(header);

        return state;
    }
}
