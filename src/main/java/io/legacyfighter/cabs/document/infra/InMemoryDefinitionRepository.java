package io.legacyfighter.cabs.document.infra;

import io.legacyfighter.cabs.contracts.model.ContractHeader;
import io.legacyfighter.cabs.contracts.model.state.dynamic.config.actions.ChangeVerifier;
import io.legacyfighter.cabs.contracts.model.state.dynamic.config.predicates.statechange.AuthorIsNotAVerifier;
import io.legacyfighter.cabs.document.model.DocumentHeader;
import io.legacyfighter.cabs.document.model.state.dynamic.ActionDefinition;
import io.legacyfighter.cabs.document.model.state.dynamic.CheckDefinition;
import io.legacyfighter.cabs.document.model.state.dynamic.StateDefinition;
import io.legacyfighter.cabs.document.model.state.dynamic.StateDefinitionRepository;
import io.legacyfighter.cabs.document.model.state.dynamic.config.actions.PublishEvent;
import io.legacyfighter.cabs.document.model.state.dynamic.config.events.DocumentPublished;
import io.legacyfighter.cabs.document.model.state.dynamic.config.events.DocumentUnpublished;
import io.legacyfighter.cabs.document.model.state.dynamic.config.predicates.statechange.ContentNotEmptyVerifier;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static io.legacyfighter.cabs.contracts.model.state.dynamic.ContractStateAssembler.*;

@Repository
public class InMemoryDefinitionRepository<T extends DocumentHeader> implements StateDefinitionRepository<T> {

    @Override
    public List<StateDefinition> findAll(Class<T> clazz) {
        List<StateDefinition> states = new ArrayList<>();

        states.add(
                new StateDefinition(
                        DRAFT,
                        List.of(
                                new CheckDefinition(ContentNotEmptyVerifier.class.getName()),
                                new CheckDefinition(AuthorIsNotAVerifier.class.getName())
                        ),
                        VERIFIED,

                        new ActionDefinition(
                                ChangeVerifier.class.getName(),
                                List.of()
                        ),
                        true,
                        false,
                        ContractHeader.class.getName()
                ));

        states.add(
                new StateDefinition(
                        DRAFT,
                        List.of(),
                        DRAFT,
                        null,
                        false,
                        true,
                        ContractHeader.class.getName()
                ));

        states.add(
                new StateDefinition(
                        VERIFIED,
                        List.of(new CheckDefinition(ContentNotEmptyVerifier.class.getName())),
                        PUBLISHED,
                        new ActionDefinition(
                                PublishEvent.class.getName(),
                                List.of(
                                        DocumentPublished.class.getName(),
                                        ApplicationEventPublisher.class.getName()
                                )
                        ),
                        false,
                        false,
                        ContractHeader.class.getName()
                ));

        states.add(
                new StateDefinition(
                        VERIFIED,
                        List.of(),
                        DRAFT,
                        null,
                        false,
                        true,
                        ContractHeader.class.getName()
                ));

        states.add(
                new StateDefinition(
                        DRAFT,
                        List.of(),
                        ARCHIVED,
                        null,
                        false,
                        false,
                        ContractHeader.class.getName()
                ));

        states.add(
                new StateDefinition(
                        VERIFIED,
                        List.of(),
                        ARCHIVED,
                        null,
                        false,
                        false,
                        ContractHeader.class.getName()
                ));

        states.add(
                new StateDefinition(
                        PUBLISHED,
                        List.of(),
                        ARCHIVED,
                        new ActionDefinition(
                                PublishEvent.class.getName(),
                                List.of(
                                        DocumentUnpublished.class.getName(),
                                        ApplicationEventPublisher.class.getName()
                                )
                        ),
                        false,
                        false,
                        ContractHeader.class.getName()
                ));

        return states.stream()
                .filter(s -> s.getDocumentHeaderClassName().equals(clazz.getName()))
                .collect(Collectors.toList());
    }
}
