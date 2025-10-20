package lt.elektromeistras.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import lt.elektromeistras.domain.Invoice;
import lt.elektromeistras.domain.Payment;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Payment Matching Service
 * Implements the payment matching algorithm from the specification
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentMatchingService {

    public double calculateMatchScore(Payment payment, Invoice invoice) {
        double score = 0.0;

        // Exact amount match (50%)
        if (amountsMatch(payment.getAmount(), invoice.getBalanceDue())) {
            score += 0.50;
        }

        // Invoice number in reference (30%)
        if (payment.getBankReference() != null && 
            payment.getBankReference().contains(invoice.getInvoiceNumber())) {
            score += 0.30;
        }

        // Customer match (15%)
        if (payment.getCustomer().getId().equals(invoice.getCustomer().getId())) {
            score += 0.15;
        }

        // VAT code in reference (5%)
        if (payment.getBankReference() != null && 
            invoice.getCustomer().getVatCode() != null &&
            payment.getBankReference().contains(invoice.getCustomer().getVatCode())) {
            score += 0.05;
        }

        log.debug("Match score between payment {} and invoice {}: {}", 
                payment.getId(), invoice.getInvoiceNumber(), score);

        return score;
    }

    private boolean amountsMatch(BigDecimal amount1, BigDecimal amount2) {
        return amount1.subtract(amount2).abs().compareTo(BigDecimal.valueOf(0.01)) < 0;
    }

    public boolean isGoodMatch(double score) {
        return score >= 0.85; // 85% or higher is considered a good match
    }

    public boolean isRecommendedMatch(double score) {
        return score >= 0.98; // 98% or higher for auto-recommendation
    }
}
