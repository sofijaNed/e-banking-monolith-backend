package rs.ac.bg.fon.ebanking.employee;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    Employee findEmployeeByUserEmployeeUsername(String username);
}
