# FUCarRentingSystem — Hướng Dẫn Triển Khai

> **Môn học:** MSS301 — Assignment 01  
> **Kiến trúc:** Microservices + API Gateway  
> **Stack:** Spring Boot 3.3.x · Spring Cloud Gateway · MySQL · Docker · JWT

---

## Mục lục

1. [Tổng quan kiến trúc](#1-tổng-quan-kiến-trúc)
2. [Cấu trúc thư mục](#2-cấu-trúc-thư-mục)
3. [Phân công công việc](#3-phân-công-công-việc)
4. [API Contract chung](#4-api-contract-chung)
5. [Hướng dẫn triển khai từng service](#5-hướng-dẫn-triển-khai-từng-service)
   - [5.1 Customer Service (Member B)](#51-customer-service-member-b)
   - [5.2 Car Service (Member C)](#52-car-service-member-c)
   - [5.3 Renting Service (Member D)](#53-renting-service-member-d)
6. [API Gateway đã triển khai](#6-api-gateway-đã-triển-khai)
7. [Cách chạy hệ thống với Docker](#7-cách-chạy-hệ-thống-với-docker)
8. [Test với Postman](#8-test-với-postman)
9. [Git Workflow](#9-git-workflow)
10. [Xử lý lỗi thường gặp](#11-xử-lý-lỗi-thường-gặp)

---

## 1. Tổng quan kiến trúc

```
Client (Postman / Browser)
          │
          ▼  port 8080
  ┌───────────────────┐
  │    API Gateway     │  ← JWT Auth Filter + Routing
  └───┬───────┬───────┘
      │       │       │
      ▼       ▼       ▼
  :8081    :8082    :8083
 Customer   Car    Renting
 Service  Service  Service
    │        │        │
    └────────┴────────┘
             │
          MySQL :3306
   customer_db / car_db / renting_db
```

### Luồng xác thực JWT

```
1. Client  →  POST /auth/login  →  Gateway
2. Gateway kiểm tra: admin? → tạo JWT ngay
                    customer? → gọi CustomerService /internal/customers/authenticate
3. Gateway trả về: { token, role, userId, email }
4. Mọi request tiếp theo: Authorization: Bearer <token>
5. AuthFilter validate token → inject header X-User-Id, X-User-Role, X-User-Email
6. Downstream services đọc header X-User-Role để kiểm tra quyền
```

---

## 2. Cấu trúc thư mục

```
Assignment01_FUCarRenting/
├── docker-compose.yml          ← Chạy toàn bộ hệ thống
├── init.sql                    ← Tạo 3 schema MySQL
├── .gitignore
├── GUIDE.md                    ← File này
│
├── api-gateway/                ← ĐÃ TRIỂN KHAI ✅
│   ├── pom.xml
│   ├── Dockerfile
│   └── src/main/java/com/fucar/gateway/
│       ├── ApiGatewayApplication.java
│       ├── security/JwtUtil.java
│       ├── filter/AuthFilter.java
│       ├── controller/AuthController.java
│       ├── config/WebClientConfig.java
│       ├── exception/GlobalErrorHandler.java
│       └── dto/{LoginRequest, LoginResponse, CustomerAuthResponse}.java
│
├── customer-service/           ← SKELETON — Member B triển khai
│   ├── pom.xml  ✅
│   ├── Dockerfile  ✅
│   └── src/main/java/com/fucar/customer/
│       ├── CustomerServiceApplication.java  ✅
│       ├── entity/        ← Tạo Customer.java
│       ├── repository/    ← Tạo CustomerRepository.java
│       ├── service/       ← Tạo CustomerService.java
│       ├── controller/    ← Tạo CustomerController.java
│       ├── dto/           ← Tạo Request/Response DTOs
│       └── exception/     ← Tạo GlobalExceptionHandler.java
│
├── car-service/                ← SKELETON — Member C triển khai
│   ├── pom.xml  ✅
│   ├── Dockerfile  ✅
│   └── src/main/java/com/fucar/car/
│       ├── CarServiceApplication.java  ✅
│       ├── entity/        ← CarInformation, Manufacturer, Supplier
│       ├── repository/
│       ├── service/
│       ├── controller/
│       ├── dto/
│       └── exception/
│
└── renting-service/            ← SKELETON — Member D triển khai
    ├── pom.xml  ✅
    ├── Dockerfile  ✅
    └── src/main/java/com/fucar/renting/
        ├── RentingServiceApplication.java  ✅
        ├── entity/        ← RentingTransaction, RentingDetail
        ├── repository/
        ├── service/
        ├── controller/
        ├── client/        ← Feign clients: CarClient, CustomerClient
        ├── dto/
        └── exception/
```

---

## 3. Phân công công việc

| Member | Service | Nhiệm vụ | Branch |
|--------|---------|----------|--------|
| **A** | API Gateway | ✅ Đã hoàn thành: JWT, AuthFilter, routing, error handler | `feature/api-gateway` |
| **B** | Customer Service | Entity, Repository, Service, Controller, BCrypt password, internal authenticate endpoint | `feature/customer-service` |
| **C** | Car Service | CarInformation, Manufacturer, Supplier CRUD | `feature/car-service` |
| **D** | Renting Service | RentingTransaction, RentingDetail, Feign clients, báo cáo thống kê | `feature/renting-service` |

### Contract quan trọng phải thống nhất trước khi code

| Endpoint nội bộ | Service | Mô tả |
|-----------------|---------|-------|
| `POST /internal/customers/authenticate` | Customer | Gateway gọi để xác thực customer login |
| `GET /internal/cars/{id}` | Car | Renting Service gọi để lấy thông tin xe |
| Headers forwarded | Gateway → All | `X-User-Id`, `X-User-Role`, `X-User-Email` |

---

## 4. API Contract chung

### Response lỗi chuẩn (áp dụng cho tất cả service)

```json
{
  "timestamp": "2026-06-11T10:00:00",
  "status": 400,
  "error": "Bad Request",
  "message": "CustomerName must not be blank",
  "path": "/api/customers"
}
```

### Tất cả endpoints (qua Gateway port 8080)

| Method | URL | Auth | Role |
|--------|-----|------|------|
| POST | `/auth/login` | None | — |
| POST | `/api/customers/register` | None | — |
| GET | `/api/customers` | Bearer | ADMIN |
| GET | `/api/customers/{id}` | Bearer | ADMIN / Owner |
| PUT | `/api/customers/{id}` | Bearer | ADMIN / Owner |
| DELETE | `/api/customers/{id}` | Bearer | ADMIN |
| GET | `/api/cars` | Bearer | All |
| GET | `/api/cars/{id}` | Bearer | All |
| POST | `/api/cars` | Bearer | ADMIN |
| PUT | `/api/cars/{id}` | Bearer | ADMIN |
| DELETE | `/api/cars/{id}` | Bearer | ADMIN |
| GET | `/api/manufacturers` | Bearer | All |
| POST | `/api/manufacturers` | Bearer | ADMIN |
| GET | `/api/suppliers` | Bearer | All |
| POST | `/api/suppliers` | Bearer | ADMIN |
| POST | `/api/rentings` | Bearer | CUSTOMER |
| GET | `/api/rentings/my` | Bearer | CUSTOMER |
| GET | `/api/rentings` | Bearer | ADMIN |
| GET | `/api/rentings/report?start=&end=` | Bearer | ADMIN |

---

## 5. Hướng dẫn triển khai từng service

---

### 5.1 Customer Service (Member B)

**Package:** `com.fucar.customer`  
**Port:** `8081`  
**Database:** `customer_db`

#### Bước 1 — Tạo Entity

```java
// entity/Customer.java
@Entity
@Table(name = "customers")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer customerId;

    @Column(nullable = false)
    private String customerName;

    private String telephone;

    @Column(unique = true, nullable = false)
    private String email;

    private LocalDate customerBirthday;

    @Builder.Default
    private Integer customerStatus = 1;  // 1=active, 0=inactive

    @Column(nullable = false)
    private String password;  // stored as BCrypt hash
}
```

#### Bước 2 — Tạo Repository

```java
// repository/CustomerRepository.java
public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    Optional<Customer> findByEmail(String email);
    boolean existsByEmail(String email);
}
```

#### Bước 3 — Tạo DTOs

```java
// dto/CustomerRequest.java
@Data
public class CustomerRequest {
    @NotBlank private String customerName;
    private String telephone;
    @Email @NotBlank private String email;
    private LocalDate customerBirthday;
    @NotBlank @Size(min = 6) private String password;
}

// dto/CustomerResponse.java
@Data @Builder
public class CustomerResponse {
    private Integer customerId;
    private String customerName;
    private String telephone;
    private String email;
    private LocalDate customerBirthday;
    private Integer customerStatus;
}

// dto/AuthenticateRequest.java (dùng cho internal endpoint)
@Data
public class AuthenticateRequest {
    private String email;
    private String password;
}

// dto/AuthenticateResponse.java
@Data @AllArgsConstructor
public class AuthenticateResponse {
    private Long customerId;
    private String email;
}
```

#### Bước 4 — Tạo Service

```java
// service/CustomerService.java
@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final PasswordEncoder passwordEncoder;  // inject BCryptPasswordEncoder

    public CustomerResponse register(CustomerRequest request) {
        if (customerRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }
        Customer customer = Customer.builder()
                .customerName(request.getCustomerName())
                .telephone(request.getTelephone())
                .email(request.getEmail())
                .customerBirthday(request.getCustomerBirthday())
                .password(passwordEncoder.encode(request.getPassword()))
                .build();
        return toResponse(customerRepository.save(customer));
    }

    public AuthenticateResponse authenticate(AuthenticateRequest request) {
        Customer customer = customerRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));
        if (!passwordEncoder.matches(request.getPassword(), customer.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }
        return new AuthenticateResponse(customer.getCustomerId().longValue(), customer.getEmail());
    }

    public List<CustomerResponse> getAllCustomers() { ... }
    public CustomerResponse getById(Integer id) { ... }
    public CustomerResponse update(Integer id, CustomerRequest request) { ... }
    public void delete(Integer id) { ... }

    private CustomerResponse toResponse(Customer c) {
        return CustomerResponse.builder()
                .customerId(c.getCustomerId())
                .customerName(c.getCustomerName())
                .email(c.getEmail())
                // ...
                .build();
    }
}
```

#### Bước 5 — Tạo Config BCrypt

```java
// config/PasswordConfig.java
@Configuration
public class PasswordConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

#### Bước 6 — Tạo Controller

```java
// controller/CustomerController.java
@RestController
@RequestMapping
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    // PUBLIC — không cần token
    @PostMapping("/api/customers/register")
    public ResponseEntity<CustomerResponse> register(@Valid @RequestBody CustomerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(customerService.register(request));
    }

    // INTERNAL — chỉ Gateway gọi (không cần JWT, không expose qua Gateway route)
    @PostMapping("/internal/customers/authenticate")
    public ResponseEntity<AuthenticateResponse> authenticate(@RequestBody AuthenticateRequest request) {
        return ResponseEntity.ok(customerService.authenticate(request));
    }

    // ADMIN only
    @GetMapping("/api/customers")
    public ResponseEntity<List<CustomerResponse>> getAll(
            @RequestHeader("X-User-Role") String role) {
        if (!"ADMIN".equals(role)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        return ResponseEntity.ok(customerService.getAllCustomers());
    }

    // ADMIN or Owner
    @GetMapping("/api/customers/{id}")
    public ResponseEntity<CustomerResponse> getById(
            @PathVariable Integer id,
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Id") String userId) {
        if (!"ADMIN".equals(role) && !userId.equals(String.valueOf(id))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(customerService.getById(id));
    }

    @PutMapping("/api/customers/{id}")
    public ResponseEntity<CustomerResponse> update(
            @PathVariable Integer id,
            @Valid @RequestBody CustomerRequest request,
            @RequestHeader("X-User-Role") String role,
            @RequestHeader("X-User-Id") String userId) {
        if (!"ADMIN".equals(role) && !userId.equals(String.valueOf(id))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(customerService.update(id, request));
    }

    @DeleteMapping("/api/customers/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable Integer id,
            @RequestHeader("X-User-Role") String role) {
        if (!"ADMIN".equals(role)) return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        customerService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
```

#### Bước 7 — Global Exception Handler

```java
// exception/GlobalExceptionHandler.java
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<Map<String, Object>> handleResponseStatus(
            ResponseStatusException ex, HttpServletRequest request) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", ex.getStatusCode().value());
        body.put("error", ex.getMessage());
        body.put("message", ex.getReason());
        body.put("path", request.getRequestURI());
        return ResponseEntity.status(ex.getStatusCode()).body(body);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .collect(Collectors.joining(", "));
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", 400);
        body.put("error", "Validation Failed");
        body.put("message", message);
        body.put("path", request.getRequestURI());
        return ResponseEntity.badRequest().body(body);
    }
}
```

---

### 5.2 Car Service (Member C)

**Package:** `com.fucar.car`  
**Port:** `8082`  
**Database:** `car_db`

#### Entities cần tạo

```java
// entity/Supplier.java
@Entity @Table(name = "suppliers") @Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Supplier {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer supplierId;
    private String supplierName;
    private String supplierDescription;
    private String supplierAddress;
}

// entity/Manufacturer.java
@Entity @Table(name = "manufacturers") @Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Manufacturer {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer manufacturerId;
    private String manufacturerName;
    private String description;
    private String manufacturerCountry;
}

// entity/CarInformation.java
@Entity @Table(name = "car_information") @Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CarInformation {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer carId;
    private String carName;
    private String carDescription;
    private Integer numberOfDoors;
    private Integer seatingCapacity;
    private String fuelType;
    private Integer year;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manufacturer_id")
    private Manufacturer manufacturer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id")
    private Supplier supplier;

    private Integer carStatus;  // 1=available, 0=unavailable
    private BigDecimal carRentingPricePerDay;
}
```

#### Endpoint nội bộ (Renting Service sẽ gọi)

```java
// Thêm vào CarController
@GetMapping("/internal/cars/{id}")
public ResponseEntity<CarResponse> getCarInternal(@PathVariable Integer id) {
    return ResponseEntity.ok(carService.getById(id));
}
```

#### Kiểm tra role trong Controller (tương tự Customer Service)

```java
@PostMapping("/api/cars")
public ResponseEntity<CarResponse> create(
        @Valid @RequestBody CarRequest request,
        @RequestHeader("X-User-Role") String role) {
    if (!"ADMIN".equals(role)) return ResponseEntity.status(403).build();
    return ResponseEntity.status(201).body(carService.create(request));
}
```

---

### 5.3 Renting Service (Member D)

**Package:** `com.fucar.renting`  
**Port:** `8083`  
**Database:** `renting_db`

#### Entities cần tạo

```java
// entity/RentingTransaction.java
@Entity @Table(name = "renting_transactions") @Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RentingTransaction {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer rentingTransactionId;
    private LocalDate rentingDate;
    private BigDecimal totalPrice;
    private Integer customerId;  // No FK — cross-service reference
    private String rentingStatus; // "PENDING", "ACTIVE", "COMPLETED", "CANCELLED"

    @OneToMany(mappedBy = "transaction", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<RentingDetail> details;
}

// entity/RentingDetail.java
@Entity @Table(name = "renting_details")
@IdClass(RentingDetailId.class)
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class RentingDetail {
    @Id
    private Integer rentingTransactionId;
    @Id
    private Integer carId;

    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal price;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "renting_transaction_id", insertable = false, updatable = false)
    private RentingTransaction transaction;
}

// entity/RentingDetailId.java (Composite PK)
@Data @NoArgsConstructor @AllArgsConstructor
public class RentingDetailId implements Serializable {
    private Integer rentingTransactionId;
    private Integer carId;
}
```

#### Feign Clients

```java
// client/CarClient.java
@FeignClient(name = "car-service", url = "${app.service.car}")
public interface CarClient {
    @GetMapping("/internal/cars/{id}")
    CarResponse getCarById(@PathVariable Integer id);
}

// client/CustomerClient.java
@FeignClient(name = "customer-service", url = "${app.service.customer}")
public interface CustomerClient {
    @GetMapping("/internal/customers/{id}")
    CustomerResponse getCustomerById(@PathVariable Integer id);
}
```

> **Lưu ý:** Cần thêm `GET /internal/customers/{id}` vào Customer Service để Renting Service có thể validate customer.

#### Report Endpoint

```java
// repository/RentingDetailRepository.java
public interface RentingDetailRepository extends JpaRepository<RentingDetail, RentingDetailId> {

    @Query("SELECT rd FROM RentingDetail rd " +
           "JOIN FETCH rd.transaction rt " +
           "WHERE rd.startDate >= :startDate AND rd.endDate <= :endDate " +
           "ORDER BY rt.totalPrice DESC")
    List<RentingDetail> findByDateRangeOrderByTotalPriceDesc(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate);
}

// controller
@GetMapping("/api/rentings/report")
public ResponseEntity<List<RentingDetailResponse>> report(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate start,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate end,
        @RequestHeader("X-User-Role") String role) {
    if (!"ADMIN".equals(role)) return ResponseEntity.status(403).build();
    return ResponseEntity.ok(rentingService.getReport(start, end));
}
```

---

## 6. API Gateway đã triển khai

File đã có sẵn, **không cần sửa** trừ khi muốn thêm route mới.

| File | Mô tả |
|------|-------|
| [api-gateway/src/.../security/JwtUtil.java](api-gateway/src/main/java/com/fucar/gateway/security/JwtUtil.java) | Tạo và validate JWT token |
| [api-gateway/src/.../filter/AuthFilter.java](api-gateway/src/main/java/com/fucar/gateway/filter/AuthFilter.java) | Global filter: validate token, inject headers |
| [api-gateway/src/.../controller/AuthController.java](api-gateway/src/main/java/com/fucar/gateway/controller/AuthController.java) | `POST /auth/login` |
| [api-gateway/src/main/resources/application.properties](api-gateway/src/main/resources/application.properties) | Routes, JWT config, admin credentials |

### Thay đổi admin password

Sửa trong [api-gateway/src/main/resources/application.properties](api-gateway/src/main/resources/application.properties):

```properties
app.admin.email=admin@fucar.com
app.admin.password=Admin@123
```

Hoặc override qua Docker env var:
```yaml
environment:
  APP_ADMIN_EMAIL: admin@fucar.com
  APP_ADMIN_PASSWORD: YourStrongPassword
```

---

## 7. Cách chạy hệ thống với Docker

### Yêu cầu
- Docker Desktop đang chạy
- Maven (hoặc để Docker build)

### Bước 1 — Build từng service (trong folder của service)

```bash
cd customer-service
mvn package -DskipTests
cd ../car-service
mvn package -DskipTests
cd ../renting-service
mvn package -DskipTests
cd ../api-gateway
mvn package -DskipTests
```

### Bước 2 — Chạy toàn bộ hệ thống

```bash
# Từ thư mục gốc (Assignment01_FUCarRenting/)
docker-compose up --build
```

### Bước 3 — Kiểm tra

```
MySQL:           localhost:3306
Customer Service: localhost:8081
Car Service:      localhost:8082
Renting Service:  localhost:8083
API Gateway:      localhost:8080  ← Điểm vào duy nhất
```

### Dừng hệ thống

```bash
docker-compose down
# Xóa cả data:
docker-compose down -v
```

### Chạy riêng lẻ để dev (không dùng Docker)

```bash
# 1. Start MySQL local (port 3306)
# 2. Chạy init.sql để tạo schema
# 3. Trong từng service folder:
mvn spring-boot:run
```

---

## 8. Test với Postman

### Login Admin

```http
POST http://localhost:8080/auth/login
Content-Type: application/json

{
  "email": "admin@fucar.com",
  "password": "Admin@123"
}
```

Response:
```json
{
  "token": "eyJhbGci...",
  "role": "ADMIN",
  "userId": 0,
  "email": "admin@fucar.com"
}
```

### Dùng token trong request tiếp theo

```
Authorization: Bearer eyJhbGci...
```

### Register Customer

```http
POST http://localhost:8080/api/customers/register
Content-Type: application/json

{
  "customerName": "Nguyen Van A",
  "telephone": "0901234567",
  "email": "customer@test.com",
  "customerBirthday": "2000-01-15",
  "password": "password123"
}
```

### Login Customer

```http
POST http://localhost:8080/auth/login
Content-Type: application/json

{
  "email": "customer@test.com",
  "password": "password123"
}
```

### Tạo xe mới (Admin)

```http
POST http://localhost:8080/api/cars
Authorization: Bearer <ADMIN_TOKEN>
Content-Type: application/json

{
  "carName": "Toyota Camry",
  "carDescription": "Sedan 5 chỗ",
  "numberOfDoors": 4,
  "seatingCapacity": 5,
  "fuelType": "Petrol",
  "year": 2023,
  "manufacturerId": 1,
  "supplierId": 1,
  "carStatus": 1,
  "carRentingPricePerDay": 1500000
}
```

### Tạo Renting Transaction (Customer)

```http
POST http://localhost:8080/api/rentings
Authorization: Bearer <CUSTOMER_TOKEN>
Content-Type: application/json

{
  "details": [
    {
      "carId": 1,
      "startDate": "2026-07-01",
      "endDate": "2026-07-05"
    }
  ]
}
```

### Xem báo cáo (Admin)

```http
GET http://localhost:8080/api/rentings/report?start=2026-01-01&end=2026-12-31
Authorization: Bearer <ADMIN_TOKEN>
```

---

## 9. Git Workflow

```
main
 └── develop
      ├── feature/api-gateway        (Member A — đã merge ✅)
      ├── feature/customer-service   (Member B)
      ├── feature/car-service        (Member C)
      └── feature/renting-service    (Member D)
```

### Commit message convention

```
feat(customer): add register endpoint
fix(car): fix null pointer in CarService
chore(gateway): update application.properties
docs: update GUIDE.md
```

### Quy trình merge

1. Code xong → `git push origin feature/<service>`
2. Tạo Pull Request vào `develop`
3. Ít nhất 1 thành viên khác review
4. Merge sau khi approved

---

## 10. Xử lý lỗi thường gặp

### Lỗi: `Connection refused` khi Gateway gọi Customer Service

- Kiểm tra Customer Service có đang chạy không
- Trong Docker: service name phải khớp với `docker-compose.yml` (dùng `customer-service` không phải `localhost`)

### Lỗi: `401 Unauthorized` khi gọi API

- Token hết hạn → login lại
- Thiếu header `Authorization: Bearer <token>`
- Token sai secret → đảm bảo `app.jwt.secret` giống nhau ở tất cả service

### Lỗi: `403 Forbidden`

- Role không đúng (Customer gọi Admin endpoint)
- Header `X-User-Role` không được forward → kiểm tra AuthFilter

### Lỗi: `Could not create connection to database server`

- MySQL chưa ready → đợi healthcheck pass
- Schema chưa tạo → chạy `init.sql` thủ công

### Build lỗi: `symbol not found` (Lombok)

- Bật annotation processing trong IDE:
  - IntelliJ: Settings → Build → Compiler → Annotation Processors → Enable

### Port bị chiếm

```bash
# Windows
netstat -ano | findstr :8080
taskkill /PID <pid> /F
```
