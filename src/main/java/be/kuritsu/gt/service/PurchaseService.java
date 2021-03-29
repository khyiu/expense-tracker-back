package be.kuritsu.gt.service;

import be.kuritsu.gt.model.PurchaseRequest;
import be.kuritsu.gt.model.PurchaseResponse;

public interface PurchaseService {

    PurchaseResponse registerPurchase(PurchaseRequest purchaseRequest);

}
