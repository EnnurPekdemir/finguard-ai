# FinGuard - Credit Risk Analysis API (SentinelBank)

FinGuard, **SentinelBank** bünyesindeki kredi başvurularını makine öğrenmesi modelleriyle değerlendiren ve karar süreçlerindeki belirsizliği **Shannon Entropy** ile ölçen yüksek performanslı bir kredi risk analiz API'si ve portalıdır.

---

## 🚀 Özellikler (Features)

*   **Gerçek Zamanlı Risk Analizi**: Eğitilmiş ve optimize edilmiş Random Forest modeli sayesinde kredi onay/ret kararlarını ve olasılıklarını milisaniyeler içinde hesaplar.
*   **Shannon Entropy Karar Destek Sistemi**: Modelin tahmin olasılıklarındaki kararsızlığı/belirsizliği $H(p) = - \sum (p_i \log_2 p_i)$ formülüyle ölçer. 
*   **Akıllı İş Akışı Yönlendirme (Decision Flow)**:
    *   **Sistem Onayı (SISTEM_ONAY)**: Düşük riskli başvurular otomatik olarak onaylanır.
    *   **Sistem Reddi (SISTEM_RED)**: Yüksek riskli başvurular otomatik olarak reddedilir.
    *   **Manuel İnceleme (MANUEL_INCELEME)**: Karar belirsizliği **0.8**'in üzerinde olan başvurular sistem tarafından askıya alınır ve risk uzmanının manuel incelemesine sevk edilir.
*   **Gelişmiş API Entegrasyonu**: Hem 10 parametreli **Web Arayüzünü** hem de gelecekteki **Spring Boot** istemcileri için sadece 3 temel alan içeren (`age`, `salary`, `debt`) istekleri destekler (opsiyonel parametreler için otomatik medyan/mod atamaları yapılır).
*   **Premium Karanlık Tema Arayüzü**: Outfit ve Plus Jakarta Sans fontları, neon efektler ve cam morfolojisi (glassmorphism) kullanılarak tasarlanmış modern web arayüzü.

---

## 🛠️ Teknolojiler (Tech Stack)

| Katman | Teknolojiler |
|--------|-------------|
| **Backend (API)** | Java 17, Spring Boot 4.1, Spring Data JPA, Hibernate, Lombok |
| **ML Servisi** | Python, FastAPI, Scikit-Learn (Random Forest), Pandas, NumPy, Joblib |
| **Veritabanı** | MySQL 8 |
| **Frontend** | HTML5, Vanilla CSS3 (Custom Variables, Keyframe Animations), Vanilla JS (Fetch API) |
| **Sunucular** | Tomcat (Spring Boot :8081), Uvicorn (FastAPI :8000) |

---

## 🏗️ Proje Mimarisi

Proje iki bağımsız servis olarak çalışır:

```
┌─────────────────────────┐      ┌──────────────────────────┐
│   Spring Boot Backend   │      │   FastAPI ML Servisi     │
│        :8081            │─────▶│        :8000             │
│                         │ HTTP │                          │
│  • Müşteri CRUD         │      │  • /predict endpoint     │
│  • Kredi Başvuru Yönetim│      │  • Random Forest Model   │
│  • İş Kuralları         │      │  • Shannon Entropy       │
└───────────┬─────────────┘      └──────────────────────────┘
            │
     ┌──────▼──────┐
     │  MySQL 8    │
     │ finguard_db │
     └─────────────┘
```

### Spring Boot Backend Katman Yapısı

```
com.sentinelbank.finguard/
├── controller/       → REST API endpoint'leri (@RestController)
├── service/          → İş mantığı katmanı (@Service)
├── repository/       → Veritabanı erişim katmanı (JpaRepository)
├── model/            → JPA Entity sınıfları (@Entity)
└── dto/              → Veri transfer nesneleri (Request/Response)
```

---

## 📦 Kurulum ve Çalıştırma (Installation & Running)

### 1. ML Servisi (FastAPI)

Gerekli Python paketlerinin kurulu olduğundan emin olun:
```bash
pip install fastapi uvicorn scikit-learn pandas numpy joblib
```

Uvicorn sunucusunu başlatın:
```bash
uvicorn main:app --reload
```
Arayüz: `http://127.0.0.1:8000/`

### 2. Spring Boot Backend

**Ön koşul:** MySQL 8 kurulu ve çalışır durumda olmalıdır.

Veritabanını oluşturun:
```sql
CREATE DATABASE finguard_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

`application.properties` dosyasındaki bağlantı bilgilerini kendi MySQL ayarlarınıza göre düzenleyin, ardından:
```bash
cd finguard-backend
./mvnw spring-boot:run
```
API: `http://localhost:8081/api/`

---

## 🔌 API Entegrasyon Kılavuzu (API Documentation)

### Spring Boot REST API — Müşteri Yönetimi

| Method | Endpoint | Açıklama |
|--------|----------|----------|
| `POST` | `/api/customers` | Yeni müşteri oluştur |
| `GET` | `/api/customers` | Tüm müşterileri listele |
| `GET` | `/api/customers/{id}` | ID ile müşteri getir |
| `PUT` | `/api/customers/{id}` | Müşteri bilgilerini güncelle |
| `DELETE` | `/api/customers/{id}` | Müşteri sil |

**Örnek — Yeni Müşteri Oluşturma:**
```bash
POST http://localhost:8081/api/customers
Content-Type: application/json

{
  "name": "Ahmet Yılmaz",
  "identityNumber": "12345678901",
  "email": "ahmet.yilmaz@email.com",
  "monthlyIncome": 25000.0
}
```

---

### FastAPI ML Servisi — `POST /predict`

Kredi başvurularını değerlendirmek için kullanılan ana endpoint'tir.

#### **Senaryo A: Spring Boot / Minimum Parametre İsteği**
Dış sistemlerden veya Spring Boot servislerinden sadece yaş, gelir ve borç bilgisi gönderilmesi yeterlidir.

*   **İstek Gövdesi (Body - JSON):**
```json
{
  "age": 25,
  "salary": 55000,
  "debt": 10000
}
```

*   **Dönen Yanıt (Response - JSON):**
```json
{
  "prediction": 0,
  "prediction_label": "ONAY",
  "probability_onay": 0.89,
  "probability_red": 0.11,
  "entropy_score": 0.4999,
  "manual_review_required": false,
  "decision_flow": "SISTEM_ONAY"
}
```

---

#### **Senaryo B: Arayüz / Tüm Parametrelerle İstek**
Web portalı gibi istemciler, tahmin doğruluğunu maksimuma çıkarmak için tüm parametreleri gönderebilir.

*   **İstek Gövdesi (Body - JSON):**
```json
{
  "person_age": 25,
  "person_income": 55000,
  "person_home_ownership": "RENT",
  "person_emp_length": 4.0,
  "loan_intent": "PERSONAL",
  "loan_grade": "C",
  "loan_amnt": 10000,
  "loan_int_rate": 11.5,
  "cb_person_default_on_file": "N",
  "cb_person_cred_hist_length": 3
}
```

*   **Dönen Yanıt (Response - JSON):**
```json
{
  "prediction": 0,
  "prediction_label": "ONAY",
  "probability_onay": 0.96,
  "probability_red": 0.04,
  "entropy_score": 0.2423,
  "manual_review_required": false,
  "decision_flow": "SISTEM_ONAY"
}
```

---

## 🔒 Lisans ve İletişim

Bu proje **SentinelBank** iç kullanımı için geliştirilmiştir. Tüm hakları saklıdır.
