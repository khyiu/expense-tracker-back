package be.kuritsu.gt.repository;

import java.util.List;

import be.kuritsu.gt.persistence.model.ExpenseEntity;

import edu.umd.cs.findbugs.annotations.CheckForNull;

public interface ExpenseRepository {

    void save(ExpenseEntity expense);

    ExpenseEntity getExpense(String ownr, String id);

    List<ExpenseEntity> getExpenses(String owrn, int pageSize, SortingDirection sortingDirection, @CheckForNull String exclusiveBoundKey);

    void delete(String ownr, String id);
}
