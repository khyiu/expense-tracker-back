package be.kuritsu.gt.converter;

import be.kuritsu.gt.model.Packaging;
import be.kuritsu.gt.model.Purchase;
import be.kuritsu.gt.model.PurchaseEntity;
import be.kuritsu.gt.model.PurchaseLocation;
import be.kuritsu.gt.model.UnitMeasurement;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

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
}
