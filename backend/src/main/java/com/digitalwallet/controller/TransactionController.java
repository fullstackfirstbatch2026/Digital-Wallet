package com.digitalwallet.controller;

import com.digitalwallet.dto.TransactionRequest;
import com.digitalwallet.dto.TransactionResponse;
import com.digitalwallet.dto.TransactionUserDetailsDto;
import com.digitalwallet.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/transactions")
@Tag(name = "Transactions", description = "Transaction history, deposits/withdrawals and the JOIN report")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @GetMapping
    @Operation(summary = "GET /api/transactions - list all transactions (newest first)")
    @ApiResponse(responseCode = "200", description = "List of transactions")
    public List<TransactionResponse> getAll() {
        return transactionService.getAllTransactions();
    }

    @GetMapping("/user-details")
    @Operation(summary = "GET /api/transactions/user-details - transactions with user info (SQL JOIN)")
    @ApiResponse(responseCode = "200", description = "Joined transaction + user data")
    public List<TransactionUserDetailsDto> getUserDetails() {
        return transactionService.getTransactionUserDetails();
    }

    @GetMapping("/{id}")
    @Operation(summary = "GET /api/transactions/{id} - get one transaction")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction found"),
            @ApiResponse(responseCode = "404", description = "Transaction not found")})
    public TransactionResponse getById(@PathVariable Long id) {
        return transactionService.getTransactionById(id);
    }

    @PostMapping
    @Operation(summary = "POST /api/transactions - create a DEPOSIT or WITHDRAW (trigger updates the balance)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Transaction created"),
            @ApiResponse(responseCode = "400", description = "Invalid transaction or insufficient balance"),
            @ApiResponse(responseCode = "404", description = "Wallet not found")})
    public ResponseEntity<TransactionResponse> create(@Valid @RequestBody TransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(transactionService.createTransaction(request));
    }
}
