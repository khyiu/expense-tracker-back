package be.kuritsu.gt.service;

import java.util.List;

import be.kuritsu.gt.model.ExpenseRequest;
import be.kuritsu.gt.model.ExpenseResponse;
import be.kuritsu.gt.repository.SortingDirection;

public interface ExpenseService {

    ExpenseResponse registerExpense(ExpenseRequest expenseRequest);

    ExpenseResponse getExpense(String id);

    List<ExpenseResponse> getExpenses(Integer pageSize, SortingDirection sortDirection, String exclusiveBoundKey);

    void deleteExpense(String id);
}
