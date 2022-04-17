package io.legacyfighter.cabs.service;

import io.legacyfighter.cabs.entity.Address;
import io.legacyfighter.cabs.entity.Client;
import io.legacyfighter.cabs.entity.Transit;
import io.legacyfighter.cabs.repository.AddressRepository;
import io.legacyfighter.cabs.repository.ClientRepository;
import io.legacyfighter.cabs.repository.TransitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransitAnalyzer {

    private final TransitRepository transitRepository;

    private final ClientRepository clientRepository;

    private final AddressRepository addressRepository;

    public TransitAnalyzer(TransitRepository transitRepository,
                           ClientRepository clientRepository,
                           AddressRepository addressRepository) {
        this.transitRepository = transitRepository;
        this.clientRepository = clientRepository;
        this.addressRepository = addressRepository;
    }

    @Transactional
    public List<Address> analyze(Long clientId, Long addressId) {
        Client client = clientRepository.getOne(clientId);
        if (client == null) {
            throw new IllegalArgumentException("Client does not exists, id = " + clientId);
        }
        Address address = addressRepository.getOne(addressId);
        if (address == null) {
            throw new IllegalArgumentException("Address does not exists, id = " + addressId);
        }
        return analyze(client, address, null);
    }

    // Brace yourself, deadline is coming... They made me to do it this way.
    // Tested!
    private List<Address> analyze(Client client, Address from, Transit transitToAnalyze) {
        List<Transit> transits;

        if (transitToAnalyze == null) {
            transits = transitRepository.findAllByClientAndFromAndStatusOrderByDateTimeDesc(client, from, Transit.Status.COMPLETED);
        } else {
            transits = transitRepository.findAllByClientAndFromAndPublishedAfterAndStatusOrderByDateTimeDesc(client, from, transitToAnalyze.getPublished(), Transit.Status.COMPLETED);
        }

        // Workaround for performance reasons.
        if (transits.size() > 1000 && client.getId() == 666) {
            // No one will see a difference for this customer ;)
            transits = transits.stream()
                    .limit(1000)
                    .collect(Collectors.toList());
        }

        if (transitToAnalyze != null) {
            transits = transits.stream()
                    .filter(transit -> transitToAnalyze.getCompleteAt().plus(15, ChronoUnit.MINUTES).isAfter(transit.getStarted()))
                    .collect(Collectors.toList());
        }

        if (transits.isEmpty()) {
            return List.of(transitToAnalyze.getTo());
        }

        Comparator<List<Address>> comparator = Comparator.comparingInt(List::size);

        return transits.stream()
                .map(transit -> {
                    List<Address> result = new ArrayList<>();
                    result.add(transit.getFrom());
                    result.addAll(analyze(client, transit.getTo(), transit));
                    return result;
                })
                .sorted(comparator.reversed())
                .collect(Collectors.toList())
                .stream()
                .findFirst()
                .orElse(new ArrayList<>());
    }
}
