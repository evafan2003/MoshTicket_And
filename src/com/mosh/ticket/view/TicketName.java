package com.mosh.ticket.view;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.mosh.ticket.R;

import android.app.Activity;
//import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
//import android.widget.CheckBox;
import android.widget.Toast;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.TextView;

public class TicketName extends Activity implements OnClickListener {
	
	private db db;
	private Button nextButton;
	private TextView ghost;
//	private TextView code, auth;
	private ListView lv = null;
	private List<Map<String, Object>> ticketnames;
	private String authkey, secret, ghostVal;
//	private CheckBox checkbox;
	ArrayList<String> listStr = null;
	static Map<String, Object> hmap = new HashMap<String, Object>();
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main_ticketname);

//		auth = (TextView) findViewById(R.id.auth);
//		code = (TextView) findViewById(R.id.code);
		ghost = (TextView) findViewById(R.id.ghost);
		nextButton = (Button) findViewById(R.id.nextButton);
		nextButton.setOnClickListener(this);
//		checkbox = (CheckBox) findViewById(R.id.ticketname);
		lv = (ListView) this.findViewById(R.id.lv);
		
		Bundle bundle = getIntent().getExtras();
		authkey = bundle.getString("authkey");
		secret = bundle.getString("secret");
	
		db = new db(this);
		String au = "&authkey=" + authkey + "&secret=" + secret;
		ticketnames = db.getAllTickname(au);
		db.close();
		System.out.println(ticketnames);
		//单机版标识
		ghostVal = bundle.getString("ghost");
		ghost.setText(ghostVal);
		getViewa();

	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.nextButton:
			Intent intent = new Intent(TicketName.this, MainEvent.class);
	        if(!hmap.isEmpty()){
	        	System.out.println(hmap);
				Set<String> key = hmap.keySet();
				StringBuffer buf = new StringBuffer();
		        for (Iterator it = key.iterator(); it.hasNext();) {
		            String s = (String) it.next();
		            buf.append(hmap.get(s) + ",");
		        }
		        String ticketname = buf.toString().substring(0, buf.toString().length() - 1 );
		        intent.putExtra("ticketname", ticketname);
	        }else{
	        	Toast.makeText(TicketName.this, "必须要选择一个票种", Toast.LENGTH_LONG).show();
	        	return;
	        }
			intent.putExtra("authkey", authkey);
			intent.putExtra("secret", secret);
			intent.putExtra("ghost", ghostVal);
			startActivity(intent);
			break;
		}
		
	}
	
	public void getViewa() {
        MyAdapter adapter = new MyAdapter(this, ticketnames, 
        		R.layout.list_ticketnames,
                new String[]{"ticketname", "checkb", "ticket_id"},
                new int[]{R.id.ticketname, R.id.checkb, R.id.ticket_id});
        
        lv.setAdapter(adapter);
        lv.setOnItemClickListener(new OnItemClickListener() {
        	
            @Override
            public void onItemClick(AdapterView<?> arg0, View view, int position, long arg3) {
                ViewHolder holder = (ViewHolder) view.getTag();
                holder.cb.toggle();// 在每次获取点击的item时改变checkbox的状态
                MyAdapter.isSelected.put(position, holder.cb.isChecked()); // 同时修改map的值保存状态
                
                if( holder.cb.isChecked() ){
                	hmap.put(holder.ticketid.getText().toString(), holder.ticketid.getText().toString());
                }else{
                	hmap.remove(holder.ticketid.getText().toString());
                }
            }

        });
        
	}

 
}