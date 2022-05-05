package io.legacyfighter.cabs.document.model.state.dynamic;

import io.legacyfighter.cabs.document.model.ContentId;
import io.legacyfighter.cabs.document.model.DocumentHeader;
import io.legacyfighter.cabs.document.model.state.dynamic.config.predicates.contentchange.NegativePredicate;
import io.legacyfighter.cabs.document.model.state.dynamic.config.predicates.statechange.PositiveVerifier;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class State {

    //before: getClass().getName()
    /**
     * Unique name of a state
     */
    private final String stateDescriptor;

    // TODO consider to get rid of this stateful object and transform State to reusable logic
    private DocumentHeader documentHeader;

    // TODO consider merging contentChangePredicate and afterContentChangeState int one function
    // before: abstract canChangeContent()
    /**
     * predicates tested if content can be changed
     */
    private Predicate<State> contentChangePredicate = new NegativePredicate();  //default

    // before: abstract stateAfterContentChange()
    /**
     * state after content change - may be the same as before content change
     */
    private State afterContentChangeState;

    // before: abstract canChangeFrom(state)
    /**
     * possible transitions to other states with rules that need to be tested to determine if transition is legal
     */
    private final Map<State, List<BiPredicate<State, ChangeCommand>>> stateChangePredicates = new HashMap<>();

    // before: abstract acquire()
    /**
     * actions that may be needed to perform while transition to the next state
     */
    private final List<BiConsumer<DocumentHeader, ChangeCommand>> afterStateChangeActions = new ArrayList<>();

    public State(String stateDescriptor) {
        this.stateDescriptor = stateDescriptor;
        List<BiPredicate<State, ChangeCommand>> predicates = new ArrayList<>();
        predicates.add(new PositiveVerifier());
        addStateChangePredicates(this, predicates); // change to self is by default always possible
    }

    /**
     * initial bounding with a document header
     */
    public void init(DocumentHeader header) {
        this.documentHeader = header;
        header.setStateDescriptor(getStateDescriptor());
    }

    public State changeContent(ContentId currentContent) {
        if (!isContentEditable())
            return this;

        State newState = afterContentChangeState; // local variable just to focus attention

        if (newState.contentChangePredicate.test(this)) {
            newState.init(documentHeader);
            this.documentHeader.changeCurrentContent(currentContent);
            return newState;
        }

        return this;
    }


    public State changeState(ChangeCommand command) {
        State desiredState = find(command.getDesiredState());
        if (desiredState == null) {
            return this;
        }

        List<BiPredicate<State, ChangeCommand>> predicates = stateChangePredicates.getOrDefault(desiredState, Collections.emptyList());

        if (predicates.stream().allMatch(predicate -> predicate.test(this, command))) {
            desiredState.init(documentHeader);
            desiredState.afterStateChangeActions.forEach(action -> action.accept(documentHeader, command));
            return desiredState;
        }

        return this;
    }

    public String getStateDescriptor() {
        return stateDescriptor;
    }

    public DocumentHeader getDocumentHeader() {
        return documentHeader;
    }

    public Map<State, List<BiPredicate<State, ChangeCommand>>> getStateChangePredicates() {
        return stateChangePredicates;
    }

    public Predicate<State> getContentChangePredicate() {
        return contentChangePredicate;
    }

    public boolean isContentEditable() {
        return afterContentChangeState != null;
    }

    @Override
    public String toString() {
        return "State{" +
                "stateDescriptor='" + stateDescriptor + '\'' +
                '}';
    }

    void addStateChangePredicates(State toState, List<BiPredicate<State, ChangeCommand>> predicatesToAdd) {
        if (stateChangePredicates.containsKey(toState)) {
            List<BiPredicate<State, ChangeCommand>> predicates = stateChangePredicates.get(toState);
            predicates.addAll(predicatesToAdd);
        } else {
            stateChangePredicates.put(toState, predicatesToAdd);
        }
    }

    void addAfterStateChangeAction(BiConsumer<DocumentHeader, ChangeCommand> action) {
        afterStateChangeActions.add(action);
    }

    void setAfterContentChangeState(State toState) {
        afterContentChangeState = toState;
    }

    void setContentChangePredicate(Predicate<State> predicate) {
        contentChangePredicate = predicate;
    }

    private State find(String desiredState) {
        return stateChangePredicates.keySet().stream()
                .filter(e -> e.getStateDescriptor().equals(desiredState))
                .findFirst()
                .orElse(null);
    }

    public String extractContentChangePredicate() {
        if (isContentEditable()) {
            return contentChangePredicate.getClass().getTypeName();
        }
        return null;
    }


    public Map<String, List<String>> extractPossibleTransitionsAndRules() {
        Map<String, List<String>> transitionsAndRules = new HashMap<>();

        for (Map.Entry<State, List<BiPredicate<State, ChangeCommand>>> entry : stateChangePredicates.entrySet()) {
            State possibleState = entry.getKey();
            if (possibleState.equals(this)) { //transition to self is not important
                continue;
            }

            List<BiPredicate<State, ChangeCommand>> predicates = entry.getValue();

            List<String> ruleNames = predicates.stream()
                    .map(predicate -> predicate.getClass().getTypeName())
                    .collect(Collectors.toList());

            transitionsAndRules.put(possibleState.getStateDescriptor(), ruleNames);
        }

        return transitionsAndRules;
    }
}
