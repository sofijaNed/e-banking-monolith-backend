package rs.ac.bg.fon.ebanking.transaction;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private TransactionImpl transactionService;

    @Autowired
    public TransactionController(TransactionImpl transactionService) {
        this.transactionService = transactionService;
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping
    public List<TransactionDTO> findAll(){
        return transactionService.findAll();
    }

    @PreAuthorize("hasRole('EMPLOYEE') or @transactionRepository.clientCanSee(#id, authentication.name)")
    @GetMapping("/{id}")
    public ResponseEntity<TransactionDTO> findById(@PathVariable("id") Long id) throws Exception {
        return ResponseEntity.ok().body(transactionService.findById(id));
    }

    @PreAuthorize("hasRole('EMPLOYEE') or @accountRepository.existsByAccountNumberAndClientUserClientUsername(#transactionDTO.sender, authentication.name)")
    @PostMapping("/savePliz")
    public ResponseEntity<TransactionDTO> save( @RequestBody TransactionDTO transactionDTO) throws Exception {
        System.out.println("Ovde sam");
        return new ResponseEntity<>(transactionService.save(transactionDTO), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('EMPLOYEE') or @accountRepository.existsByAccountNumberAndClientUserClientUsername(#id, authentication.name)")
    @GetMapping("/sender/{id}")
    public ResponseEntity<List<TransactionDTO>> findBySenderId(@PathVariable("id") String id) throws Exception {
        return ResponseEntity.ok().body(transactionService.findBySenderAccountNumber(id));
    }

    @PreAuthorize("hasRole('EMPLOYEE') or @accountRepository.existsByAccountNumberAndClientUserClientUsername(#id, authentication.name)")
    @GetMapping("/receiver/{id}")
    public ResponseEntity<List<TransactionDTO>> findByReceiverId(@PathVariable("id") String id) throws Exception {
        return ResponseEntity.ok().body(transactionService.findByReceiverAccountNumber(id));
    }
}
