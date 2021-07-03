package be.kuritsu.gt.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RestController;

import be.kuritsu.gt.api.ExpensesApi;
import be.kuritsu.gt.model.ExpenseRequest;
import be.kuritsu.gt.model.ExpenseResponse;
import be.kuritsu.gt.repository.SortingDirection;
import be.kuritsu.gt.service.ExpenseService;

@RestController
public class ExpenseController implements ExpensesApi {

    private final ExpenseService expenseService;

    @Autowired
    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @Secured("ROLE_USERS")
    @Override
    public ResponseEntity<Void> deleteExpense(String id) {
        expenseService.deleteExpense(id);
        return ResponseEntity.ok().build();
    }

    @CrossOrigin
    @Secured("ROLE_USERS")
    @Override
    public ResponseEntity<List<ExpenseResponse>> fetchExpenses(Integer pageSize, String sortDirection, String exclusiveBoundKey) {
        SortingDirection sortDir = SortingDirection.valueOf(sortDirection);
        return ResponseEntity.ok(this.expenseService.getExpenses(pageSize, sortDir, exclusiveBoundKey));
    }

    @Secured("ROLE_USERS")
    @Override
    public ResponseEntity<ExpenseResponse> getExpense(String id) {
        return ResponseEntity.ok(expenseService.getExpense(id));
    }

    @Secured("ROLE_USERS")
    @Override
    public ResponseEntity<ExpenseResponse> registerExpense(ExpenseRequest expenseRequest) {
        ExpenseResponse expenseResponse = expenseService.registerExpense(expenseRequest);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(expenseResponse);
    }
}
