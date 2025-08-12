package rs.ac.bg.fon.ebanking.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.bg.fon.ebanking.dto.EmployeeDTO;
import rs.ac.bg.fon.ebanking.dto.LoanDTO;
import rs.ac.bg.fon.ebanking.dto.LoanRequestDTO;
import rs.ac.bg.fon.ebanking.dto.LoanResponseDTO;
import rs.ac.bg.fon.ebanking.entity.LoanStatus;
import rs.ac.bg.fon.ebanking.service.implementation.LoanImpl;

import java.util.List;

@RestController
@RequestMapping("/loans")
public class LoanController {

    private LoanImpl loanService;

    @Autowired
    public LoanController(LoanImpl loanService) {
        this.loanService = loanService;
    }

    @PostMapping("/client/{clientId}/request")
    public ResponseEntity<LoanResponseDTO> submitLoanRequest(
            @PathVariable Long clientId,
            @RequestBody LoanRequestDTO requestDTO
    ) {
        LoanResponseDTO response = loanService.submitLoanRequest(clientId, requestDTO);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{loanId}/approve")
    public ResponseEntity<LoanResponseDTO> approveLoan(
            @PathVariable Long loanId,
            @RequestParam Long employeeId,
            @RequestParam(required = false) String note
    ) throws Exception {
        LoanResponseDTO response = loanService.approveLoan(loanId, employeeId, note);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<LoanDTO>> getAllLoans() {
        return ResponseEntity.ok(loanService.findAll());
    }

    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<LoanDTO>> getLoansByClientId(@PathVariable Long clientId) {
        return ResponseEntity.ok(loanService.findAllByClientId(clientId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<LoanDTO>> findAllByStatus(@PathVariable String status) {
        return ResponseEntity.ok(loanService.findAllByStatus(status));
    }
}
