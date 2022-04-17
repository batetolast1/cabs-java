package io.legacyfighter.cabs.dto;

import java.util.List;

public class AnalyzedAddressesDTO {

    private final List<AddressDTO> addresses;

    public AnalyzedAddressesDTO(List<AddressDTO> addresses) {
        this.addresses = addresses;
    }

    public List<AddressDTO> getAddresses() {
        return addresses;
    }
}
