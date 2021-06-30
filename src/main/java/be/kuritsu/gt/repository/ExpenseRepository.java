package be.kuritsu.gt.repository;

import be.kuritsu.gt.persistence.model.ExpenseEntity;

public interface ExpenseRepository {

    void save(ExpenseEntity expense);

    ExpenseEntity getExpense(String ownr, String id);

//    List<Purchase> findPurchases(String owrn, int pageSize, SortingDirection sortingDirection, @CheckForNull Integer exclusiveBoundKey);
//
//
//    void delete(String ownr, Integer creationTimestamp);
}
