package io.legacyfighter.cabs.contracts.application.dynamic;

import io.legacyfighter.cabs.contracts.legacy.User;
import io.legacyfighter.cabs.contracts.legacy.UserRepository;
import io.legacyfighter.cabs.contracts.model.ContractHeader;
import io.legacyfighter.cabs.contracts.model.state.dynamic.ContractStateAssembler;
import io.legacyfighter.cabs.document.application.dynamic.DocumentOperationResult;
import io.legacyfighter.cabs.document.application.dynamic.DocumentResourceManager;
import io.legacyfighter.cabs.document.model.ContentId;
import io.legacyfighter.cabs.document.model.DocumentHeaderRepository;
import io.legacyfighter.cabs.document.model.content.DocumentNumber;
import io.legacyfighter.cabs.document.model.state.dynamic.ChangeCommand;
import io.legacyfighter.cabs.document.model.state.dynamic.State;
import io.legacyfighter.cabs.document.model.state.dynamic.StateConfig;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Random;

@Service
public class ContractResourceManager implements DocumentResourceManager<ContractHeader> {

    private final DocumentHeaderRepository<ContractHeader> documentHeaderRepository;

    private final ContractStateAssembler assembler;

    private final UserRepository userRepository;

    private final Random random;

    public ContractResourceManager(DocumentHeaderRepository<ContractHeader> documentHeaderRepository,
                                   ContractStateAssembler assembler,
                                   UserRepository userRepository) throws NoSuchAlgorithmException {
        this.documentHeaderRepository = documentHeaderRepository;
        this.assembler = assembler;
        this.userRepository = userRepository;

        random = SecureRandom.getInstanceStrong();
    }

    @Transactional
    public DocumentOperationResult createDocument(Long authorId) {
        User author = userRepository.getOne(authorId);

        DocumentNumber number = generateNumber();
        ContractHeader contractHeader = new ContractHeader(author.getId(), number);

        StateConfig<ContractHeader> stateConfig = assembler.assemble();
        State state = stateConfig.begin(contractHeader);

        documentHeaderRepository.save(contractHeader);

        return generateDocumentOperationResult(DocumentOperationResult.Result.SUCCESS, state);
    }

    @Transactional
    public DocumentOperationResult changeState(Long documentId, String desiredState, Map<String, Object> params) {
        ContractHeader contractHeader = documentHeaderRepository.getOne(documentId, ContractHeader.class);
        StateConfig<ContractHeader> stateConfig = assembler.assemble();

        State state = stateConfig.recreate(contractHeader);
        state = state.changeState(new ChangeCommand(desiredState, params));

        documentHeaderRepository.save(contractHeader);

        return generateDocumentOperationResult(DocumentOperationResult.Result.SUCCESS, state);
    }

    @Transactional
    public DocumentOperationResult changeContent(Long headerId, ContentId contentVersion) {
        ContractHeader contractHeader = documentHeaderRepository.getOne(headerId, ContractHeader.class);
        StateConfig<ContractHeader> stateConfig = assembler.assemble();

        State state = stateConfig.recreate(contractHeader);
        state = state.changeContent(contentVersion);

        documentHeaderRepository.save(contractHeader);

        return generateDocumentOperationResult(DocumentOperationResult.Result.SUCCESS, state);
    }

    private DocumentOperationResult generateDocumentOperationResult(DocumentOperationResult.Result result, State state) {
        return new DocumentOperationResult(
                result,
                state.getDocumentHeader().getId(),
                state.getDocumentHeader().getDocumentNumber(),
                state.getStateDescriptor(),
                state.getDocumentHeader().getContentId(),
                state.extractPossibleTransitionsAndRules(),
                state.isContentEditable(),
                state.extractContentChangePredicate()
        );
    }

    private DocumentNumber generateNumber() {
        return new DocumentNumber("nr: " + random.nextInt()); //TODO integrate with doc number generator
    }
}
