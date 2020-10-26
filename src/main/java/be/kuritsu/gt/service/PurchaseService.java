package be.kuritsu.gt.service;

import be.kuritsu.gt.model.Purchase;
import be.kuritsu.gt.model.PurchaseRequest;
import be.kuritsu.gt.model.PurchasesResponse;

public interface PurchaseService {

    Purchase registerPurchase(PurchaseRequest purchaseRequest);

    PurchasesResponse getPurchases(int pageNumber, int pageSize);

    void deletePurchase(String purchaseId);
}
