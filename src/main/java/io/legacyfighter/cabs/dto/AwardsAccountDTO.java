package io.legacyfighter.cabs.dto;

import io.legacyfighter.cabs.entity.miles.AwardsAccount;

import java.time.Instant;

public class AwardsAccountDTO {

    private ClientDTO client;

    private Instant date;

    private Boolean isActive;

    private Integer transactions;

    public AwardsAccountDTO() {
    }

    public AwardsAccountDTO(AwardsAccount account) {
        this.client = new ClientDTO(account.getClient());
        this.date = account.getDate();
        this.isActive = account.isActive();
        this.transactions = account.getTransactions();
    }

    public ClientDTO getClient() {
        return client;
    }

    public void setClient(ClientDTO client) {
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

    public void setTransactions(int transactions) {
        this.transactions = transactions;
    }
}
