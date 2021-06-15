package be.kuritsu.gt.repository;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.CheckForNull;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import be.kuritsu.gt.persistence.model.Purchase;
import be.kuritsu.gt.persistence.model.PurchaseShop;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;

@Repository
public class PurchaseRepositoryImpl implements PurchaseRepository {

    public static final String TABLE_PURCHASE = "Purchase";
    private static final String ATTRIBUTE_CREATION_TIMESTAMP = "creationTimestamp";

    private final AmazonDynamoDB amazonDynamoDB;

    @Autowired
    public PurchaseRepositoryImpl(AmazonDynamoDB amazonDynamoDB) {
        this.amazonDynamoDB = amazonDynamoDB;
    }

    @Override
    public void save(Purchase purchase) {
        PutItemRequest purchasePutItemRequest = createPurchasePutItemRequest(purchase);
        amazonDynamoDB.putItem(purchasePutItemRequest);
    }

    private static PutItemRequest createPurchasePutItemRequest(Purchase purchase) {
        PutItemRequest putItemRequest = new PutItemRequest();
        putItemRequest.setTableName(TABLE_PURCHASE);
        putItemRequest.setItem(getPurchaseAttributeValue(purchase));
        return putItemRequest;
    }

    private static Map<String, AttributeValue> getPurchaseAttributeValue(Purchase purchase) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put("ownr", new AttributeValue(purchase.getOwnr()));
        item.put(ATTRIBUTE_CREATION_TIMESTAMP, new AttributeValue().withN(purchase.getCreationTimestamp()));
        item.put("shop", new AttributeValue().withM(getShopAttributeValue(purchase.getShop())));
        item.put("amount", new AttributeValue().withN(purchase.getAmount().toString()));
        item.put("items", new AttributeValue().withSS(purchase.getItems()));
        return item;
    }

    private static Map<String, AttributeValue> getShopAttributeValue(PurchaseShop purchaseShop) {
        Map<String, AttributeValue> attributeValues = new HashMap<>();
        attributeValues.put("name", new AttributeValue(purchaseShop.getName()));
        attributeValues.put("location", new AttributeValue(purchaseShop.getLocation()));
        return attributeValues;
    }

    @Override
    public List<Purchase> findPurchases(String owrn, int pageSize, SortingDirection sortingDirection, @CheckForNull Integer exclusiveBoundKey) {
        QueryRequest queryRequest = createFetchPurchasesQueryRequest(owrn, pageSize, sortingDirection, exclusiveBoundKey);
        QueryResult queryResult = amazonDynamoDB.query(queryRequest);
        return queryResult.getItems()
                .stream()
                .map(PurchaseRepositoryImpl::toPurchase)
                .collect(Collectors.toList());
    }

    private static QueryRequest createFetchPurchasesQueryRequest(String ownr, int pageSize, SortingDirection sortingDirection, @CheckForNull Integer exclusiveBoundKey) {
        QueryRequest queryRequest = new QueryRequest();
        queryRequest.setTableName(TABLE_PURCHASE);
        queryRequest.setKeyConditionExpression("#partitionAttributeName = :partitionAttributeValue");
        queryRequest.setConsistentRead(false);
        queryRequest.setScanIndexForward(sortingDirection == SortingDirection.ASC);
        queryRequest.setLimit(pageSize);

        if (exclusiveBoundKey != null) {
            queryRequest.setExclusiveStartKey(getExclusiveStartKey(ownr, exclusiveBoundKey));
        }

        queryRequest.setExpressionAttributeNames(getExpressionAttributeNames());
        queryRequest.setExpressionAttributeValues(getExpressionAttributeValues(ownr));
        return queryRequest;
    }

    private static Map<String, String> getExpressionAttributeNames() {
        Map<String, String> expressionAttributeNames = new HashMap<>();
        expressionAttributeNames.put("#partitionAttributeName", "ownr");
        return expressionAttributeNames;
    }

    private static Map<String, AttributeValue> getExpressionAttributeValues(String ownr) {
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":partitionAttributeValue", new AttributeValue(ownr));
        return expressionAttributeValues;
    }

    private static Map<String, AttributeValue> getExclusiveStartKey(String ownr, Integer exclusiveBoundKey) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("ownr", new AttributeValue(ownr));
        key.put(ATTRIBUTE_CREATION_TIMESTAMP, new AttributeValue().withN(exclusiveBoundKey.toString()));
        return key;
    }

    private static Purchase toPurchase(Map<String, AttributeValue> purchaseAttributeValues) {
        return new Purchase()
                .amount(BigDecimal.valueOf(Double.parseDouble(purchaseAttributeValues.get("amount").getN())))
                .creationTimestamp(purchaseAttributeValues.get(ATTRIBUTE_CREATION_TIMESTAMP).getN())
                .shop(toShop(purchaseAttributeValues.get("shop").getM()))
                .items(purchaseAttributeValues.get("items").getSS());
    }

    private static PurchaseShop toShop(Map<String, AttributeValue> shopAttributeValues) {
        return new PurchaseShop()
                .name(shopAttributeValues.get("name").getS())
                .location(shopAttributeValues.get("location").getS());
    }
}
