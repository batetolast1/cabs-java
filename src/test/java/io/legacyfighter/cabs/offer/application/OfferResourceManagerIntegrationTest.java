package io.legacyfighter.cabs.offer.application;

import io.legacyfighter.cabs.common.DocumentOperationResultAssert;
import io.legacyfighter.cabs.contracts.legacy.User;
import io.legacyfighter.cabs.contracts.legacy.UserRepository;
import io.legacyfighter.cabs.document.application.dynamic.DocumentOperationResult;
import io.legacyfighter.cabs.document.application.editor.CommitResult;
import io.legacyfighter.cabs.document.application.editor.DocumentDTO;
import io.legacyfighter.cabs.document.application.editor.DocumentEditor;
import io.legacyfighter.cabs.document.model.ContentId;
import io.legacyfighter.cabs.document.model.content.ContentVersion;
import io.legacyfighter.cabs.money.Money;
import io.legacyfighter.cabs.offers.application.OfferResourceManager;
import io.legacyfighter.cabs.offers.model.state.OfferStateAssembler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class OfferResourceManagerIntegrationTest {

    @Autowired
    private DocumentEditor editor;

    @Autowired
    private OfferResourceManager offerResourceManager;

    @Autowired
    private UserRepository userRepository;

    private static final String CONTENT_1 = "content 1";

    private static final ContentVersion ANY_VERSION = new ContentVersion("v1");

    private static final Money BELOW_MANAGER_APPROVAL_THRESHOLD = new Money(99);

    private static final Money MANAGER_APPROVAL_THRESHOLD = new Money(100);

    private User author;

    private Long headerId;

    @BeforeEach
    void prepare() {
        author = userRepository.save(new User());
    }

    @Test
    void canCreatePreOffer() {
        // when
        DocumentOperationResult result = offerResourceManager.createDocument(author.getId());

        // then
        new DocumentOperationResultAssert(result).state(OfferStateAssembler.PRE_OFFER)
                .editable()
                .possibleNextStates(
                        OfferStateAssembler.DISCOUNTED,
                        OfferStateAssembler.APPROVED_BY_MANAGER,
                        OfferStateAssembler.ACCEPTED_BY_CLIENT,
                        OfferStateAssembler.SOLD,
                        OfferStateAssembler.REJECTED
                )
                .content(null);
    }

    @Test
    void preOfferCanBeSoldWhenDoesntNeedManagerApproval() {
        // given
        cratePreOffer(author);
        // and
        ContentId contentId = commitContent(CONTENT_1);
        // and
        DocumentOperationResult result = offerResourceManager.changeContent(headerId, contentId);
        new DocumentOperationResultAssert(result).state(OfferStateAssembler.PRE_OFFER)
                .editable()
                .possibleNextStates(
                        OfferStateAssembler.DISCOUNTED,
                        OfferStateAssembler.APPROVED_BY_MANAGER,
                        OfferStateAssembler.ACCEPTED_BY_CLIENT,
                        OfferStateAssembler.SOLD,
                        OfferStateAssembler.REJECTED
                )
                .content(contentId);

        // when
        result = offerResourceManager.changeState(headerId, OfferStateAssembler.SOLD, totalPriceParam(BELOW_MANAGER_APPROVAL_THRESHOLD));

        // then
        new DocumentOperationResultAssert(result).state(OfferStateAssembler.SOLD)
                .nonEditable()
                .noPossibleNextStates()
                .content(contentId);
    }

    @Test
    void preOfferCannotBeSoldWhenDoesntNeedManagerApproval() {
        // given
        cratePreOffer(author);
        // and
        ContentId contentId = commitContent(CONTENT_1);
        // and
        DocumentOperationResult result = offerResourceManager.changeContent(headerId, contentId);
        new DocumentOperationResultAssert(result).state(OfferStateAssembler.PRE_OFFER)
                .editable()
                .possibleNextStates(
                        OfferStateAssembler.DISCOUNTED,
                        OfferStateAssembler.APPROVED_BY_MANAGER,
                        OfferStateAssembler.ACCEPTED_BY_CLIENT,
                        OfferStateAssembler.SOLD,
                        OfferStateAssembler.REJECTED
                )
                .content(contentId);

        // when
        result = offerResourceManager.changeState(headerId, OfferStateAssembler.SOLD, totalPriceParam(MANAGER_APPROVAL_THRESHOLD));

        // then
        new DocumentOperationResultAssert(result).state(OfferStateAssembler.PRE_OFFER)
                .editable()
                .possibleNextStates(
                        OfferStateAssembler.DISCOUNTED,
                        OfferStateAssembler.APPROVED_BY_MANAGER,
                        OfferStateAssembler.ACCEPTED_BY_CLIENT,
                        OfferStateAssembler.SOLD,
                        OfferStateAssembler.REJECTED
                )
                .content(contentId);
    }

    private void cratePreOffer(User user) {
        DocumentOperationResult result = offerResourceManager.createDocument(user.getId());

        headerId = result.getDocumentHeaderId();
    }

    private ContentId commitContent(String content) {
        DocumentDTO doc = new DocumentDTO(null, content, ANY_VERSION);

        CommitResult result = editor.commit(doc);

        assertThat(result.getResult()).isEqualTo(CommitResult.Result.SUCCESS);

        return new ContentId(result.getContentId());
    }

    private Map<String, Object> totalPriceParam(Money totalPrice) {
        return Map.of(OfferStateAssembler.PARAM_TOTAL_PRICE, totalPrice);
    }
}
