package rs.ac.bg.fon.ebanking.security.token;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TokenRepository extends JpaRepository<Token,Long> {
    @Query(value = """
      select t from Token t inner join t.user u
      on t.user.username = u.username
      where u.username = :username and t.expired = false and t.revoked = false
      """)
    List<Token> findAllValidTokenByUser(String username);

    @Query("select t from Token t where t.user.username=:username and t.tokenType=:type and t.expired=false and t.revoked=false")
    List<Token> findAllValidByUserAndType(@Param("username") String username, @Param("type") TokenType type);

    Optional<Token> findByTokenAndTokenType(String token, TokenType tokenType);


    List<Token> findByUserUsername(String username);


    Optional<Token> findByToken(String token);


    void deleteByUserUsername(String username);
}
