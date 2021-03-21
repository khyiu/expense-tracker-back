package be.kuritsu.gt.service;

import be.kuritsu.gt.model.PurchaseRequest;

public interface PurchaseService {

    // todo return persisted purchase
    void registerPurchase(PurchaseRequest purchaseRequest);

}
