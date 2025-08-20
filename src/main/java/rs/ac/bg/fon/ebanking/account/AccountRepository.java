package rs.ac.bg.fon.ebanking.account;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

    List<Account> findAccountsByClientId(Long id);


    @Modifying
    @Query("update Account a set a.balance = a.balance - :amount, a.availableBalance = a.availableBalance - :amount where a.id = :id and a.balance >= :amount")
    int withdrawIfSufficient(@Param("id") Long id, @Param("amount") BigDecimal amount);

    @Modifying
    @Query("update Account a set a.balance = a.balance + :amount, a.availableBalance = a.availableBalance + :amount where a.id = :id")
    int deposit(@Param("id") Long id, @Param("amount") BigDecimal amount);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Account a where a.id = :id")
    Optional<Account> findByIdForUpdate(@Param("id") Long id);

    Optional<Account> findByAccountNumber(String accountNumber);

    Account findAccountByAccountNumber(String accountNumber);
}
