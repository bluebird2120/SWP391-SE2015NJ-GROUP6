package model;

import java.math.BigDecimal;
import java.sql.Timestamp;

public class Payments {

    private int paymentID;
    private int invoiceID;
    private String transactionCode;
    private String paymentGateway;  // vnpay / momo / cash
    private BigDecimal amount;
    private String status;          // pending / success / failed / refunded
    private Timestamp paidAt;       // DATETIME

    public Payments() {
    }

    public Payments(int paymentID, int invoiceID, String transactionCode, String paymentGateway,
            BigDecimal amount, String status, Timestamp paidAt) {
        this.paymentID = paymentID;
        this.invoiceID = invoiceID;
        this.transactionCode = transactionCode;
        this.paymentGateway = paymentGateway;
        this.amount = amount;
        this.status = status;
        this.paidAt = paidAt;
    }

    public int getPaymentID() {
        return paymentID;
    }

    public void setPaymentID(int paymentID) {
        this.paymentID = paymentID;
    }

    public int getInvoiceID() {
        return invoiceID;
    }

    public void setInvoiceID(int invoiceID) {
        this.invoiceID = invoiceID;
    }

    public String getTransactionCode() {
        return transactionCode;
    }

    public void setTransactionCode(String transactionCode) {
        this.transactionCode = transactionCode;
    }

    public String getPaymentGateway() {
        return paymentGateway;
    }

    public void setPaymentGateway(String paymentGateway) {
        this.paymentGateway = paymentGateway;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getPaidAt() {
        return paidAt;
    }

    public void setPaidAt(Timestamp paidAt) {
        this.paidAt = paidAt;
    }
}
