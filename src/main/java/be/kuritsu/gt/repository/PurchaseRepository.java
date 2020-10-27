package be.kuritsu.gt.repository;

import be.kuritsu.gt.model.PurchaseEntity;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;
import org.socialsignin.spring.data.dynamodb.repository.DynamoDBPagingAndSortingRepository;
import org.socialsignin.spring.data.dynamodb.repository.EnableScan;
import org.socialsignin.spring.data.dynamodb.repository.EnableScanCount;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

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
}