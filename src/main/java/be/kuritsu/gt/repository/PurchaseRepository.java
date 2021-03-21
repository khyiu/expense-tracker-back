package be.kuritsu.gt.repository;

import be.kuritsu.gt.persistence.model.Purchase;
import be.kuritsu.gt.persistence.model.PurchaseItem;

import java.util.List;

public interface PurchaseRepository {

    void savePurchase(Purchase purchase, List<PurchaseItem> purchaseItems);
}
