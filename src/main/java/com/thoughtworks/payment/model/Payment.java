package com.thoughtworks.payment.model;

import com.thoughtworks.payment.api.BankDetails;
import lombok.Getter;
import org.slf4j.MDC;

import javax.persistence.*;

@Entity
@Getter
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private int amount;

    @Column(name = "bene_name")
    private String beneficiaryName;

    @Column(name = "bene_acc_num")
    private long beneficiaryAccountNumber;

    @Column(name = "bene_ifsc")
    private String beneficiaryIfscCode;

    @Column(name = "payee_name")
    private String payeeName;

    @Column(name = "payee_acc_num")
    private long payeeAccountNumber;

    @Column(name = "payee_ifsc")
    private String payeeIfscCode;

    @Column(name = "status")
    private String status;

    @Column(name = "request_id")
    private String requestId;


    public Payment() {
    }

    public Payment(int amount, BankDetails beneficiary, BankDetails payee) {
        if (payee == null) {
            throw new IllegalArgumentException("payee cannot be null");
        } else if (amount <= 0) {
            throw new IllegalArgumentException("amount should be greater than zero");
        } else if (beneficiary == null) {
            throw new IllegalArgumentException("beneficiary cannot be null");
        }

        this.amount = amount;
        this.beneficiaryName = beneficiary.getName();
        this.beneficiaryAccountNumber = beneficiary.getAccountNumber();
        this.beneficiaryIfscCode = beneficiary.getIfscCode();
        this.payeeName = payee.getName();
        this.payeeAccountNumber = payee.getAccountNumber();
        this.payeeIfscCode = payee.getIfscCode();
        this.status = "success";
        this.requestId = MDC.get("trace_id");
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
