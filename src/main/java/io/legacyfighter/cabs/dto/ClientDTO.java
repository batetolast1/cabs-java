package io.legacyfighter.cabs.dto;

import io.legacyfighter.cabs.entity.Client;

import java.util.Objects;

public class ClientDTO {

    private Long id;

    private Client.Type type;

    private String name;

    private String lastName;

    private Client.PaymentType defaultPaymentType;

    private Client.ClientType clientType;

    public ClientDTO() {
    }

    public ClientDTO(Long id,
                     Client.Type type,
                     String name,
                     String lastName,
                     Client.PaymentType defaultPaymentType,
                     Client.ClientType clientType) {
        this.id = id;
        this.type = type;
        this.name = name;
        this.lastName = lastName;
        this.defaultPaymentType = defaultPaymentType;
        this.clientType = clientType;
    }

    public ClientDTO(Client client) {
        this(client.getId(),
                client.getType(),
                client.getName(),
                client.getLastName(),
                client.getDefaultPaymentType(),
                client.getClientType());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Client.ClientType getClientType() {
        return clientType;
    }

    public void setClientType(Client.ClientType clientType) {
        this.clientType = clientType;
    }

    public Client.Type getType() {
        return type;
    }

    public void setType(Client.Type type) {
        this.type = type;
    }

    public Client.PaymentType getDefaultPaymentType() {
        return defaultPaymentType;
    }

    public void setDefaultPaymentType(Client.PaymentType defaultPaymentType) {
        this.defaultPaymentType = defaultPaymentType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ClientDTO clientDTO = (ClientDTO) o;
        return Objects.equals(id, clientDTO.id) && type == clientDTO.type && Objects.equals(name, clientDTO.name) && Objects.equals(lastName, clientDTO.lastName) && defaultPaymentType == clientDTO.defaultPaymentType && clientType == clientDTO.clientType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, name, lastName, defaultPaymentType, clientType);
    }
}
