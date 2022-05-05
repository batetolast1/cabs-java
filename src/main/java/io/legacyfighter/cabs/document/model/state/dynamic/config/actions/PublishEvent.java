package io.legacyfighter.cabs.document.model.state.dynamic.config.actions;

import io.legacyfighter.cabs.document.model.ContentId;
import io.legacyfighter.cabs.document.model.DocumentHeader;
import io.legacyfighter.cabs.document.model.content.DocumentNumber;
import io.legacyfighter.cabs.document.model.state.dynamic.ChangeCommand;
import io.legacyfighter.cabs.document.model.state.dynamic.config.events.DocumentEvent;
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
    public void accept(DocumentHeader contractHeader, ChangeCommand command) {
        try {
            Constructor<? extends DocumentEvent> constructor = eventClass.getDeclaredConstructor(
                    Long.class,
                    String.class,
                    ContentId.class,
                    DocumentNumber.class
            );

            DocumentEvent event = constructor.newInstance(
                    contractHeader.getId(),
                    contractHeader.getStateDescriptor(),
                    contractHeader.getContentId(),
                    contractHeader.getDocumentNumber()
            );

            publisher.publishEvent(event);
        } catch (Exception e) {
            throw new IllegalArgumentException(e);
        }
    }
}
