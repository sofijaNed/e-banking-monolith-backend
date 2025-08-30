package rs.ac.bg.fon.ebanking.loan;

import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import rs.ac.bg.fon.ebanking.account.Account;
import rs.ac.bg.fon.ebanking.account.AccountRepository;
import rs.ac.bg.fon.ebanking.client.ClientRepository;
import rs.ac.bg.fon.ebanking.loanpayment.LoanPaymentRepository;
import rs.ac.bg.fon.ebanking.transaction.TransactionDTO;
import rs.ac.bg.fon.ebanking.employee.Employee;
import rs.ac.bg.fon.ebanking.employee.EmployeeRepository;
import rs.ac.bg.fon.ebanking.loanpayment.LoanPayment;
import rs.ac.bg.fon.ebanking.service.ServiceInterface;
import rs.ac.bg.fon.ebanking.transaction.TransactionImpl;
import rs.ac.bg.fon.ebanking.transaction.TransactionType;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

@Service
public class LoanImpl implements ServiceInterface<LoanDTO> {

    private final LoanRepository loanRepository;
    private final AccountRepository accountRepository;
    private final ClientRepository clientRepository;
    private final EmployeeRepository employeeRepository;
    private final ModelMapper modelMapper;
    private final LoanPaymentRepository loanPaymentRepository;
    private final TransactionImpl transactionRepository;

    @Autowired
    public LoanImpl(LoanRepository loanRepository,
                       AccountRepository accountRepository,
                       ClientRepository clientRepository,
                       EmployeeRepository employeeRepository,
                        LoanPaymentRepository loanPaymentRepository,
                        TransactionImpl transactionRepository,
                       ModelMapper modelMapper) {
        this.loanRepository = loanRepository;
        this.accountRepository = accountRepository;
        this.clientRepository = clientRepository;
        this.employeeRepository = employeeRepository;
        this.loanPaymentRepository = loanPaymentRepository;
        this.transactionRepository = transactionRepository;
        this.modelMapper = modelMapper;
        this.modelMapper.typeMap(Loan.class, LoanDTO.class).addMappings(mapper -> {
            mapper.map(src -> src.getApprovedBy() != null ? src.getApprovedBy().getId() : null,
                    LoanDTO::setApprovedBy);
            mapper.map(src -> src.getAccount() != null ? src.getAccount().getId() : null,
                    LoanDTO::setAccount);
        });
    }

    public LoanResponseDTO submitLoanRequest(Long clientId, LoanRequestDTO dto) {
        Account account = accountRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Account not found"));

        Loan loan = new Loan();
        loan.setPrincipalAmount(dto.getAmount());
        loan.setInterestRate(dto.getInterestRate());
        loan.setTermMonths(dto.getTermMonths());
        loan.setCurrency(dto.getCurrency());
        loan.setNote(dto.getPurpose());
        loan.setDateIssued(LocalDate.now());
        loan.setOutstandingBalance(dto.getAmount());
        loan.setAccount(account);
        loan.setStatus(LoanStatus.PENDING);

        Loan saved = loanRepository.save(loan);
        return mapToResponse(saved);
    }


    @Transactional
    public LoanResponseDTO approveLoan(Long loanId, Long employeeId, String note) throws Exception {
        Loan loan = loanRepository.findById(loanId)
                .orElseThrow(() -> new IllegalArgumentException("Loan not found"));

        if (loan.getStatus() != LoanStatus.PENDING) {
            throw new IllegalStateException("Loan not pending");
        }

        Employee emp = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));

        loan.setApprovedBy(emp);
        loan.setApprovedAt(LocalDate.now());
        loan.setNote(note);
        loan.setStatus(LoanStatus.APPROVED);

        BigDecimal monthlyInterestRate = loan.getInterestRate()
                .divide(BigDecimal.valueOf(12 * 100), 10, RoundingMode.HALF_UP);

        int months = loan.getTermMonths();

        BigDecimal onePlusRPowerN = monthlyInterestRate.add(BigDecimal.ONE).pow(months);
        BigDecimal numerator = monthlyInterestRate.multiply(onePlusRPowerN);
        BigDecimal denominator = onePlusRPowerN.subtract(BigDecimal.ONE);

        BigDecimal monthlyPayment = loan.getPrincipalAmount()
                .multiply(numerator)
                .divide(denominator, 2, RoundingMode.HALF_UP);

        loan.setMonthlyPayment(monthlyPayment);

        Loan savedLoan = loanRepository.save(loan);

        BigDecimal remainingPrincipal = loan.getPrincipalAmount();

        for (int i = 1; i <= months; i++) {
            BigDecimal interestAmount = remainingPrincipal.multiply(monthlyInterestRate)
                    .setScale(2, RoundingMode.HALF_UP);

            BigDecimal principalAmount = monthlyPayment.subtract(interestAmount)
                    .setScale(2, RoundingMode.HALF_UP);

            remainingPrincipal = remainingPrincipal.subtract(principalAmount)
                    .setScale(2, RoundingMode.HALF_UP);

            LoanPayment lp = new LoanPayment();
            lp.setLoan(savedLoan);
            lp.setDueDate(LocalDate.now().plusMonths(i));
            lp.setAmount(monthlyPayment);
            lp.setCurrency(loan.getCurrency());
            lp.setPrincipalAmount(principalAmount);
            lp.setInterestAmount(interestAmount);
            loanPaymentRepository.save(lp);
        }

        Account clientAccount = accountRepository.findById(loan.getAccount().getId())
                .stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Client has no account"));

        Account bankAccount = accountRepository.findByAccountNumber("999-0000000001")
                .orElseThrow(() -> new IllegalStateException("Bank account not found"));

        TransactionDTO t = new TransactionDTO();
        t.setSender("999-0000000001");
        t.setReceiver(clientAccount.getAccountNumber());
        t.setAmount(loan.getPrincipalAmount());
        t.setCurrency(loan.getCurrency());
        t.setDescription("Loan disbursement for loan #" + loan.getId());
        t.setDate(LocalDateTime.now());
        t.setStatus("COMPLETED");
        t.setType(TransactionType.BANK_TO_CLIENT.name());
        transactionRepository.save(t);

        accountRepository.deposit(clientAccount.getId(), loan.getPrincipalAmount());

        return mapToResponse(savedLoan);
    }

    private LoanResponseDTO mapToResponse(Loan loan) {
        LoanResponseDTO dto = new LoanResponseDTO();
        dto.setId(loan.getId());
        dto.setStatus(loan.getStatus().name());
        dto.setCreatedAt(loan.getDateIssued());
        dto.setApprovedAt(loan.getApprovedAt());
        dto.setApprovedByEmployeeId(
                loan.getApprovedBy() != null ? loan.getApprovedBy().getId() : null
        );
        dto.setNote(loan.getNote());
        return dto;
    }

    @Override
    public List<LoanDTO> findAll() {
        return loanRepository.findAll()
                .stream()
                .map(loan -> modelMapper.map(loan, LoanDTO.class))
                .toList();
    }

    @Override
    public LoanDTO findById(Object id) throws Exception {
        return null;
    }

    public List<LoanDTO> findAllByStatus(String status) {
        LoanStatus enumStatus;
        try {
            enumStatus = LoanStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid loan status: " + status);
        }
        return loanRepository.findLoansByStatus(enumStatus).stream()
                .map(loan -> {
                    LoanDTO dto = new LoanDTO();
                    dto.setId(loan.getId());
                    dto.setPrincipalAmount(loan.getPrincipalAmount());
                    dto.setInterestRate(loan.getInterestRate());
                    dto.setTermMonths(loan.getTermMonths());
                    dto.setCurrency(loan.getCurrency());
                    dto.setNote(loan.getNote());
                    dto.setDateIssued(loan.getDateIssued());
                    dto.setMonthlyPayment(loan.getMonthlyPayment());
                    dto.setOutstandingBalance(loan.getOutstandingBalance());
                    dto.setAccount(loan.getAccount() != null ? loan.getAccount().getId() : null);
                    dto.setApprovedBy(loan.getApprovedBy() != null ? loan.getApprovedBy().getId() : null);
                    dto.setApprovedAt(loan.getApprovedAt());
                    return dto;
                }).toList();
    }


    public List<LoanDTO> findAllByClientId(Long clientId) {
        return loanRepository.findLoansByAccountClientId(clientId).stream()
                .map(loan -> {
                    LoanDTO dto = new LoanDTO();
                    dto.setId(loan.getId());
                    dto.setPrincipalAmount(loan.getPrincipalAmount());
                    dto.setInterestRate(loan.getInterestRate());
                    dto.setTermMonths(loan.getTermMonths());
                    dto.setCurrency(loan.getCurrency());
                    dto.setNote(loan.getNote());
                    dto.setDateIssued(loan.getDateIssued());
                    dto.setMonthlyPayment(loan.getMonthlyPayment());
                    dto.setOutstandingBalance(loan.getOutstandingBalance());
                    dto.setAccount(loan.getAccount() != null ? loan.getAccount().getId() : null);
                    dto.setApprovedBy(loan.getApprovedBy() != null ? loan.getApprovedBy().getId() : null);
                    dto.setApprovedAt(loan.getApprovedAt());
                    return dto;
                })
                .toList();
    }

    @Override
    public LoanDTO save(LoanDTO loanDTO) throws Exception {
        return null;
    }

    @Override
    public LoanDTO update(LoanDTO loanDTO) throws Exception {
        return null;
    }

    public List<LoanDTO> findMine() {
        return loanRepository.findMine().stream()
                .map(l -> modelMapper.map(l, LoanDTO.class)).toList();
    }

    public List<LoanDTO> findMineByStatus(String status) {
        String u = SecurityContextHolder.getContext().getAuthentication().getName();

        // ako koristiš ENUM u entitetu:
        LoanStatus target;
        try {
            target = LoanStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid loan status: " + status);
        }

        return loanRepository.findByAccountClientUserClientUsernameAndStatus(u, target)
                .stream().map(l -> modelMapper.map(l, LoanDTO.class)).toList();

        // Ako je status u entitetu String:
        // return loanRepository.findByAccountClientUserClientUsernameAndStatus(u, status.trim().toUpperCase(Locale.ROOT))
        //        .stream().map(l -> modelMapper.map(l, LoanDTO.class)).toList();
    }
}
