package io.legacyfighter.cabs.contracts.application.straightforward;

import io.legacyfighter.cabs.contracts.legacy.User;
import io.legacyfighter.cabs.contracts.legacy.UserRepository;
import io.legacyfighter.cabs.contracts.model.state.straightforward.DraftState;
import io.legacyfighter.cabs.contracts.model.state.straightforward.VerifiedState;
import io.legacyfighter.cabs.document.application.editor.CommitResult;
import io.legacyfighter.cabs.document.application.editor.DocumentDTO;
import io.legacyfighter.cabs.document.application.editor.DocumentEditor;
import io.legacyfighter.cabs.document.model.ContentId;
import io.legacyfighter.cabs.document.model.content.ContentVersion;
import io.legacyfighter.cabs.document.model.content.DocumentNumber;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class ContractProcessIntegrationTest {

    private static final String CONTENT_1 = "content 1";
    private static final String CONTENT_2 = "content 2";
    private static final ContentVersion ANY_VERSION = new ContentVersion("v1");

    @Autowired
    private DocumentEditor editor;

    @Autowired
    private ContractProcess contractProcess;

    @Autowired
    private UserRepository userRepository;

    private User author;

    private User verifier;

    private DocumentNumber documentNumber;

    private Long headerId;

    @BeforeEach
    public void setup() {
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
        documentNumber = contractProcess.changeContent(headerId, contentId).getDocumentNumber();

        // when
        ContractResult result = contractProcess.verify(headerId, verifier.getId());

        // then
        new ContractResultAssert(result)
                .success()
                .state(new VerifiedState(verifier.getId()))
                .documentHeader(headerId)
                .documentNumber(documentNumber);
    }

    @Test
    void authorCanNotVerify() {
        // given
        crateContract(author);
        // and
        ContentId contentId = commitContent(CONTENT_1);
        // and
        documentNumber = contractProcess.changeContent(headerId, contentId).getDocumentNumber();

        // when
        ContractResult result = contractProcess.verify(headerId, author.getId());

        // then
        new ContractResultAssert(result)
                .success()
                .state(new DraftState())
                .documentHeader(headerId)
                .documentNumber(documentNumber);
    }

    @Test
    void changingContentOfVerifiedMovesBackToDraft() {
        // given
        crateContract(author);
        // and
        ContentId contentId = commitContent(CONTENT_1);
        // and
        ContractResult result = contractProcess.changeContent(headerId, contentId);
        new ContractResultAssert(result)
                .success()
                .state(new DraftState());
        // and
        result = contractProcess.verify(headerId, verifier.getId());
        new ContractResultAssert(result)
                .success()
                .state(new VerifiedState(verifier.getId()));

        // when
        contentId = commitContent(CONTENT_2);

        // then
        result = contractProcess.changeContent(headerId, contentId);

        new ContractResultAssert(result)
                .success()
                .state(new DraftState())
                .documentHeader(headerId)
                .documentNumber(result.getDocumentNumber());
    }

    private ContentId commitContent(String content) {
        DocumentDTO doc = new DocumentDTO(null, content, ANY_VERSION);

        CommitResult result = editor.commit(doc);

        assertThat(result.getResult()).isEqualTo(CommitResult.Result.SUCCESS);

        return new ContentId(result.getContentId());
    }

    private void crateContract(User user) {
        ContractResult contractResult = contractProcess.createContract(user.getId());

        headerId = contractResult.getDocumentHeaderId();
    }
}
