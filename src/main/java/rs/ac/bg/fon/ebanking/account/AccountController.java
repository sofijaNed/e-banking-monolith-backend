package rs.ac.bg.fon.ebanking.account;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/accounts")
public class AccountController {

    private AccountImpl accountService;

    @Autowired
    public AccountController(AccountImpl accountService) {
        this.accountService = accountService;
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @GetMapping
    public List<AccountDTO> findAll(){
        return accountService.findAll();
    }

    @PreAuthorize("hasRole('EMPLOYEE') or @accountRepository.existsByIdAndClientUserClientUsername(#id, authentication.name)")
    @GetMapping("/{id}")
    public ResponseEntity<AccountDTO> findById(@PathVariable("id") Long id) throws Exception {
        return ResponseEntity.ok().body(accountService.findById(id));
    }

    @PreAuthorize("hasRole('EMPLOYEE')")
    @PostMapping
    public ResponseEntity<AccountDTO> save( @RequestBody AccountDTO accountDTO) throws Exception {
        return new ResponseEntity<>(accountService.save(accountDTO), HttpStatus.CREATED);
    }

    @PreAuthorize("hasRole('EMPLOYEE') or @clientRepository.existsByIdAndUserClientUsername(#id, authentication.name)")
    @GetMapping("/clients/{id}")
    public ResponseEntity<List<AccountDTO>> getAccountsByClient(@PathVariable Long id) {
        List<AccountDTO> accounts = accountService.getAccountsByClientId(id);
        return ResponseEntity.ok(accounts);
    }


}
