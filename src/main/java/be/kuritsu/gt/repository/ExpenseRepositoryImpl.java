package be.kuritsu.gt.repository;

import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import be.kuritsu.gt.persistence.model.ExpenseEntity;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;

@Repository
public class ExpenseRepositoryImpl implements ExpenseRepository {

    public static final String TABLE_EXPENSE = "Expense";
    private static final String ATTRIBUTE_TIMESTAMP = "timestamp";
    private static final String ATTRIBUTE_OWNR = "ownr";
    private static final String ATTRIBUTE_DATE = "date";
    private static final String ATTRIBUTE_AMOUNT = "amount";
    private static final String ATTRIBUTE_TAGS = "tags";
    private static final String ATTRIBUTE_DESCRIPTION = "description";
    private static final String ATTRIBUTE_CREDITCARD = "creditCard";
    private static final String ATTRIBUTE_CREDITCARD_PAID = "creditCardPaid";

    private final AmazonDynamoDB amazonDynamoDB;

    @Autowired
    public ExpenseRepositoryImpl(AmazonDynamoDB amazonDynamoDB) {
        this.amazonDynamoDB = amazonDynamoDB;
    }

    @Override
    public void save(ExpenseEntity purchase) {
        PutItemRequest expensePutItemRequest = createExpensePutItemRequest(purchase);
        amazonDynamoDB.putItem(expensePutItemRequest);
    }

    private static PutItemRequest createExpensePutItemRequest(ExpenseEntity expense) {
        PutItemRequest putItemRequest = new PutItemRequest();
        putItemRequest.setTableName(TABLE_EXPENSE);
        putItemRequest.setItem(createExpenseAttributeValueMap(expense));
        return putItemRequest;
    }

    private static Map<String, AttributeValue> createExpenseAttributeValueMap(ExpenseEntity expense) {
        Map<String, AttributeValue> item = new HashMap<>();
        item.put(ATTRIBUTE_OWNR, new AttributeValue(expense.getOwnr()));
        item.put(ATTRIBUTE_TIMESTAMP, new AttributeValue().withN(expense.getTimestamp()));
        item.put(ATTRIBUTE_DATE, new AttributeValue(expense.getDate().format(DateTimeFormatter.ISO_DATE)));
        item.put(ATTRIBUTE_AMOUNT, new AttributeValue().withN(expense.getAmount().toString()));

        if (expense.getTags() != null) {
            item.put(ATTRIBUTE_TAGS, new AttributeValue().withSS(expense.getTags()));
        }

        item.put(ATTRIBUTE_DESCRIPTION, new AttributeValue(expense.getDescription()));

        if (expense.getCreditCard() != null) {
            item.put(ATTRIBUTE_CREDITCARD, new AttributeValue().withBOOL(expense.getCreditCard()));
        }

        if (expense.getCreditCard() != null) {
            item.put(ATTRIBUTE_CREDITCARD_PAID, new AttributeValue().withBOOL(expense.getCreditCardPaid()));
        }

        return item;
    }

    //
    //    @Override
    //    public List<Purchase> findPurchases(String owrn, int pageSize, SortingDirection sortingDirection, @CheckForNull Integer exclusiveBoundKey) {
    //        QueryRequest queryRequest = createFetchPurchasesQueryRequest(owrn, pageSize, sortingDirection, exclusiveBoundKey);
    //        QueryResult queryResult = amazonDynamoDB.query(queryRequest);
    //        return queryResult.getItems()
    //                .stream()
    //                .map(ExpenseRepositoryImpl::toPurchase)
    //                .collect(Collectors.toList());
    //    }
    //
    //    private static QueryRequest createFetchPurchasesQueryRequest(String ownr, int pageSize, SortingDirection sortingDirection, @CheckForNull Integer exclusiveBoundKey) {
    //        QueryRequest queryRequest = new QueryRequest();
    //        queryRequest.setTableName(TABLE_PURCHASE);
    //        queryRequest.setKeyConditionExpression("#partitionAttributeName = :partitionAttributeValue");
    //        queryRequest.setConsistentRead(false);
    //        queryRequest.setScanIndexForward(sortingDirection == SortingDirection.ASC);
    //        queryRequest.setLimit(pageSize);
    //
    //        if (exclusiveBoundKey != null) {
    //            queryRequest.setExclusiveStartKey(getExclusiveStartKey(ownr, exclusiveBoundKey));
    //        }
    //
    //        queryRequest.setExpressionAttributeNames(getExpressionAttributeNames());
    //        queryRequest.setExpressionAttributeValues(getExpressionAttributeValues(ownr));
    //        return queryRequest;
    //    }
    //
    //    private static Map<String, String> getExpressionAttributeNames() {
    //        Map<String, String> expressionAttributeNames = new HashMap<>();
    //        expressionAttributeNames.put("#partitionAttributeName", ATTRIBUTE_OWNR);
    //        return expressionAttributeNames;
    //    }
    //
    //    private static Map<String, AttributeValue> getExpressionAttributeValues(String ownr) {
    //        Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
    //        expressionAttributeValues.put(":partitionAttributeValue", new AttributeValue(ownr));
    //        return expressionAttributeValues;
    //    }
    //
    //    private static Map<String, AttributeValue> getExclusiveStartKey(String ownr, Integer exclusiveBoundKey) {
    //        Map<String, AttributeValue> key = new HashMap<>();
    //        key.put(ATTRIBUTE_OWNR, new AttributeValue(ownr));
    //        key.put(ATTRIBUTE_CREATION_TIMESTAMP, new AttributeValue().withN(exclusiveBoundKey.toString()));
    //        return key;
    //    }
    //
    //    private static Purchase toPurchase(Map<String, AttributeValue> purchaseAttributeValues) {
    //        return new Purchase()
    //                .amount(BigDecimal.valueOf(Double.parseDouble(purchaseAttributeValues.get("amount").getN())))
    //                .creationTimestamp(purchaseAttributeValues.get(ATTRIBUTE_CREATION_TIMESTAMP).getN())
    //                .shop(toShop(purchaseAttributeValues.get("shop").getM()))
    //                .items(purchaseAttributeValues.get("items").getSS());
    //    }
    //
    //    private static PurchaseShop toShop(Map<String, AttributeValue> shopAttributeValues) {
    //        return new PurchaseShop()
    //                .name(shopAttributeValues.get("name").getS())
    //                .location(shopAttributeValues.get("location").getS());
    //    }
    //
    //    @Override
    //    public Purchase getPurchase(String ownr, Integer creationTimestamp) {
    //        GetItemRequest getItemRequest = new GetItemRequest();
    //        getItemRequest.setTableName(TABLE_PURCHASE);
    //
    //        Map<String, AttributeValue> key = new HashMap<>();
    //        key.put(ATTRIBUTE_OWNR, new AttributeValue(ownr));
    //        key.put(ATTRIBUTE_CREATION_TIMESTAMP, new AttributeValue().withN(creationTimestamp.toString()));
    //        getItemRequest.setKey(key);
    //        GetItemResult result = amazonDynamoDB.getItem(getItemRequest);
    //
    //        if (result.getItem() == null) {
    //            throw new PurchaseNotFoundException();
    //        }
    //
    //        return toPurchase(result.getItem());
    //    }
    //
    //    @Override
    //    public void delete(String ownr, Integer creationTimestamp) {
    //        DeleteItemRequest deleteItemRequest = new DeleteItemRequest();
    //        deleteItemRequest.setTableName(TABLE_PURCHASE);
    //
    //        Map<String, AttributeValue> key = new HashMap<>();
    //        key.put(ATTRIBUTE_OWNR, new AttributeValue(ownr));
    //        key.put(ATTRIBUTE_CREATION_TIMESTAMP, new AttributeValue().withN(creationTimestamp.toString()));
    //        deleteItemRequest.setKey(key);
    //
    //        amazonDynamoDB.deleteItem(deleteItemRequest);
    //    }
}
