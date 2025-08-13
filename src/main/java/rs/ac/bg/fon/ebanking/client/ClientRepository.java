package rs.ac.bg.fon.ebanking.client;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import rs.ac.bg.fon.ebanking.user.User;

@Repository
public interface ClientRepository extends JpaRepository<Client, Long> {
    Client findClientByUserClient(User user);
    Client findClientByUserClientUsername(String username);
}
