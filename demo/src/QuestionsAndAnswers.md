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
10. ## `@Repository` in java sprint boot why to use it?
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
11. ## Why to use Record in Java?
    - 💡 Think of Records as Java’s way of saying: “Sometimes you just need a clean, immutable data bag without ceremony.”
    - Here’s why you’d use them:
      - Conciseness ✨ Records automatically generate constructors, equals(), hashCode(), and toString() methods. No need to write all that boilerplate yourself. 
      - Immutability by default 🔒 Fields in a record are final. Once created, the data can’t be changed, which makes reasoning about state much easier — especially in concurrent or distributed systems. 
      - Clear intent 📖 Declaring something as a record signals: this is just a data carrier. It’s not meant to hold business logic, only state. 
      - Pattern matching synergy 🧩 Records integrate beautifully with Java’s newer features like pattern matching for switch and instanceof. That makes destructuring and working with data much more intuitive. 
      - Serialization and DTOs 📦 Perfect for modeling request/response objects in APIs, database row mappings, or configuration values where you just need to carry data around.
      - | Feature                                | Record (Java 16+)                                                            | Regular Class                          | Lombok `@Data`                                              |
        |----------------------------------------|------------------------------------------------------------------------------|----------------------------------------|-------------------------------------------------------------|
        | **Boilerplate**                        | Minimal — constructor, `equals()`, `hashCode()`, `toString()` auto‑generated | High — must write all methods manually | Reduced — Lombok generates boilerplate at compile time      |
        | **Immutability**                       | Fields are `final` by default → immutable                                    | Mutable unless explicitly coded        | Mutable by default, but can be made immutable with `@Value` |
        | **Intent clarity**                     | Signals “data carrier only”                                                  | Can mix data + behavior                | Can mix data + behavior                                     |
        | **Integration with new Java features** | Works with pattern matching, sealed classes, switch expressions              | No special integration                 | No special integration                                      |
        | **Dependencies**                       | Pure Java, no external library                                               | Pure Java                              | Requires Lombok dependency and IDE support                  |
        | **Serialization / DTO use**            | Excellent for DTOs, API payloads, configs                                    | Works but verbose                      | Works well, especially for DTOs                             |
        | **Extensibility**                      | Cannot extend other classes (records are implicitly `final`)                 | Fully extensible                       | Fully extensible                                            |
        | **Readability**                        | Very concise, intent obvious                                                 | Verbose                                | Concise, but requires Lombok knowledge                      |
12. ## RequestBody in Java spring boot
    - In Spring Boot, the @RequestBody annotation is used to bind the body of an HTTP request (usually JSON or XML) directly to a Java object. 
    - It tells Spring to automatically deserialize the request payload into the method parameter using an HttpMessageConverter.
    - | Feature                | @RequestBody                                        | @RequestParam                                                                 | @PathVariable                                             |
      |------------------------|-----------------------------------------------------|-------------------------------------------------------------------------------|-----------------------------------------------------------|
      | **Purpose**            | Binds the entire HTTP request body to a Java object | Extracts query parameters from the URL                                        | Extracts values from the URI path                         |
      | **Typical Use Case**   | Handling JSON/XML payloads in POST/PUT requests     | Reading simple key-value pairs from query string                              | Mapping dynamic segments of the URL                       |
      | **Data Format**        | JSON, XML, or raw body content                      | String, int, boolean, etc. (simple types)                                     | String, int, etc. (path variables)                        |
      | **Example URL**        | POST `/accounts/create` with JSON body              | GET `/search?keyword=java&limit=10`                                           | GET `/accounts/123`                                       |
      | **Controller Example** | `public String create(@RequestBody Account acc)`    | `public String search(@RequestParam String keyword, @RequestParam int limit)` | `public String get(@PathVariable("id") String accountId)` |
      | **Validation**         | Works with `@Valid` for complex DTOs                | Works with `@Valid` only if bound to object                                   | Works with `@Valid` only if bound to object               |
      | **Best For**           | Complex request payloads (DTOs, JSON objects)       | Optional/simple query parameters                                              | Identifiers in RESTful resource paths                     |
