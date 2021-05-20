package com.example.demo;

import com.opencsv.bean.CsvBindByPosition;

//Class for mapping CSV column data and storing results for imports

public class TransactionCSV {
    @CsvBindByPosition (position = 1)
    private String date;
    @CsvBindByPosition(position = 2)
    private String description;
    @CsvBindByPosition(position = 3)
    private String debit;
    @CsvBindByPosition(position = 4)
    private String credit;

    public TransactionCSV() {}
    
    public TransactionCSV(String date, String description, String debit, String credit) {
        this.date = date;
        this.description = description;
        this.debit = debit;
        this.credit = credit;
    }
    
    @Override
    public String toString() {
    	return this.date + " " + this.description + " " + this.debit + " " + this.credit;
    }
    
    public String getDate() {
    	return this.date;
    }
    
    public String getDescription() {
    	return this.description;
    }
    
    public String getDebit() {
    	return this.debit;
    }
    
    public String getCredit() {
    	return this.credit;
    }
 }