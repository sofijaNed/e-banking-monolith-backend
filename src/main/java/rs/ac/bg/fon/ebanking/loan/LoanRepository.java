package rs.ac.bg.fon.ebanking.loan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findLoansByAccountId(Long clientId);
    List<Loan> findLoansByStatus(LoanStatus status);
    List<Loan> findLoansByAccountClientId(Long clientId);
}
