package com.digitalwallet.service;

import com.digitalwallet.dto.TransactionRequest;
import com.digitalwallet.dto.TransactionResponse;
import com.digitalwallet.dto.TransactionUserDetailsDto;
import com.digitalwallet.entity.Transaction;
import com.digitalwallet.entity.TransactionType;
import com.digitalwallet.entity.Wallet;
import com.digitalwallet.exception.InsufficientBalanceException;
import com.digitalwallet.exception.InvalidTransactionException;
import com.digitalwallet.exception.ResourceNotFoundException;
import com.digitalwallet.exception.WalletNotFoundException;
import com.digitalwallet.repository.ReportJdbcRepository;
import com.digitalwallet.repository.TransactionRepository;
import com.digitalwallet.repository.TransactionTypeRepository;
import com.digitalwallet.repository.WalletRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final TransactionTypeRepository typeRepository;
    private final WalletRepository walletRepository;
    private final ReportJdbcRepository reportRepository;

    public TransactionService(TransactionRepository transactionRepository, TransactionTypeRepository typeRepository,
                              WalletRepository walletRepository, ReportJdbcRepository reportRepository) {
        this.transactionRepository = transactionRepository;
        this.typeRepository = typeRepository;
        this.walletRepository = walletRepository;
        this.reportRepository = reportRepository;
    }

    @Transactional(readOnly = true)
    public List<TransactionResponse> getAllTransactions() {
        return transactionRepository.findAllByOrderByTransactionDateDescIdDesc().stream()
                .map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public TransactionResponse getTransactionById(Long id) {
        return transactionRepository.findById(id).map(this::toResponse)
                .orElseThrow(() -> new ResourceNotFoundException("Transaction not found with id " + id));
    }

    /**
     * Inserts a DEPOSIT or WITHDRAW row. The wallet balance is then updated
     * automatically by the database trigger trg_transactions_after_insert.
     */
    @Transactional
    public TransactionResponse createTransaction(TransactionRequest request) {
        Wallet wallet = walletRepository.findById(request.walletId())
                .orElseThrow(() -> new WalletNotFoundException(request.walletId()));

        String typeName = request.transactionType().trim().toUpperCase();
        if (typeName.equals("TRANSFER")) {
            throw new InvalidTransactionException("Transfers must be made with POST /api/wallets/transfer");
        }
        TransactionType type = typeRepository.findByTypeName(typeName)
                .orElseThrow(() -> new InvalidTransactionException("Unknown transaction type: " + typeName));

        if (typeName.equals("WITHDRAW") && wallet.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientBalanceException();
        }

        Transaction transaction = new Transaction();
        transaction.setWallet(wallet);
        transaction.setTransactionType(type);
        transaction.setAmount(request.amount());
        transaction.setDescription(request.description());
        // saveAndFlush runs the INSERT now, so the trigger (and its errors) fire inside this method
        return toResponse(transactionRepository.saveAndFlush(transaction));
    }

    /** JOIN report. */
    @Transactional(readOnly = true)
    public List<TransactionUserDetailsDto> getTransactionUserDetails() {
        return reportRepository.findTransactionUserDetails();
    }

    private TransactionResponse toResponse(Transaction t) {
        return new TransactionResponse(t.getId(), t.getWallet().getId(), t.getTransactionType().getTypeName(),
                t.getAmount(), t.getDescription(), t.getTransactionDate());
    }
}
