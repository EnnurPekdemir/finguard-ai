package com.sentinelbank.finguard.model;

/**
 * Enum representing the possible statuses of a credit application.
 *
 * <ul>
 *   <li>{@code PENDING}        — The application has not been evaluated yet.</li>
 *   <li>{@code APPROVED}       — Automatically approved by the system (low risk).</li>
 *   <li>{@code REJECTED}       — Automatically rejected by the system (high risk).</li>
 *   <li>{@code MANUAL_REVIEW}  — Entropy score exceeded threshold; sent to risk analyst for review.</li>
 * </ul>
 */
public enum ApplicationStatus {

    PENDING,
    APPROVED,
    REJECTED,
    MANUAL_REVIEW
}
