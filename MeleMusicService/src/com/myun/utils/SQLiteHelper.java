package com.myun.utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;


import com.myun.core.MusicService;
import com.myun.utils.Utils;



import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;



		

public class SQLiteHelper extends SQLiteOpenHelper {
	
	
	

	private Cursor cursor=null;
    public  SQLiteDatabase sdcard_db=null;  
	public  SQLiteDatabase extern_db=null;
	public  SQLiteDatabase running_db=null;
    public  LinkedList<SQLiteDatabase> extern_db_list=new LinkedList<SQLiteDatabase>();
	
	
	public SQLiteHelper(Context context, String dbname, CursorFactory factory, int version) {
		super(context, dbname, factory, version);

	}
	

	@Override
	public void onCreate(SQLiteDatabase db) {
		    log("SQLiteHelper ... onCreate");
			db.execSQL("CREATE TABLE IF NOT EXISTS " +
					"musicinfo" + "(" +
					"id"        + " varchar," +
					"path"      + " varchar primary key," +
					"name"      + " varchar," +
					"artist"    + " varchar," +
					"album"     + " varchar" +

			")");


			db.execSQL("CREATE TABLE IF NOT EXISTS " +
					"folderinfo" + "(" +
					"path" + " varchar primary key" +
					
					
			")");
			db.execSQL("CREATE TABLE IF NOT EXISTS " +
					"deviceinfo" + "(" +
					"id"             + " varchar," +
					"availablespace" + " varchar" +
					
					
			")");
			
			db.execSQL("CREATE TABLE IF NOT EXISTS " +
					"favorites" + "(" +
					"type"    + " varchar primary key,"+
					"idlist"  + " varchar" +
					
			")");

			db.execSQL("CREATE TABLE IF NOT EXISTS " +
					"musicform" + "(" +
					"id"       + " varchar," +
					"formname"  + " varchar primary key," +
					"idlist"  + " varchar" +
			")");
			
			db.execSQL("CREATE TABLE IF NOT EXISTS " +
					"timeclock" + "(" +
					"id"       + " varchar primary key," +
					"name"  + " varchar," +
					"formid"  + " varchar," +
					"time"  + " varchar," +
					"timeperiod"  + " varchar," +
					"volumemode"  + " varchar," +
					"duration"  + " varchar" +
			")");


			
	}
	

	
	public void onCreateExtern(SQLiteDatabase db) {
	    log("SQLiteHelper ... onCreateExtern");
	    
	    
	    
		db.execSQL("CREATE TABLE IF NOT EXISTS " +
				"musicinfo" + "(" +
				"id"        + " varchar," +
				"path"      + " varchar primary key," +
				"name"      + " varchar," +
				"artist"    + " varchar," +
				"album"     + " varchar" +

		")");


		db.execSQL("CREATE TABLE IF NOT EXISTS " +
				"folderinfo" + "(" +
				"path" + " varchar primary key" +
				
				
		")");
		db.execSQL("CREATE TABLE IF NOT EXISTS " +
				"deviceinfo" + "(" +
				"id"             + " varchar," +
				"availablespace" + " varchar" +
				
				
		")");
		
		db.execSQL("CREATE TABLE IF NOT EXISTS " +
				"favorites" + "(" +
				"type"    + " varchar,"+
				"idlist"  + " varchar primary key" +
				
		")");

		db.execSQL("CREATE TABLE IF NOT EXISTS " +
				"musicform" + "(" +
				"id"       + " varchar," +
				"formname"  + " varchar primary key," +
				"idlist"  + " varchar" +
		")");
		db.execSQL("CREATE TABLE IF NOT EXISTS " +
				"timeclock" + "(" +
				"id"       + " varchar primary key," +
				"name"  + " varchar," +
				"formid"  + " varchar," +
				"time"  + " varchar," +
				"timeperiod"  + " varchar," +
				"volumemode"  + " varchar" +
		")");
    }	
	@Override
	public void onUpgrade(SQLiteDatabase db, int arg1, int arg2) {
		
		log("Upgrade SQLite newVersion "+arg2+" oldVersion "+arg1);
		onDeldata(db);
		onCreate(db);
	}
	public void onDeldata(SQLiteDatabase db)
	{
		
		db.execSQL("DELETE FROM "+"musicinfo");

	}
	public void onDelTable(SQLiteDatabase db, String tablename)
	{
				
		
		db.execSQL("DELETE FROM "+ tablename);
		
	}
	
	



    public SQLiteDatabase openExternDatabase(File file) {
    	
    
    	if( file==null || !file.exists() ) { extern_db=null; return null;}
        try {
            String databaseFilename = file.getPath();

            if ((new File(databaseFilename)).exists()) {
            	extern_db = SQLiteDatabase.openOrCreateDatabase(databaseFilename, null);
            	
            }
            
            onCreateExtern(extern_db);
           
        } catch (Exception e) {
        	
            e.printStackTrace();
            extern_db=null;
            
        }
        return null;
    }	
	

    
    
    public Cursor querytable(SQLiteDatabase db, String table){
    	
    	return  db.query(table, null, null, null, null, null, null);
    	
    }
	
	
	public void add(SQLiteDatabase db, ContentValues values, String table){
		
		try{
		
		    db.insert(table, "id", values);
		 
		 }catch(Exception e){
            log("error"+e.toString());
         }
	}

    public  void addMusicList(SQLiteDatabase db, LinkedList<MusicFile> list){
    	
    	if(list==null) return ;
    	for(int i=0; i<list.size(); i++){
    		addMusicItem(db, list.get(i));			    	  
    	}
    	
    }
    public  void addMusicItem(SQLiteDatabase db, MusicFile mf){
    	
    	    if(db==null) return ;
	    	add(db, getContentValues(mf), "musicinfo");			    	  
    		
    }
    public  void addMusicFormList(SQLiteDatabase db, LinkedList<MusicForm> list){
    	
    	if(list==null) return ;
    	for(int i=0; i<list.size(); i++){
    		addMusicFormItem(db, list.get(i));			    	  
    	}
    	
    }
    public  void addMusicFormItem(SQLiteDatabase db, MusicForm mf){
    	
    	    if(db==null) return ;
	    	add(db, getContentValues(mf), "musicform");			    	  
    		log("mf ..."+mf.toString());
    }
    
    public  void addTimeClockItem(SQLiteDatabase db, TimeClock tc){
    	
	    if(db==null) return ;
    	add(db, getContentValues(tc), "timeclock");			    	  
		//log("mf ..."+mf.toString());
    }
    public ContentValues getContentValues(MusicFile mf){
    	
  		ContentValues values=new ContentValues();
		values.put("id", mf.getId());	
		
		values.put("name", mf.getName());			
		String path=MusicService.instance.sd.getMountedPath(mf.getPath());
		if(path!=null){
			path=mf.getPath().substring(path.length());
		}else{
			path=mf.getPath();
		}
        values.put("path", path);
        values.put("artist", mf.getArtist());
        values.put("album", mf.getAlbum());
		
		return values;
    }
    
    public ContentValues getContentValues(MusicForm mf){
    	
  		ContentValues values=new ContentValues();
		values.put("id", mf.getId());			
		values.put("formname", mf.getForm());			

		String str="";
		for(int i=0;i<mf.getMusiclist().size();i++)
			 str+=mf.getMusiclist().get(i)+">";
        values.put("idlist", str);
		return values;
    }
    
    public ContentValues getContentValues(TimeClock tc){
    	
  		ContentValues values=new ContentValues();
		values.put("id", tc.getId());			
		values.put("formid", tc.getMuisformid());			

        values.put("name", tc.getName());
        values.put("time", tc.getTime());
        values.put("timeperiod", tc.getTimeperiod());
        values.put("volumemode", tc.getVolume_mode());
        values.put("duration", tc.getDuration());
		return values;
    }
    public long queryDeviceSpace(SQLiteDatabase db){
    	
    	if(db==null) return -1;
    	
		cursor = db.query("deviceinfo", null, null, null, null, null, null);
		cursor.moveToFirst();
		long ret=0;
		while(!cursor.isAfterLast()){

		
			try{
			  ret=Long.parseLong(cursor.getString(1));
			}catch(NumberFormatException e){
				
			}
			cursor.moveToNext();
		}
		cursor.close();
    	return ret;
    	
    }
    public void updateDeviceSpace(SQLiteDatabase db, long space){
    	
    	Utils.log("deviceinfo vol "+space);
  	   if(db==null) return ;
  	      String where="id='"+001+"'";
	 	  ContentValues values=new ContentValues();
		  values.put("availablespace", space+"");
		  values.put("id", "001");
	   try{
	

		   
		  update(db, values, "deviceinfo" , where);
		  
		  
		}catch(Exception e){
			
            
        }finally{
        	
        	
        	add(db, values, "deviceinfo");
        	//Utils.log("deviceinfo555"+space);
        }
    	
    }

	public LinkedList<MusicFile> queryAllMusic(SQLiteDatabase db, LinkedList<MusicFile> list){
		
		
		if(list==null) list = new LinkedList<MusicFile>();		
	    if(db==null) return list;
	    LinkedList<String> badlist = new LinkedList<String>();	
		cursor = db.query("musicinfo", null, null, null, null, null, null);
		cursor.moveToFirst();
		String dbpath=MusicService.instance.sd.getMountedPath(db.getPath());
		
		while(!cursor.isAfterLast()){ 
			
			
	        MusicFile m=new MusicFile();
	        if(dbpath==null) dbpath="";
	        m.setId(cursor.getString(0));
	        m.setPath(dbpath+cursor.getString(1));
	        Utils.log("****dbpath"+dbpath);
	        File f = new File(m.getPath());
		    if(!f.exists()){
		    	log("Error file path: "+m.getPath());
		    	badlist.add(m.getId());
		    	cursor.moveToNext();
		    	continue;
		    }
			m.setName(cursor.getString(2));
			m.setDb(db);
			m.setArtist(cursor.getString(3));
			m.setAlbum(cursor.getString(4));
			list.add(m); 
			

			
			
			cursor.moveToNext();
			
			
		}		
		cursor.close();
		
		for(int i=0; i<badlist.size(); i++)
			delete(db, badlist.get(i));
		
		return list;
		
 
    }
	public LinkedList<MusicForm> queryAllMusicForm(SQLiteDatabase db, LinkedList<MusicForm> list){
		
		
		if(list==null) list = new LinkedList<MusicForm>();
		else list.clear();
	    if(db==null) return list;	
		cursor = db.query("musicform", null, null, null, null, null, null);
		cursor.moveToFirst();
		
		
		while(!cursor.isAfterLast()){ 
			
			
			MusicForm m=new MusicForm(cursor.getString(0), cursor.getString(1));
            String idlist=cursor.getString(2);
            if(!idlist.equals("")){
            	
            	String items[]=idlist.split(">");
            	for(int i=0; i<items.length; i++){
            		 m.addMusicObj(items[i]);
            	}
            	
            	
            }
			list.add(m); 
			

			
			
			cursor.moveToNext();
			
			
		}		
		cursor.close();

		
		return list;
		
 
    }
	
	public LinkedList<TimeClock> queryAllClockTime(SQLiteDatabase db, LinkedList<TimeClock> list){
		
		
		if(list==null) list = new LinkedList<TimeClock>();
		else list.clear();
	    if(db==null) return list;	
		cursor = db.query("timeclock", null, null, null, null, null, null);
		cursor.moveToFirst();
		
		
		while(!cursor.isAfterLast()){ 
			
			
			TimeClock tc=new TimeClock();
            tc.setId(cursor.getString(0));
            tc.setName(cursor.getString(1));
            tc.setMuisformid(cursor.getString(2));
            tc.setTime(cursor.getString(3));
            tc.setTimeperiod(cursor.getString(4));
            tc.setVolume_mode(cursor.getString(5));
            tc.setDuration(cursor.getString(6));
            
            
			list.add(tc); 
			

			
			cursor.moveToNext();
			
			
		}		
		cursor.close();

		
		return list;
		
 
    }
	public String queryMusicFavorites(SQLiteDatabase db){
		
		
     
	    if(db==null) return "";	
		cursor = db.query("favorites", null, null, null, null, null, null);
		cursor.moveToFirst();
		String str="";
		boolean flag=false;
		if(cursor.getCount()>0) flag=true;
		while(!cursor.isAfterLast()){ 
            str=cursor.getString(1);
			cursor.moveToNext();
            flag=true;
		}		
		cursor.close();

		if(!flag){  
			 
			 ContentValues values=new ContentValues();
		     values.put("idlist", "");
		     values.put("type", "speaker");
		     add(db, values, "favorites");	
	    }
		return str;
		
 
    }
	public void update(SQLiteDatabase db, ContentValues values, String table,  String where){
		 db.update(table, values, where, null);
	}

	public void updateMusicinfo(SQLiteDatabase db, MusicFile m){
		
	  if(db==null) return ;
	   try{
		  String where="id='"+m.getId()+"'";
		  update(db, this.getContentValues(m), "musicinfo" , where);
		  
			}catch(Exception e){
        
         }finally{
          // db.endTransaction(); //处理完成
        }
		  
	}
	public void updateMusicFavorites(SQLiteDatabase db, String strlist){
		
		   if(db==null) return ;
		   
		    try{
		  	      ContentValues values=new ContentValues();
			      values.put("idlist", strlist);
			      update(db, values, "favorites" , "type=\"speaker\"");
			  
		    }catch(Exception e){
		    	

	   		     
	        }finally{
	        	 
	     
     
	     
	             // db.endTransaction(); //处理完成
	        }
			  
	}
	public void updateItem(SQLiteDatabase db, String key, String value, String id){
		try{
		    String where="id='"+id+"'";
	  	     ContentValues values=new ContentValues();
		     values.put(key, value);
		     update(db, values, "musicinfo" , where);
		  
		}catch(Exception e){
        
        }finally{
              // db.endTransaction(); //处理完成
        }
	  
	}
	public void updateMusicForm(SQLiteDatabase db, MusicForm mform){
		try{
		    String where="id='"+mform.getId()+"'";
	  	    
		     update(db, this.getContentValues(mform), "musicform" , where);
		  
		}catch(Exception e){
        
        }finally{
              // db.endTransaction(); //处理完成
        }
	  
	}
	
	public void updateTimeClock(SQLiteDatabase db, TimeClock tc){
		try{
		    String where="id='"+tc.getId()+"'";
	  	    
		     update(db, this.getContentValues(tc), "timeclock" , where);
		  
		}catch(Exception e){
        
        }finally{
              // db.endTransaction(); //处理完成
        }
	  
	}
    public void delete(SQLiteDatabase db, String table,  String where){
		 db.delete(table, where, null);
	}
	
    public void delete(SQLiteDatabase db, MusicFile m){
    	
		 db.delete("musicinfo", "id='"+m.getId()+"'", null);
	}
    public void delete(SQLiteDatabase db, String id){
    	
		 db.delete("musicinfo", "id='"+id+"'", null);
	}
    public void delete(SQLiteDatabase db, MusicForm m){
    	
		 db.delete("musicform", "id='"+m.getId()+"'", null);
	}
    
    public void delete(SQLiteDatabase db, TimeClock tc){
    	
    	db.delete("timeclock", "id='"+tc.getId()+"'", null);
    }
    
    public File getDatabasePath(String path) {
        

            String dbPath = path+"/melemusic.db";
           
            boolean isFileCreateSuccess = false; 
         
            File dbFile = new File(dbPath);
            if(!dbFile.exists()){
                try {                    
                    isFileCreateSuccess = dbFile.createNewFile();//创建文件
                } catch (IOException e) {
                   
                    e.printStackTrace();
                }
            }
            else     
                isFileCreateSuccess = true;          
            if(isFileCreateSuccess)
                return dbFile;
            else 
                return null;
       
    }
    
    public SQLiteDatabase getExternData(String path){
    	
    	return openExternDatabase(getDatabasePath(path));
    	
    }
	public void log(String msg){
		Log.i("Music SQlite", msg);
	}


}
