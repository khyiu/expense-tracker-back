package be.kuritsu.gt.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import be.kuritsu.gt.mapper.ExpenseMapper;
import be.kuritsu.gt.model.ExpenseRequest;
import be.kuritsu.gt.model.ExpenseResponse;
import be.kuritsu.gt.persistence.model.ExpenseEntity;
import be.kuritsu.gt.repository.ExpenseRepository;
import be.kuritsu.gt.repository.SortingDirection;

@Service
public class ExpenseServiceImpl implements ExpenseService {

    private final ExpenseRepository expenseRepository;

    @Autowired
    public ExpenseServiceImpl(ExpenseRepository expenseRepository) {
        this.expenseRepository = expenseRepository;
    }

    @Override
    public ExpenseResponse registerExpense(ExpenseRequest expense) {
        String ownr = SecurityContextHolder.getContext().getAuthentication().getName();
        ExpenseEntity expenseEntity = ExpenseMapper.toExpenseEntity(ownr, expense);
        expenseRepository.save(expenseEntity);
        return ExpenseMapper.toExpenseResponse(expenseEntity);
    }

    @Override
    public ExpenseResponse getExpense(String id) {
        String ownr = SecurityContextHolder.getContext().getAuthentication().getName();
        ExpenseEntity expenseEntity = expenseRepository.getExpense(ownr, id);
        return ExpenseMapper.toExpenseResponse(expenseEntity);
    }

    @Override
    public List<ExpenseResponse> getExpenses(Integer pageSize, SortingDirection sortDirection, String exclusiveBoundKey) {
        String ownr = SecurityContextHolder.getContext().getAuthentication().getName();
        List<ExpenseEntity> expenseEntities = expenseRepository.getExpenses(ownr, pageSize, sortDirection, exclusiveBoundKey);
        return expenseEntities.stream()
                .map(ExpenseMapper::toExpenseResponse)
                .collect(Collectors.toList());
    }

    //
    //    @Override
    //    public void deletePurchase(Integer creationTimestamp) {
    //        String ownr = SecurityContextHolder.getContext().getAuthentication().getName();
    //        Purchase purchase = purchaseRepository.getPurchase(ownr, creationTimestamp);
    //
    //        purchaseRepository.delete(ownr, Integer.parseInt(purchase.getCreationTimestamp()));
    //        purchase.getItems()
    //                .forEach(itemCreationTimestamp -> purchaseItemRepository.delete(ownr, Integer.parseInt(itemCreationTimestamp)));
    //    }
    //

}
