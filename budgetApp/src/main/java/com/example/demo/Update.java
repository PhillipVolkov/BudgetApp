package com.example.demo;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

//Class for modeling the UPDATES postgres table

@Entity
@Table(name="updates")
public class Update {
	@Id
    private Long update_id;
	
	private String update_time;
	private String update_date;
	
	public Long getId() {
		return this.update_id;
	}
	
	public String getTime() {
		return this.update_time;
	}
	
	public String getDate() {
		return this.update_date;
	}
}