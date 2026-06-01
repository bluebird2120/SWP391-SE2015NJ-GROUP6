package model;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;

public class Invoices {

    private int invoiceID;
    private String invoiceNumber;
    private String paymentMethod;        // cash / card / qr
    private BigDecimal subTotal;
    private BigDecimal taxAmount;
    private BigDecimal depositDeducted;
    private BigDecimal finalAmount;
    private Date issuedDate;             // DATE
    private String status;               // unpaid / paid / partial

    public Invoices() {
    }

    public Invoices(int invoiceID, String invoiceNumber, String paymentMethod,
            BigDecimal subTotal, BigDecimal taxAmount, BigDecimal depositDeducted,
            BigDecimal finalAmount, Date issuedDate, String status) {
        this.invoiceID = invoiceID;
        this.invoiceNumber = invoiceNumber;
        this.paymentMethod = paymentMethod;
        this.subTotal = subTotal;
        this.taxAmount = taxAmount;
        this.depositDeducted = depositDeducted;
        this.finalAmount = finalAmount;
        this.issuedDate = issuedDate;
        this.status = status;
    }

    public int getInvoiceID() {
        return invoiceID;
    }

    public void setInvoiceID(int invoiceID) {
        this.invoiceID = invoiceID;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public BigDecimal getSubTotal() {
        return subTotal;
    }

    public void setSubTotal(BigDecimal subTotal) {
        this.subTotal = subTotal;
    }

    public BigDecimal getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(BigDecimal taxAmount) {
        this.taxAmount = taxAmount;
    }

    public BigDecimal getDepositDeducted() {
        return depositDeducted;
    }

    public void setDepositDeducted(BigDecimal depositDeducted) {
        this.depositDeducted = depositDeducted;
    }

    public BigDecimal getFinalAmount() {
        return finalAmount;
    }

    public void setFinalAmount(BigDecimal finalAmount) {
        this.finalAmount = finalAmount;
    }

    public Date getIssuedDate() {
        return issuedDate;
    }

    public void setIssuedDate(Date issuedDate) {
        this.issuedDate = issuedDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
