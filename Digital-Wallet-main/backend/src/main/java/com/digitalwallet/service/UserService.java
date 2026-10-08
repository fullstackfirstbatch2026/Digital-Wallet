package com.digitalwallet.service;

import com.digitalwallet.dto.AboveAverageTransactionDto;
import com.digitalwallet.dto.UserRequest;
import com.digitalwallet.dto.UserResponse;
import com.digitalwallet.entity.User;
import com.digitalwallet.entity.Wallet;
import com.digitalwallet.exception.DuplicateEmailException;
import com.digitalwallet.exception.UserNotFoundException;
import com.digitalwallet.repository.ReportJdbcRepository;
import com.digitalwallet.repository.UserRepository;
import com.digitalwallet.repository.WalletRepository;
import java.math.BigDecimal;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final ReportJdbcRepository reportRepository;

    public UserService(UserRepository userRepository, WalletRepository walletRepository,
                       ReportJdbcRepository reportRepository) {
        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.reportRepository = reportRepository;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        return toResponse(findUser(id));
    }

    /** Creates the user AND an empty wallet (every user must have a wallet). */
    @Transactional
    public UserResponse createUser(UserRequest request) {
        String email = request.email().trim();
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateEmailException(email);
        }
        User user = new User();
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPhone(request.phone());
        user = userRepository.save(user);

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setBalance(BigDecimal.ZERO);
        walletRepository.save(wallet);

        return toResponse(user);
    }

    @Transactional
    public UserResponse updateUser(Long id, UserRequest request) {
        User user = findUser(id);
        String email = request.email().trim();
        if (userRepository.existsByEmailAndIdNot(email, id)) {
            throw new DuplicateEmailException(email);
        }
        user.setName(request.name().trim());
        user.setEmail(email);
        user.setPhone(request.phone());
        return toResponse(userRepository.save(user));
    }

    /** Fails with 409 if the user's wallet already has transactions (foreign key). */
    @Transactional
    public void deleteUser(Long id) {
        User user = findUser(id);
        userRepository.delete(user);
        userRepository.flush();
    }

    /** SUBQUERY report. */
    @Transactional(readOnly = true)
    public List<AboveAverageTransactionDto> getAboveAverageTransactions() {
        return reportRepository.findAboveAverageTransactions();
    }

    private User findUser(Long id) {
        return userRepository.findById(id).orElseThrow(() -> new UserNotFoundException(id));
    }

    private UserResponse toResponse(User u) {
        return new UserResponse(u.getId(), u.getName(), u.getEmail(), u.getPhone(), u.getCreatedAt());
    }
}
