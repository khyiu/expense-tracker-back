package be.kuritsu.gt.controller;

import be.kuritsu.gt.api.PurchasesApi;
import be.kuritsu.gt.model.PurchaseRequest;
import be.kuritsu.gt.model.PurchaseResponse;
import be.kuritsu.gt.service.PurchaseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

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
        this.purchaseService.registerPurchase(purchaseRequest);
        // todo kyiu: finish implementation
        return null;
    }

    //    @Secured("ROLE_USERS")
//    @Override
//    public ResponseEntity<Purchase> registerPurchase(@Valid PurchaseRequest purchaseRequest) {
//        Purchase purchase = purchaseService.registerPurchase(purchaseRequest);
//        return new ResponseEntity<>(purchase, HttpStatus.CREATED);
//    }

//    @CrossOrigin
//    @Secured("ROLE_USERS")
//    @Override
//    public ResponseEntity<PurchasesResponse> getPurchases(@NotNull @Valid Integer pageNumber,
//                                                          @NotNull @Valid Integer pageSize) {
//        return ResponseEntity.ok(purchaseService.getPurchases(pageNumber, pageSize));
//    }

}
