package integration;

import be.kuritsu.gt.Application;
import be.kuritsu.gt.model.Packaging;
import be.kuritsu.gt.model.PurchaseLocation;
import be.kuritsu.gt.model.PurchaseRequest;
import be.kuritsu.gt.model.UnitMeasurement;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.BatchGetItemRequest;
import com.amazonaws.services.dynamodbv2.model.BatchGetItemResult;
import com.amazonaws.services.dynamodbv2.model.DeleteItemRequest;
import com.amazonaws.services.dynamodbv2.model.ScanRequest;
import com.amazonaws.services.dynamodbv2.model.ScanResult;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "oauth.client.id=clientId",
        "oauth.client.secret=clientSecret"
})
public class PurchaseIntegrationTest {

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Autowired
    private AmazonDynamoDB amazonDynamoDB;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .build();
    }

    @After
    public void cleanupTestData() {
        Map<String, AttributeValue> expressionAttributeValues =
                new HashMap<>();
        expressionAttributeValues.put(":testDataValue", new AttributeValue().withN("1"));

        ScanRequest scanRequest = new ScanRequest()
                .withTableName("Purchase")
                .withFilterExpression("testData = :testDataValue")
                .withProjectionExpression("id,purchaseDate")
                .withExpressionAttributeValues(expressionAttributeValues);

        ScanResult scanResult = amazonDynamoDB.scan(scanRequest);

        scanResult.getItems().forEach(item -> {
            Map<String, AttributeValue> deleteItemRequestKeys = new HashMap<>();
            /*
             DeleteItemRequest requires Global Secondary Index, if any in the target table, to be specified,
             otherwise, deleteItem(...) operation won't work.
             */
            deleteItemRequestKeys.put("id", new AttributeValue(item.get("id").getS()));
            deleteItemRequestKeys.put("purchaseDate", new AttributeValue(item.get("purchaseDate").getS()));

            DeleteItemRequest deleteItemRequest = new DeleteItemRequest()
                    .withTableName("Purchase")
                    .withKey(deleteItemRequestKeys);
            amazonDynamoDB.deleteItem(deleteItemRequest);
        });
    }

    private PurchaseRequest getDefaultPurchaseRequest() {
        return new PurchaseRequest()
                .date(LocalDate.of(2020, 9, 14))
                .location(
                        new PurchaseLocation()
                                .description("Provigo")
                                .locationTag("Montreal")
                )
                .brand("Ben & Jerry's")
                .descriptionTags(Arrays.asList("Cookie dough", "Organic Milk"))
                .unitPrice(BigDecimal.valueOf(9.95))
                .packaging(
                        new Packaging()
                                .nbUnitPerPackage(1)
                                .unitMeasurements(
                                        new UnitMeasurement()
                                                .quantity(500)
                                                .type(UnitMeasurement.TypeEnum.ML)
                                )
                );
    }

    @Test
    public void test_register_purchase_unauthenticated_user() throws JsonProcessingException {
        String requestJsonString = objectMapper.writeValueAsString(getDefaultPurchaseRequest());
        Exception thrownException = Assert.assertThrows(Exception.class, () -> {
            mockMvc.perform(post("/purchases")
                    .contentType("application/json")
                    .content(requestJsonString));
        });

        assertThat(thrownException).hasCauseInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    @Test
    @WithMockUser(roles = "GUEST")
    public void test_register_purchase_unauthorized_user() throws JsonProcessingException {
        String requestJsonString = objectMapper.writeValueAsString(getDefaultPurchaseRequest());
        Exception thrownException = Assert.assertThrows(Exception.class, () -> {
            mockMvc.perform(post("/purchases")
                    .contentType("application/json")
                    .content(requestJsonString));
        });

        assertThat(thrownException).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(roles = "USERS", username = "ron_swanson")
    public void test_register_purchase() throws Exception {
        mockMvc.perform(post("/purchases")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(getDefaultPurchaseRequest())))
                .andExpect(status().isCreated());

        Map<String, AttributeValue> expressionAttributeValues =
                new HashMap<>();
        expressionAttributeValues.put(":testDataValue", new AttributeValue().withN("1"));
        expressionAttributeValues.put(":ownr", new AttributeValue().withS("ron_swanson"));

        ScanRequest scanRequest = new ScanRequest()
                .withTableName("Purchase")
                .withFilterExpression("testData = :testDataValue AND ownr = :ownr")
                .withProjectionExpression("id," +
                        "purchaseDate," +
                        "brand," +
                        "descriptionTags," +
                        "unitPrice," +
                        "locationId," +
                        "locationDescription," +
                        "locationLocationTag," +
                        "nbUnitPerPackage," +
                        "packageUnitMeasurementType," +
                        "packageUnitMeasureQuantity," +
                        "ownr," +
                        "testData")
                .withExpressionAttributeValues(expressionAttributeValues);

        ScanResult scanResult = amazonDynamoDB.scan(scanRequest);
        assertThat(scanResult.getCount()).isEqualTo(1);

        Map<String, AttributeValue> itemValues = scanResult.getItems().get(0);
        assertThat(itemValues.get("id")).isNotNull();
        assertThat(itemValues.get("purchaseDate")).isNotNull();
        assertThat(itemValues.get("purchaseDate").getS()).isEqualTo("2020-09-14");
        assertThat(itemValues.get("brand")).isNotNull();
        assertThat(itemValues.get("brand").getS()).isEqualTo("Ben & Jerry's");
        assertThat(itemValues.get("descriptionTags")).isNotNull();
        assertThat(itemValues.get("descriptionTags").getSS())
                .contains("Cookie dough", "Organic Milk");
        assertThat(itemValues.get("unitPrice")).isNotNull();
        assertThat(itemValues.get("unitPrice").getN()).isEqualTo("9.95");
        assertThat(itemValues.get("locationId")).isNotNull();
        assertThat(itemValues.get("locationDescription")).isNotNull();
        assertThat(itemValues.get("locationDescription").getS()).isEqualTo("Provigo");
        assertThat(itemValues.get("locationLocationTag")).isNotNull();
        assertThat(itemValues.get("locationLocationTag").getS()).isEqualTo("Montreal");
        assertThat(itemValues.get("nbUnitPerPackage")).isNotNull();
        assertThat(itemValues.get("nbUnitPerPackage").getN()).isEqualTo("1");
        assertThat(itemValues.get("packageUnitMeasurementType")).isNotNull();
        assertThat(itemValues.get("packageUnitMeasurementType").getS()).isEqualTo("ml");
        assertThat(itemValues.get("packageUnitMeasureQuantity")).isNotNull();
        assertThat(itemValues.get("packageUnitMeasureQuantity").getN()).isEqualTo("500");
        assertThat(itemValues.get("ownr")).isNotNull();
        assertThat(itemValues.get("ownr").getS()).isEqualTo("ron_swanson");
        assertThat(itemValues.get("testData")).isNotNull();
        assertThat(itemValues.get("testData").getN()).isEqualTo("1");
    }

    @Test
    @WithMockUser(roles = "USERS", username = "ron_swanson")
    public void test_register_purchase_invalid_request() throws Exception {
        PurchaseRequest purchaseRequest = getDefaultPurchaseRequest();
        purchaseRequest.setUnitPrice(null);
        mockMvc.perform(post("/purchases")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(purchaseRequest)))
                .andExpect(status().isBadRequest());
    }
}
