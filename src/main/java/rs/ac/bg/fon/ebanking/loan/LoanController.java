package rs.ac.bg.fon.ebanking.loan;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/loans")
public class LoanController {

    private LoanImpl loanService;

    @Autowired
    public LoanController(LoanImpl loanService) {
        this.loanService = loanService;
    }

    @PreAuthorize("hasRole('EMPLOYEE') or @clientRepository.existsByIdAndUserClientUsername(#clientId, authentication.name)")
    @PostMapping("/client/{clientId}/request")
    public ResponseEntity<LoanResponseDTO> submitLoanRequest(
            @PathVariable Long clientId,
            @RequestBody LoanRequestDTO requestDTO
    ) {
        LoanResponseDTO response = loanService.submitLoanRequest(clientId, requestDTO);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PutMapping("/{loanId}/approve")
    public ResponseEntity<LoanResponseDTO> approveLoan(
            @PathVariable Long loanId,
            @RequestParam Long employeeId,
            @RequestParam(required = false) String note
    ) throws Exception {
        LoanResponseDTO response = loanService.approveLoan(loanId, employeeId, note);
        return ResponseEntity.ok(response);
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping
    public ResponseEntity<List<LoanDTO>> getAllLoans() {
        return ResponseEntity.ok(loanService.findAll());
    }

    @PreAuthorize("hasRole('EMPLOYEE') or @clientRepository.existsByIdAndUserClientUsername(#clientId, authentication.name)")
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<LoanDTO>> getLoansByClientId(@PathVariable Long clientId) {
        return ResponseEntity.ok(loanService.findAllByClientId(clientId));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<LoanDTO>> findAllByStatus(@PathVariable String status) {
        return ResponseEntity.ok(loanService.findAllByStatus(status));
    }

    @PreAuthorize("hasRole('CLIENT')")
    @GetMapping("/me/status/{status}")
    public ResponseEntity<List<LoanDTO>> myLoansByStatus(@PathVariable String status) {
        return ResponseEntity.ok(loanService.findMineByStatus(status));
    }
}
