package be.kuritsu.gt.service;

import be.kuritsu.gt.converter.PurchaseConverter;
import be.kuritsu.gt.exception.PurchaseNotFoundException;
import be.kuritsu.gt.model.Purchase;
import be.kuritsu.gt.model.PurchaseEntity;
import be.kuritsu.gt.model.PurchaseRequest;
import be.kuritsu.gt.model.PurchasesResponse;
import be.kuritsu.gt.repository.PurchaseRepository;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;
import com.amazonaws.services.dynamodbv2.model.DeleteItemResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
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
        String locationId = purchaseRequest.getLocation().getId();
        if (locationId == null) {
            locationId = UUID.randomUUID().toString();
        }

        PurchaseEntity purchaseEntity = PurchaseEntity.builder()
                .purchaseDate(purchaseRequest.getDate().format(DateTimeFormatter.ISO_DATE))
                .brand(purchaseRequest.getBrand())
                .descriptionTags(new LinkedHashSet<>(purchaseRequest.getDescriptionTags()))
                .unitPrice(purchaseRequest.getUnitPrice())
                .locationId(locationId)
                .locationDescription(purchaseRequest.getLocation().getDescription())
                .locationLocationTag(purchaseRequest.getLocation().getLocationTag())
                .nbUnitPerPackage(purchaseRequest.getPackaging().getNbUnitPerPackage())
                .packageUnitMeasureQuantity(purchaseRequest.getPackaging().getUnitMeasurements().getQuantity())
                .packageUnitMeasurementType(purchaseRequest.getPackaging().getUnitMeasurements().getType().getValue())
                .ownr(SecurityContextHolder.getContext().getAuthentication().getName())
                .testData(true)
                .build();

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
}
