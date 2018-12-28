package com.myun.utils;

import java.util.LinkedList;
import java.util.Random;

import android.os.SystemClock;

public class MusicForm {
	
    public String form;
    public LinkedList<String> idlist;
    public String id;
    
	public MusicForm(String id, String form) {
		super();
		this.form = form;
		idlist=new LinkedList<String>();
		this.id = id;
	}
	
	public void addMusicObj(String id){
		idlist.add(id);
	}
	
	public String getForm() {
		return form;
	}
	public void setForm(String form) {
		this.form = form;
	}


	public LinkedList<String> getMusiclist() {
		return idlist;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public int getCount() {
		
		
		return idlist.size();
	}




    
}
