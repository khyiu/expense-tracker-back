package be.kuritsu.gt.service;

import be.kuritsu.gt.mapper.PurchaseMapper;
import be.kuritsu.gt.model.PurchaseRequest;
import be.kuritsu.gt.persistence.model.Purchase;
import be.kuritsu.gt.persistence.model.PurchaseItem;
import be.kuritsu.gt.repository.PurchaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseRepository purchaseRepository;

    @Autowired
    public PurchaseServiceImpl(PurchaseRepository purchaseRepository) {
        this.purchaseRepository = purchaseRepository;
    }

    @Override
    public void registerPurchase(PurchaseRequest purchaseRequest) {
        String ownr = SecurityContextHolder.getContext().getAuthentication().getName();
        List<PurchaseItem> purchaseItems = purchaseRequest.getItems()
                .stream()
                .map(purchaseItem -> PurchaseMapper.mapToPurchaseItem(purchaseItem, ownr, purchaseRequest.getDate()))
                .collect(Collectors.toList());
        List<String> purchaseItemIds = purchaseItems.stream()
                .map(PurchaseItem::getCreationTimestamp)
                .collect(Collectors.toList());
        Purchase purchase = PurchaseMapper.mapToPurchase(purchaseRequest, ownr, purchaseItemIds);
        purchaseRepository.savePurchase(purchase, purchaseItems);
    }
}
