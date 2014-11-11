package com.mosh.ticket.view;

import com.mosh.ticket.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.CheckBox;
 
public class common extends Activity{
	
	sHandler sder = new sHandler();
	CheckBox ghost;
	String BASEURL = "http://e.inner.mosh.cn:30210/";
//	String BASEURL = "http://e.mosh.cn/";
	public void IsHaveInternet() {
        ConnectivityManager manager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getActiveNetworkInfo();
        String state = "1";
        if ( (info == null || !info.isAvailable()) ) {
        	state = "2";
        }
		Message msg = new Message();
		Bundle bundle = new Bundle();
		bundle.putString("message", state);
		msg.what = 0;
		msg.setData(bundle);
		this.sder.sendMessage(msg);
    }
    
	private class sHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				Bundle date = msg.getData();
				String message = date.getString("message");
				if(message.equals("2")){
		            AlertDialog.Builder builder=new AlertDialog.Builder(common.this);  
		            builder.setIcon(android.R.drawable.ic_lock_lock).setMessage("当前网络不可用，请勾选单机版进入或者开启您的网络")
		            	.setPositiveButton("单机模式", new OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								ghost = (CheckBox) findViewById(R.id.ghost);
								ghost.setChecked(false);
							}
		            		
		            	}).setNeutralButton("设置网络", new OnClickListener(){

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								setInternet();
							}}).create().show(); 
				}
				break;
			}
		}
	}
    
    public void setInternet() {
    	Intent mIntent = new Intent();
    	ComponentName comp = new ComponentName("com.android.settings","com.android.settings.WirelessSettings");
    	mIntent.setComponent(comp);
    	mIntent.setAction("android.intent.action.VIEW");
    	startActivityForResult(mIntent, 0);
    }

	public class RunnableSetInternet implements Runnable {
		public void run() {
			IsHaveInternet();
		}
	} 

}