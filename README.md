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

*   **Backend**: Python, FastAPI
*   **Machine Learning**: Scikit-Learn (Random Forest Classifier), Pandas, NumPy, Joblib
*   **Frontend**: HTML5, Vanilla CSS3 (Custom Variables, Keyframe Animations), Vanilla JS (Fetch API)
*   **Server**: Uvicorn (StatReload)

---

## 📦 Kurulum ve Çalıştırma (Installation & Running)

### 1. Gereksinimleri Yükleyin
Gerekli Python paketlerinin kurulu olduğundan emin olun:
```bash
pip install fastapi uvicorn scikit-learn pandas numpy joblib
```

### 2. Sunucuyu Başlatın
Uvicorn sunucusunu otomatik yenileme (reload) moduyla ayağa kaldırın:
```bash
uvicorn main:app --reload
```

### 3. Tarayıcıdan Erişin
Arayüze erişmek için tarayıcınızda şu adresi açın:
```
http://127.0.0.1:8000/
```

---

## 🔌 API Entegrasyon Kılavuzu (API Documentation)

### **`POST /predict`**

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
