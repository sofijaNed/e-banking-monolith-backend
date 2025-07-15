package rs.ac.bg.fon.ebanking.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.bg.fon.ebanking.dto.EmployeeDTO;
import rs.ac.bg.fon.ebanking.dto.LoanDTO;
import rs.ac.bg.fon.ebanking.service.implementation.LoanImpl;

import java.util.List;

@RestController
@RequestMapping("/loans")
public class LoanController {

    private LoanImpl loanService;

    @Autowired
    public LoanController(LoanImpl loanService) {
        this.loanService = loanService;
    }

    @GetMapping
    public List<LoanDTO> findAll(){
        return loanService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<LoanDTO> findById(@PathVariable("id") Integer id) throws Exception {
        return ResponseEntity.ok().body(loanService.findById(id));
    }

    @PostMapping
    public ResponseEntity<LoanDTO> save( @RequestBody LoanDTO loanDTO) throws Exception {
        return new ResponseEntity<>(loanService.save(loanDTO), HttpStatus.CREATED);
    }
}
