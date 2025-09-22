package rs.ac.bg.fon.ebanking.exception;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ErrorResponse {

    private int status;

    private Map<String,String> message;

    private long timeStamp;
    private String cid;
}
