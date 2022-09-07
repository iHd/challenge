package com.db.awmd.challenge.repository;

import com.db.awmd.challenge.domain.Account;
import com.db.awmd.challenge.exception.DuplicateAccountIdException;
import org.springframework.stereotype.Repository;

@Repository
public interface AccountsRepository {

  void createAccount(Account account) throws DuplicateAccountIdException;

  Account getAccount(String accountId);

  void clearAccounts();

  /**
   * Should be modified such as it always returns updated object
   * @param account
   */
  Account updateAccount(Account account);
}
