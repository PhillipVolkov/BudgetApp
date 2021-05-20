package com.example.demo;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

//Class for modeling the API MAP postgres table

@Entity
@Table(name="api_map")
public class ApiMap {
	@Id
    private Long field_id;
	
	private String field_name;
	
	private int category_id;
	
	public Long getId() {
		return this.field_id;
	}
	
	public String getName() {
		return this.field_name;
	}
	
	public int getCategoryId() {
		return this.category_id;
	}
}