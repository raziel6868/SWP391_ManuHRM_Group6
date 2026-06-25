package model;

import java.sql.Date;
import java.sql.Timestamp;

public class Holiday {
	private Long id;
	private Date date;
	private String name;
	private boolean isRecurring;
	private boolean isActive;
	private String description;
	private Timestamp createdAt;
	private Timestamp updatedAt;

	public Holiday() {
	}

	public Holiday(Long id, Date date, String name, boolean isRecurring, boolean isActive, String description) {
		this.id = id;
		this.date = date;
		this.name = name;
		this.isRecurring = isRecurring;
		this.isActive = isActive;
		this.description = description;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean active) {
		isActive = active;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isRecurring() {
		return isRecurring;
	}

	public void setRecurring(boolean recurring) {
		isRecurring = recurring;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

	public Timestamp getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(Timestamp updatedAt) {
		this.updatedAt = updatedAt;
	}
}
