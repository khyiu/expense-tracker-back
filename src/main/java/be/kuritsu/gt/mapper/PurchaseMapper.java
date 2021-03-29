package be.kuritsu.gt.mapper;

import be.kuritsu.gt.model.Packaging;
import be.kuritsu.gt.model.PurchaseItem;
import be.kuritsu.gt.model.PurchaseRequest;
import be.kuritsu.gt.model.PurchaseResponse;
import be.kuritsu.gt.model.Shop;
import be.kuritsu.gt.model.UnitMeasurement;
import be.kuritsu.gt.persistence.model.Purchase;
import be.kuritsu.gt.persistence.model.PurchaseItemPackaging;
import be.kuritsu.gt.persistence.model.PurchaseItemPackagingMeasureUnit;
import be.kuritsu.gt.persistence.model.PurchaseShop;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Date;
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

    public static PurchaseItem mapToPurchaseItemResponse(be.kuritsu.gt.persistence.model.PurchaseItem purchaseItem) {
        PurchaseItemPackaging packaging = purchaseItem.getPackaging();
        UnitMeasurement unitMeasurement = new UnitMeasurement()
                .type(UnitMeasurement.TypeEnum.valueOf(packaging.getMeasureUnit().getType().name()))
                .quantity(packaging.getMeasureUnit().getQuantity().intValue());

        return new PurchaseItem()
                .brand(purchaseItem.getBrand())
                .productTags(purchaseItem.getDescriptionTags())
                .unitPrice(purchaseItem.getUnitPrice())
                .nbUnit(purchaseItem.getNbUnit())
                .packaging(new Packaging()
                        .nbUnitPerPackage(packaging.getNbUnitPerPackage())
                        .unitMeasurements(unitMeasurement));
    }

    public static PurchaseResponse mapToPurchaseResponse(Purchase purchase, List<PurchaseItem> purchaseItems) {
        return new PurchaseResponse()
                .date(getPurchaseDate(purchase.getCreationTimestamp()))
                .shop(new Shop()
                        .name(purchase.getShop().getName())
                        .location(purchase.getShop().getLocation()))
                .amount(purchase.getAmount())
                .items(purchaseItems);
    }

    private static String getTimestamp(LocalDate purchaseDate) {
        LocalDateTime timestamp = LocalDateTime.of(purchaseDate, LocalTime.now());
        return Long.toString(timestamp.toEpochSecond(ZoneOffset.UTC));
    }

    private static LocalDate getPurchaseDate(String purchaseTimestamp) {
        Date date = new Date();
        date.setTime(Long.parseLong(purchaseTimestamp) * 1000);
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
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
