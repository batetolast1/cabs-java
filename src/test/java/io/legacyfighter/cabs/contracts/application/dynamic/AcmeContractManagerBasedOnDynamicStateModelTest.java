package io.legacyfighter.cabs.contracts.application.dynamic;

import io.legacyfighter.cabs.contracts.application.acme.dynamic.DocumentOperationResult;
import io.legacyfighter.cabs.contracts.application.acme.dynamic.DocumentResourceManager;
import io.legacyfighter.cabs.contracts.application.editor.CommitResult;
import io.legacyfighter.cabs.contracts.application.editor.DocumentDTO;
import io.legacyfighter.cabs.contracts.application.editor.DocumentEditor;
import io.legacyfighter.cabs.contracts.legacy.User;
import io.legacyfighter.cabs.contracts.legacy.UserRepository;
import io.legacyfighter.cabs.contracts.model.ContentId;
import io.legacyfighter.cabs.contracts.model.content.ContentVersion;
import io.legacyfighter.cabs.contracts.model.state.dynamic.acme.AcmeContractStateAssembler;
import io.legacyfighter.cabs.contracts.model.state.dynamic.config.predicates.statechange.AuthorIsNotAVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class AcmeContractManagerBasedOnDynamicStateModelTest {

    @Autowired
    private DocumentEditor editor;

    @Autowired
    private DocumentResourceManager documentResourceManager;

    @Autowired
    private UserRepository userRepository;

    private static final String CONTENT_1 = "content 1";

    private static final String CONTENT_2 = "content 2";

    private static final ContentVersion ANY_VERSION = new ContentVersion("v1");

    private User author;

    private User verifier;

    private Long headerId;

    @BeforeEach
    void prepare() {
        author = userRepository.save(new User());
        verifier = userRepository.save(new User());
    }

    @Test
    void verifierOtherThanAuthorCanVerify() {
        // given
        crateAcmeContract(author);
        // and
        ContentId contentId = commitContent(CONTENT_1);
        // and
        DocumentOperationResult result = documentResourceManager.changeContent(headerId, contentId);
        new DocumentOperationResultAssert(result).state(AcmeContractStateAssembler.DRAFT)
                .editable()
                .possibleNextStates(AcmeContractStateAssembler.VERIFIED, AcmeContractStateAssembler.ARCHIVED);

        // when
        result = documentResourceManager.changeState(headerId, AcmeContractStateAssembler.VERIFIED, verifierParam());

        // then
        new DocumentOperationResultAssert(result).state(AcmeContractStateAssembler.VERIFIED)
                .editable()
                .possibleNextStates(AcmeContractStateAssembler.PUBLISHED, AcmeContractStateAssembler.ARCHIVED);
    }


    @Test
    void authorCanNotVerify() {
        // given
        crateAcmeContract(author);
        // and
        ContentId contentId = commitContent(CONTENT_1);
        // and
        DocumentOperationResult result = documentResourceManager.changeContent(headerId, contentId);
        new DocumentOperationResultAssert(result).state(AcmeContractStateAssembler.DRAFT);

        // when
        result = documentResourceManager.changeState(headerId, AcmeContractStateAssembler.VERIFIED, authorParam());

        // then
        new DocumentOperationResultAssert(result).state(AcmeContractStateAssembler.DRAFT);
    }

    @Test
    void changingContentOfVerifiedMovesBackToDraft() {
        // given
        crateAcmeContract(author);
        // and
        ContentId contentId = commitContent(CONTENT_1);
        // and
        DocumentOperationResult result = documentResourceManager.changeContent(headerId, contentId);
        new DocumentOperationResultAssert(result).state(AcmeContractStateAssembler.DRAFT)
                .editable();
        // and
        result = documentResourceManager.changeState(headerId, AcmeContractStateAssembler.VERIFIED, verifierParam());
        new DocumentOperationResultAssert(result).state(AcmeContractStateAssembler.VERIFIED)
                .editable();
        // and
        contentId = commitContent(CONTENT_2);

        // when
        result = documentResourceManager.changeContent(headerId, contentId);

        // then
        new DocumentOperationResultAssert(result).state(AcmeContractStateAssembler.DRAFT)
                .editable();
    }

    @Test
    void publishedCanNotBeChanged() {
        // given
        crateAcmeContract(author);
        // and
        ContentId firstContentId = commitContent(CONTENT_1);
        // and
        DocumentOperationResult result = documentResourceManager.changeContent(headerId, firstContentId);
        new DocumentOperationResultAssert(result).state(AcmeContractStateAssembler.DRAFT)
                .editable();
        // and
        result = documentResourceManager.changeState(headerId, AcmeContractStateAssembler.VERIFIED, verifierParam());
        new DocumentOperationResultAssert(result).state(AcmeContractStateAssembler.VERIFIED)
                .editable();
        // and
        result = documentResourceManager.changeState(headerId, AcmeContractStateAssembler.PUBLISHED, emptyParam());
        new DocumentOperationResultAssert(result).state(AcmeContractStateAssembler.PUBLISHED)
                .nonEditable();
        // and
        ContentId newContentId = commitContent(CONTENT_2);

        // when
        result = documentResourceManager.changeContent(headerId, newContentId);

        // then
        new DocumentOperationResultAssert(result).state(AcmeContractStateAssembler.PUBLISHED)
                .nonEditable()
                .content(firstContentId);
    }

    private void crateAcmeContract(User user) {
        DocumentOperationResult result = documentResourceManager.createDocument(user.getId());

        headerId = result.getDocumentHeaderId();
    }

    private ContentId commitContent(String content) {
        DocumentDTO doc = new DocumentDTO(null, content, ANY_VERSION);

        CommitResult result = editor.commit(doc);

        assertThat(result.getResult()).isEqualTo(CommitResult.Result.SUCCESS);

        return new ContentId(result.getContentId());
    }

    private Map<String, Object> verifierParam() {
        return Map.of(AuthorIsNotAVerifier.PARAM_VERIFIER, verifier.getId());
    }

    private Map<String, Object> authorParam() {
        return Map.of(AuthorIsNotAVerifier.PARAM_VERIFIER, author.getId());
    }

    private Map<String, Object> emptyParam() {
        return Map.of();
    }
}
