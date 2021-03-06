package pdm.networkservicesmonitor.exceptions;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import pdm.networkservicesmonitor.payload.ApiBaseResponse;
import pdm.networkservicesmonitor.payload.ApiQueryResponse;

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
    @ExceptionHandler(value = NotFoundException.class)
    public ResponseEntity<?> handleException(NotFoundException exception) {
        return new ResponseEntity<>(new ApiBaseResponse(false, exception.getMessage(), HttpStatus.NOT_FOUND),
                HttpStatus.NOT_FOUND);
    }

    @ResponseBody
    @ExceptionHandler(value = BadRequestException.class)
    public ResponseEntity<?> handleException(BadRequestException exception) {
        return new ResponseEntity<>(new ApiBaseResponse(false, exception.getMessage(), HttpStatus.BAD_REQUEST),
                HttpStatus.BAD_REQUEST);
    }

    @ResponseBody
    @ExceptionHandler(value = MethodNotAllowed.class)
    public ResponseEntity<?> handleException(MethodNotAllowed exception) {
        return new ResponseEntity<>(new ApiBaseResponse(false, exception.getMessage(), HttpStatus.METHOD_NOT_ALLOWED),
                HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ResponseBody
    @ExceptionHandler(value = OperationForbidden.class)
    public ResponseEntity<?> handleException(OperationForbidden exception) {
        return new ResponseEntity<>(new ApiBaseResponse(false, exception.getMessage(), HttpStatus.FORBIDDEN),
                HttpStatus.FORBIDDEN);
    }

    @ResponseBody
    @ExceptionHandler(value = ResourceNotFoundException.class)
    public ResponseEntity<?> handleException(ResourceNotFoundException exception) {
        return new ResponseEntity<>(new ApiBaseResponse(false, exception.getMessage(), HttpStatus.NOT_FOUND),
                HttpStatus.NOT_FOUND);
    }

    @ResponseBody
    @ExceptionHandler(value = AppNotImplementedException.class)
    public ResponseEntity<?> handleException(AppNotImplementedException exception) {
        return new ResponseEntity<>(new ApiBaseResponse(false, exception.getMessage(), HttpStatus.METHOD_NOT_ALLOWED),
                HttpStatus.METHOD_NOT_ALLOWED);
    }

    @ResponseBody
    @ExceptionHandler(value = UserBadCredentialsException.class)
    public ResponseEntity<?> handleException(UserBadCredentialsException exception) {
        return new ResponseEntity<>(new ApiBaseResponse(false, exception.getMessage(), HttpStatus.UNAUTHORIZED),
                HttpStatus.UNAUTHORIZED);
    }

    @ResponseBody
    @ExceptionHandler(value = UserDisabledException.class)
    public ResponseEntity<?> handleException(UserDisabledException exception) {
        return new ResponseEntity<>(new ApiBaseResponse(false, exception.getMessage(), HttpStatus.FORBIDDEN),
                HttpStatus.FORBIDDEN);
    }

    @ResponseBody
    @ExceptionHandler(value = QueryException.class)
    public ResponseEntity<?> handleException(QueryException exception) {
        return new ResponseEntity<>(new ApiQueryResponse(false, exception.getMessage(), HttpStatus.NOT_FOUND, exception.getQueryError()),
                HttpStatus.NOT_FOUND);
    }
}
