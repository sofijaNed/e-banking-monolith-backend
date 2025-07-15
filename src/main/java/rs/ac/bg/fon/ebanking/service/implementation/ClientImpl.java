package rs.ac.bg.fon.ebanking.service.implementation;

import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.ac.bg.fon.ebanking.dao.AccountRepository;
import rs.ac.bg.fon.ebanking.dao.ClientRepository;
import rs.ac.bg.fon.ebanking.dao.EmployeeRepository;
import rs.ac.bg.fon.ebanking.dto.AccountDTO;
import rs.ac.bg.fon.ebanking.dto.ClientDTO;
import rs.ac.bg.fon.ebanking.dto.EmployeeDTO;
import rs.ac.bg.fon.ebanking.dto.UserDTO;
import rs.ac.bg.fon.ebanking.entity.Account;
import rs.ac.bg.fon.ebanking.entity.Client;
import rs.ac.bg.fon.ebanking.entity.Employee;
import rs.ac.bg.fon.ebanking.entity.User;
import rs.ac.bg.fon.ebanking.service.ServiceInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ClientImpl implements ServiceInterface<ClientDTO> {

    private ClientRepository clientRepository;
    private AccountRepository accountRepository;

    private ModelMapper modelMapper;

    @Autowired
    public ClientImpl(ClientRepository clientRepository, ModelMapper modelMapper,
                      AccountRepository accountRepository){
        this.clientRepository = clientRepository;
        this.modelMapper = modelMapper;
        this.accountRepository = accountRepository;
    }
    @Override
    public List<ClientDTO> findAll() {
        return clientRepository.findAll().stream().map(client->modelMapper.map(client, ClientDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public ClientDTO findById(Object id) throws Exception {
        Optional<Client> client = clientRepository.findById((Integer) id);
        ClientDTO clientDTO;
        List<AccountDTO> accounts = new ArrayList<>();
        if(client.isPresent()){
            clientDTO = modelMapper.map(client.get(),ClientDTO.class);
            accounts = this.getAccounts(client.get().getId());
            clientDTO.setAccountDTOS(accounts);
        }
        else{
            //throw new NotFoundException("Yaposleni nije pronadjen");
            clientDTO = null;
        }
        return clientDTO;
    }

    @Transactional
    @Override
    public ClientDTO save(ClientDTO clientDTO) throws Exception {
        if(clientDTO==null){
            throw new NullPointerException("Klijent ne moze biti null");
        }
        Client client = clientRepository.save(modelMapper.map(clientDTO,Client.class));
        return modelMapper.map(client,ClientDTO.class);
    }

    @Override
    public ClientDTO update(ClientDTO clientDTO) throws Exception {
        return null;
    }

    public ClientDTO findByUsername(String username) throws Exception {
        Optional<Client> client = Optional.ofNullable(clientRepository.findClientByUserClientUsername((String) username));
        ClientDTO clientDTO;
        List<AccountDTO> accounts = new ArrayList<>();
        if(client.isPresent()){
            clientDTO = modelMapper.map(client.get(),ClientDTO.class);
            accounts = this.getAccounts(client.get().getId());
            clientDTO.setAccountDTOS(accounts);
        }
        else{
            //throw new NotFoundException("Yaposleni nije pronadjen");
            clientDTO = null;
        }
        return clientDTO;

    }

    public ClientDTO findByUser(UserDTO userDTO){
        User user = modelMapper.map(userDTO, User.class);
        Optional<Client> client = Optional.ofNullable(clientRepository.findClientByUserClient(user));
        ClientDTO clientDTO = null;
        List<AccountDTO> accounts = new ArrayList<>();
        if(client.isPresent()){
            clientDTO = modelMapper.map(client.get(),ClientDTO.class);
            accounts = this.getAccounts(client.get().getId());
            clientDTO.setAccountDTOS(accounts);
        }

        return clientDTO;
    }

    public List<AccountDTO> getAccounts(Integer id){

        List<Account> accounts = accountRepository.findAccountsByClientId(id);
        if(accounts == null){
            return null;
        }
        List<AccountDTO> accountDTOS = new ArrayList<>();

        for(Account a: accounts){
            AccountDTO accountDTO = modelMapper.map(a, AccountDTO.class);
//            recipeItemDTO.setMeasureDTO(modelMapper.map(ri.getMeasure(), UnitOfMeasureDTO.class));
//            recipeItemDTO.setCocktailDTO(modelMapper.map(ri.getCocktail(), CocktailDTO.class));
//            recipeItemDTO.setIngredientDTO(modelMapper.map(ri.getIngredient(), IngredientDTO.class));
            accountDTOS.add(accountDTO);
        }
        return accountDTOS;
    }




}
