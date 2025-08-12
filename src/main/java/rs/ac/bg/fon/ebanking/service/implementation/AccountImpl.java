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
        this.modelMapper.typeMap(AccountDTO.class, Account.class)
                .addMappings(mapper -> mapper.skip(Account::setClient));
        this.modelMapper.typeMap(Account.class, AccountDTO.class)
                .addMappings(mapper -> mapper.skip(AccountDTO::setClient));
    }

    @Override
    public List<AccountDTO> findAll() {
        return accountRepository.findAll().stream()
                .map(account -> {
                    AccountDTO dto = modelMapper.map(account, AccountDTO.class);
                    if (account.getClient() != null) {
                        dto.setClient(account.getClient().getId());
                    }
                    return dto;
                })
                .toList();
    }

    @Override
    public AccountDTO findById(Object id) throws Exception {
        Optional<Account> accountOpt = accountRepository.findById((Long) id);
        if (accountOpt.isEmpty()) return null;

        Account account = accountOpt.get();
        AccountDTO dto = modelMapper.map(account, AccountDTO.class);
        if (account.getClient() != null) {
            dto.setClient(account.getClient().getId());
        }
        return dto;
    }

    @Transactional
    @Override
    public AccountDTO save(AccountDTO accountDTO) throws Exception {
        if (accountDTO == null) {
            throw new NullPointerException("AccountDTO cannot be null");
        }

        Account account = modelMapper.map(accountDTO, Account.class);

        if (accountDTO.getClient() != null) {
            ClientDTO clientDTO = clientImpl.findById(accountDTO.getClient());
            if (clientDTO == null) {
                throw new IllegalArgumentException("Client not found for ID: " + accountDTO.getClient());
            }
            Client client = modelMapper.map(clientDTO, Client.class);
            account.setClient(client);
        }

        Account saved = accountRepository.save(account);
        AccountDTO resultDTO = modelMapper.map(saved, AccountDTO.class);
        resultDTO.setClient(saved.getClient() != null ? saved.getClient().getId() : null);
        return resultDTO;
    }

    @Transactional
    @Override
    public AccountDTO update(AccountDTO accountDTO) throws Exception {
        if (accountDTO.getId() == null) {
            throw new IllegalArgumentException("ID je obavezan za update");
        }
        Optional<Account> existingOpt = accountRepository.findById(accountDTO.getId());
        if (existingOpt.isEmpty()) {
            throw new IllegalArgumentException("Nalog ne postoji");
        }

        Account existing = existingOpt.get();
        modelMapper.map(accountDTO, existing);

        if (accountDTO.getClient() != null) {
            ClientDTO clientDTO = clientImpl.findById(accountDTO.getClient());
            if (clientDTO == null) {
                throw new IllegalArgumentException("Klijent ne postoji za ID: " + accountDTO.getClient());
            }
            existing.setClient(modelMapper.map(clientDTO, Client.class));
        }

        Account updated = accountRepository.save(existing);
        return mapToDTO(updated);
    }

    private AccountDTO mapToDTO(Account account) {
        AccountDTO dto = modelMapper.map(account, AccountDTO.class);
        if (account.getClient() != null) {
            dto.setClient(account.getClient().getId());
        }
        return dto;
    }

    public List<AccountDTO> getAccountsByClientId(Long clientId) {
        List<Account> accounts = accountRepository.findAccountsByClientId(clientId);
        if (accounts == null || accounts.isEmpty()) return new ArrayList<>();

        return accounts.stream()
                .map(account -> {
                    AccountDTO dto = modelMapper.map(account, AccountDTO.class);
                    if (account.getClient() != null) {
                        dto.setClient(account.getClient().getId());
                    }
                    return dto;
                })
                .toList();
    }
}
