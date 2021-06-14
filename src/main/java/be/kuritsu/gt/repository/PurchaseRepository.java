package be.kuritsu.gt.repository;

import be.kuritsu.gt.persistence.model.Purchase;

import javax.annotation.CheckForNull;

import java.util.List;

public interface PurchaseRepository {

    void save(Purchase purchase);

    List<Purchase> findPurchases(String owrn, int pageSize, SortingDirection sortingDirection, @CheckForNull Integer exclusiveBoundKey);
}
