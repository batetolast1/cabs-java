package io.legacyfighter.cabs.offers.application;

import io.legacyfighter.cabs.contracts.legacy.User;
import io.legacyfighter.cabs.contracts.legacy.UserRepository;
import io.legacyfighter.cabs.document.application.dynamic.DocumentOperationResult;
import io.legacyfighter.cabs.document.application.dynamic.DocumentResourceManager;
import io.legacyfighter.cabs.document.model.ContentId;
import io.legacyfighter.cabs.document.model.DocumentHeaderRepository;
import io.legacyfighter.cabs.document.model.content.DocumentNumber;
import io.legacyfighter.cabs.document.model.state.dynamic.ChangeCommand;
import io.legacyfighter.cabs.document.model.state.dynamic.State;
import io.legacyfighter.cabs.document.model.state.dynamic.StateConfig;
import io.legacyfighter.cabs.offers.model.OfferHeader;
import io.legacyfighter.cabs.offers.model.state.OfferStateAssembler;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Map;
import java.util.Random;

@Service
public class OfferResourceManager implements DocumentResourceManager<OfferHeader> {

    private final DocumentHeaderRepository<OfferHeader> documentHeaderRepository;

    private final OfferStateAssembler assembler;

    private final UserRepository userRepository;

    private final Random random;

    public OfferResourceManager(DocumentHeaderRepository<OfferHeader> documentHeaderRepository,
                                OfferStateAssembler assembler,
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
        OfferHeader offerHeader = new OfferHeader(author.getId(), number);

        StateConfig<OfferHeader> stateConfig = assembler.assemble();
        State state = stateConfig.begin(offerHeader);

        documentHeaderRepository.save(offerHeader);

        return generateDocumentOperationResult(DocumentOperationResult.Result.SUCCESS, state);
    }

    @Transactional
    public DocumentOperationResult changeState(Long documentId, String desiredState, Map<String, Object> params) {
        OfferHeader offerHeader = documentHeaderRepository.getOne(documentId, OfferHeader.class);
        StateConfig<OfferHeader> stateConfig = assembler.assemble();

        State state = stateConfig.recreate(offerHeader);
        state = state.changeState(new ChangeCommand(desiredState, params));

        documentHeaderRepository.save(offerHeader);

        return generateDocumentOperationResult(DocumentOperationResult.Result.SUCCESS, state);
    }

    @Transactional
    public DocumentOperationResult changeContent(Long headerId, ContentId contentVersion) {
        OfferHeader offerHeader = documentHeaderRepository.getOne(headerId, OfferHeader.class);
        StateConfig<OfferHeader> stateConfig = assembler.assemble();

        State state = stateConfig.recreate(offerHeader);
        state = state.changeContent(contentVersion);

        documentHeaderRepository.save(offerHeader);

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
