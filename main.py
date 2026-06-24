from fastapi import FastAPI, HTTPException
from fastapi.responses import HTMLResponse
from pydantic import BaseModel, Field, ConfigDict
import joblib
import pandas as pd
import numpy as np
import os

# Create FastAPI app
app = FastAPI(
    title="FinGuard Kredi Risk Analiz API",
    description="SentinelBank bünyesinde kredi başvurularını yapay zeka ile değerlendiren ve karar belirsizliğini Shannon Entropy ile ölçen API.",
    version="1.0.0"
)

# Load model data
model_path = os.path.join(os.path.dirname(__file__), "finguard_model.pkl")
if not os.path.exists(model_path):
    raise RuntimeError(f"Model dosyası bulunamadı: {model_path}. Lütfen önce modeli eğitin.")

model_data = joblib.load(model_path)
model = model_data['model']
scaler = model_data['scaler']
expected_features = model_data['features']
numeric_cols = model_data['numeric_cols']


class CreditApplication(BaseModel):
    """
    Pydantic schema representing the credit application request payload.
    Supports alias inputs for Spring Boot compatibility (age, salary, debt)
    and provides default fallbacks for the remaining features if omitted.
    """
    model_config = ConfigDict(populate_by_name=True)

    person_age: int = Field(..., alias="age", ge=18, le=100, description="Müşterinin yaşı", example=25)
    person_income: int = Field(..., alias="salary", gt=0, description="Müşterinin yıllık geliri", example=55000)
    loan_amnt: int = Field(..., alias="debt", gt=0, description="Talep edilen kredi tutarı", example=10000)
    
    person_home_ownership: str = Field("RENT", description="Ev sahipliği durumu ('RENT', 'OWN', 'MORTGAGE', 'OTHER')", example="RENT")
    person_emp_length: float = Field(4.0, ge=0, description="Çalışma süresi (yıl)", example=4.0)
    loan_intent: str = Field("PERSONAL", description="Kredinin kullanım amacı ('PERSONAL', 'EDUCATION', 'MEDICAL', 'VENTURE', 'HOMEIMPROVEMENT', 'DEBTCONSOLIDATION')", example="PERSONAL")
    loan_grade: str = Field("C", description="Kredi notu derecesi ('A', 'B', 'C', 'D', 'E', 'F', 'G')", example="C")
    loan_int_rate: float = Field(11.0, gt=0, description="Kredi faiz oranı", example=11.5)
    cb_person_default_on_file: str = Field("N", description="Geçmiş borç ödememe kaydı var mı ('Y', 'N')", example="N")
    cb_person_cred_hist_length: int = Field(3, ge=0, description="Kredi geçmişi uzunluğu (yıl)", example=3)


def calculate_shannon_entropy(probs: np.ndarray) -> float:
    """
    Calculate the Shannon Entropy of a binary classification probability distribution.
    
    Args:
        probs (np.ndarray): Probability array containing class probabilities [P_approve, P_reject].
        
    Returns:
        float: Calculated entropy score in bits (ranges from 0.0 to 1.0).
    """
    eps = 1e-15
    probs = np.clip(probs, eps, 1 - eps)
    # Binary Shannon Entropy: H(p) = - (p0*log2(p0) + p1*log2(p1))
    entropy = -np.sum(probs * np.log2(probs))
    return float(entropy)


@app.get("/", response_class=HTMLResponse)
def read_root():
    """
    Serve the HTML landing page / portal for the FinGuard application.
    
    Returns:
        HTMLResponse: Loaded index.html page content.
    """
    html_path = os.path.join(os.path.dirname(__file__), "index.html")
    if not os.path.exists(html_path):
        raise HTTPException(status_code=404, detail="Arayüz dosyası (index.html) bulunamadı.")
    with open(html_path, "r", encoding="utf-8") as f:
        html_content = f.read()
    return HTMLResponse(content=html_content)


@app.post("/predict")
def predict_credit_risk(app_data: CreditApplication):
    """
    Process a credit risk prediction request.
    Validates inputs, maps variables, scales features, queries the Random Forest 
    classifier, and returns decision outcomes along with an uncertainty score.
    
    Args:
        app_data (CreditApplication): Request object containing applicant profile details.
        
    Returns:
        dict: Prediction results including decision, probabilities, and entropy.
    """
    try:
        # 1. Initialize all dummy variables to 0
        feature_dict = {feat: 0.0 for feat in expected_features}
        
        # 2. Fill in numeric values
        feature_dict['person_age'] = float(app_data.person_age)
        feature_dict['person_income'] = float(app_data.person_income)
        feature_dict['person_emp_length'] = float(app_data.person_emp_length)
        feature_dict['loan_amnt'] = float(app_data.loan_amnt)
        feature_dict['loan_int_rate'] = float(app_data.loan_int_rate)
        feature_dict['cb_person_cred_hist_length'] = float(app_data.cb_person_cred_hist_length)
        
        # Calculate loan_percent_income dynamically
        feature_dict['loan_percent_income'] = float(app_data.loan_amnt) / float(app_data.person_income)
        
        # 3. Handle categorical columns (One-Hot Encoding mappings)
        ownership = app_data.person_home_ownership.upper()
        if f'person_home_ownership_{ownership}' in feature_dict:
            feature_dict[f'person_home_ownership_{ownership}'] = 1.0
            
        intent = app_data.loan_intent.upper()
        if f'loan_intent_{intent}' in feature_dict:
            feature_dict[f'loan_intent_{intent}'] = 1.0
            
        grade = app_data.loan_grade.upper()
        if f'loan_grade_{grade}' in feature_dict:
            feature_dict[f'loan_grade_{grade}'] = 1.0
            
        default_file = app_data.cb_person_default_on_file.upper()
        if default_file == 'Y' and 'cb_person_default_on_file_Y' in feature_dict:
            feature_dict['cb_person_default_on_file_Y'] = 1.0
            
        # 4. Create DataFrame in the exact expected order
        df_input = pd.DataFrame([feature_dict])[expected_features]
        
        # 5. Apply StandardScaler to numerical columns
        df_input[numeric_cols] = scaler.transform(df_input[numeric_cols])
        
        # 6. Make prediction and get probabilities
        prediction = int(model.predict(df_input)[0])
        probabilities = model.predict_proba(df_input)[0]  # [prob_approved, prob_rejected]
        
        # 7. Calculate Shannon Entropy (Uncertainty Score)
        entropy_score = calculate_shannon_entropy(probabilities)
        
        # 8. Check uncertainty threshold (0.8)
        threshold = 0.8
        manual_review_required = entropy_score > threshold
        
        # Return response
        return {
            "prediction": prediction,
            "prediction_label": "RED" if prediction == 1 else "ONAY",
            "probability_onay": round(float(probabilities[0]), 4),
            "probability_red": round(float(probabilities[1]), 4),
            "entropy_score": round(entropy_score, 4),
            "manual_review_required": manual_review_required,
            "decision_flow": "MANUEL_INCELEME" if manual_review_required else ("SISTEM_RED" if prediction == 1 else "SISTEM_ONAY")
        }
        
    except Exception as e:
        raise HTTPException(status_code=400, detail=f"Tahmin işlemi sırasında bir hata oluştu: {str(e)}")
