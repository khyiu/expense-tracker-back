package be.kuritsu.gt.repository;

import be.kuritsu.gt.persistence.model.Purchase;
import be.kuritsu.gt.persistence.model.PurchaseShop;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.Map;

@Repository
public class PurchaseRepositoryImpl implements PurchaseRepository {

    private static final String TABLE_PURCHASE = "Purchase";

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
        item.put("creationTimestamp", new AttributeValue().withN(purchase.getCreationTimestamp()));
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
}
