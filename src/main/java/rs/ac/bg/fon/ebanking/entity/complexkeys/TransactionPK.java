package rs.ac.bg.fon.ebanking.entity.complexkeys;

import jakarta.persistence.*;
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
    @Basic(optional = false)
    @Column(name = "transactionid")
    private Integer transactionid;


    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Basic(optional = false)
    @Column(name = "sender")
    private String sender;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Basic(optional = false)
    @Column(name = "receiver")
    private String receiver;
}
