package be.kuritsu.gt.repository;

import be.kuritsu.gt.persistence.model.PurchaseItem;

import java.util.List;

public interface PurchaseItemRepository {

    void saveAll(List<PurchaseItem> purchaseItems);

    List<PurchaseItem> getPurchaseItems(String ownr, List<String> creationTimestamps);

    void delete(String ownr, Integer creationTimestamp);
}
