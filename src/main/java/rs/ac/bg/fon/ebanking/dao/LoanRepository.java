package rs.ac.bg.fon.ebanking.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.ac.bg.fon.ebanking.entity.Loan;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Integer> {
}
