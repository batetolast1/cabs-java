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
    private Instant date;

    private String milesJson;

    @ManyToOne
    private AwardsAccount account;

    public AwardedMiles() {
        // for JPA
    }

    AwardedMiles(Transit transit,
                 Instant at,
                 Miles miles,
                 AwardsAccount account) {
        this.client = account.getClient();
        this.transit = transit;
        this.date = at;
        this.setMiles(miles);
        this.account = account;
    }

    Client getClient() {
        return this.client;
    }

    Transit getTransit() {
        return this.transit;
    }

    Miles getMiles() {
        return MilesJsonMapper.deserialize(this.milesJson);
    }

    Instant getDate() {
        return this.date;
    }

    AwardsAccount getAccount() {
        return this.account;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AwardedMiles that = (AwardedMiles) o;
        return Objects.equals(client, that.client) && Objects.equals(transit, that.transit) && Objects.equals(date, that.date) && Objects.equals(milesJson, that.milesJson) && Objects.equals(account, that.account);
    }

    Integer getMilesAmount(Instant at) {
        return this.getMiles().getAmount(at);
    }

    Instant getExpirationDate() {
        return this.getMiles().expiresAt();
    }

    boolean cantExpire() {
        Miles miles = this.getMiles();

        return miles.expiresAt() != null && Objects.equals(miles.expiresAt(), Instant.MAX);
    }

    boolean expired(Instant at) {
        Miles miles = this.getMiles();

        return miles.expiresAt() != null && (miles.expiresAt().isAfter(at) || Objects.equals(miles.expiresAt(), Instant.MAX));
    }

    void subtract(Integer miles, Instant at) {
        setMiles(this.getMiles().subtract(miles, at));
    }

    void removeAllMiles(Instant at) {
        setMiles(this.getMiles().subtract(this.getMilesAmount(at), at));
    }

    private void setMiles(Miles miles) {
        this.milesJson = MilesJsonMapper.serialize(miles);
    }

    void transferToAccount(AwardsAccount awardsAccount) {
        this.client = awardsAccount.getClient();
        this.account = awardsAccount;
    }
}
