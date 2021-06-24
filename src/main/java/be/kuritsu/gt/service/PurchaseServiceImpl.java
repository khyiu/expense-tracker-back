package be.kuritsu.gt.service;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import be.kuritsu.gt.mapper.PurchaseMapper;
import be.kuritsu.gt.model.PurchaseItemResponse;
import be.kuritsu.gt.model.PurchaseRequest;
import be.kuritsu.gt.model.PurchaseResponse;
import be.kuritsu.gt.persistence.model.Purchase;
import be.kuritsu.gt.persistence.model.PurchaseItem;
import be.kuritsu.gt.repository.PurchaseItemRepository;
import be.kuritsu.gt.repository.PurchaseRepository;
import be.kuritsu.gt.repository.SortingDirection;

@Service
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final PurchaseItemRepository purchaseItemRepository;

    @Autowired
    public PurchaseServiceImpl(PurchaseRepository purchaseRepository,
            PurchaseItemRepository purchaseItemRepository) {
        this.purchaseRepository = purchaseRepository;
        this.purchaseItemRepository = purchaseItemRepository;
    }

    @Override
    public PurchaseResponse registerPurchase(PurchaseRequest purchaseRequest) {
        String ownr = SecurityContextHolder.getContext().getAuthentication().getName();
        List<PurchaseItem> purchaseItems = purchaseRequest.getItems()
                .stream()
                .map(purchaseItem -> PurchaseMapper.mapToPurchaseItem(purchaseItem, ownr, purchaseRequest.getDate()))
                .collect(Collectors.toList());
        purchaseItemRepository.saveAll(purchaseItems);

        List<String> purchaseItemIds = purchaseItems.stream()
                .map(PurchaseItem::getCreationTimestamp)
                .collect(Collectors.toList());
        Purchase purchase = PurchaseMapper.mapToPurchase(purchaseRequest, ownr, purchaseItemIds);
        purchaseRepository.save(purchase);

        List<PurchaseItemResponse> purchaseItemResponses = purchaseItems.stream()
                .map(PurchaseMapper::mapToPurchaseItemResponse)
                .collect(Collectors.toList());
        return PurchaseMapper.mapToPurchaseResponse(purchase, purchaseItemResponses);
    }

    @Override
    public List<PurchaseResponse> fetchPurchases(Integer pageSize, SortingDirection sortDirection, Integer exclusiveBoundKey) {
        String ownr = SecurityContextHolder.getContext().getAuthentication().getName();
        List<Purchase> purchases = purchaseRepository.findPurchases(ownr, pageSize, sortDirection, exclusiveBoundKey);
        List<String> purchaseItemsCreationTimestamps = purchases.stream()
                .flatMap(purchase -> purchase.getItems().stream())
                .sorted()
                .collect(Collectors.toList());
        Map<String, PurchaseItem> purchaseItems = purchaseItemsCreationTimestamps.isEmpty() ?
                Collections.emptyMap() :
                purchaseItemRepository.getPurchaseItems(ownr, purchaseItemsCreationTimestamps)
                        .stream()
                        .collect(Collectors.toMap(PurchaseItem::getCreationTimestamp, item -> item));

        return purchases.stream()
                .map(purchase -> {
                    List<PurchaseItem> associatedPurchaseItems = purchaseItems.entrySet()
                            .stream()
                            .filter(entry -> purchase.getItems().contains(entry.getKey()))
                            .map(Map.Entry::getValue)
                            .collect(Collectors.toList());

                    List<PurchaseItemResponse> purchaseItemResponses = associatedPurchaseItems
                            .stream()
                            .map(PurchaseMapper::mapToPurchaseItemResponse)
                            .collect(Collectors.toList());

                    return PurchaseMapper.mapToPurchaseResponse(purchase, purchaseItemResponses);
                })
                .collect(Collectors.toList());
    }

    @Override
    public void deletePurchase(Integer creationTimestamp) {
        String ownr = SecurityContextHolder.getContext().getAuthentication().getName();
        Purchase purchase = purchaseRepository.getPurchase(ownr, creationTimestamp);

        purchaseRepository.delete(ownr, Integer.parseInt(purchase.getCreationTimestamp()));
        purchase.getItems()
                .forEach(itemCreationTimestamp -> purchaseItemRepository.delete(ownr, Integer.parseInt(itemCreationTimestamp)));
    }

    @Override
    public PurchaseResponse getPurchase(Integer creationTimestamp) {
        String ownr = SecurityContextHolder.getContext().getAuthentication().getName();
        Purchase purchase = purchaseRepository.getPurchase(ownr, creationTimestamp);
        List<PurchaseItemResponse> purchaseItemResponses = purchaseItemRepository.getPurchaseItems(ownr, purchase.getItems())
                .stream()
                .map(PurchaseMapper::mapToPurchaseItemResponse)
                .collect(Collectors.toList());
        return PurchaseMapper.mapToPurchaseResponse(purchase, purchaseItemResponses);
    }
}
