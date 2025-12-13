# ✅ Why not just return Account or null?
- Because:
Returning null is error‑prone
Returning Account forces you to invent a fake “empty” object
Returning Optional<Account> makes the contract explicit: “This method may return nothing. Handle it.”
How to handle Optional in Java? Use methods like isPresent(), ifPresent(), orElse(), orElseGet(), orElseThrow() to work with the value safely.
- ✅ 1. Throw an exception if not found 
- ✅ 2. Provide a default value 
- ✅ 3. Execute a block of code if present

# ✅ How to handle database errors?
1. Optional is NOT for database errors. It only represents “no row found”.
2. Database errors (connection failure, SQL exception, lock timeout, etc.) are handled by Spring:
   - ✅ Spring Data JPA wraps DB errors in DataAccessException 
     - Examples: CannotAcquireLockException, QueryTimeoutException, DataIntegrityViolationException, JpaSystemException
   - You handle them with: 
     - Try/catch:
       - ```java
          try {
          Optional<Account> acc = accountRepository.findByIdForUpdate(id);
          } catch (DataAccessException ex) {
          // log, retry, or throw custom exception
          }```
  
       - Or use @Transactional + global exception handler
           - ```java
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
   - ✅ Use @Transactional + Global Exception Handler for most cases. 
     - Centralized error handling, Works well with Spring's transaction management, Cleaner service methods. 
   - ✅ Use try/catch only when you can meaningfully recover from the error inside the method.
     - ```java 
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
 
4. ✅ Recommended Architecture (Best Practice)
   -
      | Layer          | How to handle errors                                                                    |
      |----------------|-----------------------------------------------------------------------------------------|
      | Controller     | No try/catch → rely on global exception handler                                         |
      | Service        | Use `@Transactional`, throw domain exceptions, no try/catch unless recovery is possible |
      | Repository     | Never use try/catch; let Spring translate DB errors                                     |
      | Global Handler | Convert exceptions → HTTP responses                                                     |
5. ## ✅ What CQRS Actually Is (Not the Buzzword Version)
   - CQRS = Command Query Responsibility Segregation. 
   - It means:
     - Commands (writes) and Queries (reads) are handled by different models, possibly different databases, and often different services. 
     - The write side focuses on business rules, invariants, and correctness. 
     - The read side focuses on speed, denormalization, and query flexibility. 
     - This separation exists because reads and writes have fundamentally different requirements.
   - 🏛️ What Is a CQRS Write Store?
     - The write store is the database (or persistence mechanism) used only for handling commands:
     - ✅ Create ✅ Update ✅ Delete ✅ Domain logic ✅ Validation ✅ Consistency ✅ Transactions ✅ Concurrency control 
     - It is not optimized for queries. It is optimized for correctness. 
     - Think of it as the source of truth.  
   - 🧱 What Does the Write Store Usually Look Like?
     - There are two common patterns:
       - ✅ 1. State‑based write store (traditional)
         - You store the current state of aggregates. 
           - Example: Account, Order, User, InventoryItem
         - This is what you see in typical Spring Boot + JPA systems. 
           - The write store ensures:
             - ACID transactions 
             - Pessimistic or optimistic locking 
             - Referential integrity 
             - Domain invariants
       - ✅ 2. Event‑sourced write store (advanced)
         - Instead of storing the current state, you store events: AccountCreated, MoneyDeposited, MoneyWithdrawn, OrderPlaced, OrderCancelled 
         - The write store becomes an append‑only event log. 
         - The current state is reconstructed by replaying events. 
         - This gives you:
           - Perfect audit log 
           - Time travel 
           - Easy replication 
           - Guaranteed consistency 
           - Natural integration with message buses
     - 🧩 Why Separate the Write Store From the Read Store? Because they have opposite goals.
       - 
         | Write Store                     | Read Store                     |
         |---------------------------------|--------------------------------|
         | Enforce business rules          | Serve queries fast             |
         | Normalize data                  | Denormalize data               |
         | ACID                            | Eventually consistent          |
         | Small, focused aggregates       | Wide, query\-optimized views   |
         | Often relational                | Often NoSQL / search engine    |
         | Low write latency               | High read throughput           |
     -  ## How Data Flows in a CQRS System
       - ### Client sends a command
         - Example: WithdrawMoney 
       - ### Write model validates and persists
         - Check balance 
         - Check card status 
         - Apply business rules 
         - Save to write store (state or events)
       - ### Write model emits an event
         - Example: MoneyWithdrawn
       - ### Event handlers update the read store 
         - Update balance projection 
         - Update transaction history 
         - Update dashboards 
         - Update search indexes
       - ### Read model serves queries 
         - Fast, denormalized, optimized for UI.
     - ## Why This Matters in System Design Interviews
       - When someone says: “Use a CQRS write store.” 
       - They’re implying:
         - You need strong consistency on writes 
         - You need scalable, flexible reads 
         - You may need event sourcing 
         - You want separation of concerns 
         - You want auditability 
         - You want horizontal scalability 
         - It’s a signal that the system has high write complexity and high read volume.
6. ## How the Webclient in Java Spring boot works?
   - How to make the java springboot service or apis async
7. ## How to Write a code for SAGA pattern to avoid distributed locking or 2PC?
8. ## <? extends X> is used because generics in Java are invariant. 
   - ### What does “generics in Java are invariant” mean?
     - Invariant means: List<Dog> is not a subtype of List<Animal>, even though Dog is a subtype of Animal. 
     - In other words, Java does not automatically treat Generic<Subclass> as compatible with Generic<Superclass>. 
     - That’s why you need wildcards (? extends T or ? super T) to express variance..
       - ```java
          import java.util.Optional;
          import java.util.function.Supplier;
    
          public class Demo {
          public static void main(String[] args) {
          Optional<String> opt = Optional.empty();

          // Case 1: Supplier<X>
          Supplier<RuntimeException> supplier1 = IllegalArgumentException::new;

          // ❌ This would fail if orElseThrow expected Supplier<RuntimeException>
          // because Supplier<IllegalArgumentException> is not assignable to Supplier<RuntimeException>
          // String value = opt.orElseThrow(supplier1);

          // Case 2: Supplier<? extends RuntimeException>
          Supplier<? extends RuntimeException> supplier2 = IllegalArgumentException::new;

          // ✅ This works because IllegalArgumentException extends RuntimeException
          String value = opt.orElseThrow(supplier2);

          System.out.println("Result: " + value);
          }
         }

9. ## Why command pattern is used for Withdraw action? How to make a intuition of it. 
   - When to use command pattern in building a software system
   - 🎯 What the Command pattern really does 
     - The Command pattern turns an action into a first‑class object. 
       - Instead of just calling a method like withdraw(account, amount), you wrap the request into a WithdrawCommand object that has:
     - All the data needed (accountId, amount, txId)
     - The logic to perform the action (execute())
     - A uniform interface (TransactionCommand)
     - So now, “Withdraw $100 from account A” is not just a method call — it’s an object you can pass around, store, log, retry, or undo.
   - 🏦 Why it makes sense for ATM transactions 
     - Think about what happens in a real ATM:
       - A customer requests an operation (withdraw, deposit, transfer). 
       - The ATM system must validate, execute, log, and possibly roll back that operation. 
       - Each transaction type is different, but they all share the same lifecycle: create → execute → persist → audit. 
     - The Command pattern captures this perfectly:
       - Encapsulation: Each transaction knows how to execute itself. 
       - Uniformity: All transactions look the same (execute()), so the ATM service can treat them polymorphically. 
       - Extensibility: Adding a new transaction type (e.g., BillPaymentCommand) doesn’t break existing code. 
       - Cross‑cutting concerns: You can queue commands, retry them, log them, or even undo them.
   - 🧠 How to build intuition for Command pattern 
     - Here’s the mental shortcut:
       - 👉 Use Command when your domain revolves around requests/actions that must be executed, logged, retried, or undone. 
       - Some classic triggers:
         - You want to decouple the invoker/client (ATM controller) from the actual execution logic. 
         - You need to queue or schedule actions (e.g., batch transactions). 
         - You want to log/audit every action (finance, compliance). 
         - You may need undo/compensation (failed transfers). 
         - You want a uniform interface for many different operations.
10. `@Repository` in java sprint boot why to use it?
    - 🧱 What @Repository Actually Does 
      - Specialization of @Component 
        - Like @Component, it makes the class a Spring bean. 
        - But it semantically signals: “This is a DAO / repository class.”
      - Exception Translation
        - JDBC, JPA, Hibernate, etc. throw vendor‑specific exceptions. 
        - @Repository triggers Spring’s PersistenceExceptionTranslationPostProcessor, which converts them into Spring’s consistent DataAccessException. 
        - This means your service layer doesn’t need to worry about vendor‑specific exception types. 
      - Layered Architecture Clarity 
        - @Controller → web layer 
        - @Service → business logic 
        - @Repository → data access layer Using these annotations makes your codebase easier to read and maintain.