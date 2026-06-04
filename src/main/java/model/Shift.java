package model;

import java.sql.Time;
import java.sql.Timestamp;

public class Shift {

	private Long id;
	private String code;
	private String name;
	private Time startTime;
	private Time endTime;
	private Integer breakMinutes;
	private Boolean isNightShift;
	private Boolean isActive;
	private Timestamp createdAt;
	private Timestamp updatedAt;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Time getStartTime() {
		return startTime;
	}

	public void setStartTime(Time startTime) {
		this.startTime = startTime;
	}

	public Time getEndTime() {
		return endTime;
	}

	public void setEndTime(Time endTime) {
		this.endTime = endTime;
	}

	public Integer getBreakMinutes() {
		return breakMinutes;
	}

	public void setBreakMinutes(Integer breakMinutes) {
		this.breakMinutes = breakMinutes;
	}

	public Boolean getIsNightShift() {
		return isNightShift;
	}

	public void setIsNightShift(Boolean isNightShift) {
		this.isNightShift = isNightShift;
	}

	public Boolean getIsActive() {
		return isActive;
	}

	public void setIsActive(Boolean isActive) {
		this.isActive = isActive;
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
