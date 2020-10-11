package be.kuritsu.gt.service;

import be.kuritsu.gt.model.Purchase;
import be.kuritsu.gt.model.PurchaseRequest;

public interface PurchaseService {

    Purchase registerPurchase(PurchaseRequest purchaseRequest);
}
