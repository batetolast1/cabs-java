package io.legacyfighter.cabs.document.model.state.dynamic;

import java.util.List;

public class ActionDefinition {

    private final String actionClassName;

    private final List<String> actionParameterClassNames;

    public ActionDefinition(String actionClassName, List<String> actionParameterClassNames) {
        this.actionClassName = actionClassName;
        this.actionParameterClassNames = actionParameterClassNames;
    }

    public String getActionClassName() {
        return actionClassName;
    }

    public List<String> getActionParameterClassNames() {
        return actionParameterClassNames;
    }
}
