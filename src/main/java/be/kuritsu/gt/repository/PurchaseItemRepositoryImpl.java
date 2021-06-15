package be.kuritsu.gt.repository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import be.kuritsu.gt.persistence.model.PurchaseItem;
import be.kuritsu.gt.persistence.model.PurchaseItemPackaging;
import be.kuritsu.gt.persistence.model.PurchaseItemPackagingMeasureUnit;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;

@Repository
public class PurchaseItemRepositoryImpl implements PurchaseItemRepository {

    public static final String TABLE_PURCHASE_ITEM = "PurchaseItem";
    private static final String ATTRIBUTE_CREATION_TIMESTAMP = "creationTimestamp";
    private static final String PARTITION_KEY_NAME = "#partitionKeyName";
    private static final String PARTITION_KEY_VALUE = ":partitionKeyValue";
    private static final String SORTING_KEY_NAME = "#sortingKeyName";
    private static final String SORTING_KEY_RANGE_START_VALUE = ":sortingKeyRangeStartValue";
    private static final String SORTING_KEY_RANGE_END_VALUE = ":sortingKeyRangeEndValue";

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

    @Override
    public List<PurchaseItem> getPurchaseItems(String ownr, List<String> creationTimestamps) {
        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setTableName(TABLE_PURCHASE_ITEM);
        String keyConditionExpression = PARTITION_KEY_NAME + " = " + PARTITION_KEY_VALUE +
                " And " + SORTING_KEY_NAME + " BETWEEN " + SORTING_KEY_RANGE_START_VALUE + " AND " + SORTING_KEY_RANGE_END_VALUE;
        queryRequest.setKeyConditionExpression(keyConditionExpression);
        queryRequest.setConsistentRead(false);
        queryRequest.setScanIndexForward(true);
        queryRequest.setExpressionAttributeNames(getPurchaseItemsAttributeValue());
        queryRequest.setExpressionAttributeValues(getExpressionAttributeValues(ownr, creationTimestamps.get(0), creationTimestamps.get(creationTimestamps.size() - 1)));
        QueryResult queryResult = amazonDynamoDB.query(queryRequest);
        return queryResult.getItems()
                .stream()
                .map(PurchaseItemRepositoryImpl::toPurchaseItem)
                .collect(Collectors.toList());
    }

    private static Map<String, String> getPurchaseItemsAttributeValue() {
        Map<String, String> expressionAttributeNames = new HashMap<>();
        expressionAttributeNames.put(PARTITION_KEY_NAME, "ownr");
        expressionAttributeNames.put(SORTING_KEY_NAME, ATTRIBUTE_CREATION_TIMESTAMP);
        return expressionAttributeNames;
    }

    private static Map<String, AttributeValue> getExpressionAttributeValues(String ownr, String sortingRangeStart, String sortingRangeEnd) {
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(PARTITION_KEY_VALUE, new AttributeValue(ownr));
        expressionAttributeValues.put(SORTING_KEY_RANGE_START_VALUE, new AttributeValue(sortingRangeStart));
        expressionAttributeValues.put(SORTING_KEY_RANGE_END_VALUE, new AttributeValue(sortingRangeEnd));
        return expressionAttributeValues;
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
        item.put(ATTRIBUTE_CREATION_TIMESTAMP, new AttributeValue().withN(purchaseItem.getCreationTimestamp()));
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

    private static PurchaseItem toPurchaseItem(Map<String, AttributeValue> purchaseItemAttributeValues) {
        return new PurchaseItem()
                .creationTimestamp(purchaseItemAttributeValues.get(ATTRIBUTE_CREATION_TIMESTAMP).getS())
                .brand(purchaseItemAttributeValues.get("brand").getS())
                .descriptionTags(purchaseItemAttributeValues.get("descriptionTags").getSS())
                .unitPrice(BigDecimal.valueOf(Double.parseDouble(purchaseItemAttributeValues.get("unitPrice").getN())))
                .nbUnit(Integer.parseInt(purchaseItemAttributeValues.get("nbUnit").getN()))
                .packaging(toPurchaseItemPackaging(purchaseItemAttributeValues.get("packaging").getM()));
    }

    private static PurchaseItemPackaging toPurchaseItemPackaging(Map<String, AttributeValue> packagingAttributeValues) {
        Map<String, AttributeValue> measureUnitAttributeValues = packagingAttributeValues.get("measureUnit").getM();
        return new PurchaseItemPackaging()
                .nbUnitPerPackage(Integer.parseInt(packagingAttributeValues.get("nbUnitPerPackage").getN()))
                .measureUnit(
                        new PurchaseItemPackagingMeasureUnit()
                                .quantity(BigDecimal.valueOf(Integer.parseInt(measureUnitAttributeValues.get("quantity").getN())))
                                .type(PurchaseItemPackagingMeasureUnit.TypeEnum.fromValue(measureUnitAttributeValues.get("type").getS()))
                );
    }
}
