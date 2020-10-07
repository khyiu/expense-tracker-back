package be.kuritsu.gt.controller;

import be.kuritsu.gt.api.PurchasesApi;
import be.kuritsu.gt.model.Purchase;
import be.kuritsu.gt.model.PurchaseEntity;
import be.kuritsu.gt.model.PurchaseRequest;
import be.kuritsu.gt.repository.PurchaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import java.time.format.DateTimeFormatter;

@RestController
public class PurchaseController implements PurchasesApi {

    @Autowired
    private PurchaseRepository purchaseRepository;

    @Secured("ROLE_USERS")
    @Override
    public ResponseEntity<Purchase> registerPurchase(@Valid PurchaseRequest purchaseRequest) {
        PurchaseEntity purchaseEntity = PurchaseEntity.builder()
                .date(purchaseRequest.getDate().format(DateTimeFormatter.ISO_DATE))
                .brand(purchaseRequest.getBrand())
                .build();

        PurchaseEntity savedPurchaseEntity = purchaseRepository.save(purchaseEntity);
        return new ResponseEntity<>(null, HttpStatus.CREATED);
    }
}
