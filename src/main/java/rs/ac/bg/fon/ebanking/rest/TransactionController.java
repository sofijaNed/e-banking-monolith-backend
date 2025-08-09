package rs.ac.bg.fon.ebanking.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.bg.fon.ebanking.dto.EmployeeDTO;
import rs.ac.bg.fon.ebanking.dto.TransactionDTO;
import rs.ac.bg.fon.ebanking.entity.complexkeys.TransactionPK;
import rs.ac.bg.fon.ebanking.service.implementation.TransactionImpl;

import java.util.List;

@RestController
@RequestMapping("/transactions")
public class TransactionController {

    private TransactionImpl transactionService;

    @Autowired
    public TransactionController(TransactionImpl transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    public List<TransactionDTO> findAll(){
        return transactionService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionDTO> findById(@PathVariable("id") TransactionPK id) throws Exception {
        return ResponseEntity.ok().body(transactionService.findById(id));
    }

    @PostMapping("/savePliz")
    public ResponseEntity<TransactionDTO> save( @RequestBody TransactionDTO transactionDTO) throws Exception {
        System.out.println("Ovde sam");
        return new ResponseEntity<>(transactionService.save(transactionDTO), HttpStatus.CREATED);
    }

    @GetMapping("/sender/{id}")
    public ResponseEntity<List<TransactionDTO>> findBySenderId(@PathVariable("id") String id) throws Exception {
        return ResponseEntity.ok().body(transactionService.findBySenderId(id));
    }

    @GetMapping("/receiver/{id}")
    public ResponseEntity<List<TransactionDTO>> findByReceiverId(@PathVariable("id") String id) throws Exception {
        return ResponseEntity.ok().body(transactionService.findByReceiverId(id));
    }
}
