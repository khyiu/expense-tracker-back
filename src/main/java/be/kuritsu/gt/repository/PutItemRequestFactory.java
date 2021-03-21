package be.kuritsu.gt.repository;

import be.kuritsu.gt.model.PurchaseItem;
import be.kuritsu.gt.persistence.model.Purchase;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PutItemRequestFactory {

    private PutItemRequestFactory() {
    }

    public static PutItemRequest getPutItemRequest(String ownr,
                                                   long creationTimestamp,
                                                   PurchaseItem purchaseItem,
                                                   String tableName) {
        PutItemRequest putItemRequest = new PutItemRequest();
        putItemRequest.setTableName(tableName);
        putItemRequest.setItem(getItem(ownr,
                creationTimestamp,
                purchaseItem));
        return putItemRequest;
    }

    private static Map<String, AttributeValue> getItem(String ownr,
                                                       long creationTimestamp,
                                                       PurchaseItem purchaseItem) {
        Map<String, AttributeValue> purchaseItemAttributeValues = new HashMap<>();
        purchaseItemAttributeValues.put("ownr", new AttributeValue(ownr));
        purchaseItemAttributeValues.put("creationTimestamp", new AttributeValue(Long.toString(creationTimestamp)));
        purchaseItemAttributeValues.put("brand", new AttributeValue(purchaseItem.getBrand()));
        purchaseItemAttributeValues.put("descriptionTags", new AttributeValue().withSS(purchaseItem.getProductTags().toArray(new String[0])));
        purchaseItemAttributeValues.put("unitPrice", new AttributeValue().withN(purchaseItem.getUnitPrice().toString()));
        purchaseItemAttributeValues.put("nbUnit", new AttributeValue().withN(purchaseItem.getNbUnit().toString()));

        Map<String, AttributeValue> measureUnitAttributeValues = new HashMap<>();
        measureUnitAttributeValues.put("type", new AttributeValue(purchaseItem.getPackaging().getUnitMeasurements().getType().name()));
        measureUnitAttributeValues.put("quantity", new AttributeValue().withN(purchaseItem.getPackaging().getUnitMeasurements().getQuantity().toString()));

        Map<String, AttributeValue> packagingAttributeValues = new HashMap<>();
        packagingAttributeValues.put("nbUnitPerPackage", new AttributeValue().withN(purchaseItem.getNbUnit().toString()));
        packagingAttributeValues.put("measureUnit", new AttributeValue().withM(measureUnitAttributeValues));
        purchaseItemAttributeValues.put("packagingAttributeValues", new AttributeValue().withM(packagingAttributeValues));
        return purchaseItemAttributeValues;
    }

    public static PutItemRequest getPutItemRequest(String ownr,
                                                   long creationTimestamp,
                                                   Purchase purchase,
                                                   List<String> purchaseItemsCreationTimestamps,
                                                   String tableName) {
        PutItemRequest putItemRequest = new PutItemRequest();
        putItemRequest.setTableName(tableName);
        putItemRequest.setItem(getItem(ownr, creationTimestamp, purchase, purchaseItemsCreationTimestamps));
        return putItemRequest;
    }

    private static Map<String, AttributeValue> getItem(String ownr,
                                                       long creationTimestamp,
                                                       Purchase purchase,
                                                       List<String> purchaseItemsCreationTimestamps) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("ownr", new AttributeValue(ownr));
        item.put("creationTimestamp", new AttributeValue().withN(Long.toString(creationTimestamp)));

        Map<String, AttributeValue> shopAttributeValues = new HashMap<>();
        shopAttributeValues.put("name", new AttributeValue(purchase.getShop().getName()));
        shopAttributeValues.put("location", new AttributeValue(purchase.getShop().getLocation()));
        item.put("shop", new AttributeValue().withM(shopAttributeValues));
        item.put("amount", new AttributeValue().withN(purchase.getAmount().toString()));
        item.put("items", new AttributeValue().withSS(purchaseItemsCreationTimestamps));
        return item;
    }
}
