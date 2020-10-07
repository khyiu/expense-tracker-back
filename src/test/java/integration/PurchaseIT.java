package integration;

import be.kuritsu.gt.Application;
import be.kuritsu.gt.model.Packaging;
import be.kuritsu.gt.model.PurchaseLocation;
import be.kuritsu.gt.model.PurchaseRequest;
import be.kuritsu.gt.model.UnitMeasurement;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = Application.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT, properties = {
        "oauth.client.id=clientId",
        "oauth.client.secret=clientSecret"
})
public class PurchaseIT {

    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @Before
    public void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(webApplicationContext)
                .build();
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
                .descriptionTags(Collections.singletonList("Cookie dough"))
                .unitPrice(BigDecimal.valueOf(10.0))
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
    public void test_register_purchase_unauthenticated_user() {
        Exception thrownException = Assert.assertThrows(Exception.class, () -> {
            mockMvc.perform(post("/purchases")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(getDefaultPurchaseRequest())));
        });

        assertThat(thrownException).hasCauseInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    @Test
    @WithMockUser(roles = "GUEST")
    public void test_register_purchase_unauthorized_user() {
        Exception thrownException = Assert.assertThrows(Exception.class, () -> {
            mockMvc.perform(post("/purchases")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(getDefaultPurchaseRequest())));
        });

        assertThat(thrownException).hasCauseInstanceOf(AccessDeniedException.class);
    }

    @Test
    @WithMockUser(roles = "USERS")
    public void test_register_purchase() throws Exception {
        mockMvc.perform(post("/purchases")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(getDefaultPurchaseRequest())))
                .andExpect(status().isCreated());
    }
}
