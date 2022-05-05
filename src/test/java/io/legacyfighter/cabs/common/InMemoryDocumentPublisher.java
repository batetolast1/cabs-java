package io.legacyfighter.cabs.common;

import io.legacyfighter.cabs.document.model.state.dynamic.config.events.DocumentEvent;
import org.springframework.context.ApplicationEventPublisher;

import java.util.HashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class InMemoryDocumentPublisher implements ApplicationEventPublisher {

    private final Set<Object> events = new HashSet<>();

    @Override
    public void publishEvent(Object event) {
        events.add(event);
    }

    public void reset() {
        events.clear();
    }

    public void contains(Class<? extends DocumentEvent> event) {
        boolean containsEvent = events.stream().anyMatch(e -> e.getClass().equals(event));

        assertThat(containsEvent).isTrue();
    }

    public void containsNoEvents() {
        assertThat(events).isEmpty();
    }
}
