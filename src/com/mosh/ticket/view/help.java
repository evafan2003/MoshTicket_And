package com.mosh.ticket.view;

import com.mosh.ticket.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;

public class help extends Activity {
	public void onCreate(Bundle savedInstanceState) {
		
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main_help); 
		
	}
}