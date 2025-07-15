package rs.ac.bg.fon.ebanking.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.bg.fon.ebanking.dto.AccountDTO;
import rs.ac.bg.fon.ebanking.dto.EmployeeDTO;
import rs.ac.bg.fon.ebanking.service.implementation.AccountImpl;

import java.util.List;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private AccountImpl accountService;

    @Autowired
    public AccountController(AccountImpl accountService) {
        this.accountService = accountService;
    }

    @GetMapping
    public List<AccountDTO> findAll(){
        return accountService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AccountDTO> findById(@PathVariable("id") String id) throws Exception {
        return ResponseEntity.ok().body(accountService.findById(id));
    }

    @PostMapping
    public ResponseEntity<AccountDTO> save( @RequestBody AccountDTO accountDTO) throws Exception {
        return new ResponseEntity<>(accountService.save(accountDTO), HttpStatus.CREATED);
    }


}
