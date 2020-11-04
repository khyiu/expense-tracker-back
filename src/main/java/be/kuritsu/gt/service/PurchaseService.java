package be.kuritsu.gt.service;

import be.kuritsu.gt.model.Purchase;
import be.kuritsu.gt.model.PurchaseLocation;
import be.kuritsu.gt.model.PurchaseRequest;
import be.kuritsu.gt.model.PurchasesResponse;

import java.util.List;

public interface PurchaseService {

    Purchase registerPurchase(PurchaseRequest purchaseRequest);

    PurchasesResponse getPurchases(int pageNumber, int pageSize);

    void deletePurchase(String purchaseId);

    Purchase updatePurchase(String purchaseId, PurchaseRequest purchaseRequest);

    List<PurchaseLocation> getPurchaseLocations();
}
