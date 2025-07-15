package rs.ac.bg.fon.ebanking.service.implementation;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.ac.bg.fon.ebanking.dao.UserRepository;
import rs.ac.bg.fon.ebanking.dto.TransactionDTO;
import rs.ac.bg.fon.ebanking.dto.UserDTO;
import rs.ac.bg.fon.ebanking.entity.Transaction;
import rs.ac.bg.fon.ebanking.entity.User;
import rs.ac.bg.fon.ebanking.exception.type.NotFoundException;
import rs.ac.bg.fon.ebanking.service.ServiceInterface;

import java.util.List;
import java.util.Optional;

@Service
public class UserImpl implements ServiceInterface<UserDTO> {

    private UserRepository userRepository;

    private ModelMapper modelMapper;

    @Autowired
    public UserImpl(UserRepository userRepository, ModelMapper modelMapper) {
        this.userRepository = userRepository;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<UserDTO> findAll() {
        return null;
    }

    @Override
    public UserDTO findById(Object id) throws Exception {
        Optional<User> user = userRepository.findById((String) id);
        UserDTO userDTO = null;
        if(user.isPresent()){
            userDTO = modelMapper.map(user.get(),UserDTO.class);
        }
//        else{
//            userDTO = null;
//            throw new NotFoundException("Ne postoji User sa ovim username-om");
//
//        }
        return userDTO;
    }

    @Override
    public UserDTO save(UserDTO userDTO) throws Exception {
        return null;
    }

    @Override
    public UserDTO update(UserDTO userDTO) throws Exception {
        return null;
    }


    public UserDTO login(String username, String password) throws Exception{

        UserDTO userDTO = this.findById(username);

        if(userDTO != null){
            User user = modelMapper.map(userDTO,User.class);
            if(user.getPassword().equals(password)){
                return userDTO;
            }
        }
        System.out.println(modelMapper.map(userDTO,User.class));
        return null;
    }
}
