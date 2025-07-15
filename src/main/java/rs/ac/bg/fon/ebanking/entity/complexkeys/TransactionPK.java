package rs.ac.bg.fon.ebanking.entity.complexkeys;

import jakarta.persistence.Basic;
import jakarta.persistence.Embeddable;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;

@Embeddable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionPK implements Serializable {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Basic(optional = false)
    private Integer transactionid;


    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Basic(optional = false)
    private String sender;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Basic(optional = false)
    private String receiver;
}
