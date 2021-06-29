package be.kuritsu.gt.repository;

import be.kuritsu.gt.persistence.model.ExpenseEntity;

public interface ExpenseRepository {

    void save(ExpenseEntity expense);

//    List<Purchase> findPurchases(String owrn, int pageSize, SortingDirection sortingDirection, @CheckForNull Integer exclusiveBoundKey);
//
//    Purchase getPurchase(String ownr, Integer creationTimestamp);
//
//    void delete(String ownr, Integer creationTimestamp);
}
