package rs.ac.bg.fon.ebanking.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.ac.bg.fon.ebanking.entity.Transaction;
import rs.ac.bg.fon.ebanking.entity.complexkeys.TransactionPK;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, TransactionPK> {

}
