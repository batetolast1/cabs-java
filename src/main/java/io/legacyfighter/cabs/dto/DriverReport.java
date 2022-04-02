package io.legacyfighter.cabs.dto;

import java.util.*;

public class DriverReport {

    private DriverDTO driverDTO;

    private List<DriverAttributeDTO> attributes = new ArrayList<>();

    private Map<DriverSessionDTO, List<TransitDTO>> sessions = new HashMap<>();

    public DriverDTO getDriverDTO() {
        return driverDTO;
    }

    public void setDriverDTO(DriverDTO driverDTO) {
        this.driverDTO = driverDTO;
    }

    public List<DriverAttributeDTO> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<DriverAttributeDTO> attributes) {
        this.attributes = attributes;
    }

    public Map<DriverSessionDTO, List<TransitDTO>> getSessions() {
        return sessions;
    }

    public void setSessions(Map<DriverSessionDTO, List<TransitDTO>> sessions) {
        this.sessions = sessions;
    }

    public void addAttribute(DriverAttributeDTO attribute) {
        attributes.add(attribute);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DriverReport that = (DriverReport) o;
        return Objects.equals(driverDTO, that.driverDTO)
                && areEqual(attributes, that.attributes)
                && areEqual(sessions, that.sessions);
    }

    @Override
    public int hashCode() {
        return Objects.hash(driverDTO, attributes, sessions);
    }

    private boolean areEqual(List<DriverAttributeDTO> first, List<DriverAttributeDTO> second) {
        return first.containsAll(second) && second.containsAll(first);
    }

    private boolean areEqual(Map<DriverSessionDTO, List<TransitDTO>> first, Map<DriverSessionDTO, List<TransitDTO>> second) {
        return first.entrySet().stream()
                .allMatch(e -> Objects.equals(e.getValue(), second.get(e.getKey())))
                && second.entrySet().stream()
                .allMatch(e -> Objects.equals(e.getValue(), first.get(e.getKey())));
    }
}
