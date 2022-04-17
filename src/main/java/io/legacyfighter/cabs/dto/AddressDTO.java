package io.legacyfighter.cabs.dto;

import io.legacyfighter.cabs.entity.Address;

import java.util.Objects;

public class AddressDTO {

    private String country;

    private String district;

    private String city;

    private String street;

    private Integer buildingNumber;

    private Integer additionalNumber;

    private String postalCode;

    private String name;

    private Integer hash;

    public AddressDTO() {
    }

    public AddressDTO(String country,
                      String city,
                      String street,
                      Integer buildingNumber) {
        this.country = country;
        this.city = city;
        this.street = street;
        this.buildingNumber = buildingNumber;
    }

    public AddressDTO(String country,
                      String district,
                      String city,
                      String street,
                      Integer buildingNumber,
                      Integer additionalNumber,
                      String postalCode,
                      String name,
                      Integer hash) {
        this.country = country;
        this.district = district;
        this.city = city;
        this.street = street;
        this.buildingNumber = buildingNumber;
        this.additionalNumber = additionalNumber;
        this.postalCode = postalCode;
        this.name = name;
        this.hash = hash;
    }

    public AddressDTO(Address a) {
        this(a.getCountry(),
                a.getDistrict(),
                a.getCity(),
                a.getStreet(),
                a.getBuildingNumber(),
                a.getAdditionalNumber(),
                a.getPostalCode(),
                a.getName(),
                a.getHash());
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDistrict() {
        return district;
    }

    public void setDistrict(String district) {
        this.district = district;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getStreet() {
        return street;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public Integer getBuildingNumber() {
        return buildingNumber;
    }

    public void setBuildingNumber(Integer buildingNumber) {
        this.buildingNumber = buildingNumber;
    }

    public Integer getAdditionalNumber() {
        return additionalNumber;
    }

    public void setAdditionalNumber(Integer additionalNumber) {
        this.additionalNumber = additionalNumber;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public void setPostalCode(String postalCode) {
        this.postalCode = postalCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getHash() {
        return hash;
    }

    public void setHash(Integer hash) {
        this.hash = hash;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AddressDTO that = (AddressDTO) o;
        return Objects.equals(country, that.country) && Objects.equals(district, that.district) && Objects.equals(city, that.city) && Objects.equals(street, that.street) && Objects.equals(buildingNumber, that.buildingNumber) && Objects.equals(additionalNumber, that.additionalNumber) && Objects.equals(postalCode, that.postalCode) && Objects.equals(name, that.name) && Objects.equals(hash, that.hash);
    }

    @Override
    public int hashCode() {
        return Objects.hash(country, district, city, street, buildingNumber, additionalNumber, postalCode, name, hash);
    }

    public Address toAddressEntity() {
        Address address = new Address();
        address.setAdditionalNumber(this.getAdditionalNumber());
        address.setBuildingNumber(this.getBuildingNumber());
        address.setCity(this.getCity());
        address.setName(this.getName());
        address.setStreet(this.getStreet());
        address.setCountry(this.getCountry());
        address.setPostalCode(this.getPostalCode());
        address.setDistrict(this.getDistrict());
        return address;
    }
}
