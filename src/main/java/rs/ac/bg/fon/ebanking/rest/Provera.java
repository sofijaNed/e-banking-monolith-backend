package rs.ac.bg.fon.ebanking.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import rs.ac.bg.fon.ebanking.security.twofactorauth.EmailService;

@RestController
@RequestMapping("/test")
public class Provera {

    @Autowired
    private EmailService emailService;
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("Test endpoint is working");
    }

    @GetMapping("/test-mail")
    public String sendMail() {
        emailService.sendTestEmail("sofijaned2000@gmail.com");
        return "Mail sent";
    }
}
