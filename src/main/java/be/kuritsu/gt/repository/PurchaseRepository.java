package be.kuritsu.gt.repository;

import be.kuritsu.gt.model.PurchaseEntity;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;
import com.amazonaws.services.dynamodbv2.model.UpdateItemRequest;
import org.socialsignin.spring.data.dynamodb.repository.DynamoDBPagingAndSortingRepository;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.socialsignin.spring.data.dynamodb.repository.EnableScanCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.HashMap;
import java.util.Map;

@EnableScanCount
@EnableScan
public interface PurchaseRepository extends DynamoDBPagingAndSortingRepository<PurchaseEntity, String> {

    Page<PurchaseEntity> findByOwnr(String ownr, Pageable pageable);

    PurchaseEntity findByOwnrAndId(String ownr, String id);

    default void deletePurchase(AmazonDynamoDB amazonDynamoDB, PurchaseEntity purchase) {
        // Could not get deletion working using spring data repository, so, fallback to AWS DynamoDB SDK...
        Map<String, AttributeValue> deleteItemRequestKeys = new HashMap<>();
        deleteItemRequestKeys.put("id", new AttributeValue(purchase.getId()));
        deleteItemRequestKeys.put("purchaseDate", new AttributeValue(purchase.getPurchaseDate()));

        DeleteItemRequest deleteItemRequest = new DeleteItemRequest()
                .withTableName("Purchase")
                .withKey(deleteItemRequestKeys);
        amazonDynamoDB.deleteItem(deleteItemRequest);
    }

    default void updatePurchase(AmazonDynamoDB amazonDynamoDB, PurchaseEntity purchase) {
        Map<String, AttributeValue> key = new HashMap<>();
        key.put("id", new AttributeValue(purchase.getId()));
        key.put("purchaseDate", new AttributeValue(purchase.getPurchaseDate()));

        Map<String, String> expressionAttributeNames = new HashMap<>();
        expressionAttributeNames.put("#brand", "brand");
        expressionAttributeNames.put("#descriptionTags", "descriptionTags");
        expressionAttributeNames.put("#locationDescription", "locationDescription");
        expressionAttributeNames.put("#locationId", "locationId");
        expressionAttributeNames.put("#locationLocationTag", "locationLocationTag");
        expressionAttributeNames.put("#nbUnitPerPackage", "nbUnitPerPackage");
        expressionAttributeNames.put("#ownr", "ownr");
        expressionAttributeNames.put("#packageUnitMeasureQuantity", "packageUnitMeasureQuantity");
        expressionAttributeNames.put("#packageUnitMeasurementType", "packageUnitMeasurementType");
        expressionAttributeNames.put("#unitPrice", "unitPrice");

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
        expressionAttributeValues.put(":brand", new AttributeValue(purchase.getBrand()));
        expressionAttributeValues.put(":descriptionTags", new AttributeValue().withSS(purchase.getDescriptionTags()));
        expressionAttributeValues.put(":locationDescription", new AttributeValue(purchase.getLocationDescription()));
        expressionAttributeValues.put(":locationId", new AttributeValue(purchase.getLocationId()));
        expressionAttributeValues.put(":locationLocationTag", new AttributeValue(purchase.getLocationLocationTag()));
        expressionAttributeValues.put(":nbUnitPerPackage", new AttributeValue().withN(purchase.getNbUnitPerPackage().toString()));
        expressionAttributeValues.put(":ownr", new AttributeValue(username));
        expressionAttributeValues.put(":packageUnitMeasureQuantity", new AttributeValue().withN(purchase.getPackageUnitMeasureQuantity().toString()));
        expressionAttributeValues.put(":packageUnitMeasurementType", new AttributeValue(purchase.getPackageUnitMeasurementType()));
        expressionAttributeValues.put(":unitPrice", new AttributeValue().withN(purchase.getUnitPrice().toString()));

        UpdateItemRequest updateItemRequest = new UpdateItemRequest();
        updateItemRequest.setTableName("Purchase");
        updateItemRequest.setKey(key);
        String updateExpression = "SET " +
                "#brand = :brand, " +
                "#descriptionTags = :descriptionTags, " +
                "#locationDescription = :locationDescription, " +
                "#locationId = :locationId, " +
                "#locationLocationTag = :locationLocationTag, " +
                "#nbUnitPerPackage = :nbUnitPerPackage, " +
                "#ownr = :ownr, " +
                "#packageUnitMeasureQuantity = :packageUnitMeasureQuantity, " +
                "#packageUnitMeasurementType = :packageUnitMeasurementType, " +
                "#unitPrice = :unitPrice";
        updateItemRequest.setUpdateExpression(updateExpression);
        updateItemRequest.setExpressionAttributeNames(expressionAttributeNames);
        updateItemRequest.setExpressionAttributeValues(expressionAttributeValues);
        amazonDynamoDB.updateItem(updateItemRequest);
    }
}