package io.legacyfighter.cabs.document.model.state.dynamic;

import io.legacyfighter.cabs.document.model.DocumentHeader;

import java.util.List;

public interface StateDefinitionRepository<T extends DocumentHeader> {

    List<StateDefinition> findAll(Class<T> type);
}
