package io.legacyfighter.cabs.entity.events;

import io.legacyfighter.cabs.common.Event;

import java.time.Instant;

public class TransitCompleted implements Event {

    private final Long clientId;
    private final Long transitId;
    private final Integer addressFromHash;
    private final Integer addressToHash;
    private final Instant started;
    private final Instant completeAt;
    private final Instant eventTimestamp;

    public TransitCompleted(Long clientId,
                            Long transitId,
                            Integer addressFromHash,
                            Integer addressToHash,
                            Instant started,
                            Instant completeAt,
                            Instant eventTimestamp) {
        this.clientId = clientId;
        this.transitId = transitId;
        this.addressFromHash = addressFromHash;
        this.addressToHash = addressToHash;
        this.started = started;
        this.completeAt = completeAt;
        this.eventTimestamp = eventTimestamp;
    }

    public Long getClientId() {
        return clientId;
    }

    public Long getTransitId() {
        return transitId;
    }

    public Integer getAddressFromHash() {
        return addressFromHash;
    }

    public Integer getAddressToHash() {
        return addressToHash;
    }

    public Instant getStarted() {
        return started;
    }

    public Instant getCompleteAt() {
        return completeAt;
    }

    public Instant getEventTimestamp() {
        return eventTimestamp;
    }
}
