package com.example.demo;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

//Hibernate entity mapping to the subtotals postgres table

@Entity
@Table(name="subtotals")
public class Subtotal {
	@Id
    private Long subtotal_id;
	
	private double subtotal_amount;
	private Long period_id;
	private Long category_id;
	
	public Long getId() {
		return this.subtotal_id;
	}
	
	public double getAmount() {
		return this.subtotal_amount;
	}
	
	public Long getPeriodId() {
		return this.period_id;
	}
	
	public Long getCategoryId() {
		return this.category_id;
	}
}