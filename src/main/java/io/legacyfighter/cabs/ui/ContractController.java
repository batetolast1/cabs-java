package io.legacyfighter.cabs.ui;

import io.legacyfighter.cabs.dto.ContractAttachmentDTO;
import io.legacyfighter.cabs.dto.ContractDTO;
import io.legacyfighter.cabs.entity.contract.Contract;
import io.legacyfighter.cabs.service.ContractService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashSet;

@RestController
public class ContractController {

    private final ContractService contractService;

    public ContractController(ContractService contractService) {
        this.contractService = contractService;
    }

    @PostMapping("/contracts/")
    ResponseEntity<ContractDTO> create(@RequestBody ContractDTO contractDTO) {
        Contract created = contractService.createContract(contractDTO);
        return ResponseEntity.ok(new ContractDTO(created, new HashSet<>()));
    }

    @GetMapping("/contracts/{id}}")
    public ResponseEntity<ContractDTO> find(@PathVariable Long id) {
        ContractDTO contract = contractService.findDto(id);
        return ResponseEntity.ok(contract);
    }

    @PostMapping("/contracts/{id}/attachment")
    public ResponseEntity<ContractAttachmentDTO> proposeAttachment(@PathVariable Long id, @RequestBody ContractAttachmentDTO contractAttachmentDTO) {
        ContractAttachmentDTO dto = contractService.proposeAttachment(id, contractAttachmentDTO);
        return ResponseEntity.ok(dto);
    }

    @PostMapping("/contracts/{contractId}/attachment/{attachmentId}/reject")
    public ResponseEntity<Void> rejectAttachment(@PathVariable Long contractId, @PathVariable Long attachmentId) {
        contractService.rejectAttachment(attachmentId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/contracts/{contractId}/attachment/{attachmentId}/accept")
    public ResponseEntity<Void> acceptAttachment(@PathVariable Long contractId, @PathVariable Long attachmentId) {
        contractService.acceptAttachment(attachmentId);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/contracts/{contractId}/attachment/{attachmentId}")
    public ResponseEntity<Void> removeAttachment(@PathVariable Long contractId, @PathVariable Long attachmentId) {
        contractService.removeAttachment(contractId, attachmentId);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/contracts/{id}/accept")
    public ResponseEntity<Void> acceptContract(@PathVariable Long id) {
        contractService.acceptContract(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/contracts/{id}/reject")
    public ResponseEntity<Void> rejectContract(@PathVariable Long id) {
        contractService.rejectContract(id);
        return ResponseEntity.ok().build();
    }
}
