# FinGuard - Credit Risk Analysis Platform (SentinelBank)

FinGuard is a high-performance credit risk analysis API and interactive management portal designed for **SentinelBank**. It evaluates credit applications using optimized machine learning models and measures decision-making uncertainty using **Shannon Entropy**.

---

## 🚀 Key Features

*   **Real-time Risk Evaluation:** Utilizes a serialized Random Forest classifier to estimate default probabilities and make credit decisions in milliseconds.
*   **Shannon Entropy Decision Framework:** Measures model uncertainty/indecision using binary Shannon Entropy:
    $$H(p) = - \sum (p_i \log_2 p_i)$$
*   **Intelligent Workflows (Decision Flow):**
    *   **System Approved (`SISTEM_ONAY` / `APPROVED`):** Low-risk applications are automatically approved.
    *   **System Rejected (`SISTEM_RED` / `REJECTED`):** High-risk applications are automatically declined.
    *   **Manual Review (`MANUAL_REVIEW`):** Applications where the decision uncertainty exceeds the threshold ($H(p) > 0.8$) are flagged and routed to risk experts.
*   **Layered Enterprise Architecture:** Spring Boot backend with JPA/Hibernate database persistence, custom Spring Security filters, and stateless JWT authentication.
*   **Interactive Premium Dashboard:** Dark/Light theme administrative interface equipped with ApexCharts for visualizing risk distributions and managing applications.
*   **Swagger API Documentation:** Automated API docs powered by Springdoc OpenAPI at `/swagger-ui/index.html`.

---

## 🔬 Why Shannon Entropy?

Traditional machine learning classifiers predict labels by applying a threshold (e.g., $0.5$) to output probabilities. However, in credit risk assessment, a model predicting a $51\%$ chance of approval is highly unsure compared to a $99\%$ prediction. 

FinGuard solves this problem by using **Shannon Entropy** to measure prediction uncertainty:
*   When a prediction is highly certain (e.g., $P(\text{approve}) = 0.98, P(\text{reject}) = 0.02$), the entropy $H(p)$ is close to $0$.
*   When a prediction is borderline (e.g., $P(\text{approve}) = 0.52, P(\text{reject}) = 0.48$), the entropy $H(p)$ climbs towards $1$.

By routing applications with an entropy score **above 0.8** to a **Manual Review** queue, SentinelBank combines the speed and efficiency of automated AI with the safety and experience of human credit analysts, protecting the bank from high-risk automated approvals.

---

## 🛠️ Technology Stack

| Layer | Technologies |
|---|---|
| **Backend (Core API)** | Java 17, Spring Boot 3.x/4.x, Spring Security, JWT, Spring Data JPA, Hibernate, Lombok |
| **ML Engine (Service)** | Python, FastAPI, Scikit-Learn (Random Forest), Pandas, NumPy, Joblib |
| **Database** | MySQL 8 |
| **Documentation** | Springdoc OpenAPI (Swagger UI) |
| **Frontend** | HTML5, Vanilla CSS3 (Custom Variables, Keyframe Animations), Vanilla JS (Fetch API) |
| **Deployment Servers** | Tomcat (Spring Boot on `:8081`), Uvicorn (FastAPI on `:8000`) |

---

## 🏗️ System Architecture

```
┌─────────────────────────┐      ┌──────────────────────────┐
│   Spring Boot Backend   │      │   FastAPI ML Service     │
│        :8081            │─────▶│        :8000             │
│                         │ HTTP │                          │
│  • Customer CRUD        │      │  • /predict endpoint     │
│  • JWT Authentication   │      │  • Random Forest Model   │
│  • Decision Engine      │      │  • Shannon Entropy Calc  │
│  • Swagger Doc UI       │      │  • Web Portal UI         │
│  └──────────┬───────────┘      └──────────────────────────┘
              │
       ┌──────▼──────┐
       │  MySQL 8    │
       │ finguard_db │
       └─────────────┘
```

---

## 📦 Getting Started

### 1. ML Service (FastAPI)

Ensure Python dependencies are installed:
```bash
pip install fastapi uvicorn scikit-learn pandas numpy joblib
```

Start the Uvicorn server:
```bash
uvicorn main:app --reload
```
ML Service Portal: `http://localhost:8000/`

### 2. Spring Boot Backend

**Prerequisites:** MySQL 8 must be installed and running.

1. Create the database:
   ```sql
   CREATE DATABASE finguard_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```
2. Configure your database username and password in `finguard-backend/src/main/resources/application.properties`.
3. Run the application:
   ```bash
   cd finguard-backend
   ./mvnw spring-boot:run
   ```
Core Backend API: `http://localhost:8081/`

---

## 🔌 API Reference & Documentation

### API Documentation UI (Swagger)
Once the backend is running, access the interactive OpenAPI document:
*   **Swagger Web UI:** `http://localhost:8081/swagger-ui/index.html`
*   **OpenAPI JSON Spec:** `http://localhost:8081/v3/api-docs`

### Authentication Flow
Except for auth endpoints, all API routes require a valid JWT passed in the request header.

1.  **Register a User:**
    ```bash
    POST http://localhost:8081/api/auth/register
    Content-Type: application/json

    {
      "username": "analyst1",
      "password": "SecurePassword123",
      "email": "analyst1@sentinelbank.com",
      "role": "USER"
    }
    ```
2.  **Authenticate & Get Token:**
    ```bash
    POST http://localhost:8081/api/auth/login
    Content-Type: application/json

    {
      "username": "analyst1",
      "password": "SecurePassword123"
    }
    ```
    This returns a JWT string:
    ```json
    {
      "token": "eyJhbGciOiJIUzI1NiJ9.ey...",
      "username": "analyst1",
      "role": "USER"
    }
    ```
3.  **Access Secured Endpoints:**
    Attach the token as a Bearer header to subsequent requests:
    ```http
    Authorization: Bearer eyJhbGciOiJIUzI1NiJ9.ey...
    ```

---

## 🔒 License

This project is proprietary software developed for the internal use of **SentinelBank**. All rights reserved.
