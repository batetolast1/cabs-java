package io.legacyfighter.cabs.document.model.state.dynamic;

import java.util.HashMap;
import java.util.Map;

public class ChangeCommand {

    private final String desiredState;

    private final Map<String, Object> params;

    public ChangeCommand(String desiredState, Map<String, Object> params) {
        this.desiredState = desiredState;
        this.params = params;
    }

    public ChangeCommand(String desiredState) {
        this(desiredState, new HashMap<>());
    }

    public ChangeCommand withParam(String name, Object value) {
        params.put(name, value);
        return this;
    }

    public String getDesiredState() {
        return desiredState;
    }

    @SuppressWarnings({"unchecked", "unused"})
    public <T> T getParam(String name, Class<T> type) {
        return (T) params.get(name);
    }
}
