package com.example.demo;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

//Object for modeling the categories postgres table

@Entity
@Table(name="categories")
public class Category {
	@Id
    private Long category_id;
	
	private String category_name;
	
	public Long getId() {
		return this.category_id;
	}
	
	public String getName() {
		return this.category_name;
	}
}