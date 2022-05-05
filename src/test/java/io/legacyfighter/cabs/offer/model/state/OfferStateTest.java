package io.legacyfighter.cabs.offer.model.state;

import io.legacyfighter.cabs.common.InMemoryDocumentPublisher;
import io.legacyfighter.cabs.document.model.ContentId;
import io.legacyfighter.cabs.document.model.content.DocumentNumber;
import io.legacyfighter.cabs.document.model.state.dynamic.ChangeCommand;
import io.legacyfighter.cabs.document.model.state.dynamic.State;
import io.legacyfighter.cabs.document.model.state.dynamic.StateConfig;
import io.legacyfighter.cabs.document.model.state.dynamic.config.events.DocumentPublished;
import io.legacyfighter.cabs.money.Money;
import io.legacyfighter.cabs.offers.model.OfferHeader;
import io.legacyfighter.cabs.offers.model.state.OfferStateAssembler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class OfferStateTest {

    private static final DocumentNumber ANY_NUMBER = new DocumentNumber("nr: 1");

    private static final Long ANY_USER = 1L;

    private static final Long OTHER_USER = 2L;

    private static final ContentId ANY_VERSION = new ContentId(UUID.randomUUID());

    private static final ContentId OTHER_VERSION = new ContentId(UUID.randomUUID());

    private static final Money BELOW_MANAGER_APPROVAL_THRESHOLD = new Money(99);

    private static final Money MANAGER_APPROVAL_THRESHOLD = new Money(100);

    private InMemoryDocumentPublisher publisher;

    @BeforeEach
    void setup() {
        publisher = new InMemoryDocumentPublisher();
    }

    @Test
    void preOfferWithoutContentCannotBeChanged() {
        // given
        State state = preOffer();

        // expect
        state = state.changeState(new ChangeCommand(OfferStateAssembler.DISCOUNTED));
        assertThat(state.getStateDescriptor()).isEqualTo(OfferStateAssembler.PRE_OFFER);

        state = state.changeState(new ChangeCommand(OfferStateAssembler.APPROVED_BY_MANAGER));
        assertThat(state.getStateDescriptor()).isEqualTo(OfferStateAssembler.PRE_OFFER);

        state = state.changeState(new ChangeCommand(OfferStateAssembler.ACCEPTED_BY_CLIENT));
        assertThat(state.getStateDescriptor()).isEqualTo(OfferStateAssembler.PRE_OFFER);

        state = state.changeState(new ChangeCommand(OfferStateAssembler.SOLD));
        assertThat(state.getStateDescriptor()).isEqualTo(OfferStateAssembler.PRE_OFFER);
    }

    @Test
    void preOfferWithoutContentCanBeRejected() {
        // given
        State state = preOffer();

        // when
        state = state.changeState(new ChangeCommand(OfferStateAssembler.REJECTED).withParam(OfferStateAssembler.PARAM_REJECTED_BY_ID, OTHER_USER));

        // then
        publisher.containsNoEvents();

        assertThat(state.getStateDescriptor()).isEqualTo(OfferStateAssembler.REJECTED);
        assertThat(((OfferHeader) state.getDocumentHeader()).getRejectedById()).isEqualTo(OTHER_USER);
    }

    @Test
    void preOfferCannotBeRejectedWhenNoOneIsRejecting() {
        // given
        State state = preOffer();

        // when
        state = state.changeState(new ChangeCommand(OfferStateAssembler.REJECTED));

        // then
        publisher.containsNoEvents();

        assertThat(state.getStateDescriptor()).isEqualTo(OfferStateAssembler.PRE_OFFER);
        assertThat(((OfferHeader) state.getDocumentHeader()).getRejectedById()).isNull();
    }

    @Test
    void changingPreOfferMovesToPreOffer() {
        // given
        State state = preOffer().changeContent(ANY_VERSION);

        // when
        state = state.changeContent(OTHER_VERSION);

        // then
        publisher.containsNoEvents();

        assertThat(state.getStateDescriptor()).isEqualTo(OfferStateAssembler.PRE_OFFER);
        assertThat(state.getDocumentHeader().getContentId()).isEqualTo(OTHER_VERSION);
    }

    @Test
    void preOfferCanBeChangedToDiscounted() {
        // given
        State state = preOffer().changeContent(ANY_VERSION);

        // when
        state = state.changeState(new ChangeCommand(OfferStateAssembler.DISCOUNTED));

        // then
        publisher.containsNoEvents();

        assertThat(state.getStateDescriptor()).isEqualTo(OfferStateAssembler.DISCOUNTED);
    }

    @Test
    void preOfferCanBeChangedToApprovedByManager() {
        // given
        State state = preOffer().changeContent(ANY_VERSION);

        // when
        state = state.changeState(new ChangeCommand(OfferStateAssembler.APPROVED_BY_MANAGER).withParam(OfferStateAssembler.PARAM_APPROVING_BY_ID, OTHER_USER));

        // then
        publisher.containsNoEvents();

        assertThat(state.getStateDescriptor()).isEqualTo(OfferStateAssembler.APPROVED_BY_MANAGER);
        assertThat(((OfferHeader) state.getDocumentHeader()).getApprovingId()).isEqualTo(OTHER_USER);
    }

    @Test
    void preOfferCannotBeChangedToApprovedByManagerWhenNoOneIsApproving() {
        // given
        State state = preOffer().changeContent(ANY_VERSION);

        // when
        state = state.changeState(new ChangeCommand(OfferStateAssembler.APPROVED_BY_MANAGER));

        // then
        publisher.containsNoEvents();

        assertThat(state.getStateDescriptor()).isEqualTo(OfferStateAssembler.PRE_OFFER);
        assertThat(((OfferHeader) state.getDocumentHeader()).getApprovingId()).isNull();
    }

    @Test
    void preOfferCannotBeChangedToApprovedByManagerWhenTotalPriceIsNotProvided() {
        // given
        State state = preOffer().changeContent(ANY_VERSION);

        // when
        state = state.changeState(new ChangeCommand(OfferStateAssembler.APPROVED_BY_MANAGER));

        // then
        publisher.containsNoEvents();

        assertThat(state.getStateDescriptor()).isEqualTo(OfferStateAssembler.PRE_OFFER);
        assertThat(((OfferHeader) state.getDocumentHeader()).getApprovingId()).isNull();
    }

    @Test
    void preOfferCanBeChangedToAcceptedByClientWhenOfferDoesntNeedManagerApproval() {
        // given
        State state = preOffer().changeContent(ANY_VERSION);

        // when
        state = state.changeState(new ChangeCommand(OfferStateAssembler.ACCEPTED_BY_CLIENT).withParam(OfferStateAssembler.PARAM_TOTAL_PRICE, BELOW_MANAGER_APPROVAL_THRESHOLD));

        // then
        publisher.containsNoEvents();

        assertThat(state.getStateDescriptor()).isEqualTo(OfferStateAssembler.ACCEPTED_BY_CLIENT);
        assertThat(((OfferHeader) state.getDocumentHeader()).getApprovingId()).isNull();
    }

    @Test
    void preOfferCannotBeChangedToAcceptedByClientWhenOfferNeedsManagerApproval() {
        // given
        State state = preOffer().changeContent(ANY_VERSION);

        // when
        state = state.changeState(new ChangeCommand(OfferStateAssembler.ACCEPTED_BY_CLIENT).withParam(OfferStateAssembler.PARAM_TOTAL_PRICE, MANAGER_APPROVAL_THRESHOLD));

        // then
        publisher.containsNoEvents();

        assertThat(state.getStateDescriptor()).isEqualTo(OfferStateAssembler.PRE_OFFER);
        assertThat(((OfferHeader) state.getDocumentHeader()).getApprovingId()).isNull();
    }

    @Test
    void preOfferCanBeChangedToSoldWhenOfferDoesntNeedManagerApproval() {
        // given
        State state = preOffer().changeContent(ANY_VERSION);

        // when
        state = state.changeState(new ChangeCommand(OfferStateAssembler.SOLD).withParam(OfferStateAssembler.PARAM_TOTAL_PRICE, BELOW_MANAGER_APPROVAL_THRESHOLD));

        // then
        publisher.contains(DocumentPublished.class);

        assertThat(state.getStateDescriptor()).isEqualTo(OfferStateAssembler.SOLD);
        assertThat(((OfferHeader) state.getDocumentHeader()).getApprovingId()).isNull();
    }

    @Test
    void preOfferCannotBeChangedToSoldWhenOfferNeedsManagerApproval() {
        // given
        State state = preOffer().changeContent(ANY_VERSION);

        // when
        state = state.changeState(new ChangeCommand(OfferStateAssembler.SOLD).withParam(OfferStateAssembler.PARAM_TOTAL_PRICE, MANAGER_APPROVAL_THRESHOLD));

        // then
        publisher.containsNoEvents();

        assertThat(state.getStateDescriptor()).isEqualTo(OfferStateAssembler.PRE_OFFER);
        assertThat(((OfferHeader) state.getDocumentHeader()).getApprovingId()).isNull();
    }

    @Test
    void offerApprovedByManagerWithTotalPriceBelowManagerApprovalThresholdCanBeSold() {
        // given
        State state = preOffer().changeContent(ANY_VERSION)
                .changeState(new ChangeCommand(OfferStateAssembler.APPROVED_BY_MANAGER).withParam(OfferStateAssembler.PARAM_APPROVING_BY_ID, OTHER_USER));

        // when
        state = state.changeState(new ChangeCommand(OfferStateAssembler.SOLD).withParam(OfferStateAssembler.PARAM_TOTAL_PRICE, MANAGER_APPROVAL_THRESHOLD));

        // then
        publisher.contains(DocumentPublished.class);

        assertThat(state.getStateDescriptor()).isEqualTo(OfferStateAssembler.SOLD);
        assertThat(((OfferHeader) state.getDocumentHeader()).getApprovingId()).isEqualTo(OTHER_USER);
    }

    @Test
    void cannotApproveAgainByDifferentManager() {
        // given
        State state = preOffer().changeContent(ANY_VERSION)
                .changeState(new ChangeCommand(OfferStateAssembler.APPROVED_BY_MANAGER).withParam(OfferStateAssembler.PARAM_APPROVING_BY_ID, ANY_USER));

        // when
        state = state.changeState(new ChangeCommand(OfferStateAssembler.APPROVED_BY_MANAGER).withParam(OfferStateAssembler.PARAM_APPROVING_BY_ID, OTHER_USER));

        // then
        publisher.containsNoEvents();

        assertThat(state.getStateDescriptor()).isEqualTo(OfferStateAssembler.APPROVED_BY_MANAGER);
        assertThat(((OfferHeader) state.getDocumentHeader()).getApprovingId()).isEqualTo(ANY_USER);
    }

    @Test
    void changingRejectedMovesToPreOffer() {
        // given
        State state = preOffer().changeContent(ANY_VERSION)
                .changeState(new ChangeCommand(OfferStateAssembler.REJECTED));

        // when
        state = state.changeContent(OTHER_VERSION);

        // then
        publisher.containsNoEvents();

        assertThat(state.getStateDescriptor()).isEqualTo(OfferStateAssembler.PRE_OFFER);
        assertThat(state.getDocumentHeader().getContentId()).isEqualTo(OTHER_VERSION);
    }

    @Test
    void canChangeStateToTheSame() {
        State state = preOffer().changeContent(ANY_VERSION);
        assertThat(state.getStateDescriptor()).isEqualTo(OfferStateAssembler.PRE_OFFER);
        state.changeState(new ChangeCommand(OfferStateAssembler.PRE_OFFER));
        assertThat(state.getStateDescriptor()).isEqualTo(OfferStateAssembler.PRE_OFFER);

        state = state.changeState(new ChangeCommand(OfferStateAssembler.DISCOUNTED));
        assertThat(state.getStateDescriptor()).isEqualTo(OfferStateAssembler.DISCOUNTED);
        state = state.changeState(new ChangeCommand(OfferStateAssembler.DISCOUNTED));
        assertThat(state.getStateDescriptor()).isEqualTo(OfferStateAssembler.DISCOUNTED);
    }


    @Test
    void cannotChangeStateToTheSame() {
        State state = preOffer().changeContent(ANY_VERSION);

        State afterFirstChangeState = state.changeState(new ChangeCommand(OfferStateAssembler.ACCEPTED_BY_CLIENT).withParam(OfferStateAssembler.PARAM_TOTAL_PRICE, BELOW_MANAGER_APPROVAL_THRESHOLD));
        assertThat(afterFirstChangeState).isNotEqualTo(state);
        assertThat(afterFirstChangeState.getStateDescriptor()).isEqualTo(OfferStateAssembler.ACCEPTED_BY_CLIENT);
        State afterSecondChangeState = afterFirstChangeState.changeState(new ChangeCommand(OfferStateAssembler.ACCEPTED_BY_CLIENT).withParam(OfferStateAssembler.PARAM_TOTAL_PRICE, BELOW_MANAGER_APPROVAL_THRESHOLD));
        assertThat(afterSecondChangeState).isEqualTo(afterFirstChangeState);

        afterFirstChangeState = state.changeState(new ChangeCommand(OfferStateAssembler.SOLD).withParam(OfferStateAssembler.PARAM_TOTAL_PRICE, BELOW_MANAGER_APPROVAL_THRESHOLD));
        assertThat(afterFirstChangeState).isNotEqualTo(state);
        assertThat(afterFirstChangeState.getStateDescriptor()).isEqualTo(OfferStateAssembler.SOLD);
        afterSecondChangeState = afterFirstChangeState.changeState(new ChangeCommand(OfferStateAssembler.SOLD).withParam(OfferStateAssembler.PARAM_TOTAL_PRICE, BELOW_MANAGER_APPROVAL_THRESHOLD));
        assertThat(afterSecondChangeState).isEqualTo(afterFirstChangeState);

        afterFirstChangeState = state.changeState(new ChangeCommand(OfferStateAssembler.REJECTED).withParam(OfferStateAssembler.PARAM_REJECTED_BY_ID, ANY_USER));
        assertThat(afterFirstChangeState).isNotEqualTo(state);
        assertThat(afterFirstChangeState.getStateDescriptor()).isEqualTo(OfferStateAssembler.REJECTED);
        afterSecondChangeState = afterFirstChangeState.changeState(new ChangeCommand(OfferStateAssembler.REJECTED).withParam(OfferStateAssembler.PARAM_REJECTED_BY_ID, OTHER_USER));
        assertThat(afterSecondChangeState).isEqualTo(afterFirstChangeState);
    }

    private State preOffer() {
        OfferHeader header = new OfferHeader(ANY_USER, ANY_NUMBER);
        header.setStateDescriptor(OfferStateAssembler.PRE_OFFER);

        OfferStateAssembler assembler = new OfferStateAssembler(publisher);
        StateConfig<OfferHeader> config = assembler.assemble();

        return config.recreate(header);
    }
}
