package com.digitalwallet.controller;

import com.digitalwallet.dto.BalanceResponse;
import com.digitalwallet.dto.TransferRequest;
import com.digitalwallet.dto.TransferResponse;
import com.digitalwallet.dto.WalletRequest;
import com.digitalwallet.dto.WalletResponse;
import com.digitalwallet.service.WalletService;
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
@RequestMapping("/api/wallets")
@Tag(name = "Wallets", description = "Wallets, balance (DB function) and money transfer (stored procedure)")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @GetMapping
    @Operation(summary = "GET /api/wallets - list all wallets")
    @ApiResponse(responseCode = "200", description = "List of wallets")
    public List<WalletResponse> getAll() {
        return walletService.getAllWallets();
    }

    @GetMapping("/{id}")
    @Operation(summary = "GET /api/wallets/{id} - get one wallet")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Wallet found"),
            @ApiResponse(responseCode = "404", description = "Wallet not found")})
    public WalletResponse getById(@PathVariable Long id) {
        return walletService.getWalletById(id);
    }

    @PostMapping
    @Operation(summary = "POST /api/wallets - create a wallet for an existing user")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Wallet created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "409", description = "User already has a wallet")})
    public ResponseEntity<WalletResponse> create(@Valid @RequestBody WalletRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(walletService.createWallet(request));
    }

    @GetMapping("/{walletId}/balance")
    @Operation(summary = "GET /api/wallets/{walletId}/balance - balance via MySQL function get_wallet_balance()")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Current balance"),
            @ApiResponse(responseCode = "404", description = "Wallet not found")})
    public BalanceResponse getBalance(@PathVariable Long walletId) {
        return walletService.getBalance(walletId);
    }

    @PostMapping("/transfer")
    @Operation(summary = "POST /api/wallets/transfer - transfer money via stored procedure transfer_money")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transfer completed"),
            @ApiResponse(responseCode = "400", description = "Invalid amount, same wallet or insufficient balance"),
            @ApiResponse(responseCode = "404", description = "Wallet not found"),
            @ApiResponse(responseCode = "500", description = "Database error")})
    public TransferResponse transfer(@Valid @RequestBody TransferRequest request) {
        return walletService.transfer(request);
    }
}
