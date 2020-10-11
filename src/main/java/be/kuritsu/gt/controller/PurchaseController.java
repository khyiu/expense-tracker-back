package be.kuritsu.gt.controller;

import be.kuritsu.gt.api.PurchasesApi;
import be.kuritsu.gt.model.Purchase;
import be.kuritsu.gt.model.PurchaseRequest;
import be.kuritsu.gt.service.PurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class PurchaseController implements PurchasesApi {

    private final PurchaseService purchaseService;

    @Autowired
    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @Secured("ROLE_USERS")
    @Override
    public ResponseEntity<Purchase> registerPurchase(@Valid PurchaseRequest purchaseRequest) {
        Purchase purchase = purchaseService.registerPurchase(purchaseRequest);
        return new ResponseEntity<>(purchase, HttpStatus.CREATED);
    }
}
