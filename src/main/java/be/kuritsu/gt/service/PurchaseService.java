package be.kuritsu.gt.service;

import be.kuritsu.gt.model.PurchaseRequest;
import be.kuritsu.gt.model.PurchaseResponse;
import be.kuritsu.gt.repository.SortingDirection;

import java.util.List;

public interface PurchaseService {


    PurchaseResponse registerPurchase(PurchaseRequest purchaseRequest);

    List<PurchaseResponse> fetchPurchases(Integer pageSize, SortingDirection sortDirection, Integer exclusiveBoundKey);

    void deletePurchase(Integer creationTimestamp);

    PurchaseResponse getPurchase(Integer creationTimestamp);
}
