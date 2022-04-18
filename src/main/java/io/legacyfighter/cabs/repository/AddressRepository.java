package io.legacyfighter.cabs.repository;

import io.legacyfighter.cabs.entity.Address;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class AddressRepository {

    private final AddressRepositoryInterface addressRepositoryInterface;

    public AddressRepository(AddressRepositoryInterface addressRepositoryInterface) {
        this.addressRepositoryInterface = addressRepositoryInterface;
    }

    // FIX ME: To replace with getOrCreate method instead of that?
    // Actual workaround for address uniqueness problem: assign result from repo.save to variable for later usage
    public Address save(Address address) {
        address.hash();

        if (address.getId() == null) {
            Address existingAddress = findByHash(address.getHash());

            if (existingAddress != null) {
                return existingAddress;
            }
        }

        return addressRepositoryInterface.save(address);
    }

    public Address getOne(Long id) {
        return addressRepositoryInterface.getOne(id);
    }

    public Optional<Address> findById(Long addressId) {
        return addressRepositoryInterface.findById(addressId);
    }

    public Address findByHash(Integer hash) {
        return addressRepositoryInterface.findByHash(hash);
    }
}
