package com.myun.utils;

public class TimeClock {
      
	 private String id;
	 private String name;
	 private String muisformid;
	 private String time;
	 private String timeperiod;
	 private String enable;
	 private String volume_mode;
	 private String duration;
	 
	 
	 
	public TimeClock() {
		super();
		id=Utils.getId();
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getMuisformid() {
		return muisformid;
	}
	public void setMuisformid(String muisformid) {
		this.muisformid = muisformid;
	}
	public String getTime() {
		return time;
	}
	public void setTime(String time) {
		this.time = time;
	}
	public String getTimeperiod() {
		return timeperiod;
	}
	public void setTimeperiod(String timeperiod) {
		this.timeperiod = timeperiod;
	}
	public String getEnable() {
		return enable;
	}
	public void setEnable(String enable) {
		this.enable = enable;
	}
	public String getVolume_mode() {
		return volume_mode;
	}
	public void setVolume_mode(String volume_mode) {
		this.volume_mode = volume_mode;
	}
	public String getDuration() {
		return duration;
	}
	public void setDuration(String duration) {
		this.duration = duration;
	}
	
	
}
