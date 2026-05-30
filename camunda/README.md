# Personal Loan Approval — Camunda 8 + Spring Boot Demo

A step-by-step demo project demonstrating Camunda 8 process automation integrated with Spring Boot, H2 database, and DMN decision tables.

---

## Prerequisites

| Requirement | Version | Notes |
|---|---|---|
| Java | 21 | [Download](https://adoptium.net/) |
| Maven | 3.8+ | Bundled via `mvnw` |
| Camunda 8 (Self-Managed) | 8.x | Running locally |
| Camunda Modeler | 5.46.1+ | For BPMN/DMN/Form editing and deployment |

### Camunda 8 Self-Managed — Required Ports

Ensure the following ports are available and running before starting the application:

| Port | Component | Purpose |
|---|---|---|
| `26500` | Zeebe Gateway | gRPC — Spring Boot connects here |
| `8080` | Operate / Tasklist | UI for process monitoring and task management |
| `9600` | Zeebe Monitoring | Health and metrics |

Verify with:
```
netstat -ano | findstr "26500 8080 9600"
```

---

## Running the Application

```bash
cd demo
mvn spring-boot:run
```

Application starts on **http://localhost:8082**

---

## Spring Boot Configuration

Located at `src/main/resources/application.properties`:

```properties
spring.application.name=camunda

# Server — runs on 8082 to avoid conflict with Camunda Operate on 8080
server.port=8082

# Camunda 8 / Zeebe connection
zeebe.client.broker.gateway-address=localhost:26500
zeebe.client.security.plaintext=true

# H2 In-Memory Database
spring.datasource.url=jdbc:h2:mem:demodb;DB_CLOSE_ON_EXIT=FALSE;DB_CLOSE_DELAY=-1
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.jpa.hibernate.ddl-auto=create-drop
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

### Key Configuration Points

**`zeebe.client.broker.gateway-address=localhost:26500`**
Connects to the Zeebe broker via gRPC. This is the entry point for deploying processes, starting instances, and activating jobs.

**`zeebe.client.security.plaintext=true`**
Disables TLS — required for local self-managed setup without SSL certificates.

**`spring.jpa.hibernate.ddl-auto=create-drop`**
H2 schema is created on startup and dropped on shutdown. Suitable for development — change to `validate` or `update` for persistent databases.

**`DB_CLOSE_DELAY=-1`**
Keeps H2 in-memory database alive for the lifetime of the JVM, preventing premature connection closure.

### H2 Console
Access at: **http://localhost:8082/h2-console**

| Field | Value |
|---|---|
| JDBC URL | `jdbc:h2:mem:demodb` |
| Username | `sa` |
| Password | *(leave blank)* |

---

## Process Flow — Personal Loan Approval

### BPMN: `sample-process.bpmn` | Process ID: `personal_loan_approval`

```
Start
  │
  ▼
[Loan Application] ──── 48h Timer (non-interrupting) ────► [Reminder Email] ──► End
  │ (User Task)                                              (Service Task)
  │ Task Listener: save-person-info
  │
  ▼
[Eligibility Check]
  │ (Business Rule Task → DMN: Decision_Eligibility)
  │ Input: age | Output: eligibility (boolean)
  │
  ▼
[XOR Gateway] ── eligibility = false ──► [Reject End]
  │
  │ eligibility = true
  ▼
[Parallel Gateway — Split]
  │
  ├──► [Manager Review] ──── 60s Timer (non-interrupting) ──► [Reminder Email] ──► End
  │     (User Task)
  │     Form: manager-review.form (person data readonly + approval radio)
  │
  └──► [Background Check]
        (Service Task — type: backgroundCheck)
  │
  ▼
[Parallel Gateway — Join]  ← waits for both Manager Review + Background Check
  │
  ▼
[End]
```

### Step-by-step

**Step 1 — Loan Application (User Task)**
The applicant fills in personal details via `person.form` in Camunda Tasklist. On completion, the `save-person-info` task listener fires — the Spring Boot worker maps form variables to `PersonEntity` and saves to H2.

**Step 2 — Eligibility Check (Business Rule Task)**
The DMN decision table `Decision_Eligibility` evaluates `age`:
- Age < 18 or > 60 → `eligibility = false` → process ends at Reject
- Age 18–60 → `eligibility = true` → continues

**Step 3 — Parallel Split**
Two tasks execute simultaneously:
- **Manager Review** — manager opens `manager-review.form` with person data pre-filled (readonly) and sets `approval = approved | rejected`
- **Background Check** — `BackgroundCheckWorker` runs automatically and sets `backgroundCheckPassed = true`

**Step 4 — Reminders (Non-Interrupting Timers)**
- If applicant doesn't complete Loan Application within **48h** → reminder fires
- If manager doesn't complete review within **60s** → reminder fires
- Both route to `ReminderWorker` which logs: `Reminder for <name> : <taskName> is waiting for your attention`
- Original tasks remain active — timers do not cancel them

**Step 5 — Parallel Join**
Waits for both Manager Review and Background Check to complete, then moves to End.

---

## Project Structure

```
src/main/java/com/amit/skillup/camunda/
├── CamundaApplication.java          # Spring Boot entry point
├── dto/
│   └── Person.java                  # Java record — form data transfer
├── entity/
│   └── PersonEntity.java            # JPA entity — persisted to H2
├── repository/
│   └── PersonRepository.java        # Spring Data JPA repository
├── service/
│   └── PersonService.java           # Business logic — save person to DB
└── worker/
    ├── PersonInfoWorker.java         # Task listener: save-person-info
    ├── BackgroundCheckWorker.java    # Service task: backgroundCheck
    └── ReminderWorker.java          # Service task: send-manager-reminder

src/main/resources/
├── application.properties           # Spring Boot + Zeebe + H2 config
├── sample-process.bpmn              # Main BPMN process
├── eligibility.dmn                  # DMN decision table
├── person.form                      # Loan Application form
└── manager-review.form              # Manager Review form (readonly + approval)
```

---

## Job Workers

| Class | Job Type | Trigger | Responsibility |
|---|---|---|---|
| `PersonInfoWorker` | `save-person-info` | Task listener on Loan Application completing | Maps form variables → `PersonEntity` → saves to H2 |
| `BackgroundCheckWorker` | `backgroundCheck` | Background Check service task | Simulates background verification, sets `backgroundCheckPassed` |
| `ReminderWorker` | `send-manager-reminder` | Non-interrupting timers (48h / 60s) | Logs reminder — replace with actual email (SendGrid, JavaMailSender) |

---

## Deployment

Deploy the following from **Camunda Modeler** (Ctrl+Shift+D) in this order:

1. `eligibility.dmn`
2. `person.form`
3. `manager-review.form`
4. `sample-process.bpmn`

Then start a process instance from **Camunda Operate** (http://localhost:8080/operate) and complete tasks in **Camunda Tasklist** (http://localhost:8080/tasklist).

---

## Key Dependencies

```xml
<dependency>
    <groupId>io.camunda.spring</groupId>
    <artifactId>spring-boot-starter-camunda</artifactId>
    <version>8.5.0</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
<dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
</dependency>
```

> **Note:** Spring Boot version is pinned to **3.2.5** for compatibility with `spring-boot-starter-camunda` 8.5.0. Spring Boot 4.x is not yet supported by Camunda 8.5.0.
