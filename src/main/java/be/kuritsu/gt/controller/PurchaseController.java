package be.kuritsu.gt.controller;

import be.kuritsu.gt.api.PurchaseApi;
import be.kuritsu.gt.api.PurchasesApi;
import be.kuritsu.gt.model.Purchase;
import be.kuritsu.gt.model.PurchaseRequest;
import be.kuritsu.gt.model.PurchasesResponse;
import be.kuritsu.gt.service.PurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

@RestController
public class PurchaseController implements PurchasesApi, PurchaseApi {

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

    @Secured("ROLE_USERS")
    @Override
    public ResponseEntity<PurchasesResponse> getPurchases(@NotNull @Valid Integer pageNumber,
                                                          @NotNull @Valid Integer pageSize) {
        return ResponseEntity.ok(purchaseService.getPurchases(pageNumber, pageSize));
    }

    @Secured("ROLE_USERS")
    @Override
    public ResponseEntity<Void> deletePurchase(String purchaseId) {
        purchaseService.deletePurchase(purchaseId);
        return ResponseEntity.ok().build();
    }

    @Secured("ROLE_USERS")
    @Override
    public ResponseEntity<Purchase> updatePurchase(String purchaseId, @Valid PurchaseRequest purchaseRequest) {
        return ResponseEntity.ok(purchaseService.updatePurchase(purchaseId, purchaseRequest));
    }
}
