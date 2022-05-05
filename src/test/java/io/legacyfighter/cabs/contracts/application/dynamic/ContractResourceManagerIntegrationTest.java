package io.legacyfighter.cabs.contracts.application.dynamic;

import io.legacyfighter.cabs.common.DocumentOperationResultAssert;
import io.legacyfighter.cabs.contracts.legacy.User;
import io.legacyfighter.cabs.contracts.legacy.UserRepository;
import io.legacyfighter.cabs.contracts.model.state.dynamic.ContractStateAssembler;
import io.legacyfighter.cabs.contracts.model.state.dynamic.config.predicates.statechange.AuthorIsNotAVerifier;
import io.legacyfighter.cabs.document.application.dynamic.DocumentOperationResult;
import io.legacyfighter.cabs.document.application.editor.CommitResult;
import io.legacyfighter.cabs.document.application.editor.DocumentDTO;
import io.legacyfighter.cabs.document.application.editor.DocumentEditor;
import io.legacyfighter.cabs.document.model.ContentId;
import io.legacyfighter.cabs.document.model.content.ContentVersion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ContractResourceManagerIntegrationTest {

    @Autowired
    private DocumentEditor editor;

    @Autowired
    private ContractResourceManager contractResourceManager;

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
        crateContract(author);
        // and
        ContentId contentId = commitContent(CONTENT_1);
        // and
        DocumentOperationResult result = contractResourceManager.changeContent(headerId, contentId);
        new DocumentOperationResultAssert(result).state(ContractStateAssembler.DRAFT)
                .editable()
                .possibleNextStates(ContractStateAssembler.VERIFIED, ContractStateAssembler.ARCHIVED);

        // when
        result = contractResourceManager.changeState(headerId, ContractStateAssembler.VERIFIED, verifierParam());

        // then
        new DocumentOperationResultAssert(result).state(ContractStateAssembler.VERIFIED)
                .editable()
                .possibleNextStates(ContractStateAssembler.PUBLISHED, ContractStateAssembler.ARCHIVED);
    }


    @Test
    void authorCanNotVerify() {
        // given
        crateContract(author);
        // and
        ContentId contentId = commitContent(CONTENT_1);
        // and
        DocumentOperationResult result = contractResourceManager.changeContent(headerId, contentId);
        new DocumentOperationResultAssert(result).state(ContractStateAssembler.DRAFT);

        // when
        result = contractResourceManager.changeState(headerId, ContractStateAssembler.VERIFIED, authorParam());

        // then
        new DocumentOperationResultAssert(result).state(ContractStateAssembler.DRAFT);
    }

    @Test
    void changingContentOfVerifiedMovesBackToDraft() {
        // given
        crateContract(author);
        // and
        ContentId contentId = commitContent(CONTENT_1);
        // and
        DocumentOperationResult result = contractResourceManager.changeContent(headerId, contentId);
        new DocumentOperationResultAssert(result).state(ContractStateAssembler.DRAFT)
                .editable();
        // and
        result = contractResourceManager.changeState(headerId, ContractStateAssembler.VERIFIED, verifierParam());
        new DocumentOperationResultAssert(result).state(ContractStateAssembler.VERIFIED)
                .editable();
        // and
        contentId = commitContent(CONTENT_2);

        // when
        result = contractResourceManager.changeContent(headerId, contentId);

        // then
        new DocumentOperationResultAssert(result).state(ContractStateAssembler.DRAFT)
                .editable();
    }

    @Test
    void publishedCanNotBeChanged() {
        // given
        crateContract(author);
        // and
        ContentId firstContentId = commitContent(CONTENT_1);
        // and
        DocumentOperationResult result = contractResourceManager.changeContent(headerId, firstContentId);
        new DocumentOperationResultAssert(result).state(ContractStateAssembler.DRAFT)
                .editable();
        // and
        result = contractResourceManager.changeState(headerId, ContractStateAssembler.VERIFIED, verifierParam());
        new DocumentOperationResultAssert(result).state(ContractStateAssembler.VERIFIED)
                .editable();
        // and
        result = contractResourceManager.changeState(headerId, ContractStateAssembler.PUBLISHED, emptyParam());
        new DocumentOperationResultAssert(result).state(ContractStateAssembler.PUBLISHED)
                .nonEditable();
        // and
        ContentId newContentId = commitContent(CONTENT_2);

        // when
        result = contractResourceManager.changeContent(headerId, newContentId);

        // then
        new DocumentOperationResultAssert(result).state(ContractStateAssembler.PUBLISHED)
                .nonEditable()
                .content(firstContentId);
    }

    private void crateContract(User user) {
        DocumentOperationResult result = contractResourceManager.createDocument(user.getId());

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
