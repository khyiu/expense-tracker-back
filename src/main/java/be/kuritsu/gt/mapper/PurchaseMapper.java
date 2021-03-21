package be.kuritsu.gt.mapper;

import be.kuritsu.gt.model.Packaging;
import be.kuritsu.gt.model.PurchaseItem;
import be.kuritsu.gt.model.PurchaseRequest;
import be.kuritsu.gt.model.Shop;
import be.kuritsu.gt.persistence.model.Purchase;
import be.kuritsu.gt.persistence.model.PurchaseItemPackaging;
import be.kuritsu.gt.persistence.model.PurchaseItemPackagingMeasureUnit;
import be.kuritsu.gt.persistence.model.PurchaseShop;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;

public class PurchaseMapper {

    private PurchaseMapper() {

    }

    public static be.kuritsu.gt.persistence.model.PurchaseItem mapToPurchaseItem(PurchaseItem purchaseItem, String ownr, LocalDate purchaseDate) {
        return new be.kuritsu.gt.persistence.model.PurchaseItem()
                .ownr(ownr)
                .creationTimestamp(getTimestamp(purchaseDate))
                .brand(purchaseItem.getBrand())
                .descriptionTags(purchaseItem.getProductTags())
                .unitPrice(purchaseItem.getUnitPrice())
                .nbUnit(purchaseItem.getNbUnit())
                .packaging(mapToPackaging(purchaseItem.getPackaging()));
    }

    public static Purchase mapToPurchase(PurchaseRequest purchaseRequest, String ownr, List<String> itemIds) {
        return new Purchase()
                .ownr(ownr)
                .creationTimestamp(getTimestamp(purchaseRequest.getDate()))
                .shop(mapToPurchaseShop(purchaseRequest.getShop()))
                .amount(purchaseRequest.getAmount())
                .items(itemIds);
    }

    private static String getTimestamp(LocalDate purchaseDate) {
        LocalDateTime timestamp = LocalDateTime.of(purchaseDate, LocalTime.now());
        return Long.toString(timestamp.toEpochSecond(ZoneOffset.UTC));
    }

    private static PurchaseItemPackaging mapToPackaging(Packaging packaging) {
        return new PurchaseItemPackaging()
                .nbUnitPerPackage(packaging.getNbUnitPerPackage())
                .measureUnit(
                        new PurchaseItemPackagingMeasureUnit()
                                .type(PurchaseItemPackagingMeasureUnit.TypeEnum.valueOf(packaging.getUnitMeasurements().getType().name()))
                                .quantity(BigDecimal.valueOf(packaging.getUnitMeasurements().getQuantity()))
                );
    }

    private static PurchaseShop mapToPurchaseShop(Shop shop) {
        return new PurchaseShop()
                .name(shop.getName())
                .location(shop.getLocation());
    }
}
