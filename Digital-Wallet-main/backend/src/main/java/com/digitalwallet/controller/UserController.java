package com.digitalwallet.controller;

import com.digitalwallet.dto.AboveAverageTransactionDto;
import com.digitalwallet.dto.UserRequest;
import com.digitalwallet.dto.UserResponse;
import com.digitalwallet.service.UserService;
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
@RequestMapping("/api/users")
@Tag(name = "Users", description = "User management")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    @Operation(summary = "GET /api/users - list all users")
    @ApiResponse(responseCode = "200", description = "List of users")
    public List<UserResponse> getAll() {
        return userService.getAllUsers();
    }

    @GetMapping("/above-average-transactions")
    @Operation(summary = "GET /api/users/above-average-transactions - users with a transaction above the average (SQL subquery)")
    @ApiResponse(responseCode = "200", description = "Users and the transaction amounts above the average")
    public List<AboveAverageTransactionDto> getAboveAverage() {
        return userService.getAboveAverageTransactions();
    }

    @GetMapping("/{id}")
    @Operation(summary = "GET /api/users/{id} - get one user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User found"),
            @ApiResponse(responseCode = "404", description = "User not found")})
    public UserResponse getById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @PostMapping
    @Operation(summary = "POST /api/users - create a user (an empty wallet is created automatically)")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "409", description = "Email already exists")})
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(request));
    }

    @PutMapping("/{id}")
    @Operation(summary = "PUT /api/users/{id} - update a user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User updated"),
            @ApiResponse(responseCode = "400", description = "Validation error"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "409", description = "Email already exists")})
    public UserResponse update(@PathVariable Long id, @Valid @RequestBody UserRequest request) {
        return userService.updateUser(id, request);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "DELETE /api/users/{id} - delete a user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User deleted"),
            @ApiResponse(responseCode = "404", description = "User not found"),
            @ApiResponse(responseCode = "409", description = "User has transactions and cannot be deleted")})
    public ResponseEntity<java.util.Map<String, String>> delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(java.util.Map.of("message", "User deleted successfully"));
    }
}
