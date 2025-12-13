package com.practice.atm.repositories;

import com.practice.atm.domain.models.Account;

import java.util.Optional;

// Why to use Optional? To represent a value that may or may not be present, avoiding null checks.
// ForUpdate is used to lock the selected row in the database for update, preventing other transactions from modifying it until the current transaction is complete.

/*

✅ Why not just return Account or null?
Because:
Returning null is error‑prone
Returning Account forces you to invent a fake “empty” object
Returning Optional<Account> makes the contract explicit: “This method may return nothing. Handle it.”
How to handle Optional in Java? Use methods like isPresent(), ifPresent(), orElse(), orElseGet(), orElseThrow() to work with the value safely.
    ✅ 1. Throw an exception if not found
    ✅ 2. Provide a default value
    ✅ 3. Execute a block of code if present

✅ How to handle database errors?
    1. Optional is NOT for database errors. It only represents “no row found”.
    2. Database errors (connection failure, SQL exception, lock timeout, etc.) are handled by Spring:
    ✅ Spring Data JPA wraps DB errors in DataAccessException
        Examples: CannotAcquireLockException, QueryTimeoutException, DataIntegrityViolationException, JpaSystemException
        You handle them with:
            ✅ Try/catch:
            try {
                Optional<Account> acc = accountRepository.findByIdForUpdate(id);
            } catch (DataAccessException ex) {
                // log, retry, or throw custom exception
            }
            ✅ Or use @Transactional + global exception handler

            @Service
            @Transactional
            public class AccountService {

                public void process(String id) {
                    Account acc = accountRepository.findByIdForUpdate(id)
                            .orElseThrow(() -> new IllegalArgumentException("Account not found"));
                }
            }

            @ControllerAdvice
            public class GlobalExceptionHandler {

                @ExceptionHandler(DataAccessException.class)
                public ResponseEntity<String> handleDbErrors(DataAccessException ex) {
                    return ResponseEntity.status(500).body("Database error occurred");
                }
            }

     3. What is better handling with try/catch or use @transactional + global exception handler?
        ✅ Use @Transactional + Global Exception Handler for most cases.
            Centralized error handling, Works well with Spring's transaction management, Cleaner service methods.
        ✅ Use try/catch only when you can meaningfully recover from the error inside the method.
            ✅ 1. Retry logic
                try {
                    repo.save(account);
                } catch (CannotAcquireLockException e) {
                    retrySave(account);
                }

            ✅ 2. Fallback mechanisms
                try {
                    fraudService.checkWithdraw(tx);
                } catch (FraudServiceUnavailableException e) {
                    log.warn("Fraud service down, skipping check");
                }

            ✅ 3. Custom logging for specific operations
                try {
                    repo.save(account);
                } catch (DataIntegrityViolationException e) {
                    throw new DuplicateAccountException("Account already exists");
                }

    ✅ Recommended Architecture (Best Practice)
        Layer	        How to handle errors
        Controller      No try/catch → rely on global exception handler
        Service	        Use @Transactional, throw domain exceptions, no try/catch unless recovery is possible
        Repository	    Never use try/catch; let Spring translate DB errors
        Global Handler	Convert exceptions → HTTP responses

*/

public interface AccountRepository {
    Optional<Account> findByIdForUpdate(String accountId);
    void save(Account account);
}
