package rs.ac.bg.fon.ebanking.service.implementation;

import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import rs.ac.bg.fon.ebanking.dao.LoanRepository;
import rs.ac.bg.fon.ebanking.dto.LoanDTO;
import rs.ac.bg.fon.ebanking.dto.TransactionDTO;
import rs.ac.bg.fon.ebanking.entity.Loan;
import rs.ac.bg.fon.ebanking.entity.Transaction;
import rs.ac.bg.fon.ebanking.entity.complexkeys.TransactionPK;
import rs.ac.bg.fon.ebanking.service.ServiceInterface;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LoanImpl implements ServiceInterface<LoanDTO> {

    private LoanRepository loanRepository;

    private ModelMapper modelMapper;

    @Autowired
    public LoanImpl(LoanRepository loanRepository, ModelMapper modelMapper) {
        this.loanRepository = loanRepository;
        this.modelMapper = modelMapper;
    }



    @Override
    public List<LoanDTO> findAll() {
        return loanRepository.findAll().stream().map(loan->modelMapper.map(loan, LoanDTO.class))
                .collect(Collectors.toList());
    }

    @Override
    public LoanDTO findById(Object id) throws Exception {

        Optional<Loan> loan = loanRepository.findById((Integer) id);
        LoanDTO loanDTO;
        if(loan.isPresent()){
            loanDTO = modelMapper.map(loan.get(), LoanDTO.class);
        }
        else{
            //throw new NotFoundException("Yaposleni nije pronadjen");
            loanDTO = null;
        }
        return loanDTO;
    }

    @Transactional
    @Override
    public LoanDTO save(LoanDTO loanDTO) throws Exception {
        if (loanDTO == null) {
            throw new NullPointerException("Kredit ne moze biti null");
        }
        Loan loan = modelMapper.map(loanDTO,Loan.class);
        Loan savedLoan = loanRepository.save(loan);
        return modelMapper.map(savedLoan, LoanDTO.class);
    }

    @Override
    public LoanDTO update(LoanDTO loanDTO) throws Exception {
        return null;
    }
}
