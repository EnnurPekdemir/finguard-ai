package com.sentinelbank.finguard.model;

/**
 * Kredi başvurusunun olası durumlarını temsil eden enum.
 *
 * <ul>
 *   <li>{@code PENDING}        — Başvuru henüz değerlendirilmedi.</li>
 *   <li>{@code APPROVED}       — Sistem tarafından otomatik onaylandı (düşük risk).</li>
 *   <li>{@code REJECTED}       — Sistem tarafından otomatik reddedildi (yüksek risk).</li>
 *   <li>{@code MANUAL_REVIEW}  — Entropy skoru eşik değerini aştı; uzman incelemesine sevk edildi.</li>
 * </ul>
 */
public enum ApplicationStatus {

    PENDING,
    APPROVED,
    REJECTED,
    MANUAL_REVIEW
}
