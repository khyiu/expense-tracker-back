package be.kuritsu.gt.service;

import be.kuritsu.gt.model.Purchase;
import be.kuritsu.gt.model.PurchaseEntity;
import be.kuritsu.gt.model.PurchaseRequest;
import be.kuritsu.gt.repository.PurchaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseRepository purchaseRepository;

    @Autowired
    public PurchaseServiceImpl(PurchaseRepository purchaseRepository) {
        this.purchaseRepository = purchaseRepository;
    }

    @Override
    public Purchase registerPurchase(PurchaseRequest purchaseRequest) {
        PurchaseEntity purchaseEntity = PurchaseEntity.builder()
                .purchaseDate(purchaseRequest.getDate().format(DateTimeFormatter.ISO_DATE))
                .brand(purchaseRequest.getBrand())
                .testData(true)
                .build();

        purchaseRepository.save(purchaseEntity);
        return null;
    }
}
