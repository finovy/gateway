package tech.finovy.gateway.exception;

import lombok.Data;
import org.springframework.http.HttpStatusCode;

@Data
public class ExceptionEntity {
    private HttpStatusCode httpStatus;
    private int httpCode;
    private String body;

    public ExceptionEntity(HttpStatusCode httpStatus, int httpCode, String body) {
        this.httpStatus = httpStatus;
        this.httpCode = httpCode;
        this.body = body;
    }

    public void refresh(HttpStatusCode httpStatus, int httpCode, String body) {
        this.httpStatus = httpStatus;
        this.httpCode = httpCode;
        this.body = body;
    }
}
