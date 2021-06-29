package be.kuritsu.gt.integration;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;

import be.kuritsu.gt.model.ExpenseRequest;

public class TestDataFactory {

    private TestDataFactory() {
    }

    public static ExpenseRequest getDefaultExpenseRequest() {
        return new ExpenseRequest()
                .date(LocalDate.of(2020, 12, 14))
                .amount(BigDecimal.valueOf(10.5))
                .tags(Arrays.asList("Food", "Abroad"))
                .description("McDonald's Breda")
                .creditCard(true)
                .creditCardPaid(false);
    }
}
