package io.legacyfighter.cabs.document.model.state.dynamic;

import java.util.List;

public class StateDefinition {

    private final String stateFrom;

    private final List<CheckDefinition> checks;

    private final String stateTo;

    private final ActionDefinition action;

    private final boolean isInitialState;

    private final boolean whenContentChanged;

    private final String documentHeaderClassName;

    public StateDefinition(String stateFrom,
                           List<CheckDefinition> checks,
                           String stateTo,
                           ActionDefinition action,
                           boolean isInitialState,
                           boolean whenContentChanged,
                           String documentHeaderClassName) {
        this.stateFrom = stateFrom;
        this.checks = checks;
        this.stateTo = stateTo;
        this.action = action;
        this.isInitialState = isInitialState;
        this.whenContentChanged = whenContentChanged;
        this.documentHeaderClassName = documentHeaderClassName;
    }

    public String getStateFrom() {
        return stateFrom;
    }

    public List<CheckDefinition> getChecks() {
        return checks;
    }

    public String getStateTo() {
        return stateTo;
    }

    public ActionDefinition getAction() {
        return action;
    }

    public boolean isInitialState() {
        return isInitialState;
    }

    public boolean isWhenContentChanged() {
        return whenContentChanged;
    }

    public String getDocumentHeaderClassName() {
        return documentHeaderClassName;
    }
}
