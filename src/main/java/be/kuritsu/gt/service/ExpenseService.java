package be.kuritsu.gt.service;

import be.kuritsu.gt.model.ExpenseRequest;
import be.kuritsu.gt.model.ExpenseResponse;

public interface ExpenseService {

    ExpenseResponse registerExpense(ExpenseRequest expenseRequest);

    ExpenseResponse getExpense(String id);

    //    List<PurchaseResponse> fetchPurchases(Integer pageSize, SortingDirection sortDirection, Integer exclusiveBoundKey);
    //
    //    void deletePurchase(Integer creationTimestamp);
}
