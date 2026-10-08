package com.digitalwallet.repository;

import com.digitalwallet.entity.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalletRepository extends JpaRepository<Wallet, Long> {
    boolean existsByUserId(Long userId);
}
