package io.legacyfighter.cabs.document.model.state.dynamic;

import io.legacyfighter.cabs.document.model.DocumentHeader;

public interface StateConfig<T extends DocumentHeader> {

    State begin(T header);

    State recreate(T header);
}
