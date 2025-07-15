package rs.ac.bg.fon.ebanking.service.implementation;

import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.ac.bg.fon.ebanking.dao.AccountRepository;
import rs.ac.bg.fon.ebanking.dto.AccountDTO;
import rs.ac.bg.fon.ebanking.dto.ClientDTO;
import rs.ac.bg.fon.ebanking.entity.Account;
import rs.ac.bg.fon.ebanking.entity.Client;
import rs.ac.bg.fon.ebanking.service.ServiceInterface;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class AccountImpl implements ServiceInterface<AccountDTO> {

    private AccountRepository accountRepository;
    private ClientImpl clientImpl;

    private ModelMapper modelMapper;

    @Autowired
    public AccountImpl(AccountRepository accountRepository,ClientImpl clientImpl, ModelMapper modelMapper) {
        this.accountRepository = accountRepository;
        this.clientImpl = clientImpl;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<AccountDTO> findAll() {
        List<Account> accounts = accountRepository.findAll();
        List<AccountDTO> accountDTOS = new ArrayList<>();
        for(Account account:accounts){
            ClientDTO clientDTO = modelMapper.map(account.getClient(),ClientDTO.class);
            AccountDTO accountDTO = modelMapper.map(account,AccountDTO.class);
            accountDTO.setClientDTO(clientDTO);
            accountDTOS.add(accountDTO);
        }
        return accountDTOS;
    }

    @Override
    public AccountDTO findById(Object id) throws Exception {

        Optional<Account> account = accountRepository.findById((String)id);
        AccountDTO accountDTO;
        if(account.isPresent()){
            ClientDTO clientDTO = null;
            if(account.get().getClient() != null) {
                 clientDTO = modelMapper.map(account.get().getClient(), ClientDTO.class);
            }
            accountDTO = modelMapper.map(account.get(),AccountDTO.class);

            accountDTO.setClientDTO(clientDTO);


        }
        else{
           // throw new NotFoundException("Odgovor nije pronadjen");
            accountDTO = null;
        }
        return accountDTO;
    }

    @Transactional
    @Override
    public AccountDTO save(AccountDTO accountDTO) throws Exception {
        if (accountDTO == null) {
            throw new NullPointerException("Racun ne moze biti null");
        }


        Account account = modelMapper.map(accountDTO,Account.class);
        Client client = null;
        if(account.getClient() != null) {
            client = modelMapper.map(account.getClient(), Client.class);
        }
        account.setClient(client);
        Account savedAccount = accountRepository.save(account);
        return modelMapper.map(savedAccount, AccountDTO.class);
    }

    @Override
    public AccountDTO update(AccountDTO accountDTO) throws Exception {
        return null;
    }
}
