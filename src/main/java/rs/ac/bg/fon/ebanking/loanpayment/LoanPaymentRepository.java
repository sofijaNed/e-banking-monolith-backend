package rs.ac.bg.fon.ebanking.loanpayment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LoanPaymentRepository extends JpaRepository<LoanPayment, Long> {
    List<LoanPayment> findByLoanId(Long loanId);
    List<LoanPayment> findByPaidFalseAndDueDateBefore(LocalDate date);
    boolean existsByIdAndLoanAccountClientUserClientUsername(Long id, String username);

    @Query("select lp from LoanPayment lp where lp.loan.account.client.userClient.username = ?#{authentication.name}")
    List<LoanPayment> findMine();
}
