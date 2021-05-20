package com.example.demo;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

//Hibernate entity mapping to the periods postgres table

@Entity
@Table(name="periods")
public class Period {
	@Id
    private Long period_id;
	
	private String period_start;
	private String period_end;
	
	public Long getId() {
		return this.period_id;
	}
	
	public String getStart() {
		return this.period_start;
	}
	
	public String getEnd() {
		return this.period_end;
	}
}