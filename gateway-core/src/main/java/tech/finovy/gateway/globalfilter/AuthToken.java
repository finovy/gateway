package tech.finovy.gateway.globalfilter;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

@Data
public class AuthToken implements Serializable {
    @Serial
    private static final long serialVersionUID = -616935538782845301L;
    private boolean available;
    private String token;
    private String value;
}
