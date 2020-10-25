package be.kuritsu.gt.service;

import be.kuritsu.gt.converter.PurchaseConverter;
import be.kuritsu.gt.exception.PurchaseNotFoundException;
import be.kuritsu.gt.model.Purchase;
import be.kuritsu.gt.model.PurchaseEntity;
import be.kuritsu.gt.model.PurchaseRequest;
import be.kuritsu.gt.model.PurchasesResponse;
import be.kuritsu.gt.repository.PurchaseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PurchaseServiceImpl implements PurchaseService {

    private final PurchaseRepository purchaseRepository;

    @Autowired
    public PurchaseServiceImpl(PurchaseRepository purchaseRepository) {
        this.purchaseRepository = purchaseRepository;
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
        return null;
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

        purchaseRepository.delete(purchase);
    }
}
