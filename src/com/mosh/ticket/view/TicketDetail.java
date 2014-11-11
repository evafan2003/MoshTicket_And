package com.mosh.ticket.view;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.mosh.ticket.R;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ArrayAdapter;
import android.widget.SimpleAdapter;
 
public class TicketDetail extends ListActivity implements OnClickListener {

	private ListView ticketDetailList;
//	private Button changeButton;
	List<Map<String, Object>> content;
	
	
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.ticket_detail);
		
		ticketDetailList = (ListView) findViewById(android.R.id.list);
//		changeButton = (Button) findViewById(R.id.changeButton);
		
		
//		changeButton.setOnClickListener(null);
		
		Bundle bundle = getIntent().getExtras();
		String[] a= bundle.getStringArray("content");
		
		ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("title", "名称：");
		map.put("content", a[0]);
		list.add(map);
		map = new HashMap<String, Object>();
		
		map.put("title", "密码：");
		map.put("content", a[1]);
		list.add(map);
		map = new HashMap<String, Object>();
		
		map.put("title", "票种：");
		map.put("content", a[2]);
		list.add(map);
		map = new HashMap<String, Object>();
		
		map.put("title", "代码：");
		map.put("content", a[3]);
		list.add(map);
		map = new HashMap<String, Object>();
		
		map.put("title", "价格：");
		map.put("content", a[4]);
		list.add(map);
		map = new HashMap<String, Object>();
		
		map.put("title", "姓名：");
		map.put("content", a[5]);
		list.add(map);
		map = new HashMap<String, Object>();
		
		map.put("title", "手机：");
		map.put("content", a[6]);
		list.add(map);
		map = new HashMap<String, Object>();
		
		map.put("title", "邮箱：");
		map.put("content", a[7]);
		list.add(map);
		map = new HashMap<String, Object>();
		
		map.put("title", "时间：");
		map.put("content", a[8]);
		list.add(map);
		
		
		SimpleAdapter adapter = new SimpleAdapter(this, list, 
        		R.layout.detail_text,
                new String[]{"title", "content"},
                new int[]{R.id.detailTitle, R.id.detailContent});
		ticketDetailList.setAdapter(adapter);
		
        /* 这个是数组string类型的数组 */ 
//        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(TicketDetail.this, R.layout.detail_text, a); 
//		ticketDetailList.setAdapter(arrayAdapter);
		ticketDetailList.setDivider(null);
		ticketDetailList.setFocusable(false); 

	}
	
	@Override
	public void onClick(View v) {

		
	}
	
}