package io.legacyfighter.cabs.ui;

import io.legacyfighter.cabs.dto.DriverSessionDTO;
import io.legacyfighter.cabs.service.DriverSessionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
public class DriverSessionController {

    private final DriverSessionService driverSessionService;

    public DriverSessionController(DriverSessionService driverSessionService) {
        this.driverSessionService = driverSessionService;
    }

    @PostMapping("/drivers/{driverId}/driverSessions/login")
    public ResponseEntity<Void> logIn(@PathVariable Long driverId, @RequestBody DriverSessionDTO dto) {
        driverSessionService.logIn(driverId, dto.getPlatesNumber(), dto.getCarClass(), dto.getCarBrand());
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/drivers/{driverId}/driverSessions/{sessionId}")
    public ResponseEntity<Void> logOut(@PathVariable Long driverId, @PathVariable Long sessionId) {
        driverSessionService.logOut(sessionId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/drivers/{driverId}/driverSessions/")
    public ResponseEntity<Void> logOutCurrent(@PathVariable Long driverId) {
        driverSessionService.logOutCurrentSession(driverId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/drivers/{driverId}/driverSessions/")
    public ResponseEntity<List<DriverSessionDTO>> list(@PathVariable Long driverId) {
        return ResponseEntity.ok(driverSessionService.findByDriver(driverId)
                .stream()
                .map(DriverSessionDTO::new)
                .collect(Collectors.toList()));
    }
}

