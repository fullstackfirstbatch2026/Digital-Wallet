package com.digitalwallet.service;

import com.digitalwallet.dto.BalanceResponse;
import com.digitalwallet.dto.TransferRequest;
import com.digitalwallet.dto.TransferResponse;
import com.digitalwallet.dto.WalletRequest;
import com.digitalwallet.dto.WalletResponse;
import com.digitalwallet.entity.Transaction;
import com.digitalwallet.entity.User;
import com.digitalwallet.entity.Wallet;
import com.digitalwallet.exception.ConflictException;
import com.digitalwallet.exception.InsufficientBalanceException;
import com.digitalwallet.exception.InvalidTransferException;
import com.digitalwallet.exception.UserNotFoundException;
import com.digitalwallet.exception.WalletNotFoundException;
import com.digitalwallet.repository.TransactionRepository;
import com.digitalwallet.repository.UserRepository;
import com.digitalwallet.repository.WalletJdbcRepository;
import com.digitalwallet.repository.WalletRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class WalletService {

    private final WalletRepository walletRepository;
    private final UserRepository userRepository;
    private final WalletJdbcRepository walletJdbcRepository;
    private final TransactionRepository transactionRepository;
    private final JdbcTemplate jdbcTemplate;

    public WalletService(WalletRepository walletRepository, UserRepository userRepository,
                         WalletJdbcRepository walletJdbcRepository, TransactionRepository transactionRepository,
                         JdbcTemplate jdbcTemplate) {
        this.walletRepository = walletRepository;
        this.userRepository = userRepository;
        this.walletJdbcRepository = walletJdbcRepository;
        this.transactionRepository = transactionRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Transactional
    public List<WalletResponse> getAllWallets() {
        List<Wallet> wallets = walletRepository.findAll();
        wallets.forEach(this::syncWalletBalanceIfNeeded);
        return wallets.stream().map(this::toResponse).toList();
    }

    @Transactional
    public WalletResponse getWalletById(Long id) {
        Wallet wallet = findWallet(id);
        syncWalletBalanceIfNeeded(wallet);
        return toResponse(wallet);
    }

    @Transactional
    public WalletResponse createWallet(WalletRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new UserNotFoundException(request.userId()));
        if (walletRepository.existsByUserId(user.getId())) {
            throw new ConflictException("User " + user.getId() + " already has a wallet");
        }
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(BigDecimal.ZERO);
        return toResponse(walletRepository.save(wallet));
    }

    /** Calls the MySQL function get_wallet_balance(wallet_id). */
    public BalanceResponse getBalance(Long walletId) {
        if (!walletRepository.existsById(walletId)) {
            throw new WalletNotFoundException(walletId);
        }
        return new BalanceResponse(walletId, walletJdbcRepository.getWalletBalance(walletId));
    }

    /**
     * Calls the stored procedure transfer_money.
     * NOTE: intentionally NOT @Transactional - the procedure opens and commits its own
     * database transaction (a Spring transaction around it would be committed by START TRANSACTION).
     */
    public TransferResponse transfer(TransferRequest request) {
        Long senderId = request.senderWalletId();
        Long receiverId = request.receiverWalletId();

        // Friendly pre-checks (the procedure repeats them, so the DB stays the final authority)
        if (senderId.equals(receiverId)) {
            throw new InvalidTransferException("Sender and receiver wallets cannot be the same");
        }
        Wallet sender = findWallet(senderId);
        findWallet(receiverId);
        if (sender.getBalance().compareTo(request.amount()) < 0) {
            throw new InsufficientBalanceException();
        }

        walletJdbcRepository.transferMoney(senderId, receiverId, request.amount(), request.description());

        return new TransferResponse("Transfer completed successfully", senderId, receiverId, request.amount(),
                walletJdbcRepository.getWalletBalance(senderId),
                walletJdbcRepository.getWalletBalance(receiverId));
    }

    private Wallet findWallet(Long id) {
        return walletRepository.findById(id).orElseThrow(() -> new WalletNotFoundException(id));
    }

    private void syncWalletBalanceIfNeeded(Wallet wallet) {
        BigDecimal currentBalance = wallet.getBalance() == null ? BigDecimal.ZERO : wallet.getBalance();
        BigDecimal recalculatedBalance = calculateWalletBalance(wallet.getId());

        if (recalculatedBalance.compareTo(currentBalance) != 0) {
            jdbcTemplate.update("UPDATE wallets SET balance = ? WHERE id = ?", recalculatedBalance, wallet.getId());
            wallet.setBalance(recalculatedBalance);
        }
    }

    private BigDecimal calculateWalletBalance(Long walletId) {
        BigDecimal total = BigDecimal.ZERO;

        for (Transaction transaction : transactionRepository.findAll()) {
            if (!walletId.equals(transaction.getWallet().getId())) {
                continue;
            }

            switch (transaction.getTransactionType().getTypeName()) {
                case "DEPOSIT" -> total = total.add(transaction.getAmount());
                case "WITHDRAW" -> total = total.subtract(transaction.getAmount());
                case "TRANSFER" -> {
                    String description = transaction.getDescription() == null ? "" : transaction.getDescription().toLowerCase();
                    if (description.contains("sent to wallet")) {
                        total = total.subtract(transaction.getAmount());
                    } else if (description.contains("received from wallet")) {
                        total = total.add(transaction.getAmount());
                    }
                }
                default -> { }
            }
        }

        return total.max(BigDecimal.ZERO);
    }

    private WalletResponse toResponse(Wallet w) {
        return new WalletResponse(w.getId(), w.getUser().getId(), w.getUser().getName(),
                w.getBalance(), w.getCreatedAt());
    }
}
