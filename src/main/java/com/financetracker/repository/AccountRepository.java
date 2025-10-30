package com.financetracker.repository;

import com.financetracker.model.Account;
import com.financetracker.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByUserAndIsActiveTrue(User user);
    List<Account> findByUser(User user);
}