package rs.ac.bg.fon.ebanking.service.implementation;

import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.ac.bg.fon.ebanking.dao.AccountRepository;
import rs.ac.bg.fon.ebanking.dao.TransactionRepository;
import rs.ac.bg.fon.ebanking.dto.AccountDTO;
import rs.ac.bg.fon.ebanking.dto.ClientDTO;
import rs.ac.bg.fon.ebanking.dto.EmployeeDTO;
import rs.ac.bg.fon.ebanking.dto.TransactionDTO;
import rs.ac.bg.fon.ebanking.entity.Account;
import rs.ac.bg.fon.ebanking.entity.Client;
import rs.ac.bg.fon.ebanking.entity.Employee;
import rs.ac.bg.fon.ebanking.entity.Transaction;
import rs.ac.bg.fon.ebanking.entity.complexkeys.TransactionPK;
import rs.ac.bg.fon.ebanking.service.ServiceInterface;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TransactionImpl implements ServiceInterface<TransactionDTO> {

    private TransactionRepository transactionRepository;
    private AccountImpl accountImpl;

    private ModelMapper modelMapper;

    @Autowired
    public TransactionImpl(TransactionRepository transactionRepository,
                           AccountImpl accountImpl,
                           ModelMapper modelMapper) {
        this.transactionRepository = transactionRepository;
        this.accountImpl = accountImpl;
        this.modelMapper = modelMapper;
    }

    @Override
    public List<TransactionDTO> findAll() {
        return transactionRepository.findAll().stream().map(transaction->modelMapper.map(transaction, TransactionDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public TransactionDTO findById(Object id) throws Exception {
        TransactionPK transactionPK = (TransactionPK) id;
        Optional<Transaction> transaction = transactionRepository.findById(transactionPK);
        TransactionDTO transactionDTO;
        if(transaction.isPresent()){
            transactionDTO = modelMapper.map(transaction.get(),TransactionDTO.class);
        }
        else{
            //throw new NotFoundException("Yaposleni nije pronadjen");
            transactionDTO = null;
        }
        return transactionDTO;
    }

    @Transactional
    @Override
    public TransactionDTO save(TransactionDTO dto) throws Exception {
        validateTransactionDTO(dto);

        Transaction transaction = modelMapper.map(dto, Transaction.class);
        transaction.setSender(modelMapper.map(dto.getSenderDTO(), Account.class));
        transaction.setReceiver(modelMapper.map(dto.getReceiverDTO(), Account.class));

        updateSenderBalance(transaction);
        updateReceiverBalance(transaction);

        Transaction saved = transactionRepository.save(transaction);
        return modelMapper.map(saved, TransactionDTO.class);
    }

    private void validateTransactionDTO(TransactionDTO dto) {
        if (dto == null) throw new NullPointerException("Transaction cannot be null");
        if (dto.getSenderDTO() == null) throw new IllegalArgumentException("Sender cannot be null");
        if (dto.getReceiverDTO() == null) throw new IllegalArgumentException("Receiver cannot be null");
        if (dto.getAmount() <= 0) throw new IllegalArgumentException("Amount must be greater than 0");
    }

    private void updateSenderBalance(Transaction transaction) throws Exception {
        Account sender = transaction.getSender();
        sender.setBalance(sender.getBalance() - transaction.getAmount());
        accountImpl.save(modelMapper.map(sender, AccountDTO.class));
    }

    private void updateReceiverBalance(Transaction transaction) throws Exception {
        AccountDTO receiverDTO = accountImpl.findById(transaction.getReceiver().getId());
        if (receiverDTO == null) throw new IllegalArgumentException("Receiver account not found");

        if (!transaction.getSender().getCurrency().equals(receiverDTO.getCurrency()))
            throw new IllegalArgumentException("Currency mismatch");

        Account receiver = modelMapper.map(receiverDTO, Account.class);
        receiver.setBalance(receiver.getBalance() + transaction.getAmount());
        accountImpl.save(modelMapper.map(receiver, AccountDTO.class));
    }

    @Override
    public TransactionDTO update(TransactionDTO transactionDTO) throws Exception {
        return null;
    }
}
