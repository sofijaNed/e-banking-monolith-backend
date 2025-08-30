package rs.ac.bg.fon.ebanking.loanpayment;

import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.ac.bg.fon.ebanking.account.AccountRepository;
import rs.ac.bg.fon.ebanking.transaction.TransactionDTO;
import rs.ac.bg.fon.ebanking.account.Account;
import rs.ac.bg.fon.ebanking.transaction.TransactionImpl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class LoanPaymentImpl {
    @Autowired
    private LoanPaymentRepository loanPaymentRepository;
    @Autowired
    private AccountRepository accountRepository;
    @Autowired
    private TransactionImpl transactionRepository;
    @Autowired
    private ModelMapper modelMapper;

    @Transactional
    public void payInstallment(Long paymentId, Long clientAccountId) throws Exception {
        LoanPayment payment = loanPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Payment not found"));

        if (payment.isPaid()) {
            throw new IllegalStateException("Payment already paid");
        }

        Account sender = accountRepository.findById(clientAccountId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        Account bankAccount = accountRepository.findByAccountNumber("999-0000000001")
                .orElseThrow(() -> new IllegalStateException("Bank account not found"));

        int updated = accountRepository.withdrawIfSufficient(sender.getId(), payment.getAmount());
        if (updated == 0) {
            throw new IllegalStateException("Insufficient funds");
        }

        TransactionDTO t = new TransactionDTO();
        t.setSender(sender.getAccountNumber());
        t.setReceiver("999-0000000001");
        t.setAmount(payment.getAmount());
        t.setCurrency(payment.getCurrency());
        t.setDescription("Loan installment payment for loan #" + payment.getLoan().getId());
        t.setDate(LocalDateTime.now());
        t.setStatus("COMPLETED");
        t.setType("CLIENT_TO_BANK");
        transactionRepository.save(t);

        payment.setPaid(true);
        payment.setPaidAt(LocalDate.now());
        loanPaymentRepository.save(payment);
    }

    public List<LoanPaymentDTO> findByLoanId(Long loanId) {
        return loanPaymentRepository.findByLoanId(loanId)
                .stream()
                .map(p -> {
                    LoanPaymentDTO dto = modelMapper.map(p, LoanPaymentDTO.class);
                    dto.setLoanId(p.getLoan().getId());
                    return dto;
                })
                .toList();
    }

    public List<LoanPaymentDTO> findMine() {
        return loanPaymentRepository.findMine().stream()
                .map(lp -> modelMapper.map(lp, LoanPaymentDTO.class)).toList();
    }
}
