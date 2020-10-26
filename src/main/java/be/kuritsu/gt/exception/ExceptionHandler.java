package be.kuritsu.gt.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ResponseStatus;

@ControllerAdvice
public class ExceptionHandler {

    @org.springframework.web.bind.annotation.ExceptionHandler(PurchaseNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handlePurchaseNotFound(PurchaseNotFoundException purchaseNotFoundException) {
        // do nothing -> response status defined through @ResponseStatus
    }
}
