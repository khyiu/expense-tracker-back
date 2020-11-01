package be.kuritsu.gt.converter;

import be.kuritsu.gt.model.Packaging;
import be.kuritsu.gt.model.Purchase;
import be.kuritsu.gt.model.PurchaseEntity;
import be.kuritsu.gt.model.PurchaseLocation;
import be.kuritsu.gt.model.PurchaseRequest;
import be.kuritsu.gt.model.UnitMeasurement;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.UUID;

public class PurchaseConverter {

    private PurchaseConverter() {

    }

    public static Purchase purchaseEntityToPurchase(PurchaseEntity purchaseEntity) {
        return new Purchase()
                .id(purchaseEntity.getId())
                .date(LocalDate.parse(purchaseEntity.getPurchaseDate(), DateTimeFormatter.ISO_DATE))
                .brand(purchaseEntity.getBrand())
                .descriptionTags(new ArrayList<>(purchaseEntity.getDescriptionTags()))
                .unitPrice(purchaseEntity.getUnitPrice())
                .location(
                        toPurchaseLocation(purchaseEntity.getLocationId(),
                                purchaseEntity.getLocationDescription(),
                                purchaseEntity.getLocationLocationTag()
                        )
                ).packaging(toPackaging(purchaseEntity.getNbUnitPerPackage(),
                        purchaseEntity.getPackageUnitMeasurementType(),
                        purchaseEntity.getPackageUnitMeasureQuantity()));
    }

    private static PurchaseLocation toPurchaseLocation(String locationId,
                                                       String locationDescription,
                                                       String locationTag) {
        return new PurchaseLocation()
                .id(locationId)
                .description(locationDescription)
                .locationTag(locationTag);
    }

    private static Packaging toPackaging(Integer nbUnitPerPackage,
                                         String packageUnitMeasurementType,
                                         Integer packageUnitMeasureQuantity) {
        return new Packaging()
                .nbUnitPerPackage(nbUnitPerPackage)
                .unitMeasurements(new UnitMeasurement()
                        .type(UnitMeasurement.TypeEnum.fromValue(packageUnitMeasurementType))
                        .quantity(packageUnitMeasureQuantity)
                );
    }

    public static PurchaseEntity toPurchaseEntity(PurchaseRequest purchaseRequest) {
        String locationId = purchaseRequest.getLocation().getId();

        if (locationId == null) {
            locationId = UUID.randomUUID().toString();
        }

        return PurchaseEntity.builder()
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
    }
}
