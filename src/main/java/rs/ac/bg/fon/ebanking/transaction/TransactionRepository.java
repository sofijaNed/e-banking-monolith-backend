package rs.ac.bg.fon.ebanking.transaction;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @Query("""
    select (count(t)>0) from Transaction t
    where t.id = :txId and (
      t.sender.client.userClient.username = :u
      or t.receiver.client.userClient.username = :u
    )
    """)
    boolean clientCanSee(@Param("txId") Long txId, @Param("u") String username);

    @Query("""
        select t from Transaction t
        where t.sender.client.userClient.username = ?#{authentication.name}
           or t.receiver.client.userClient.username = ?#{authentication.name}
       """)
    List<Transaction> findVisibleToMe();

}
