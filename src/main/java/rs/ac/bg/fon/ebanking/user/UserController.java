package rs.ac.bg.fon.ebanking.user;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/users")
public class UserController {

    private UserImpl userImplementation;

    @Autowired
    public UserController(UserImpl userImplementation) {
        this.userImplementation = userImplementation;
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserDTO> login(@PathVariable("id") String username, String password) throws Exception {
        return ResponseEntity.ok().body(userImplementation.login(username, password));
    }
}
