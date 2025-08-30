package rs.ac.bg.fon.ebanking.loanpayment;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/loan-payments")
public class LoanPaymentController {

    @Autowired
    private LoanPaymentImpl loanPaymentService;

    @PreAuthorize("hasRole('EMPLOYEE') or @loanPaymentRepository.existsByIdAndLoanAccountClientUserClientUsername(#paymentId, authentication.name)")
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

    @PreAuthorize("hasRole('EMPLOYEE') or @loanRepository.existsByIdAndAccountClientUserClientUsername(#loanId, authentication.name)")
    @GetMapping("/loan/{loanId}")
    public ResponseEntity<List<LoanPaymentDTO>> getPaymentsByLoanId(@PathVariable Long loanId) {
        return ResponseEntity.ok(loanPaymentService.findByLoanId(loanId));
    }
}
