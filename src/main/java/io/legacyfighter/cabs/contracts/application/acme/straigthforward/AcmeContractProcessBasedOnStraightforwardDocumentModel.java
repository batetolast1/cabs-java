package io.legacyfighter.cabs.contracts.application.acme.straigthforward;

import io.legacyfighter.cabs.contracts.legacy.User;
import io.legacyfighter.cabs.contracts.legacy.UserRepository;
import io.legacyfighter.cabs.contracts.model.ContentId;
import io.legacyfighter.cabs.contracts.model.DocumentHeader;
import io.legacyfighter.cabs.contracts.model.DocumentHeaderRepository;
import io.legacyfighter.cabs.contracts.model.content.DocumentNumber;
import io.legacyfighter.cabs.contracts.model.state.straightforward.BaseState;
import io.legacyfighter.cabs.contracts.model.state.straightforward.acme.VerifiedState;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
public class AcmeContractProcessBasedOnStraightforwardDocumentModel {

    private final UserRepository userRepository;

    private final DocumentHeaderRepository documentHeaderRepository;

    private final AcmeStateFactory stateFactory;

    public AcmeContractProcessBasedOnStraightforwardDocumentModel(UserRepository userRepository,
                                                                  DocumentHeaderRepository documentHeaderRepository,
                                                                  AcmeStateFactory stateFactory) {
        this.userRepository = userRepository;
        this.documentHeaderRepository = documentHeaderRepository;
        this.stateFactory = stateFactory;
    }

    @Transactional
    public ContractResult createContract(Long authorId) {
        User author = userRepository.getOne(authorId);

        DocumentNumber number = generateNumber();
        DocumentHeader header = new DocumentHeader(author.getId(), number);

        documentHeaderRepository.save(header);

        return new ContractResult(
                ContractResult.Result.SUCCESS,
                header.getId(),
                number,
                header.getStateDescriptor()
        );
    }


    @Transactional
    public ContractResult verify(Long headerId, Long verifierId) {
        User verifier = userRepository.getOne(verifierId);
        //TODO user authorization

        DocumentHeader header = documentHeaderRepository.getOne(headerId);

        BaseState state = stateFactory.create(header);
        state.changeState(new VerifiedState(verifierId));

        documentHeaderRepository.save(header);

        return new ContractResult(
                ContractResult.Result.SUCCESS,
                headerId,
                header.getDocumentNumber(),
                header.getStateDescriptor()
        );
    }

    @Transactional
    public ContractResult changeContent(Long headerId, ContentId contentVersion) {
        DocumentHeader header = documentHeaderRepository.getOne(headerId);

        BaseState state = stateFactory.create(header);
        state.changeContent(contentVersion);

        documentHeaderRepository.save(header);

        return new ContractResult(
                ContractResult.Result.SUCCESS,
                headerId,
                header.getDocumentNumber(),
                header.getStateDescriptor());
    }

    private DocumentNumber generateNumber() {
        return new DocumentNumber("nr: " + new Random().nextInt()); //TODO integrate with doc number generator
    }
}
