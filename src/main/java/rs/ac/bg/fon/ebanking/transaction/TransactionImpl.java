package rs.ac.bg.fon.ebanking.transaction;

import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.ac.bg.fon.ebanking.account.Account;
import rs.ac.bg.fon.ebanking.audit.Audit;
import rs.ac.bg.fon.ebanking.audit.AuditRepository;
import rs.ac.bg.fon.ebanking.account.AccountRepository;
import rs.ac.bg.fon.ebanking.service.ServiceInterface;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class TransactionImpl implements ServiceInterface<TransactionDTO> {

    private TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private ModelMapper modelMapper;
    private final AuditRepository auditRepository;

    @Autowired
    public TransactionImpl(TransactionRepository transactionRepository,
                           ModelMapper modelMapper,
                           AccountRepository accountRepository,
                           AuditRepository auditRepository) {
        this.transactionRepository = transactionRepository;
        this.modelMapper = modelMapper;
        this.accountRepository = accountRepository;
        this.auditRepository = auditRepository;
    }

    @Override
    public List<TransactionDTO> findAll() {
        return transactionRepository.findAll().stream()
                .map(transaction -> modelMapper.map(transaction, TransactionDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public TransactionDTO findById(Object id) throws Exception {
        return transactionRepository.findById((Long) id)
                .map(this::mapToDTO)
                .orElse(null);
    }

    @Transactional
    @Override
    public TransactionDTO save(TransactionDTO dto) throws Exception {
        validateTransactionDTO(dto);

        Account sender = accountRepository.findByAccountNumber(dto.getSender())
                .orElseThrow(() -> new IllegalArgumentException("Sender account not found"));
        Account receiver = accountRepository.findByAccountNumber(dto.getReceiver())
                .orElseThrow(() -> new IllegalArgumentException("Receiver account not found"));

        if (!sender.getCurrency().name().equalsIgnoreCase(dto.getCurrency())) {
            throw new IllegalArgumentException("Currency mismatch");
        }

        Transaction transaction = new Transaction();
        transaction.setAmount(dto.getAmount());
        transaction.setCurrency(dto.getCurrency());
        transaction.setModel(dto.getModel());
        transaction.setNumber(dto.getNumber());
        transaction.setDate(LocalDateTime.now());
        transaction.setDescription(dto.getDescription());
        transaction.setReference(dto.getReference());
        transaction.setSender(sender);
        transaction.setReceiver(receiver);

        try {
            int updated = accountRepository.withdrawIfSufficient(sender.getId(), dto.getAmount());
            if (updated == 0) {
                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setDescription(dto.getDescription() + " | Reason: Insufficient funds");
                transactionRepository.save(transaction);
                return mapToDTO(transaction);
            }

            int depUpdated = accountRepository.deposit(receiver.getId(), dto.getAmount());
            if (depUpdated == 0) {
                transaction.setStatus(TransactionStatus.FAILED);
                transaction.setDescription(dto.getDescription() + " | Reason: Receiver account not found");
                transactionRepository.save(transaction);
                return mapToDTO(transaction);
            }

            transaction.setStatus(TransactionStatus.COMPLETED);
            transaction.setType(TransactionType.valueOf(dto.getType().toUpperCase()));
            Transaction saved = transactionRepository.save(transaction);

            Audit audit = new Audit();
            audit.setTableName("transaction");
            audit.setRecordId(saved.getId());
            audit.setAction("CREATE");
            audit.setChangedAt(Instant.now());
            if (sender.getClient() != null) {
                audit.setChangedBy(sender.getClient().getFirstname());
            } else {
                audit.setChangedBy("BANK");
            }

            auditRepository.save(audit);
            return mapToDTO(saved);

        } catch (Exception ex) {
            transaction.setStatus(TransactionStatus.FAILED);
            transaction.setDescription(dto.getDescription() + " | Error: " + ex.getMessage());
            transactionRepository.save(transaction);
            throw ex;
        }
    }

    public List<TransactionDTO> findBySenderAccountNumber(String senderAccountNumber) {
        return transactionRepository.findAll().stream()
                .filter(tx -> tx.getSender() != null && senderAccountNumber.equals(tx.getSender().getAccountNumber()))
                .map(this::mapToDTO)
                .toList();
    }

    public List<TransactionDTO> findByReceiverAccountNumber(String receiverAccountNumber) {
        return transactionRepository.findAll().stream()
                .filter(tx -> tx.getReceiver() != null && receiverAccountNumber.equals(tx.getReceiver().getAccountNumber()))
                .map(this::mapToDTO)
                .toList();
    }

    private void validateTransactionDTO(TransactionDTO dto) {
        if (dto == null) throw new NullPointerException("Transaction cannot be null");
        if (dto.getAmount() == null || dto.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0");
        }
    }

    @Override
    public TransactionDTO update(TransactionDTO transactionDTO) {
        throw new UnsupportedOperationException("Updating transactions is not allowed");
    }

    private TransactionDTO mapToDTO(Transaction transaction) {
        TransactionDTO dto = modelMapper.map(transaction, TransactionDTO.class);
        if (transaction.getSender() != null) {
            dto.setSender(transaction.getSender().getAccountNumber());
        }
        if (transaction.getReceiver() != null) {
            dto.setReceiver(transaction.getReceiver().getAccountNumber());
        }
        return dto;
    }
}
