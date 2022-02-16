package io.legacyfighter.cabs.entity;

public class InvalidDriverLicense implements DriverLicense {

    private final String license;

    public InvalidDriverLicense(String license) {
        this.license = license;
    }

    @Override
    public String getLicense() {
        return license;
    }
}
