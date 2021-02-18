package be.kuritsu.gt.service;

import be.kuritsu.gt.model.PurchaseRequest;

public interface PurchaseService {

    void registerPurchase(PurchaseRequest purchaseRequest);

}
