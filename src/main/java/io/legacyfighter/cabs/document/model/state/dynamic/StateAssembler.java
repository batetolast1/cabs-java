package io.legacyfighter.cabs.document.model.state.dynamic;

import io.legacyfighter.cabs.document.model.DocumentHeader;

public interface StateAssembler<T extends DocumentHeader> {

    StateConfig<T> assemble();
}
