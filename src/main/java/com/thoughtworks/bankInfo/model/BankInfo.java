package com.thoughtworks.bankInfo.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Getter
@NoArgsConstructor
public class BankInfo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column
    private String bankCode;

    @Column
    private String url;

    public BankInfo(String bankCode, String url) {
        this.bankCode = bankCode;
        this.url = url;
    }

}
