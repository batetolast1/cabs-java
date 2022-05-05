package io.legacyfighter.cabs.offers.model.state;

import io.legacyfighter.cabs.document.model.state.dynamic.StateAssembler;
import io.legacyfighter.cabs.document.model.state.dynamic.StateBuilder;
import io.legacyfighter.cabs.document.model.state.dynamic.StateConfig;
import io.legacyfighter.cabs.document.model.state.dynamic.config.actions.PublishEvent;
import io.legacyfighter.cabs.document.model.state.dynamic.config.events.DocumentPublished;
import io.legacyfighter.cabs.document.model.state.dynamic.config.predicates.statechange.ContentNotEmptyVerifier;
import io.legacyfighter.cabs.document.model.state.dynamic.config.predicates.statechange.NegativeVerifier;
import io.legacyfighter.cabs.money.Money;
import io.legacyfighter.cabs.offers.model.OfferHeader;
import io.legacyfighter.cabs.offers.model.state.config.actions.SetApprovingBy;
import io.legacyfighter.cabs.offers.model.state.config.actions.SetRejectedBy;
import io.legacyfighter.cabs.offers.model.state.config.predicates.statechange.ApprovalVerifier;
import io.legacyfighter.cabs.offers.model.state.config.predicates.statechange.NeedsManagerApprovalVerifier;
import io.legacyfighter.cabs.offers.model.state.config.predicates.statechange.RejectionVerifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

/**
 * Sample static config.
 */
@Component
public class OfferStateAssembler implements StateAssembler<OfferHeader> {

    public static final String PRE_OFFER = "pre-offer";
    public static final String DISCOUNTED = "discounted";
    public static final String REJECTED = "rejected";
    public static final String APPROVED_BY_MANAGER = "approved by manager";
    public static final String ACCEPTED_BY_CLIENT = "accepted by client";
    public static final String SOLD = "sold";

    public static final Money APPROVAL_REQUIRED_THRESHOLD = new Money(100);

    public static final String PARAM_TOTAL_PRICE = NeedsManagerApprovalVerifier.PARAM_TOTAL_PRICE;

    public static final String PARAM_REJECTED_BY_ID = SetRejectedBy.PARAM_REJECTED_BY_ID;

    public static final String PARAM_APPROVING_BY_ID = SetApprovingBy.PARAM_APPROVING_BY_ID;

    private final ApplicationEventPublisher publisher;

    @Autowired
    public OfferStateAssembler(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public StateConfig<OfferHeader> assemble() {
        StateBuilder<OfferHeader> builder = new StateBuilder<>();

        builder.beginWith(PRE_OFFER).whenContentChanged()
                .to(PRE_OFFER);

        builder.from(PRE_OFFER).check(new ContentNotEmptyVerifier())
                .to(DISCOUNTED);

        builder.from(PRE_OFFER).check(new RejectionVerifier())
                .to(REJECTED).action(new SetRejectedBy());

        builder.from(PRE_OFFER).check(new ContentNotEmptyVerifier()).check(new ApprovalVerifier())
                .to(APPROVED_BY_MANAGER).action(new SetApprovingBy());

        builder.from(PRE_OFFER).check(new ContentNotEmptyVerifier()).check(new NeedsManagerApprovalVerifier(APPROVAL_REQUIRED_THRESHOLD))
                .to(ACCEPTED_BY_CLIENT);

        builder.from(PRE_OFFER).check(new ContentNotEmptyVerifier()).check(new NeedsManagerApprovalVerifier(APPROVAL_REQUIRED_THRESHOLD))
                .to(SOLD).action(new PublishEvent(DocumentPublished.class, publisher));

        builder.from(DISCOUNTED).check(new RejectionVerifier())
                .to(REJECTED).action(new SetRejectedBy());

        builder.from(DISCOUNTED).check(new ApprovalVerifier())
                .to(APPROVED_BY_MANAGER).action(new SetApprovingBy());

        builder.from(DISCOUNTED).check(new NeedsManagerApprovalVerifier(APPROVAL_REQUIRED_THRESHOLD))
                .to(ACCEPTED_BY_CLIENT);

        builder.from(DISCOUNTED).check(new NeedsManagerApprovalVerifier(APPROVAL_REQUIRED_THRESHOLD))
                .to(SOLD).action(new PublishEvent(DocumentPublished.class, publisher));

        builder.from(APPROVED_BY_MANAGER).check(new RejectionVerifier())
                .to(REJECTED).action(new SetRejectedBy());

        builder.from(APPROVED_BY_MANAGER).check(new NegativeVerifier())
                .to(APPROVED_BY_MANAGER);

        builder.from(APPROVED_BY_MANAGER)
                .to(ACCEPTED_BY_CLIENT);

        builder.from(APPROVED_BY_MANAGER)
                .to(SOLD).action(new PublishEvent(DocumentPublished.class, publisher));

        builder.from(ACCEPTED_BY_CLIENT)
                .to(SOLD).action(new PublishEvent(DocumentPublished.class, publisher));

        builder.from(ACCEPTED_BY_CLIENT).check(new NegativeVerifier())
                .to(ACCEPTED_BY_CLIENT);

        builder.from(ACCEPTED_BY_CLIENT).check(new RejectionVerifier())
                .to(REJECTED).action(new SetRejectedBy());

        builder.from(SOLD).check(new NegativeVerifier())
                .to(SOLD);

        builder.from(REJECTED).whenContentChanged()
                .to(PRE_OFFER);

        builder.from(REJECTED).check(new NegativeVerifier())
                .to(REJECTED);

        return builder;
    }
}
