package com.myun.core;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeoutException;

import com.myun.utils.MusicFile;
import com.myun.utils.Utils;







import android.app.ActivityManager;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;



public class MusicPlayer extends Thread{
    
    private MediaPlayer player=null;
    private Uri uri=null;
    private boolean run=true;
    public int position=0;
    private Context mContext=null;
    public int status=-1;
    public String title=null;
    private boolean is_callback=false;
    
	public BlockingQueue<String> playlist=null;
	public MusicPlayerListener musicplayerlistener=null;
	public String playmusic=null;
//	public BlockingQueue<String> sdcardlist=null;
	
	public MusicPlayer(Context context, BlockingQueue<String> playlist/*, BlockingQueue<String> sdcardlist*/) {

		this.mContext=context;
		this.playlist=playlist;
		is_callback=false;
		run=true;
		//this.sdcardlist=sdcardlist;
		onCreatePlayer();
	}
    public void onCreatePlayer() {
 
        player=new MediaPlayer();
        player.setAudioStreamType(AudioManager.STREAM_MUSIC);
        player.setOnBufferingUpdateListener(BufferingUpdateListener);
        player.setOnCompletionListener(listener);
        player.setOnInfoListener(OnInfoListener);
        player.setOnErrorListener(onerrorlistener);
    }
   

	@Override
	public void run() {
		super.run();
		
		Utils.log("MusicPlayer Thread Start .......");
		while(run){
			
			   try{
				   
				    
			    	String url=playlist.take();
			    	playmusic=url;
			    	status=2;
			    	if(musicplayerlistener!=null){
			    		musicplayerlistener.setMusicPlayerStatus("start");
			    		is_callback=true;
			    	}
			    	
			        if(player!=null && player.isPlaying()){
			            player.stop();
			        }
			    	player.reset();
			    	url=url.replace("%3A", ":");
				    url=url.replace("%2F", "/");
				    
			        try {
		    			title = URLDecoder.decode(url,"utf-8");
		    	    } catch (UnsupportedEncodingException e) {
		    			e.printStackTrace();
		    	    }
			        
			        Utils.log("MusicPlayer  Starting ......."+title);
			        
			        
		            int index=title.lastIndexOf('/'), end=title.indexOf("?deviceid");
		            title=title.substring(index!=-1?index+1:0, end==-1?title.length():end);
				    
//		            if(url.contains("http://") && !testaddr(url)) {
//		            	is_callback=false;
//		            	throw new Exception();
//		            }
		            
		            is_callback=false;
			        uri=Uri.parse(url);
			        player.setDataSource(mContext, uri);
                    player.prepare();
                    player.start();
                    status=1;
                    Utils.setVar("playpath", playmusic);
			        
			        
			    	
		
			   }catch (Exception e) {
				   
				    status=0;
                    e.printStackTrace();
                    Utils.log("error ..."+e.toString());
			    	if(playlist.size()==0 && musicplayerlistener!=null && !is_callback){
			    		
			    		is_callback=true;
			    		musicplayerlistener.setMusicPlayerStatus("finish");
			    		
			    	}
			    	
               } 
		
		
		
		
		}
		
	}

   public boolean testaddr(String uri){
        
		try {
			 

			    URL url = new URL(uri);
				HttpURLConnection conn=(HttpURLConnection)url.openConnection();
				conn.setConnectTimeout(6000);
				if (conn.getResponseCode() != 200)
				{

				   conn.disconnect();
				   conn=null;
				   return false;

				}
				conn.disconnect();
				conn=null;
				return true;
		}catch (MalformedURLException e) {

	    }catch (IOException e) {
		  
		}
		return false;
				
   }
    

    public MediaPlayer.OnCompletionListener listener=new  MediaPlayer.OnCompletionListener(){
        
        public void onCompletion(MediaPlayer arg0) {
           
        	
        	  status=0;
              if(musicplayerlistener!=null && !is_callback){
            	  
            	  is_callback=true;
            	  musicplayerlistener.setMusicPlayerStatus("finish");
            	  
              }
              Log.i("TAG","OnCompletionListener....finish");
        }
    };    
    public MediaPlayer.OnBufferingUpdateListener BufferingUpdateListener=new   MediaPlayer.OnBufferingUpdateListener(){

		
		public void onBufferingUpdate(MediaPlayer mp, int percent) {
		
			// Log.i("TAG","percent...."+percent);
			 

		}
		

    };  
    public MediaPlayer.OnInfoListener OnInfoListener=new MediaPlayer.OnInfoListener() {
		
		
		public boolean onInfo(MediaPlayer mp, int what, int extra) {
			
	        if (what == MediaPlayer.MEDIA_INFO_BUFFERING_START) {
	        	 Log.i("TAG", "MEDIA_INFO_BUFFERING_START");

	        } else if (what == MediaPlayer.MEDIA_INFO_BUFFERING_END) {
	        	 Log.i("TAG", "MEDIA_INFO_BUFFERING_end");

	        }
			return false;
		}
	};
	public MediaPlayer.OnErrorListener onerrorlistener =new MediaPlayer.OnErrorListener(){

		@Override
		public boolean onError(MediaPlayer mp, int what, int extra) {
			Utils.log("OnErrorListener  ...........................");
			status=0;
	    	if(playlist.size()==0 && musicplayerlistener!=null && !is_callback){
	    		
	    		is_callback=true;
	    		musicplayerlistener.setMusicPlayerStatus("finish");
	    	}
			return false;
		}
		
	};


	public String getMusicPlayState(){
		
		String info=new String(), state="", title="";
		int d=0, p=0;
       		
        if(player==null){
            info+="state=end\n";
            return info;
        }
		if(status==0){
			
//			if(sdcardlist.size()>0){
//				state="prepare";
//				d=0;
//				p=d;
//				title=this.title;
				
			//}else{
				status=-1;
				state="finish";
				d=0;//player.getDuration();
				p=d;
				title=this.title;
			//}
			
			
		}else if(status==1){
			state="running";
			if(!player.isPlaying()){
				state="pause";
			}
			title=this.title;
			d=player.getDuration();
			p=player.getCurrentPosition();
		}else if(status==2){
			
			state="prepare";
			d=0;
			p=d;
			title=this.title;
			
			
		}else{	
			state="end";
		}
        
		info+="duration="+d+"\n"
		     +"position="+p+"\n"
		     +"title="+title+"\n"
		     +"state="+state+"\n";
		//Utils.log(info);
		return info;
	}
    
	public void onPause() {

        if(player!=null && player.isPlaying()){
            player.pause();
        }
		
	}
	public int getgetDuration(){
		
		if(status==1){

			return player.getDuration();
			
		}
		return 0;
	}
	
	public int getCurrentPosition(){
		
		if(status==1){

			return player.getCurrentPosition();
			
		}
		return 0;
	}
	public  String doPlayStateInfo(String info, String name){
		
		
		int indef=info.indexOf(name);
		if(indef==-1) return null;
		
		return info.substring(indef+1+name.length(), info.indexOf("\n", indef));
		
	}
	public void onStart() {

        if(player!=null && !player.isPlaying()){
            player.start();
        }
		
	}
	
	public boolean onGetPlayerStatus(){
		if(player==null) return false;
		return player.isPlaying();
	}
	public void onStartorPause(){
		
		if(player==null) return ;
		if(player.isPlaying()){
			player.pause();
		}else{
		    player.start();
		}
		
		
	}
	public void onSeek(int position){
		
		 onStart();
         if(player!=null){
            player.seekTo(position);

         }
		
	}
	public void onReset(){
		
		Utils.log("musicplayer ......onreset");
		status=-1;
		playlist.clear();
		if(player==null) return ;
        if(player.isPlaying()){
            player.stop();
        }
    	player.reset();
    	
	}
	
	public void finish() {

		
		
	
	}

	
    public String getPlaymusic() {
		return playmusic;
	}
	protected void onDestroy() {

		Utils.log("MusicPlayer onDestroy ........ !!!");
        run=false;
        if(player!=null && player.isPlaying()){
            player.stop();
        }
        
        if(player!=null){
           player.reset();
           player.release();
           player=null;
        }

    }
	  
    public MusicPlayerListener getMusicplayerlistener() {
		return musicplayerlistener;
	}
	public void setMusicplayerlistener(MusicPlayerListener musicplayerlistener) {
		this.musicplayerlistener = musicplayerlistener;
	}

	public interface MusicPlayerListener {
    	 public void setMusicPlayerStatus(String status);
    }
}
