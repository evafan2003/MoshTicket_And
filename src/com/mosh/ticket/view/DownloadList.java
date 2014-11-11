package com.mosh.ticket.view;

import org.json.JSONArray;
import org.json.JSONObject;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;

import com.mosh.ticket.CaptureActivity;
import com.mosh.ticket.R;

public class DownloadList extends common implements OnClickListener, OnFocusChangeListener {

	protected HttpClient http = null;
	private ProgressDialog dialogPro;
	private Button b1, howButton;
	private EditText authkeyText, secretText;
	private String authkey, secret;
	String rs;
	String fileName = "";
	boolean online = true;
	private CheckBox rempwd,ghost;
	private db db;

	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main_download); 
        //检查网络
        RunnableSetInternet rSetInt = new RunnableSetInternet();
        Thread r = new Thread(rSetInt);
        r.start();
        
        howButton = (Button) findViewById(R.id.how);
        b1 = (Button) findViewById(R.id.login);
        authkeyText = (EditText) findViewById(R.id.authkey);
        secretText = (EditText) findViewById(R.id.secret);
        ghost = (CheckBox) findViewById(R.id.ghost);
        
        howButton.setOnClickListener(this);
        b1.setOnClickListener(this);
        b1.setOnFocusChangeListener(this);
        b1.setFocusable(true); 
        b1.setFocusableInTouchMode(true);
        b1.requestFocus();
		dialogPro = new ProgressDialog(this);
		dialogPro.setMessage("正在下载票信息，请稍后");
		dialogPro.setIndeterminate(true);
		dialogPro.setCancelable(true);
		db = new db(this);
		http = new HttpClient();
		String auth = db.getAuthByRempwd();
		if(!auth.equals("0")){
			String authkey = auth.substring(9, 17);
			String secret = auth.substring(25, 33);
			authkeyText.setText(authkey);
			secretText.setText(secret);
		}
    }
	
	public void onClick(View v) {
		
		if(v.getId() == R.id.how) {
			
			Intent intent = new Intent(this, help.class);
			startActivity(intent);
			DownloadList.this.finish();
			
		} else {
		
			authkey = authkeyText.getText().toString();
			secret = secretText.getText().toString();
			String auth = "&authkey=" + authkey + "&secret=" + secret;
			
			if (!ghost.isChecked()) {
				String eid = db.getEidByAuth(auth);
				db.close();
				if (!eid.equals("0")) {
					Intent intent = new Intent(DownloadList.this, TicketName.class);
					intent.putExtra("authkey", authkey);
					intent.putExtra("secret", secret);
					intent.putExtra("ghost", "1");
					startActivity(intent);
					DownloadList.this.finish();
				} else {
					Toast.makeText(DownloadList.this, "账号密码错误", Toast.LENGTH_LONG)
							.show();
				}
			} else {
				if (authkey.equals("")) {
					Toast.makeText(DownloadList.this, "账号不能为空", Toast.LENGTH_LONG)
							.show();
				} else if (secret.equals("")) {
					Toast.makeText(DownloadList.this, "密码不能为空", Toast.LENGTH_LONG)
							.show();
				} else {
					if (this.checkOauthkey(authkey, secret)) {
						dialogPro.show();
						new Thread() {
							public void run() {
								try {
									download("mobile/list", authkey, secret);
								} catch (Exception e) {
									e.printStackTrace();
								} finally {
									// 卸载所创建的myDialog对象。
									dialogPro.dismiss();
									Intent intent = new Intent(DownloadList.this,
											TicketName.class);
									intent.putExtra("authkey", authkey);
									intent.putExtra("secret", secret);
									intent.putExtra("ghost", "0");
									startActivity(intent);
									DownloadList.this.finish();
								}
							}
						}.start();
					} else {
						Toast.makeText(DownloadList.this, "亲，账号密码不对哟",
								Toast.LENGTH_LONG).show();
					}
				}
			}
			
		}
		
	}

	public boolean checkOauthkey(String authkey, String secret) {
		try {
			Response res = http.get(BASEURL + "mobile/login/?authkey="
					+ authkey + "&secret=" + secret, true);
			JSONObject array = res.asJSONObject();
			if (array.get("key").toString().equals("n")) {
				return false;
			}
			return true;
		} catch (Exception e) {
			Log.e("http", e.getMessage());
		}
		return false;
	}

	public void download(String urlParam, String authkey, String secret) {
		try {
			Response res;
			res = http.post(BASEURL + urlParam + "/?authkey=" + authkey
					+ "&secret=" + secret, true);
			JSONObject array = res.asJSONObject();
			JSONArray listString = array.getJSONArray("list");
			String eid = array.getString("eid");
			String title = array.getString("title");
			String e = db.selectEid(eid);
			db.close();
			if (e.equals("")) {
				for (int i = 0; i < listString.length(); i++) {
					JSONObject arr = listString.optJSONObject(i);
					db.insert(eid, title, arr.getString("ticket_name"),
							arr.getString("t_password"), arr.getString("name"),
							arr.getString("email"), arr.getString("tel"),
							arr.getString("t_id"), arr.getString("t_state"),
							arr.getString("t_price"),
							arr.getString("ticket_id"));
				}
				db.close();
			}
			String auth = "&authkey=" + authkey + "&secret=" + secret;
			String rem = "1";
//			if (!rempwd.isChecked()) {
//				rem = "0";
//			}
			
			//niemals
			db.insertAuth(eid, auth, rem);
			db.close();
		} catch (Exception e) {
			Log.e("http", e.getMessage());
		}
	}

	public void onFocusChange(View v, boolean hasFocus) {
		// TODO Auto-generated method stub
//		if(v.getId() == R.id.login) {
//			
//			b1.setBackgroundResource(R.drawable.thick_pressed);
//			
//		} else {
//			
//			b1.setBackgroundResource(R.drawable.thick);
//		}
//        if(hasFocus) {
//	     	   //获得焦点
//	            b1.setBackgroundResource(R.drawable.thick_pressed);
//	            
//	        }else{
//	     	   //失去焦点
//	            b1.setBackgroundResource(R.drawable.thick);
//	            
//	    }
		
	}
	
	
	
	

}