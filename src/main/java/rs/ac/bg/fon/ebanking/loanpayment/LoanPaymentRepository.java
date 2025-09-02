package rs.ac.bg.fon.ebanking.loanpayment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface LoanPaymentRepository extends JpaRepository<LoanPayment, LoanPaymentId> {
    List<LoanPayment> findByIdLoanIdOrderByIdInstallmentNoAsc(Long loanId);
    List<LoanPayment> findByPaidFalseAndDueDateBefore(LocalDate date);
    boolean existsByIdLoanIdAndIdInstallmentNoAndLoanAccountClientUserClientUsername(
            Long loanId, Integer installmentNo, String username
    );
    LoanPayment findFirstByIdLoanIdAndPaidFalseOrderByIdInstallmentNoAsc(Long loanId);
    boolean existsByIdLoanIdAndPaidFalse(Long loanId);
    @Query("select lp from LoanPayment lp where lp.loan.account.client.userClient.username = ?#{authentication.name}")
    List<LoanPayment> findMine();
//    @Query("""
//       select (count(lp) > 0)
//       from LoanPayment lp
//       where lp.id.loanId = :loanId
//         and lp.id.installmentNo = :installmentNo
//         and lower(lp.loan.account.client.userClient.username) = lower(:username)
//       """)
//    boolean userOwnsInstallment(@org.springframework.data.repository.query.Param("loanId") Long loanId,
//                                @org.springframework.data.repository.query.Param("installmentNo") Integer installmentNo,
//                                @org.springframework.data.repository.query.Param("username") String username);
//
//
//    @Query("""
//       select (count(l) > 0)
//       from Loan l
//       where l.id = :loanId
//         and lower(l.account.client.userClient.username) = lower(:username)
//       """)
//    boolean userOwnsLoan(@org.springframework.data.repository.query.Param("loanId") Long loanId,
//                         @org.springframework.data.repository.query.Param("username") String username);
}
