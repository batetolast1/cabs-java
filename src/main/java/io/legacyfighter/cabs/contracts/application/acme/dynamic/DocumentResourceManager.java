package io.legacyfighter.cabs.contracts.application.acme.dynamic;

import io.legacyfighter.cabs.contracts.legacy.User;
import io.legacyfighter.cabs.contracts.legacy.UserRepository;
import io.legacyfighter.cabs.contracts.model.ContentId;
import io.legacyfighter.cabs.contracts.model.DocumentHeader;
import io.legacyfighter.cabs.contracts.model.DocumentHeaderRepository;
import io.legacyfighter.cabs.contracts.model.content.DocumentNumber;
import io.legacyfighter.cabs.contracts.model.state.dynamic.ChangeCommand;
import io.legacyfighter.cabs.contracts.model.state.dynamic.State;
import io.legacyfighter.cabs.contracts.model.state.dynamic.StateConfig;
import io.legacyfighter.cabs.contracts.model.state.dynamic.acme.AcmeContractStateAssembler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.function.BiPredicate;
import java.util.stream.Collectors;

@Service
public class DocumentResourceManager {

    private final DocumentHeaderRepository documentHeaderRepository;

    private final AcmeContractStateAssembler assembler;

    private final UserRepository userRepository;

    private final Random random;

    public DocumentResourceManager(DocumentHeaderRepository documentHeaderRepository,
                                   AcmeContractStateAssembler assembler,
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
        DocumentHeader documentHeader = new DocumentHeader(author.getId(), number);

        StateConfig stateConfig = assembler.assemble();
        State state = stateConfig.begin(documentHeader);

        documentHeaderRepository.save(documentHeader);

        return generateDocumentOperationResult(DocumentOperationResult.Result.SUCCESS, state);
    }

    @Transactional
    public DocumentOperationResult changeState(Long documentId, String desiredState, Map<String, Object> params) {
        DocumentHeader documentHeader = documentHeaderRepository.getOne(documentId);
        StateConfig stateConfig = assembler.assemble();

        State state = stateConfig.recreate(documentHeader);
        state = state.changeState(new ChangeCommand(desiredState, params));

        documentHeaderRepository.save(documentHeader);

        return generateDocumentOperationResult(DocumentOperationResult.Result.SUCCESS, state);
    }

    @Transactional
    public DocumentOperationResult changeContent(Long headerId, ContentId contentVersion) {
        DocumentHeader documentHeader = documentHeaderRepository.getOne(headerId);
        StateConfig stateConfig = assembler.assemble();

        State state = stateConfig.recreate(documentHeader);
        state = state.changeContent(contentVersion);

        documentHeaderRepository.save(documentHeader);

        return generateDocumentOperationResult(DocumentOperationResult.Result.SUCCESS, state);
    }

    private DocumentOperationResult generateDocumentOperationResult(DocumentOperationResult.Result result, State state) {
        return new DocumentOperationResult(
                result,
                state.getDocumentHeader().getId(),
                state.getDocumentHeader().getDocumentNumber(),
                state.getStateDescriptor(),
                state.getDocumentHeader().getContentId(),
                extractPossibleTransitionsAndRules(state),
                state.isContentEditable(),
                extractContentChangePredicate(state)
        );
    }

    private String extractContentChangePredicate(State state) {
        if (state.isContentEditable()) {
            return state.getContentChangePredicate().getClass().getTypeName();
        }
        return null;
    }


    private Map<String, List<String>> extractPossibleTransitionsAndRules(State state) {
        Map<String, List<String>> transitionsAndRules = new HashMap<>();

        Map<State, List<BiPredicate<State, ChangeCommand>>> stateChangePredicates = state.getStateChangePredicates();
        for (Map.Entry<State, List<BiPredicate<State, ChangeCommand>>> entry : stateChangePredicates.entrySet()) {
            State possibleState = entry.getKey();
            if (possibleState.equals(state)) { //transition to self is not important
                continue;
            }

            List<BiPredicate<State, ChangeCommand>> predicates = entry.getValue();

            List<String> ruleNames = predicates.stream()
                    .map(predicate -> predicate.getClass().getTypeName())
                    .collect(Collectors.toList());

            transitionsAndRules.put(possibleState.getStateDescriptor(), ruleNames);
        }

        return transitionsAndRules;
    }

    private DocumentNumber generateNumber() {
        return new DocumentNumber("nr: " + random.nextInt()); //TODO integrate with doc number generator
    }
}
