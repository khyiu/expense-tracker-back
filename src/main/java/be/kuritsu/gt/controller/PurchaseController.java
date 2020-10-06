package be.kuritsu.gt.controller;

import be.kuritsu.gt.api.PurchasesApi;
import be.kuritsu.gt.model.Purchase;
import be.kuritsu.gt.model.PurchaseRequest;
import be.kuritsu.gt.repository.PurchaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

@RestController
public class PurchaseController implements PurchasesApi {

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Secured("ROLE_USERS")
    @Override
    public ResponseEntity<Purchase> registerPurchase(@Valid PurchaseRequest purchaseRequest) {
        return null;
    }
}
