package be.kuritsu.gt.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import be.kuritsu.gt.api.ExpensesApi;
import be.kuritsu.gt.mapper.ExpenseMapper;
import be.kuritsu.gt.model.ExpenseRequest;
import be.kuritsu.gt.model.ExpenseResponse;
import be.kuritsu.gt.persistence.model.ExpenseEntity;
import be.kuritsu.gt.repository.ExpenseRepository;

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

    //    @Override
    //    public List<PurchaseResponse> fetchPurchases(Integer pageSize, SortingDirection sortDirection, Integer exclusiveBoundKey) {
    //        String ownr = SecurityContextHolder.getContext().getAuthentication().getName();
    //        List<Purchase> purchases = purchaseRepository.findPurchases(ownr, pageSize, sortDirection, exclusiveBoundKey);
    //        List<String> purchaseItemsCreationTimestamps = purchases.stream()
    //                .flatMap(purchase -> purchase.getItems().stream())
    //                .sorted()
    //                .collect(Collectors.toList());
    //        Map<String, PurchaseItem> purchaseItems = purchaseItemsCreationTimestamps.isEmpty() ?
    //                Collections.emptyMap() :
    //                purchaseItemRepository.getPurchaseItems(ownr, purchaseItemsCreationTimestamps)
    //                        .stream()
    //                        .collect(Collectors.toMap(PurchaseItem::getCreationTimestamp, item -> item));
    //
    //        return purchases.stream()
    //                .map(purchase -> {
    //                    List<PurchaseItem> associatedPurchaseItems = purchaseItems.entrySet()
    //                            .stream()
    //                            .filter(entry -> purchase.getItems().contains(entry.getKey()))
    //                            .map(Map.Entry::getValue)
    //                            .collect(Collectors.toList());
    //
    //                    List<PurchaseItemResponse> purchaseItemResponses = associatedPurchaseItems
    //                            .stream()
    //                            .map(PurchaseMapper::mapToPurchaseItemResponse)
    //                            .collect(Collectors.toList());
    //
    //                    return PurchaseMapper.mapToPurchaseResponse(purchase, purchaseItemResponses);
    //                })
    //                .collect(Collectors.toList());
    //    }
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
    //    @Override
    //    public PurchaseResponse getPurchase(Integer creationTimestamp) {
    //        String ownr = SecurityContextHolder.getContext().getAuthentication().getName();
    //        Purchase purchase = purchaseRepository.getPurchase(ownr, creationTimestamp);
    //        List<PurchaseItemResponse> purchaseItemResponses = purchaseItemRepository.getPurchaseItems(ownr, purchase.getItems())
    //                .stream()
    //                .map(PurchaseMapper::mapToPurchaseItemResponse)
    //                .collect(Collectors.toList());
    //        return PurchaseMapper.mapToPurchaseResponse(purchase, purchaseItemResponses);
    //    }
}
