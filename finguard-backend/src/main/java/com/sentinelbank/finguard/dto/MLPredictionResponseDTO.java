package com.sentinelbank.finguard.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

/**
 * DTO representing the risk prediction response returned from FastAPI /predict endpoint.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MLPredictionResponseDTO {

    private Integer prediction;

    @JsonProperty("prediction_label")
    private String predictionLabel;

    @JsonProperty("probability_onay")
    private Double probabilityOnay;

    @JsonProperty("probability_red")
    private Double probabilityRed;

    @JsonProperty("entropy_score")
    private Double entropyScore;

    @JsonProperty("manual_review_required")
    private Boolean manualReviewRequired;

    @JsonProperty("decision_flow")
    private String decisionFlow;
}
