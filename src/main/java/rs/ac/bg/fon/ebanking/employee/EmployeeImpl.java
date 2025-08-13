package rs.ac.bg.fon.ebanking.employee;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.ac.bg.fon.ebanking.service.ServiceInterface;
import org.modelmapper.ModelMapper;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class EmployeeImpl implements ServiceInterface<EmployeeDTO> {

    private EmployeeRepository employeeRepository;

    private ModelMapper modelMapper;

    @Autowired
    public EmployeeImpl(EmployeeRepository employeeRepository, ModelMapper modelMapper){
        this.employeeRepository = employeeRepository;
        this.modelMapper = modelMapper;
    }
    @Override
    public List<EmployeeDTO> findAll() {
        return employeeRepository.findAll().stream().map(employee->modelMapper.map(employee, EmployeeDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public EmployeeDTO findById(Object id) throws Exception {
        Optional<Employee> employee = employeeRepository.findById((Long) id);
        EmployeeDTO employeeDTO;
        if(employee.isPresent()){
            employeeDTO = modelMapper.map(employee.get(),EmployeeDTO.class);
        }
        else{
            //throw new NotFoundException("Yaposleni nije pronadjen");
            employeeDTO = null;
        }
        return employeeDTO;
    }

    public EmployeeDTO findByUsername(String username) throws Exception {
        return modelMapper.map(employeeRepository.findEmployeeByUserEmployeeUsername(username), EmployeeDTO.class);
    }

    @Transactional
    @Override
    public EmployeeDTO save(EmployeeDTO employeeDTO) throws Exception {
        Employee employee = employeeRepository.save(modelMapper.map(employeeDTO,Employee.class));
        return modelMapper.map(employee, EmployeeDTO.class);
    }

    @Override
    public EmployeeDTO update(EmployeeDTO employeeDTO) throws Exception {
        return null;
    }
}
