package com.example.demo;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

//Class for modeling the UPDATES postgres table

@Entity
@Table(name="duplicates")
public class Duplicate {
	@Id
    private Long duplicate_id;
	
	private Long transaction1_id;
	private Long transaction2_id;
	
	public Long getId() {
		return this.duplicate_id;
	}
	
	public Long getFirstId() {
		return this.transaction1_id;
	}
	
	public Long getSecondId() {
		return this.transaction2_id;
	}
}