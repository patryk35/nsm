package pdm.networkservicesmonitor.agent.controller.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pdm.networkservicesmonitor.agent.payloads.proxy.ApiBaseResponse;

@RestControllerAdvice
@Slf4j
public class AppExceptionHandler {


    @ResponseBody
    @ExceptionHandler(value = AppException.class)
    public ResponseEntity<?> handleException(AppException exception) {
        HttpHeaders responseHeaders = new HttpHeaders();
        return new ResponseEntity<>(new ApiBaseResponse(false, exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ResponseBody
    @ExceptionHandler(value = ProxyException.class)
    public ResponseEntity<?> handleException(ProxyException exception) {
        return new ResponseEntity<>(new ApiBaseResponse(false, exception.getMessage(), HttpStatus.NOT_ACCEPTABLE),
                HttpStatus.NOT_ACCEPTABLE);
    }

    @ResponseBody
    @ExceptionHandler(value = ProxyDisabledException.class)
    public ResponseEntity<?> handleException(ProxyDisabledException exception) {
        return new ResponseEntity<>(new ApiBaseResponse(false, exception.getMessage(), HttpStatus.NOT_FOUND),
                HttpStatus.NOT_FOUND);
    }

}
