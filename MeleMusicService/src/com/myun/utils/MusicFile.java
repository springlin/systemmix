package com.myun.utils;

import android.database.sqlite.SQLiteDatabase;

public class MusicFile {

	private String id;
	private String path;
	private String sd_path;
	private String artist;
	private String album;
	private int duration;
	private String name;
	private SQLiteDatabase db;
	private String devicename;
	private String host;
	private boolean valid;
	
	public MusicFile() {
		super();
		valid=true;
		artist=null;
		album=null;
	}
	
	
	public MusicFile(String id, String path,  String name, String sd_path,
			SQLiteDatabase db) {
		super();
		this.id = id;
		this.path = path;
		this.sd_path = sd_path;
		this.name = name;
		this.db = db;
		valid=true;
		artist=null;
		album=null;
	}


	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public String getSd_path() {
		return sd_path;
	}
	public void setSd_path(String sd_path) {
		this.sd_path = sd_path;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public SQLiteDatabase getDb() {
		return db;
	}
	public void setDb(SQLiteDatabase db) {
		this.db = db;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}


	public String getDevicename() {
		return devicename;
	}


	public void setDevicename(String devicename) {
		this.devicename = devicename;
	}


	public String getHost() {
		return host;
	}


	public void setHost(String host) {
		this.host = host;
	}


	public boolean isValid() {
		return valid;
	}


	public void setValid(boolean valid) {
		this.valid = valid;
	}


	public String getArtist() {
		if(artist==null || artist.equals("")) return "null";
		return artist;
	}


	public void setArtist(String artist) {
		this.artist = artist;
	}


	public String getAlbum() {
		if(album==null || album.equals("")) return "null";
		return album;
	}


	public void setAlbum(String album) {
		this.album = album;
	}


	public int getDuration() {
		return duration;
	}


	public void setDuration(int duration) {
		this.duration = duration;
	}
	
	
	
}
