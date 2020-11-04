package be.kuritsu.gt.service;

import be.kuritsu.gt.converter.PurchaseConverter;
import be.kuritsu.gt.exception.PurchaseNotFoundException;
import be.kuritsu.gt.model.Purchase;
import be.kuritsu.gt.model.PurchaseEntity;
import be.kuritsu.gt.model.PurchaseLocation;
import be.kuritsu.gt.model.PurchaseRequest;
import be.kuritsu.gt.model.PurchasesResponse;
import be.kuritsu.gt.repository.PurchaseRepository;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseRepository purchaseRepository;
    private final AmazonDynamoDB amazonDynamoDB;

    @Autowired
    public PurchaseServiceImpl(PurchaseRepository purchaseRepository,
                               AmazonDynamoDB amazonDynamoDB) {
        this.purchaseRepository = purchaseRepository;
        this.amazonDynamoDB = amazonDynamoDB;
    }

    @Override
    public Purchase registerPurchase(PurchaseRequest purchaseRequest) {
        PurchaseEntity purchaseEntity = PurchaseConverter.toPurchaseEntity(purchaseRequest);
        purchaseRepository.save(purchaseEntity);
        return PurchaseConverter.purchaseEntityToPurchase(purchaseEntity);
    }

    @Override
    public PurchasesResponse getPurchases(int pageNumber, int pageSize) {
        Pageable page = PageRequest.of(pageNumber, pageSize, Sort.Direction.DESC, "purchaseDate");
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Page<PurchaseEntity> purchaseEntities = purchaseRepository.findByOwnr(username, page);
        List<Purchase> purchases = purchaseEntities.getContent()
                .stream()
                .map(PurchaseConverter::purchaseEntityToPurchase)
                .collect(Collectors.toList());
        return new PurchasesResponse()
                .totalElements((int) purchaseEntities.getTotalElements())
                .number(purchaseEntities.getNumber())
                .totalPages(purchaseEntities.getTotalPages())
                .content(purchases);
    }

    @Override
    public void deletePurchase(String purchaseId) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        PurchaseEntity purchase = purchaseRepository.findByOwnrAndId(username, purchaseId);

        if (purchase == null) {
            throw new PurchaseNotFoundException();
        }

        purchaseRepository.deletePurchase(amazonDynamoDB, purchase);
    }

    @Override
    public Purchase updatePurchase(String purchaseId, PurchaseRequest purchaseRequest) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        PurchaseEntity currentPurchase = purchaseRepository.findByOwnrAndId(username, purchaseId);

        if (currentPurchase == null) {
            throw new PurchaseNotFoundException();
        }

        PurchaseEntity purchaseWithUpdatedValues = PurchaseConverter.toPurchaseEntity(purchaseRequest);
        purchaseWithUpdatedValues.setId(purchaseId);
        purchaseRepository.updatePurchase(amazonDynamoDB, purchaseWithUpdatedValues);

        return PurchaseConverter.purchaseEntityToPurchase(purchaseRepository.findByOwnrAndId(username, purchaseId));
    }

    @Override
    public List<PurchaseLocation> getPurchaseLocations() {
        Set<PurchaseLocation> purchaseLocations = purchaseRepository.getPurchaseLocations(amazonDynamoDB);
        return purchaseLocations.stream()
                .sorted(Comparator.comparing(purchaseLocation -> purchaseLocation.getDescription() + purchaseLocation.getLocationTag()))
                .collect(Collectors.toList());
    }
}
