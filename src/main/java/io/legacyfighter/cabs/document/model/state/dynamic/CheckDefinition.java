package io.legacyfighter.cabs.document.model.state.dynamic;

public class CheckDefinition {

    private final String checkClassName;

    public CheckDefinition(String checkClassName) {
        this.checkClassName = checkClassName;
    }

    public String getCheckClassName() {
        return checkClassName;
    }
}
