package rs.ac.bg.fon.ebanking.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.bg.fon.ebanking.dto.LoanPaymentDTO;
import rs.ac.bg.fon.ebanking.service.implementation.LoanPaymentImpl;

import java.util.List;

@RestController
@RequestMapping("/loan-payments")
public class LoanPaymentController {

    @Autowired
    private LoanPaymentImpl loanPaymentService;

    @PostMapping("/{paymentId}/pay")
    public ResponseEntity<String> payInstallment(
            @PathVariable Long paymentId,
            @RequestParam Long clientAccountId) {
        try {
            loanPaymentService.payInstallment(paymentId, clientAccountId);
            return ResponseEntity.ok("Installment paid successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/loan/{loanId}")
    public ResponseEntity<List<LoanPaymentDTO>> getPaymentsByLoanId(@PathVariable Long loanId) {
        return ResponseEntity.ok(loanPaymentService.findByLoanId(loanId));
    }
}
