package com.mosh.ticket.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mosh.ticket.R;

import android.R.string;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;
import android.view.LayoutInflater; 
import android.content.Context;  
import java.util.Map;

public class TicketList extends ListActivity implements OnClickListener {
	private db db;
	private Button searchButton;
	private EditText searchText;
	private TextView ticketCount;
	ListView mListView = null;
	ProgressDialog dialogPro;
	private String eid = "";
	List<Map<String, Object>> list;
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main_ticketlist);
		ticketCount = (TextView)findViewById(R.id.ticketCount);
		searchButton = (Button) findViewById(R.id.searchButton);
		searchButton.setOnClickListener(this);
		searchText = (EditText) findViewById(R.id.searchText);
		dialogPro = new ProgressDialog(this);
		Bundle bundle = getIntent().getExtras();
		eid = bundle.getString("eid");
		
		//ListView lists = (ListView) findViewById(R.id.ListView01);
		getView("");

	}
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.searchButton : 
			String name = searchText.getText().toString();
			getView(name);
			break;
		}
		
	}
	
	public void getView(String name) {
		db = new db(this);
		list = db.selectList(eid, name);
		db.close();
        
    	int niemals = list.size();
    	String usedCount =db.countUsed(eid, name);
    	
    	ticketCount.setText("总票数："+usedCount+"/"+Integer.valueOf(niemals) );
		
    	
		SimpleAdapter adapter = new SimpleAdapter(this, list, 
        		R.layout.list_tickets,
                new String[]{"id", "pwd", "state", "ptime"},
                new int[]{R.id.id, R.id.pwd, R.id.state, R.id.ptime});
        setListAdapter(adapter);
        //ListView listView = new ListView(this);
        mListView = getListView();
        
        //点击弹框
        mListView.setOnItemClickListener(new OnItemClickListener() {

    		public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
    		
	    	String[] a = {
	    			list.get(position).get("title").toString(),
	    			list.get(position).get("pwd").toString(),
	    			list.get(position).get("ticket").toString(),
	    			list.get(position).get("code").toString(),
	    			list.get(position).get("price").toString(),
	    			list.get(position).get("name").toString(),
	    			list.get(position).get("tel").toString(),
	    			list.get(position).get("email").toString(),
	    			list.get(position).get("ptime").toString()
	    	};
	    	
            Intent intent=new Intent(); 
            intent.putExtra("content", a); 
            
            intent.setClass(TicketList.this,TicketDetail.class); 
            //启动intent的Activity 
            TicketList.this.startActivity(intent); 
	    	
    			
    			
			//------------------------------------------------------------------------------------------------------------------------------------------------
    			
    			
//	    	String[] a = {
//	    			list.get(position).get("title").toString(),
//	    			list.get(position).get("pwd").toString(),
//	    			list.get(position).get("ticket").toString(),
//	    			list.get(position).get("code").toString(),
//	    			list.get(position).get("price").toString(),
//	    			list.get(position).get("name").toString(),
//	    			list.get(position).get("tel").toString(),
//	    			list.get(position).get("email").toString(),
//	    			list.get(position).get("ptime").toString()
//	    	};
//	    	
//	    	
//	    	//自定义弹框 niemals!!!!
//	        WindowManager manager = getWindowManager();  
//	        Display display = manager.getDefaultDisplay();  
//	        int width = display.getWidth();  
//	        int height = display.getHeight();  
//	    	
//	    	Context mContext = TicketList.this;
//	    	LayoutInflater inflater = (LayoutInflater)mContext.getSystemService (Context.LAYOUT_INFLATER_SERVICE);
//
//	    	View dialog=inflater.inflate(R.layout.title_bar, null);
//
//	    	
//            AlertDialog.Builder builder = new AlertDialog.Builder(TicketList.this);
//            builder.setCustomTitle(dialog);
////            builder.setTitle("详细信息");
//            
//            builder.setItems(a, new DialogInterface.OnClickListener() {  
//                 
//            	
//                @Override  
//                public void onClick(DialogInterface dialog, int which) {  
//                    // TODO Auto-generated method stub  
//                   
//                }  
//            }).create().show();  
	 
    	    }
    	});		
	}
}