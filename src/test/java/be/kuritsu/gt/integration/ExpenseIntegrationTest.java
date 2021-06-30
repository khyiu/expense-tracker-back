package be.kuritsu.gt.integration;

import static be.kuritsu.gt.integration.TestDataFactory.getDefaultExpenseRequest;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

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

import be.kuritsu.gt.Application;
import be.kuritsu.gt.model.ExpenseRequest;
import be.kuritsu.gt.model.ExpenseResponse;
import be.kuritsu.gt.repository.ExpenseRepositoryImpl;

import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "oauth.client.id=clientId",
        "oauth.client.secret=clientSecret"
})
public class ExpenseIntegrationTest {

    private static final String TEST_USERNAME = "ron_swanson";
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
        IntegrationTestUtils.cleanupTestData(amazonDynamoDB, ExpenseRepositoryImpl.TABLE_EXPENSE, TEST_USERNAME);
    }

    @Test
    public void test_register_expense_unauthenticated_user() throws JsonProcessingException {
        String requestJsonString = objectMapper.writeValueAsString(getDefaultExpenseRequest());
        Exception thrownException = Assert.assertThrows(Exception.class, () ->
                mockMvc.perform(post("/expenses")
                        .contentType("application/json")
                        .content(requestJsonString)));

        assertThat(thrownException).hasCauseInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    @Test
    @WithMockUser(roles = "GUESTS", username = TEST_USERNAME)
    public void test_register_expense_unauthorized_user() throws JsonProcessingException {
        String requestJsonString = objectMapper.writeValueAsString(getDefaultExpenseRequest());
        Exception thrownException = Assert.assertThrows(Exception.class, () ->
                mockMvc.perform(post("/expenses")
                        .contentType("application/json")
                        .content(requestJsonString)));

        assertThat(thrownException).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(roles = "USERS", username = TEST_USERNAME)
    public void test_register_expense() throws Exception {
        String requestJsonString = objectMapper.writeValueAsString(getDefaultExpenseRequest());
        String jsonString = mockMvc.perform(post("/expenses")
                .contentType("application/json")
                .content(requestJsonString))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ExpenseResponse expenseResponse = objectMapper.readValue(jsonString, ExpenseResponse.class);
        assertThat(expenseResponse.getId()).isNotNull();
        assertThat(expenseResponse.getDate()).isEqualTo(LocalDate.of(2020, 12, 14));
        assertThat(expenseResponse.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(10.5));
        assertThat(expenseResponse.getTags())
                .hasSize(2)
                .contains("food", "abroad");
        assertThat(expenseResponse.getDescription()).isEqualTo("McDonald's Breda");
        assertThat(expenseResponse.getCreditCard()).isTrue();
        assertThat(expenseResponse.getCreditCardPaid()).isFalse();
    }

    @Test
    @WithMockUser(roles = "USERS", username = TEST_USERNAME)
    public void test_register_expense_invalid_request() throws Exception {
        ExpenseRequest expenseRequest = new ExpenseRequest()
                .tags(Arrays.asList("Food", "Abroad"))
                .description("McDonald's Breda")
                .creditCard(true)
                .creditCardPaid(false);
        String requestJsonString = objectMapper.writeValueAsString(expenseRequest);
        mockMvc.perform(post("/expenses")
                .contentType("application/json")
                .content(requestJsonString))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(roles = "GUESTS", username = TEST_USERNAME)
    public void test_get_expense_unauthorized_user() {
        Exception thrownException = Assert.assertThrows(Exception.class, () ->
                mockMvc.perform(get("/expenses/{id}", "123456")
                        .contentType("application/json")));

        assertThat(thrownException).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(roles = "USERS", username = TEST_USERNAME)
    public void test_get_expense_not_found() throws Exception {
        mockMvc.perform(get("/expenses/{id}", "123456")
                .contentType("application/json"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(roles = "USERS", username = TEST_USERNAME)
    public void test_get_expense() throws Exception {
        String requestJsonString = objectMapper.writeValueAsString(getDefaultExpenseRequest());
        String jsonString = mockMvc.perform(post("/expenses")
                .contentType("application/json")
                .content(requestJsonString))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        ExpenseResponse expenseResponse = objectMapper.readValue(jsonString, ExpenseResponse.class);

        jsonString = mockMvc.perform(get("/expenses/{id}", expenseResponse.getId())
                .contentType("application/json"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        expenseResponse = objectMapper.readValue(jsonString, ExpenseResponse.class);

        assertThat(expenseResponse.getId()).isNotNull();
        assertThat(expenseResponse.getDate()).isEqualTo(LocalDate.of(2020, 12, 14));
        assertThat(expenseResponse.getAmount()).isEqualByComparingTo(BigDecimal.valueOf(10.5));
        assertThat(expenseResponse.getTags())
                .hasSize(2)
                .contains("food", "abroad");
        assertThat(expenseResponse.getDescription()).isEqualTo("McDonald's Breda");
        assertThat(expenseResponse.getCreditCard()).isTrue();
        assertThat(expenseResponse.getCreditCardPaid()).isFalse();
    }

    //
    //    @Test
    //    public void test_fetch_purchases_unauthenticated_user() {
    //        Exception thrownException = Assert.assertThrows(Exception.class, () ->
    //                mockMvc.perform(get("/purchases")
    //                        .contentType("application/json")
    //                        .queryParam("pageSize", "3")
    //                        .queryParam("sortDirection", "DESC")
    //                        .queryParam("exclusiveBoundKey", "123456")));
    //
    //        assertThat(thrownException).hasCauseInstanceOf(AuthenticationCredentialsNotFoundException.class);
    //    }
    //
    //    @Test
    //    @WithMockUser(roles = "GUESTS", username = TEST_USERNAME)
    //    public void test_fetch_purchases_unauthorized_user() {
    //        Exception thrownException = Assert.assertThrows(Exception.class, () ->
    //                mockMvc.perform(get("/purchases")
    //                        .contentType("application/json")
    //                        .queryParam("pageSize", "3")
    //                        .queryParam("sortDirection", "DESC")
    //                        .queryParam("exclusiveBoundKey", "123456")));
    //
    //        assertThat(thrownException).hasCauseInstanceOf(AccessDeniedException.class);
    //    }
    //
    //    @Test
    //    @WithMockUser(roles = "USERS", username = TEST_USERNAME)
    //    public void test_fetch_purchases_no_results() throws Exception {
    //        mockMvc.perform(get("/purchases")
    //                .contentType("application/json")
    //                .queryParam("pageSize", "3")
    //                .queryParam("sortDirection", "DESC")
    //                .queryParam("exclusiveBoundKey", "123456"))
    //                .andExpect(status().isOk())
    //                .andExpect(jsonPath("$").isEmpty());
    //    }
    //
    //    @Test
    //    public void test_get_purchase_unauthenticated_user() {
    //        Exception thrownException = Assert.assertThrows(Exception.class, () ->
    //                mockMvc.perform(get("/purchases/123456")
    //                        .contentType("application/json")));
    //
    //        assertThat(thrownException).hasCauseInstanceOf(AuthenticationCredentialsNotFoundException.class);
    //    }
    //

    //
    //    @Test
    //    public void test_delete_purchase_unauthenticated_user() {
    //        Exception thrownException = Assert.assertThrows(Exception.class, () ->
    //                mockMvc.perform(delete("/purchases/123456")
    //                        .contentType("application/json")));
    //
    //        assertThat(thrownException).hasCauseInstanceOf(AuthenticationCredentialsNotFoundException.class);
    //    }
    //
    //    @Test
    //    @WithMockUser(roles = "GUESTS", username = TEST_USERNAME)
    //    public void test_delete_purchase_unauthorized_user() {
    //        Exception thrownException = Assert.assertThrows(Exception.class, () ->
    //                mockMvc.perform(delete("/purchases/123456")
    //                        .contentType("application/json")));
    //
    //        assertThat(thrownException).hasCauseInstanceOf(AccessDeniedException.class);
    //    }
    //
    //    @Test
    //    @WithMockUser(roles = "USERS", username = TEST_USERNAME)
    //    public void test_delete_purchase_not_found() throws Exception {
    //        mockMvc.perform(delete("/purchases/123456")
    //                .contentType("application/json"))
    //                .andExpect(status().isNotFound());
    //    }
    //
    //    @Test
    //    @WithMockUser(roles = "USERS", username = TEST_USERNAME)
    //    public void test_delete_purchase() throws Exception {
    //        MvcResult mvcResult = mockMvc.perform(post("/purchases")
    //                .contentType("application/json")
    //                .content(objectMapper.writeValueAsString(getDefaultPurchaseRequest())))
    //                .andExpect(status().isCreated())
    //                .andReturn();
    //
    //        String jsonResponse = mvcResult.getResponse().getContentAsString();
    //        PurchaseResponse createdPurchaseResponse = objectMapper.readValue(jsonResponse, PurchaseResponse.class);
    //
    //        mvcResult = mockMvc.perform(get("/purchases/{creationTimestamp}", createdPurchaseResponse.getId().toString())
    //                .contentType("application/json"))
    //                .andExpect(status().isOk())
    //                .andReturn();
    //
    //        jsonResponse = mvcResult.getResponse().getContentAsString();
    //        PurchaseResponse fetchedPurchaseResponse = objectMapper.readValue(jsonResponse, PurchaseResponse.class);
    //        assertThat(fetchedPurchaseResponse.getItems()).hasSize(1);
    //
    //        mockMvc.perform(delete("/purchases/{creationTimestamp}", fetchedPurchaseResponse.getId().toString())
    //                .contentType("application/json"))
    //                .andExpect(status().isOk());
    //
    //        mockMvc.perform(get("/purchases/{creationTimestamp}", createdPurchaseResponse.getId().toString())
    //                .contentType("application/json"))
    //                .andExpect(status().isNotFound());
    //    }
}
