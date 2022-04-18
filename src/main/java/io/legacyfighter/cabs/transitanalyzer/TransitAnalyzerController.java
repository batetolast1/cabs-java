package io.legacyfighter.cabs.transitanalyzer;

import io.legacyfighter.cabs.dto.AddressDTO;
import io.legacyfighter.cabs.dto.AnalyzedAddressesDTO;
import io.legacyfighter.cabs.entity.Address;
import io.legacyfighter.cabs.repository.AddressRepository;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.stream.Collectors;

@RestController
class TransitAnalyzerController {

    private final GraphTransitAnalyzer transitAnalyzer;

    private final AddressRepository addressRepository;

    TransitAnalyzerController(GraphTransitAnalyzer transitAnalyzer,
                              AddressRepository addressRepository) {
        this.transitAnalyzer = transitAnalyzer;
        this.addressRepository = addressRepository;
    }

    @GetMapping("/transitAnalyze/{clientId}/{addressId}")
    AnalyzedAddressesDTO analyze(@PathVariable Long clientId,
                                 @PathVariable Long addressId) {
        Address address = addressRepository.findById(addressId).orElseThrow(EntityNotFoundException::new);

        List<Long> addressHashes = transitAnalyzer.analyze(clientId, address.getHash());

        List<AddressDTO> addressDTOs = addressHashes
                .stream()
                .map(Long::intValue)
                .map(addressRepository::findByHash)
                .map(AddressDTO::new)
                .collect(Collectors.toList());

        return new AnalyzedAddressesDTO(addressDTOs);
    }
}
