package be.kuritsu.gt.integration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;

public class IntegrationTestUtils {
    private IntegrationTestUtils() {
    }

    public static void cleanupTestData(AmazonDynamoDB amazonDynamoDB, String tableName, String owner) {
        ScanRequest scanRequest = new ScanRequest();
        scanRequest.setTableName(tableName);
        String filterExpression = "#ownr = :ownr";
        scanRequest.setFilterExpression(filterExpression);
        scanRequest.setConsistentRead(false);
        scanRequest.setExpressionAttributeNames(Collections.singletonMap("#ownr", "ownr"));
        scanRequest.setExpressionAttributeValues(Collections.singletonMap(":ownr", new AttributeValue(owner)));
        ScanResult scanResult = amazonDynamoDB.scan(scanRequest);

        scanResult.getItems().forEach(item -> {
            String creationTimestamp = item.get("timestamp").getN();

            DeleteItemRequest deleteItemRequest = new DeleteItemRequest();
            Map<String, AttributeValue> key = new HashMap<>();
            key.put("ownr", new AttributeValue(owner));
            key.put("timestamp", new AttributeValue().withN(creationTimestamp));
            deleteItemRequest.setTableName(tableName);
            deleteItemRequest.setKey(key);
            amazonDynamoDB.deleteItem(deleteItemRequest);
        });
    }
}
