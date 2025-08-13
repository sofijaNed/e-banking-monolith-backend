package rs.ac.bg.fon.ebanking.client;

import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.ac.bg.fon.ebanking.account.AccountRepository;
import rs.ac.bg.fon.ebanking.user.UserDTO;
import rs.ac.bg.fon.ebanking.user.User;
import rs.ac.bg.fon.ebanking.service.ServiceInterface;

import java.util.List;
import java.util.Optional;

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
        this.modelMapper.typeMap(ClientDTO.class, Client.class)
                .addMappings(mapper -> mapper.skip(Client::setUserClient));

        this.modelMapper.typeMap(Client.class, ClientDTO.class)
                .addMappings(mapper -> mapper.skip(ClientDTO::setUserClient));
    }
    @Override
    public List<ClientDTO> findAll() {
        return clientRepository.findAll().stream()
                .map(client -> {
                    ClientDTO dto = modelMapper.map(client, ClientDTO.class);
                    if (client.getUserClient() != null) {
                        dto.setUserClient(client.getUserClient().getUsername());
                    }

                    return dto;
                })
                .toList();
    }

    @Override
    public ClientDTO findById(Object id) throws Exception {
        Optional<Client> client = clientRepository.findById((Long) id);
        return client.map(cl -> {
            ClientDTO dto = modelMapper.map(cl, ClientDTO.class);
            if (cl.getUserClient() != null) {
                dto.setUserClient(cl.getUserClient().getUsername());
            }

            return dto;
        }).orElse(null);
    }

    @Transactional
    @Override
    public ClientDTO save(ClientDTO clientDTO) throws Exception {
        if (clientDTO == null) {
            throw new NullPointerException("Klijent ne može biti null");
        }
        Client saved = clientRepository.save(modelMapper.map(clientDTO, Client.class));
        return modelMapper.map(saved, ClientDTO.class);
    }

    @Override
    public ClientDTO update(ClientDTO clientDTO) throws Exception {
        return null;
    }

    public ClientDTO findByUsername(String username) throws Exception {
        Client client = clientRepository.findClientByUserClientUsername(username);
        return client != null ? mapClientWithAccounts(client) : null;
    }

    public ClientDTO findByUser(UserDTO userDTO) {
        User user = modelMapper.map(userDTO, User.class);
        Client client = clientRepository.findClientByUserClient(user);
        return client != null ? mapClientWithAccounts(client) : null;
    }

    private ClientDTO mapClientWithAccounts(Client client) {
        ClientDTO dto = modelMapper.map(client, ClientDTO.class);
        if (client.getUserClient() != null) {
            dto.setUserClient(client.getUserClient().getUsername());
        }

        return dto;
    }





}
