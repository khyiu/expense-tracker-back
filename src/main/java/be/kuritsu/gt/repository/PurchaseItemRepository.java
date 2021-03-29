package be.kuritsu.gt.repository;

import be.kuritsu.gt.persistence.model.PurchaseItem;

import java.util.List;

public interface PurchaseItemRepository {

    void saveAll(List<PurchaseItem> purchaseItems);
}
