package io.legacyfighter.cabs.entity.miles;

import io.legacyfighter.cabs.common.BaseEntity;
import io.legacyfighter.cabs.entity.Client;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToOne;
import java.time.Instant;

@Entity
public class AwardsAccount extends BaseEntity {

    @OneToOne
    private Client client;

    @Column(nullable = false)
    private Instant date = Instant.now();

    @Column(nullable = false)
    private Boolean isActive = false;

    @Column(nullable = false)
    private Integer transactions = 0;

    public AwardsAccount() {
        // for JPA
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

    public Instant getDate() {
        return date;
    }

    public void setDate(Instant date) {
        this.date = date;
    }

    public Boolean isActive() {
        return isActive;
    }

    public void setActive(Boolean active) {
        isActive = active;
    }

    public Integer getTransactions() {
        return transactions;
    }

    public void increaseTransactions() {
        transactions++;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (!(o instanceof AwardsAccount))
            return false;

        AwardsAccount other = (AwardsAccount) o;

        return this.getId() != null &&
                this.getId().equals(other.getId());
    }
}
