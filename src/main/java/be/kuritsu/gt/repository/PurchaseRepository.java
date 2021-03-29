package be.kuritsu.gt.repository;

import be.kuritsu.gt.persistence.model.Purchase;

public interface PurchaseRepository {

    void save(Purchase purchase);
}
