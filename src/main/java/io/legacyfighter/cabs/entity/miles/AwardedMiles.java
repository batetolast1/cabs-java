package io.legacyfighter.cabs.entity.miles;

import io.legacyfighter.cabs.common.BaseEntity;
import io.legacyfighter.cabs.entity.Client;
import io.legacyfighter.cabs.entity.Transit;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import java.time.Instant;
import java.util.Objects;

@Entity
public class AwardedMiles extends BaseEntity {

    @ManyToOne
    private Client client;

    @ManyToOne
    private Transit transit;

    @Column(nullable = false)
    private Instant date = Instant.now();

    private String milesJson;

    public AwardedMiles() {
        // for JPA
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Transit getTransit() {
        return transit;
    }

    public void setTransit(Transit transit) {
        this.transit = transit;
    }

    public Miles getMiles() {
        return MilesJsonMapper.deserialize(milesJson);
    }

    public void setMiles(Miles miles) {
        this.milesJson = MilesJsonMapper.serialize(miles);
    }

    public Instant getDate() {
        return date;
    }

    public void setDate(Instant date) {
        this.date = date;
    }

    public Integer getMilesAmount(Instant when) {
        return this.getMiles().getAmountFor(when);
    }

    public Instant getExpirationDate() {
        return this.getMiles().expiresAt();
    }

    public Boolean cantExpire() {
        return Objects.equals(this.getMiles().expiresAt(), Instant.MAX);
    }

    public void subtract(Integer miles, Instant when) {
        setMiles(this.getMiles().subtract(miles, when));
    }

    public void removeAllMiles(Instant when) {
        setMiles(this.getMiles().subtract(this.getMilesAmount(when), when));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof AwardedMiles))
            return false;

        AwardedMiles other = (AwardedMiles) o;

        return this.getId() != null &&
                this.getId().equals(other.getId());
    }
}
