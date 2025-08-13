package rs.ac.bg.fon.ebanking.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/clients")
public class ClientController {

    private ClientImpl clientService;

    @Autowired
    public ClientController(ClientImpl clientService) {
        this.clientService = clientService;
    }

    @GetMapping
    public List<ClientDTO> findAll(){
        return clientService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientDTO> findById(@PathVariable("id") Long id) throws Exception {
        return ResponseEntity.ok().body(clientService.findById(id));
    }

    @PostMapping
    public ResponseEntity<ClientDTO> save( @RequestBody ClientDTO clientDTO) throws Exception {
        return new ResponseEntity<>(clientService.save(clientDTO), HttpStatus.CREATED);
    }
    @GetMapping("/byUser/{username}")
    public ResponseEntity<ClientDTO> findByUser(@PathVariable("username") String username) throws Exception {

        return ResponseEntity.ok().body(clientService.findByUsername(username));
    }

//    @GetMapping("/{id}/accounts")
//    public List<ClientDTO> getAccounts(@PathVariable("id") Integer id) throws Exception {
//        return ResponseEntity.ok().body(clientService.getAccounts(id));
//    }


}
