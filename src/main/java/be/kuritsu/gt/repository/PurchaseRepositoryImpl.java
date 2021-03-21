package be.kuritsu.gt.repository;

import be.kuritsu.gt.persistence.model.Purchase;
import be.kuritsu.gt.persistence.model.PurchaseItem;
import be.kuritsu.gt.persistence.model.PurchaseItemPackaging;
import be.kuritsu.gt.persistence.model.PurchaseItemPackagingMeasureUnit;
import be.kuritsu.gt.persistence.model.PurchaseShop;
import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ConditionalCheckFailedException;
import com.amazonaws.services.dynamodbv2.model.InternalServerErrorException;
import com.amazonaws.services.dynamodbv2.model.ItemCollectionSizeLimitExceededException;
import com.amazonaws.services.dynamodbv2.model.ProvisionedThroughputExceededException;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.RequestLimitExceededException;
import com.amazonaws.services.dynamodbv2.model.ResourceNotFoundException;
import com.amazonaws.services.dynamodbv2.model.TransactionConflictException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Repository
public class PurchaseRepositoryImpl implements PurchaseRepository {

    private static final String TABLE_PURCHASE_ITEM = "PurchaseItem";
    private static final String TABLE_PURCHASE = "Purchase";

    private final AmazonDynamoDB amazonDynamoDB;

    @Autowired
    public PurchaseRepositoryImpl(AmazonDynamoDB amazonDynamoDB) {
        this.amazonDynamoDB = amazonDynamoDB;
    }

    @Override
    public void savePurchase(Purchase purchase, List<PurchaseItem> purchaseItems) {
        try {
            purchaseItems.stream()
                    .map(PurchaseRepositoryImpl::createPurchaseItemPutItemRequest)
                    .forEach(amazonDynamoDB::putItem);

            PutItemRequest purchasePutItemRequest = createPurchasePutItemRequest(purchase);
            amazonDynamoDB.putItem(purchasePutItemRequest);
        } catch (Exception e) {
            handlePutItemErrors(e);
        }
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

    private static void handlePutItemErrors(Exception exception) {
        try {
            throw exception;
        } catch (ConditionalCheckFailedException ccfe) {
            System.out.println("Condition check specified in the operation failed, review and update the condition " +
                    "check before retrying. Error: " + ccfe.getErrorMessage());
        } catch (TransactionConflictException tce) {
            System.out.println("Operation was rejected because there is an ongoing transaction for the item, generally " +
                    "safe to retry with exponential back-off. Error: " + tce.getErrorMessage());
        } catch (ItemCollectionSizeLimitExceededException icslee) {
            System.out.println("An item collection is too large, you're using Local Secondary Index and exceeded " +
                    "size limit of items per partition key. Consider using Global Secondary Index instead. Error: " + icslee.getErrorMessage());
        } catch (Exception e) {
            handleCommonErrors(e);
        }
    }

    private static void handleCommonErrors(Exception exception) {
        try {
            throw exception;
        } catch (InternalServerErrorException isee) {
            System.out.println("Internal Server Error, generally safe to retry with exponential back-off. Error: " + isee.getErrorMessage());
        } catch (RequestLimitExceededException rlee) {
            System.out.println("Throughput exceeds the current throughput limit for your account, increase account level throughput before " +
                    "retrying. Error: " + rlee.getErrorMessage());
        } catch (ProvisionedThroughputExceededException ptee) {
            System.out.println("Request rate is too high. If you're using a custom retry strategy make sure to retry with exponential back-off. " +
                    "Otherwise consider reducing frequency of requests or increasing provisioned capacity for your table or secondary index. Error: " +
                    ptee.getErrorMessage());
        } catch (ResourceNotFoundException rnfe) {
            System.out.println("One of the tables was not found, verify table exists before retrying. Error: " + rnfe.getErrorMessage());
        } catch (AmazonServiceException ase) {
            System.out.println("An AmazonServiceException occurred, indicates that the request was correctly transmitted to the DynamoDB " +
                    "service, but for some reason, the service was not able to process it, and returned an error response instead. Investigate and " +
                    "configure retry strategy. Error type: " + ase.getErrorType() + ". Error message: " + ase.getErrorMessage());
        } catch (AmazonClientException ace) {
            System.out.println("An AmazonClientException occurred, indicates that the client was unable to get a response from DynamoDB " +
                    "service, or the client was unable to parse the response from the service. Investigate and configure retry strategy. " +
                    "Error: " + ace.getMessage());
        } catch (Exception e) {
            System.out.println("An exception occurred, investigate and configure retry strategy. Error: " + e.getMessage());
        }
    }
}
