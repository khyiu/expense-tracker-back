package be.kuritsu.gt.integration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import be.kuritsu.gt.model.Packaging;
import be.kuritsu.gt.model.PurchaseItem;
import be.kuritsu.gt.model.PurchaseRequest;
import be.kuritsu.gt.model.Shop;
import be.kuritsu.gt.model.UnitMeasurement;
import be.kuritsu.gt.persistence.model.Purchase;

public class TestDataFactory {

    private TestDataFactory() {
    }

    public static PurchaseRequest getDefaultPurchaseRequest() {
        return new PurchaseRequest()
                .date(LocalDate.of(2020, 12, 14))
                .shop(new Shop()
                        .name("Provigo")
                        .location("Montreal"))
                .amount(BigDecimal.valueOf(27.15))
                .items(Collections.singletonList(
                        new PurchaseItem()
                                .brand("Ben & Jerry's")
                                .productTags(Collections.singletonList("ice cream"))
                                .unitPrice(BigDecimal.valueOf(9.05))
                                .nbUnit(3)
                                .packaging(
                                        new Packaging()
                                                .nbUnitPerPackage(1)
                                                .unitMeasurements(
                                                        new UnitMeasurement()
                                                                .quantity(465)
                                                                .type(UnitMeasurement.TypeEnum.ML)
                                                )
                                )
                ));
    }
}
