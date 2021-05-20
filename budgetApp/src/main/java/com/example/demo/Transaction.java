package com.example.demo;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

//Hibernate entity mapping to the transactions postgres table

@Entity
@Table(name="transactions")
public class Transaction {
	@Id
    private Long transaction_id;
	
	private String transaction_date;
	private double transaction_amount;
	private String transaction_description;
	private Long merchant_id;
	private Long update_id;
	
	public Transaction() {}
	
	public Transaction(String newDate, double newAmount, String newDescription) {
		this.transaction_date = newDate;
		this.transaction_amount = newAmount;
		this.transaction_description = newDescription;
	}
	
	public Long getId() {
		return this.transaction_id;
	}
	
	public String getDate() {
		return this.transaction_date;
	}
	
	public double getAmount() {
		return this.transaction_amount;
	}
	
	public String getDescription() {
		return this.transaction_description;
	}
	
	public Long getMerchantId() {
		return this.merchant_id;
	}
	
	public Long getUpdateId() {
		return this.update_id;
	}
	
	public String toString() {
		return transaction_date + " | " + String.valueOf(transaction_amount) + " | " + transaction_description;
	}
}