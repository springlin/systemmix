package com.myun.core;

import android.os.Bundle;
import android.os.RemoteException;
import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;

public class CaraOKUI extends Activity {

	public static CaraOKUI instance=null;
	public Button caraok, gener, auxin;
	public SeekBar seekbar1, seekbar2, seekbar3, seekbar4, seekbar5;
	public TextView txt1, txt2, txt3, txt4, txt5, mode_txt;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		  
//		setContentView(R.layout.activity_main);
//		gener=(Button)this.findViewById(R.id.gener);
//		caraok=(Button)this.findViewById(R.id.caraok);
//		auxin=(Button)this.findViewById(R.id.auxin);
//		
//		auxin.setOnClickListener(onclicklistener);
//		caraok.setOnClickListener(onclicklistener);
//		gener.setOnClickListener(onclicklistener);
//		
//		seekbar1=(SeekBar)this.findViewById(R.id.ctrl_bar1);
//		seekbar1.setOnSeekBarChangeListener(seekbarlistener);
//		
//		seekbar2=(SeekBar)this.findViewById(R.id.ctrl_bar2);
//		seekbar2.setOnSeekBarChangeListener(seekbarlistener);
//		
//		seekbar3=(SeekBar)this.findViewById(R.id.ctrl_bar3);
//		seekbar3.setOnSeekBarChangeListener(seekbarlistener);
//		
//		seekbar4=(SeekBar)this.findViewById(R.id.ctrl_bar4);
//		seekbar4.setOnSeekBarChangeListener(seekbarlistener);
//		
//		seekbar5=(SeekBar)this.findViewById(R.id.ctrl_bar5);
//		seekbar5.setOnSeekBarChangeListener(seekbarlistener);
//		
//		
//		txt1=(TextView)this.findViewById(R.id.bar1_txt);
//		txt2=(TextView)this.findViewById(R.id.bar2_txt);
//		txt3=(TextView)this.findViewById(R.id.bar3_txt);
//		txt4=(TextView)this.findViewById(R.id.bar4_txt);
//		txt5=(TextView)this.findViewById(R.id.bar5_txt);
//		mode_txt=(TextView)this.findViewById(R.id.mode_txt);
		
		instance=this;
		Intent intent=this.getIntent();
		String ui=intent.getStringExtra("ui");
		if(ui!=null && ui.equals("true")){
		
		}else{
		  Intent i=new Intent("com.myun.musicservice");
		  i.putExtra("action", "1");
		  startService(i);
          finish();
		}
	}
    public View.OnClickListener onclicklistener=new View.OnClickListener(){

		@Override
		public void onClick(View v) {
		
			  if(caraok==v){
				  
				///  MusicService.instance.onCaraOKUIDg(); 
				  finish();
				    
			  }else if(gener==v){
					int p1buf[]={0xfb}, p0buf[]={0xfd};
					try {
						MusicService.instance.remoteservice.jni_i2c_writedev(0x27, 0x03, p1buf, 1);
						MusicService.instance.remoteservice.jni_i2c_writedev(0x27, 0x02, p0buf, 1);
					} catch (RemoteException e) {
						e.printStackTrace();
					} 
					mode_txt.setText("普通模式"); 
					
			  }else if(auxin==v){
				  
				  
				    mode_txt.setText("AUX-IN模式");
					int p1buf[]={0xff}, p0buf[]={0xff};
					try {
						MusicService.instance.remoteservice.jni_i2c_writedev(0x27, 0x03, p1buf, 1);
						MusicService.instance.remoteservice.jni_i2c_writedev(0x27, 0x02, p0buf, 1);
					} catch (RemoteException e) {
						e.printStackTrace();
					} 
			  }
			
		}
    	
    };
    
	public SeekBar.OnSeekBarChangeListener seekbarlistener=new SeekBar.OnSeekBarChangeListener(){

		@Override
		public void onProgressChanged(SeekBar seekbar, int progress,
				boolean fromUser) {
		     int value=seekbar.getProgress()-1;
		     if(value<0) value=0; 
		     int buf10[]={value/10}, buf1[]={value%10};
		     Log.i("mic", "value"+value+" 10::"+buf10[0]+"  1::"+buf1[0]);
		     if(seekbar==seekbar1){
		    	    
					
					try {
					    MusicService.instance.remoteservice.jni_i2c_writedev(0x44, 0x40|buf10[0], buf10, 0);
						MusicService.instance.remoteservice.jni_i2c_writedev(0x44, 0x50|buf1[0], buf1, 0);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
		    	    txt1.setText("IN1--->"+value);
		    	 
		     }else 	if(seekbar==seekbar2){
		    	    
					//int value=seekbar.getProgress()-1, buf10[]={value/10}, buf1[]={value%10};
					try {
						MusicService.instance.remoteservice.jni_i2c_writedev(0x44, 0x80|buf10[0], buf10, 0);
						MusicService.instance.remoteservice.jni_i2c_writedev(0x44, 0x90|buf1[0], buf1, 0);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
		    	 
					txt2.setText("IN2--->"+value);
		     }else 	if(seekbar==seekbar3){
		    	    
					//int value=seekbar.getProgress()-1, buf10[]={value/10}, buf1[]={value%10};
					try {
						MusicService.instance.remoteservice.jni_i2c_writedev(0x44, 0x00|buf10[0], buf10, 0);
						MusicService.instance.remoteservice.jni_i2c_writedev(0x44, 0x10|buf1[0], buf1, 0);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					txt3.setText("IN3--->"+value);
		    	 
		     }else 	if(seekbar==seekbar4){
		    	    
					//int value=seekbar.getProgress()-1, buf10[]={value/10}, buf1[]={value%10};
					try {
						MusicService.instance.remoteservice.jni_i2c_writedev(0x44, 0x20|buf10[0], buf10, 0);
						MusicService.instance.remoteservice.jni_i2c_writedev(0x44, 0x30|buf1[0], buf1, 0);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					txt4.setText("IN4--->"+value);
		    	 
		     }else 	if(seekbar==seekbar5){
		    	    
					//int value=seekbar.getProgress()-1, buf10[]={value/10}, buf1[]={value%10};
					try {
						MusicService.instance.remoteservice.jni_i2c_writedev(0x44, 0x60|buf10[0], buf10, 0);
						MusicService.instance.remoteservice.jni_i2c_writedev(0x44, 0x70|buf1[0], buf1, 0);
					} catch (RemoteException e) {
						e.printStackTrace();
					}
					txt5.setText("IN5--->"+value);
		    	 
		     }
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
                    
			
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekbar) {


			
			

		}
		
	};
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		
		
		return true;
	}

	@Override
	protected void onPause() {
		super.onPause();
		//finish();
	}

	@Override
	public void finish() {
		super.finish();
		instance=null;
	}
}