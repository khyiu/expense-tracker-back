package be.kuritsu.gt.repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import be.kuritsu.gt.exception.ExpenseNotFoundException;
import be.kuritsu.gt.persistence.model.ExpenseEntity;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;
import com.amazonaws.services.dynamodbv2.model.GetItemResult;
import com.amazonaws.services.dynamodbv2.model.PutItemRequest;
import com.amazonaws.services.dynamodbv2.model.QueryRequest;
import com.amazonaws.services.dynamodbv2.model.QueryResult;

import edu.umd.cs.findbugs.annotations.CheckForNull;

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
    private static final String PLACEHOLDER_PARTITION_ATTRIBUTE_NAME = "#partitionAttributeName";
    private static final String PLACEHOLDER_PARTITION_ATTRIBUTE_VALUE = ":partitionAttributeValue";

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
        item.put(ATTRIBUTE_TAGS, new AttributeValue().withSS(expense.getTags()));
        item.put(ATTRIBUTE_DESCRIPTION, new AttributeValue(expense.getDescription()));

        if (expense.getCreditCard() != null) {
            item.put(ATTRIBUTE_CREDITCARD, new AttributeValue().withBOOL(expense.getCreditCard()));
        }

        if (expense.getCreditCard() != null) {
            item.put(ATTRIBUTE_CREDITCARD_PAID, new AttributeValue().withBOOL(expense.getCreditCardPaid()));
        }

        return item;
    }

    @Override
    public ExpenseEntity getExpense(String ownr, String id) {
        GetItemRequest getItemRequest = new GetItemRequest();
        getItemRequest.setTableName(TABLE_EXPENSE);

        Map<String, AttributeValue> key = new HashMap<>();
        key.put(ATTRIBUTE_OWNR, new AttributeValue(ownr));
        key.put(ATTRIBUTE_TIMESTAMP, new AttributeValue().withN(id));
        getItemRequest.setKey(key);
        GetItemResult result = amazonDynamoDB.getItem(getItemRequest);

        if (result.getItem() == null) {
            throw new ExpenseNotFoundException();
        }

        return toExpense(result.getItem());
    }

    private static ExpenseEntity toExpense(Map<String, AttributeValue> expenseAttributeValues) {
        AttributeValue creditCardAttributeValue = expenseAttributeValues.get(ATTRIBUTE_CREDITCARD);
        AttributeValue creditCardPaidAttributeValue = expenseAttributeValues.get(ATTRIBUTE_CREDITCARD_PAID);

        return new ExpenseEntity()
                .ownr(expenseAttributeValues.get(ATTRIBUTE_OWNR).getS())
                .timestamp(expenseAttributeValues.get(ATTRIBUTE_TIMESTAMP).getN())
                .date(LocalDate.parse(expenseAttributeValues.get(ATTRIBUTE_DATE).getS(), DateTimeFormatter.ISO_DATE))
                .amount(BigDecimal.valueOf(Double.parseDouble(expenseAttributeValues.get(ATTRIBUTE_AMOUNT).getN())))
                .tags(expenseAttributeValues.get(ATTRIBUTE_TAGS).getSS())
                .description(expenseAttributeValues.get(ATTRIBUTE_DESCRIPTION).getS())
                .creditCard(creditCardAttributeValue == null ? null : creditCardAttributeValue.getBOOL())
                .creditCardPaid(creditCardPaidAttributeValue == null ? null : creditCardPaidAttributeValue.getBOOL());
    }


        @Override
        public List<ExpenseEntity> getExpenses(String owrn, int pageSize, SortingDirection sortingDirection, @CheckForNull String exclusiveBoundKey) {
            QueryRequest queryRequest = createGetExpensesQueryRequest(owrn, pageSize, sortingDirection, exclusiveBoundKey);
            QueryResult queryResult = amazonDynamoDB.query(queryRequest);
            return queryResult.getItems()
                    .stream()
                    .map(ExpenseRepositoryImpl::toExpense)
                    .collect(Collectors.toList());
        }

        private static QueryRequest createGetExpensesQueryRequest(String ownr, int pageSize, SortingDirection sortingDirection, @CheckForNull String exclusiveBoundKey) {
            QueryRequest queryRequest = new QueryRequest();
            queryRequest.setTableName(TABLE_EXPENSE);
            queryRequest.setKeyConditionExpression(PLACEHOLDER_PARTITION_ATTRIBUTE_NAME + " = :partitionAttributeValue");
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

        private static Map<String, AttributeValue> getExclusiveStartKey(String ownr, String exclusiveBoundKey) {
            Map<String, AttributeValue> key = new HashMap<>();
            key.put(ATTRIBUTE_OWNR, new AttributeValue(ownr));
            key.put(ATTRIBUTE_TIMESTAMP, new AttributeValue().withN(exclusiveBoundKey.toString()));
            return key;
        }

        private static Map<String, String> getExpressionAttributeNames() {
            Map<String, String> expressionAttributeNames = new HashMap<>();
            expressionAttributeNames.put(PLACEHOLDER_PARTITION_ATTRIBUTE_NAME, ATTRIBUTE_OWNR);
            return expressionAttributeNames;
        }

        private static Map<String, AttributeValue> getExpressionAttributeValues(String ownr) {
            Map<String, AttributeValue> expressionAttributeValues = new HashMap<>();
            expressionAttributeValues.put(PLACEHOLDER_PARTITION_ATTRIBUTE_VALUE, new AttributeValue(ownr));
            return expressionAttributeValues;
        }

    //
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
