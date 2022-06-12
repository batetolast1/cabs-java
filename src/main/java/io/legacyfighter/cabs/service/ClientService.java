package io.legacyfighter.cabs.service;

import io.legacyfighter.cabs.dto.ClientDTO;
import io.legacyfighter.cabs.entity.Client;
import io.legacyfighter.cabs.repository.ClientRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ClientService {
    private final ClientRepository clientRepository;

    public ClientService(ClientRepository clientRepository) {
        this.clientRepository = clientRepository;
    }

    @Transactional
    public Client registerClient(String name, String lastName, Client.Type type, Client.PaymentType paymentType) {
        Client client = new Client();
        client.setName(name);
        client.setLastName(lastName);
        client.setType(type);
        client.setDefaultPaymentType(paymentType);
        return clientRepository.save(client);
    }

    @Transactional
    public void changeDefaultPaymentType(Long clientId, Client.PaymentType paymentType) {
        Client client = clientRepository.getOne(clientId);
        if (client == null) {
            throw new IllegalArgumentException("Client does not exists, id = " + clientId);
        }
        client.setDefaultPaymentType(paymentType);
        clientRepository.save(client);
    }

    @Transactional
    public void upgradeToVIP(Long clientId) {
        Client client = clientRepository.getOne(clientId);
        if (client == null) {
            throw new IllegalArgumentException("Client does not exists, id = " + clientId);
        }
        client.setType(Client.Type.VIP);
        clientRepository.save(client);
    }

    @Transactional
    public void downgradeToRegular(Long clientId) {
        Client client = clientRepository.getOne(clientId);
        if (client == null) {
            throw new IllegalArgumentException("Client does not exists, id = " + clientId);
        }
        client.setType(Client.Type.NORMAL);
        clientRepository.save(client);
    }

    @Transactional
    public ClientDTO load(Long id) {
        return new ClientDTO(clientRepository.getOne(id));
    }
}
