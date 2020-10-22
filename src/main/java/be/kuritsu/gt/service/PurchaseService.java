package be.kuritsu.gt.service;

import be.kuritsu.gt.model.Purchase;
import be.kuritsu.gt.model.PurchaseRequest;

import java.util.List;

public interface PurchaseService {

    Purchase registerPurchase(PurchaseRequest purchaseRequest);

    List<Purchase> getPurchases(int pageNumber, int pageSize);
}
