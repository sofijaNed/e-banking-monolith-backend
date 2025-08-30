package rs.ac.bg.fon.ebanking.loan;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long> {
    List<Loan> findLoansByAccountId(Long clientId);
    List<Loan> findLoansByStatus(LoanStatus status);
    List<Loan> findLoansByAccountClientId(Long clientId);
    boolean existsByIdAndAccountClientUserClientUsername(Long id, String username);

    @Query("select l from Loan l where l.account.client.userClient.username = ?#{authentication.name}")
    List<Loan> findMine();
    @Query("select l from Loan l where l.account.client.userClient.username = ?#{authentication.name} and l.status = :status")
    List<Loan> findMineByStatus(@Param("status") String status);
    List<Loan> findByAccountClientUserClientUsernameAndStatus(String username, LoanStatus status);

}
