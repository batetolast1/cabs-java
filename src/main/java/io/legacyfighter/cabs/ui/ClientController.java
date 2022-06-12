package io.legacyfighter.cabs.ui;

import io.legacyfighter.cabs.dto.ClientDTO;
import io.legacyfighter.cabs.entity.Client;
import io.legacyfighter.cabs.service.ClientService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class ClientController {

    private final ClientService clientService;

    public ClientController(ClientService clientService) {
        this.clientService = clientService;
    }

    @PostMapping("/clients")
    public ResponseEntity<ClientDTO> register(@RequestBody ClientDTO dto) {
        Client c = clientService.registerClient(dto.getName(), dto.getLastName(), dto.getType(), dto.getDefaultPaymentType());
        return ResponseEntity.ok(clientService.load(c.getId()));
    }

    @GetMapping("/clients/{clientId}")
    public ResponseEntity<ClientDTO> find(@PathVariable Long clientId) {
        return ResponseEntity.ok(clientService.load(clientId));
    }

    @PostMapping("/clients/{clientId}/upgrade")
    public ResponseEntity<ClientDTO> upgradeToVIP(@PathVariable Long clientId) {
        clientService.upgradeToVIP(clientId);
        return ResponseEntity.ok(clientService.load(clientId));
    }

    @PostMapping("/clients/{clientId}/downgrade")
    public ResponseEntity<ClientDTO> downgrade(@PathVariable Long clientId) {
        clientService.downgradeToRegular(clientId);
        return ResponseEntity.ok(clientService.load(clientId));
    }

    @PostMapping("/clients/{clientId}/changeDefaultPaymentType")
    public ResponseEntity<ClientDTO> changeDefaultPaymentType(@PathVariable Long clientId, @RequestBody ClientDTO dto) {
        clientService.changeDefaultPaymentType(clientId, dto.getDefaultPaymentType());
        return ResponseEntity.ok(clientService.load(clientId));
    }

}

