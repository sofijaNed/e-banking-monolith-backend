package rs.ac.bg.fon.ebanking.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import rs.ac.bg.fon.ebanking.dto.ClientDTO;
import rs.ac.bg.fon.ebanking.dto.EmployeeDTO;
//import rs.ac.bg.fon.ebanking.exception.type.NotFoundException;
import rs.ac.bg.fon.ebanking.service.implementation.EmployeeImpl;

import java.util.List;

@RestController
@RequestMapping("/employees")
public class EmployeeController {

    private EmployeeImpl employeeService;

    @Autowired
    public EmployeeController(EmployeeImpl employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping
    public List<EmployeeDTO> findAll(){
        return employeeService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> findById(@PathVariable("id") Long id) throws Exception {
        return ResponseEntity.ok().body(employeeService.findById(id));
    }

    @PostMapping
    public ResponseEntity<EmployeeDTO> save( @RequestBody EmployeeDTO employeeDTO) throws Exception {
        return new ResponseEntity<>(employeeService.save(employeeDTO), HttpStatus.CREATED);
    }

    @GetMapping("/byUser/{username}")
    public ResponseEntity<EmployeeDTO> findByUser(@PathVariable("username") String username) throws Exception {

        return ResponseEntity.ok().body(employeeService.findByUsername(username));
    }
}
