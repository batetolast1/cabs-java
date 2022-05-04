package io.legacyfighter.cabs.contracts.model.state.dynamic.config.actions;

import io.legacyfighter.cabs.contracts.model.ContentId;
import io.legacyfighter.cabs.contracts.model.DocumentHeader;
import io.legacyfighter.cabs.contracts.model.content.DocumentNumber;
import io.legacyfighter.cabs.contracts.model.state.dynamic.ChangeCommand;
import io.legacyfighter.cabs.contracts.model.state.dynamic.config.events.DocumentEvent;
import org.springframework.context.ApplicationEventPublisher;

import java.lang.reflect.Constructor;
import java.util.function.BiConsumer;

public class PublishEvent implements BiConsumer<DocumentHeader, ChangeCommand> {

    private final Class<? extends DocumentEvent> eventClass;

    private final ApplicationEventPublisher publisher;

    public PublishEvent(Class<? extends DocumentEvent> eventClass, ApplicationEventPublisher publisher) {
        this.eventClass = eventClass;
        this.publisher = publisher;
    }

    @Override
    public void accept(DocumentHeader documentHeader, ChangeCommand command) {
        try {
            Constructor<? extends DocumentEvent> constructor = eventClass.getDeclaredConstructor(
                    Long.class,
                    String.class,
                    ContentId.class,
                    DocumentNumber.class
            );

            DocumentEvent event = constructor.newInstance(
                    documentHeader.getId(),
                    documentHeader.getStateDescriptor(),
                    documentHeader.getContentId(),
                    documentHeader.getDocumentNumber()
            );

            publisher.publishEvent(event);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
