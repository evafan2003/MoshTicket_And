package com.mosh.ticket.view;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
//import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.mosh.ticket.CaptureActivity;
import com.mosh.ticket.R;

public class MainEvent extends common implements OnClickListener {

	private static CaptureClient udb;
	protected Handler myHander, pHandler, aHandler;
	private Button b1, b2, b3, b4, b5;
	private EditText editText1;
	private TextView jieguo, ticket, password, price, tel, email, code, auth, ghost, checktime;
	private static final int SELECT_PICTURE = 100;
	private String codeNum = "", ticket_id = "";
	private ProgressDialog dialogPro;
	private db db;
	protected HttpClient http = null;
	String eid;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.main_event);
		
		udb = new CaptureClient();
		myHander = new MyHandler();
		pHandler = new postHandler();
		aHandler = new appendHandler();
		b1 = (Button) findViewById(R.id.button1);
		b2 = (Button) findViewById(R.id.button2);
		b3 = (Button) findViewById(R.id.button3);
		b4 = (Button) findViewById(R.id.button4);
		b5 = (Button) findViewById(R.id.button5);
		b1.setOnClickListener(this);
		b2.setOnClickListener(this);
		b3.setOnClickListener(this);
		b4.setOnClickListener(this);
		b5.setOnClickListener(this);
		editText1 = (EditText) findViewById(R.id.edittext1);
		editText1.setText("13307006922");
		jieguo = (TextView) findViewById(R.id.jieguo);
		ticket = (TextView) findViewById(R.id.ticket);
		password = (TextView) findViewById(R.id.password);
		price = (TextView) findViewById(R.id.price);
		tel = (TextView) findViewById(R.id.tel);
		email = (TextView) findViewById(R.id.email);
		auth = (TextView) findViewById(R.id.auth);
		code = (TextView) findViewById(R.id.code);
		checktime = (TextView) findViewById(R.id.checktime);
		ghost = (TextView) findViewById(R.id.ghost);
		
		dialogPro = new ProgressDialog(this);
		dialogPro.setMessage("Please wait while loading...");
		dialogPro.setIndeterminate(true);
		dialogPro.setCancelable(true);
		http = new HttpClient();
		Bundle bundle = getIntent().getExtras();
		String authkey = bundle.getString("authkey");
		String secret = bundle.getString("secret");
		ticket_id = bundle.getString("ticketname");
	
		db = new db(this);
		String au = "&authkey=" + authkey + "&secret=" + secret;
		String d = db.selectCode(au);
		db.close();
		eid = db.getEidByAuth(au);
		db.close();
		auth.setText(au);
		code.setText("&code=" + d);
		//单机版标识
		String ghostVal = bundle.getString("ghost");
		ghost.setText(ghostVal);

	}

	public class RunnableFile implements Runnable {
		public void run() {
			readNumToShar(codeNum, ticket_id);
		}
	}

	private void cleanText(){
		ticket.setText("");
		password.setText("");
		price.setText("");
		tel.setText("");
		email.setText("");
	}
	
	private class MyHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				String defeid = String.format("%05d", Integer.valueOf(eid));
				editText1.setText(defeid);
				editText1.setSelection(defeid.length());
				dialogPro.dismiss();
				Bundle date = msg.getData();
				String message = date.getString("message");
				jieguo.setText(message);
				ticket.setText("票名:" + date.getString("ticket"));
				password.setText("密码:" + date.getString("password"));
				price.setText("价钱:" + date.getString("price"));
				tel.setText("手机:" + date.getString("tel"));
				email.setText("邮箱:" + date.getString("email"));
				checktime.setText("验票时间:" + db.TimeStampDate( date.getString("checktime")) );
				break;
			}
		}
	}
	
	@Override
	public void onClick(View v) {
		cleanText();
		switch (v.getId()) {
		case R.id.button1://二维码
			Intent intent = new Intent(MainEvent.this, CaptureActivity.class);
			
			startActivityForResult(intent, SELECT_PICTURE);
			break;
		case R.id.button2://输码验票
			codeNum = editText1.getText().toString();
			if (codeNum.equals("")) {
				Toast.makeText(MainEvent.this, "请先输入验票号码",
						Toast.LENGTH_SHORT).show();
			} else {
				RunnableFile rf = new RunnableFile();
				Thread r = new Thread(rf);
				r.start();
				dialogPro.show();
			}
			break;
		case R.id.button3://统计
			Intent intent1 = new Intent(MainEvent.this, TicketList.class);
			intent1.putExtra("eid", eid);
			startActivity(intent1);
			break;
		case R.id.button4://数据更新
			dialogPro.show();
			new Thread() {
				public void run() {
					try {
						postData();
						appendTicket();
					} catch (Exception e) {
						Log.e("append", e.getMessage());
					}finally {
						dialogPro.dismiss();
					}
				}
			}.start();
			break;
		case R.id.button5:
			dialogPro.show();
			new Thread() {
				public void run() {
					try {
						postData();
					} catch (Exception e) {
						Log.e("upload", e.getMessage());
					}
				}
			}.start();		
		}
	}
	
	private void readNumToShar(String codeNum, String ticket_id) {
		String error = null, ticket = null, password = null, price = null, tel = null, email = null, ghostText, checktime=null;
		ghostText = ghost.getText().toString();
		try {
			db = new db(this);
			Map<String, String> map = null;
			if(ghostText.equals("1")){
				map = udb.exchangeGhost(codeNum, ticket_id, db);
			}else{
				map = udb.exchangeStar(codeNum, ticket_id, db);
			}
			if (null != map) {
				error = map.get("error");
				ticket = map.get("ticket");
				password = map.get("password");
				price = map.get("price");
				tel = map.get("tel");
				email = map.get("email");
				checktime = map.get("checktime");
			}
		} catch (Exception e) {
			Log.w("tttt",e);
		}
		
		Message msg = new Message();
		Bundle bundle = new Bundle();
		bundle.putString("message", error);
		bundle.putString("ticket", ticket);
		bundle.putString("password", password);
		bundle.putString("price", price);
		bundle.putString("tel", tel);
		bundle.putString("email", email);
		bundle.putString("checktime", checktime);
		msg.what = 0;
		msg.setData(bundle);
		MainEvent.this.myHander.sendMessage(msg);
	}
	
	private class appendHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				dialogPro.dismiss();
				Bundle date = msg.getData();
				String message = date.getString("message");
				Toast.makeText(MainEvent.this, message,
						Toast.LENGTH_SHORT).show();
				break;
			}
		}
	}
	
	//button4
	public void appendTicket(){
		String urlAuth = auth.getText().toString();
		try{
			Response res;
			res = http.post(BASEURL + "mobile/list/?" + urlAuth, true);
	    	JSONObject array = res.asJSONObject();
	    	String eid = array.getString("eid");
	    	if(!eid.equals("0")){
		    	String title = array.getString("title");
		    	JSONArray listString = array.getJSONArray("list");
		    	for (int i = 0 ; i < listString.length();i++) {
					JSONObject arr = listString.optJSONObject(i);
					db.checkTicket(
							eid,
							title,
							arr.getString("ticket_name"),
							arr.getString("t_password"),
							arr.getString("name"),
							arr.getString("email"),
							arr.getString("tel"),
							arr.getString("t_id"),
							arr.getString("t_state"),
							arr.getString("t_price"),
							arr.getString("ticket_id")
					);
		    	}
		    	db.close();
	    	}
			Message msg = new Message();
			Bundle bundle = new Bundle();
			bundle.putString("message", "更新成功");
			msg.what = 0;
			msg.setData(bundle);
			MainEvent.this.aHandler.sendMessage(msg);
		}catch(Exception e){
			Log.e("http", e.getMessage());
		}
	}

	private class postHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:
				dialogPro.dismiss();
				Bundle date = msg.getData();
				String message = date.getString("message");
				Toast.makeText(MainEvent.this, message,
						Toast.LENGTH_SHORT).show();
				break;
			}
		}
	}	
	
	//button5
	public void postData() throws JSONException, ClientProtocolException, IOException{
		String urlAuth= auth.getText().toString();
		String urlCode = code.getText().toString();
		HttpPost httppost = new HttpPost(BASEURL + "mobile/state/?" + urlAuth + urlCode);
		StringBuffer buf = db.getState(urlAuth);
		db.close();
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		params.add(new BasicNameValuePair("data", buf.toString()));
		httppost.setEntity(new UrlEncodedFormEntity(params, HTTP.UTF_8));
		HttpResponse res = new DefaultHttpClient().execute(httppost); 
		int status = res.getStatusLine().getStatusCode();
		if (status == 200) {
			Message msg = new Message();
			Bundle bundle = new Bundle();
			bundle.putString("message", "上传成功");
			msg.what = 0;
			msg.setData(bundle);
			MainEvent.this.pHandler.sendMessage(msg);
		}
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
		if (null != udb) {
			udb = null;
		}
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_OK) {
			if (requestCode == SELECT_PICTURE) {
				Bundle extras = data.getExtras();
				codeNum = extras.getString("code");
				RunnableFile rf = new RunnableFile();
				Thread r = new Thread(rf);
				r.start();
				dialogPro.show();
			}
		}
	}
}
