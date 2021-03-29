package be.kuritsu.gt.repository;

import be.kuritsu.gt.persistence.model.PurchaseItem;
import be.kuritsu.gt.persistence.model.PurchaseItemPackaging;
import be.kuritsu.gt.persistence.model.PurchaseItemPackagingMeasureUnit;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class PurchaseItemRepositoryImpl implements PurchaseItemRepository{

    private static final String TABLE_PURCHASE_ITEM = "PurchaseItem";

    private final AmazonDynamoDB amazonDynamoDB;

    @Autowired
    public PurchaseItemRepositoryImpl(AmazonDynamoDB amazonDynamoDB) {
        this.amazonDynamoDB = amazonDynamoDB;
    }

    @Override
    public void saveAll(List<PurchaseItem> purchaseItems) {
        purchaseItems.stream()
                .map(PurchaseItemRepositoryImpl::createPurchaseItemPutItemRequest)
                .forEach(amazonDynamoDB::putItem);
    }

    private static PutItemRequest createPurchaseItemPutItemRequest(PurchaseItem purchaseItem) {
        PutItemRequest putItemRequest = new PutItemRequest();
        putItemRequest.setTableName(TABLE_PURCHASE_ITEM);
        putItemRequest.setItem(getPurchaseItemAttributeValue(purchaseItem));
        return putItemRequest;
    }

    private static Map<String, AttributeValue> getPurchaseItemAttributeValue(PurchaseItem purchaseItem) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("ownr", new AttributeValue(purchaseItem.getOwnr()));
        item.put("creationTimestamp", new AttributeValue(purchaseItem.getCreationTimestamp()));
        item.put("brand", new AttributeValue(purchaseItem.getBrand()));
        item.put("descriptionTags", new AttributeValue().withSS(purchaseItem.getDescriptionTags()));
        item.put("unitPrice", new AttributeValue().withN(purchaseItem.getUnitPrice().toString()));
        item.put("nbUnit", new AttributeValue().withN(purchaseItem.getNbUnit().toString()));
        item.put("packaging", new AttributeValue().withM(getPackagingAttributeValue(purchaseItem.getPackaging())));
        return item;
    }

    private static Map<String, AttributeValue> getPackagingAttributeValue(PurchaseItemPackaging purchaseItemPackaging) {
        Map<String, AttributeValue> attributeValues = new HashMap<>();
        attributeValues.put("nbUnitPerPackage", new AttributeValue().withN(purchaseItemPackaging.getNbUnitPerPackage().toString()));
        attributeValues.put("measureUnit", new AttributeValue().withM(getMeasurementAttributeValue(purchaseItemPackaging.getMeasureUnit())));
        return attributeValues;
    }

    private static Map<String, AttributeValue> getMeasurementAttributeValue(PurchaseItemPackagingMeasureUnit measureUnit) {
        Map<String, AttributeValue> attributeValues = new HashMap<>();
        attributeValues.put("type", new AttributeValue(measureUnit.getType().getValue()));
        attributeValues.put("quantity", new AttributeValue().withN(measureUnit.getQuantity().toString()));
        return attributeValues;
    }
}
