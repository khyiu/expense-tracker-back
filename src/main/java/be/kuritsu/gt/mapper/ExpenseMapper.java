package be.kuritsu.gt.mapper;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

import be.kuritsu.gt.model.ExpenseRequest;
import be.kuritsu.gt.model.ExpenseResponse;
import be.kuritsu.gt.persistence.model.ExpenseEntity;

public class ExpenseMapper {

    private ExpenseMapper() {

    }

    public static ExpenseEntity toExpenseEntity(String ownr, ExpenseRequest expenseRequest) {
        List<String> tags = expenseRequest.getTags()
                .stream()
                .map(tag -> tag.toLowerCase(Locale.ROOT))
                .collect(Collectors.toList());

        return new ExpenseEntity()
                .ownr(ownr)
                .timestamp(getTimestamp())
                .date(expenseRequest.getDate())
                .amount(expenseRequest.getAmount())
                .tags(tags)
                .description(expenseRequest.getDescription())
                .creditCard(expenseRequest.getCreditCard())
                .creditCardPaid(expenseRequest.getCreditCardPaid());
    }

    public static ExpenseResponse toExpenseResponse(ExpenseEntity expenseEntity) {
        return new ExpenseResponse()
                .date(expenseEntity.getDate())
                .amount(expenseEntity.getAmount())
                .tags(expenseEntity.getTags())
                .description(expenseEntity.getDescription())
                .creditCard(expenseEntity.getCreditCard())
                .creditCardPaid(expenseEntity.getCreditCardPaid())
                .id(expenseEntity.getTimestamp());

    }

    private static String getTimestamp() {
        return Long.toString(ChronoUnit.MICROS.between(Instant.EPOCH, Instant.now()));
    }

}
