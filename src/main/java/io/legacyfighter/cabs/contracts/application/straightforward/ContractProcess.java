package io.legacyfighter.cabs.contracts.application.straightforward;

import io.legacyfighter.cabs.contracts.legacy.User;
import io.legacyfighter.cabs.contracts.legacy.UserRepository;
import io.legacyfighter.cabs.contracts.model.ContractHeader;
import io.legacyfighter.cabs.contracts.model.state.straightforward.VerifiedState;
import io.legacyfighter.cabs.document.model.ContentId;
import io.legacyfighter.cabs.document.model.DocumentHeaderRepository;
import io.legacyfighter.cabs.document.model.content.DocumentNumber;
import io.legacyfighter.cabs.document.model.state.straightforward.BaseState;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

@Service
public class ContractProcess {

    private final UserRepository userRepository;

    private final DocumentHeaderRepository<ContractHeader> documentHeaderRepository;

    private final ContractStateFactory stateFactory;

    public ContractProcess(UserRepository userRepository,
                           DocumentHeaderRepository<ContractHeader> documentHeaderRepository,
                           ContractStateFactory stateFactory) {
        this.userRepository = userRepository;
        this.documentHeaderRepository = documentHeaderRepository;
        this.stateFactory = stateFactory;
    }

    @Transactional
    public ContractResult createContract(Long authorId) {
        User author = userRepository.getOne(authorId);

        DocumentNumber number = generateNumber();
        ContractHeader header = new ContractHeader(author.getId(), number);

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

        ContractHeader header = documentHeaderRepository.getOne(headerId, ContractHeader.class);

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
        ContractHeader header = documentHeaderRepository.getOne(headerId, ContractHeader.class);

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
