package com.example.demo;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="goals")
public class Goal {
	@Id
    private Long goal_id;
	
	private double goal_amount;
	private Long period_id;
	private Long category_id;
	
	public Long getId() {
		return this.goal_id;
	}
	
	public double getAmount() {
		return this.goal_amount;
	}
	
	public Long getPeriodId() {
		return this.period_id;
	}
	
	public Long getCategoryId() {
		return this.category_id;
	}
}