package models;

import enums.paymentmethod;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Represents an invoice for an Hotel Reservation sys.
 * Contains details about the total amount due, the timestamp,
 * the payment method used, and whether the invoice has been fully paid.
 */
public class Invoice {
    private Reservation reservation;
    private double amount;
    private LocalDateTime generatedAt;
    private paymentmethod paymentMethod;
    private boolean paid;

    public Invoice() {
        this.generatedAt = LocalDateTime.now();
    }

    public Invoice(Reservation reservation, double amount) {
        this();
        setReservation(reservation);
        setAmount(amount);
    }

    public static Invoice generate(Reservation reservation, double amount) {
        return new Invoice(reservation, amount);
    }

    public Reservation getReservation() {
        return reservation;
    }

    public void setReservation(Reservation reservation) {
        if (reservation == null) {
            throw new IllegalArgumentException("Reservation cannot be null.");
        }
        this.reservation = reservation;
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        if (amount < 0) {
            throw new IllegalArgumentException("Invoice amount cannot be negative.");
        }
        this.amount = amount;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public paymentmethod getPaymentMethod() {
        return paymentMethod;
    }

    public boolean isPaid() {
        return paid;
    }

    public void markPaid(paymentmethod paymentMethod) {
        if (paymentMethod == null) {
            throw new IllegalArgumentException("Payment method cannot be null.");
        }
        this.paymentMethod = paymentMethod;
        this.paid = true;
    }

    public String printInvoice() {
        String invoiceText = toString();
        System.out.println(invoiceText);
        return invoiceText;
    }

    @Override
    public String toString() {
        return "Invoice{" +
                "reservation=" + reservation +
                ", amount=" + amount +
                ", generatedAt=" + generatedAt.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME) +
                ", paymentMethod=" + paymentMethod +
                ", paid=" + paid +
                '}';
    }
}