package rs.ac.bg.fon.ebanking.loan;

import lombok.Data;

import java.time.LocalDate;

@Data
public class LoanResponseDTO {
    private Long id;
    private String status;
    private LocalDate createdAt;
    private LocalDate approvedAt;
    private Long approvedByEmployeeId;
    private String note;
}
