package be.kuritsu.gt.controller;

import java.util.List;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import be.kuritsu.gt.api.PurchasesApi;
import be.kuritsu.gt.model.PurchaseRequest;
import be.kuritsu.gt.model.PurchaseResponse;
import be.kuritsu.gt.repository.SortingDirection;
import be.kuritsu.gt.service.PurchaseService;

@RestController
public class PurchaseController implements PurchasesApi {

    private final PurchaseService purchaseService;

    @Autowired
    public PurchaseController(PurchaseService purchaseService) {
        this.purchaseService = purchaseService;
    }

    @Secured("ROLE_USERS")
    @Override
    public ResponseEntity<PurchaseResponse> registerPurchase(@Valid PurchaseRequest purchaseRequest) {
        return new ResponseEntity<>(purchaseService.registerPurchase(purchaseRequest), HttpStatus.CREATED);
    }

    @CrossOrigin
    @Secured("ROLE_USERS")
    @Override
    public ResponseEntity<List<PurchaseResponse>> fetchPurchases(Integer pageSize, String sortDirection, Integer exclusiveBoundKey) {
        SortingDirection sortDir = SortingDirection.valueOf(sortDirection);
        return ResponseEntity.ok(this.purchaseService.fetchPurchases(pageSize, sortDir, exclusiveBoundKey));
    }

}
