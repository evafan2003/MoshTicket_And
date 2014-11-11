package com.mosh.ticket.view;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.Editable;
import android.util.Log;

public class db extends SQLiteOpenHelper {

    private final static String DATABASE_NAME = "e_mail.db";
    private final static String TABLE_NAME = "e_mail";
    private final static String TABLE_NAME_AUTHKEY = "authkey";
    private final static int DATABASE_VERSION = 1;
    private final static String
    		FIELD_ID = "id", 
    		FIELD_EID = "eid", 
    		FIELD_TITLE = "title",
    		FIELD_TICKET = "ticket",
    		FIELD_PWD = "pwd",
    		FIELD_NAME = "name",
    		FIELD_EMAIL = "email", 
    		FIELD_TEL = "tel", 
    		FIELD_CODE = "code", 
    		FIELD_PRICE = "price",
    		FIELD_STATE = "state",
    		FIELD_TIME = "ptime",
    		FIELD_TICKETID = "ticket_id";

    public db(Context context)
    {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    public void onCreate(SQLiteDatabase db) {
        // TODO Auto-generated method stub
    	db.execSQL("Create table if not exists " + TABLE_NAME + "("
        		+ FIELD_ID + " integer primary key autoincrement,"
        		+ FIELD_EID + " integer,"
        		+ FIELD_TITLE + " text,"
        		+ FIELD_TICKET + " text,"
        		+ FIELD_PWD + " text,"
        		+ FIELD_NAME + " text,"
        		+ FIELD_EMAIL +" text,"
        		+ FIELD_TEL + " integer,"
        		+ FIELD_CODE + " integer unique,"
        		+ FIELD_PRICE + " text,"
        		+ FIELD_STATE + " integer default 1,"
        		+ FIELD_TIME + " integer default 0,"
        		+ FIELD_TICKETID + " ticket_id)");
    	db.execSQL("Create unique index eid_pwd on " + TABLE_NAME + "(eid,pwd)");
        db.execSQL("Create table if not exists " + TABLE_NAME_AUTHKEY + "(" +
        		"eid integer unique, authkey text, rempwd integer default 0)");
    }
    
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // TODO Auto-generated method stub
    	db.execSQL(" DROP TABLE IF EXISTS " + TABLE_NAME);
    	db.execSQL(" DROP TABLE IF EXISTS " + TABLE_NAME_AUTHKEY);
        onCreate(db);
    }

    public String selectEid(String eid)
    {
    	String rs = "";
    	try{
	    	SQLiteDatabase db = this.getReadableDatabase();
	        Cursor cursor = db.query(TABLE_NAME, new String[]{"id"}, "eid=?", new String[]{eid}, null, null, null, "1");
	        if( cursor.moveToNext() ) {
	        	rs = cursor.getString(cursor.getColumnIndex("id"));
	        }
        	cursor.close();
        } catch (Exception e) {
        	Log.i( "selectEid", e.getMessage() );
        }
        return rs;
    }
    
    public List<Map<String, Object>> selectList(String eid, String name) {
    	SQLiteDatabase db = this.getReadableDatabase();
    	ArrayList<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
    	Cursor cursor = null;
    	if(name.equals("")) {
    		 cursor = db.query(TABLE_NAME, new String[]{"*"}, "eid=?" , new String[]{eid}, null, null, null, null);
    	} else if(isTel(name)) {
    		cursor = db.query(TABLE_NAME, new String[]{"*"}, "eid=? and tel=?" , new String[]{eid, name}, null, null, null, null);
    	} else {
    		cursor = db.query(TABLE_NAME, new String[]{"*"}, "eid=? and name like ?" , new String[]{eid, "%" + name + "%"}, null, null, null, null);
    	}
    	while(cursor.moveToNext()){
    		for(int i = 0; i < cursor.getCount(); i++){
    			Map<String, Object> map = new HashMap<String, Object>();
    			cursor.moveToPosition(i);
    			String state = "";
    			if(cursor.getString(10).equals("1")){
    				state = "未使用";
    			}else{
    				state = "已使用";
    			}
    			map.put("id", i+1);
    			map.put("eid", cursor.getString(1));
    			map.put("title", cursor.getString(2));
    			map.put("ticket", cursor.getString(3));
    			map.put("pwd", cursor.getString(4));
    			map.put("name", cursor.getString(5));
    			map.put("email", cursor.getString(6));
    			map.put("tel", cursor.getString(7));
    			map.put("code", cursor.getString(8));
    			map.put("price", cursor.getString(9));
    			map.put("state", state);
    			map.put("ptime", TimeStampDate(cursor.getString(11)));    			
    			list.add(map);
    		} 
    	}
    	
    	cursor.close();
    	return list;
    }
    
    //单机验票 param:pwd(票密码)，return 票相关数据
    public Map<String, String> getTicketByPwd(String pwd, String ticketid){
    	SQLiteDatabase db = this.getReadableDatabase();
    	Cursor cursor = db.rawQuery("SELECT ticket, pwd, price, tel, email, code FROM e_mail WHERE ticket_id IN (" + ticketid + ") AND pwd = ?", new String[]{pwd});
    	//Cursor cursor = db.query(TABLE_NAME, new String[]{"ticket", "pwd", "price", "tel", "email", "code"}, "pwd=? AND ticket_id IN ?", new String[]{pwd, "(" + ticketid + ")"}, null, null, null, "1");
    	Map<String, String> map = new HashMap<String, String>();
    	if(cursor.moveToNext()){
    		map.put("ispost", "1");
			map.put("ticket", cursor.getString(0));
			map.put("password", cursor.getString(1));
			map.put("price", cursor.getString(2));
			map.put("tel", cursor.getString(3));
			map.put("email", cursor.getString(4));
			map.put("code", cursor.getString(5));
		}else{
			map.put("ispost", "0");
		}
    	System.out.println(map);
    	return map;
    	
    }
    
    //插入票信息
    public long insert(String eid, String title, String ticket, String pwd, String name, String email, String tel, String code, String state, String price, String ticket_id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(FIELD_EID, eid);
        cv.put(FIELD_TITLE, title);
        cv.put(FIELD_TICKET, ticket);
        cv.put(FIELD_PWD, pwd);
        cv.put(FIELD_NAME, name);
        cv.put(FIELD_EMAIL, email);
        cv.put(FIELD_TEL, tel);
        cv.put(FIELD_CODE, code);
        cv.put(FIELD_STATE, state);
        cv.put(FIELD_PRICE, price);
        cv.put(FIELD_TICKETID, ticket_id);
        long row = db.insert(TABLE_NAME, null, cv);
        return row;
    }
    
    public void delete(int id)
    {
        SQLiteDatabase db = this.getWritableDatabase();
        String where = FIELD_ID + "=?";
        String[] whereValue={Integer.toString(id)};
        db.delete(TABLE_NAME, where, whereValue);
    }
    
    public long update(int id, int state, String ptime)
    {
    	long row = 0;
    	try{
	        SQLiteDatabase db = this.getWritableDatabase();
	        String where = FIELD_CODE + "=?";
	        String[] whereValue = {Integer.toString(id)};
	        ContentValues cv = new ContentValues(); 
	        cv.put(FIELD_STATE, state);
	        cv.put(FIELD_TIME, ptime);
	        row = db.update(TABLE_NAME, cv, where, whereValue);
    	}catch(Exception e){
    		Log.w("db", row+"");
    	}
    	return row;
        
    }
    //把所有rempwd更新为0
    public void updateAuth(){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues(); 
        cv.put("rempwd", 0);
        db.update(TABLE_NAME_AUTHKEY, cv, null, null);
    }
    
    //登录信息存入auth表
    public long insertAuth(String eid, String authkey, String rempwd){
    	updateAuth();
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("eid", eid);
        cv.put("authkey", authkey);
        cv.put("rempwd", rempwd);
        long row = db.replace(TABLE_NAME_AUTHKEY, null, cv);
        return row;    	
    }
    public String getAuthByRempwd(){
    	SQLiteDatabase db = this.getReadableDatabase();
    	Cursor cursor = db.query(TABLE_NAME_AUTHKEY, new String[]{"authkey"}, "rempwd=?", new String[]{"1"}, null, null, null, "1");
        String rs = "";
    	if( cursor.moveToNext() ) {
        	rs = cursor.getString(0);
        }else{
        	rs = "0";
        }
    	cursor.close();
		return rs;   	
    }
    public String selectAuth(String eid){
    	SQLiteDatabase db = this.getReadableDatabase();
    	Cursor cursor = db.query(TABLE_NAME_AUTHKEY, new String[]{"authkey"}, "eid=?", new String[]{eid}, null, null, null, "1");
        String rs = "";
    	if( cursor.moveToNext() ) {
        	rs = cursor.getString(0);
        }else{
        	rs = "0";
        }
    	cursor.close();
		return rs;
    }
    
    public String selectCode(String authkey){
    	SQLiteDatabase db = this.getReadableDatabase();
        String eid = "", code = "";
		eid = getEidByAuth(authkey);
    	Cursor cur = db.query(TABLE_NAME, new String[]{"code"}, "eid=?", new String[]{eid}, null, null, "code desc", "1");
       	if( cur.moveToNext() ) {
        	code = cur.getString(0);
        }else{
        	return "0";
        }
       	cur.close();
    	return code;
    }
    
    public String getEidByAuth(String authkey){
    	SQLiteDatabase db = this.getReadableDatabase();
    	Cursor cursor = db.query(TABLE_NAME_AUTHKEY, new String[]{"eid"}, "authkey=?", new String[]{authkey}, null, null, null, "1");
        String eid = "";
    	if( cursor.moveToNext() ) {
    		eid = cursor.getString(0);
        }else{
        	eid = "0";
        }
    	cursor.close();
    	return eid;
    }
    
    
    //使用过的票的计数
    public String countUsed(String eid, String name) {
    	
    	SQLiteDatabase db = this.getReadableDatabase();
    	
    	Cursor cursor = db.query(TABLE_NAME, new String[]{"code, ptime"}, "eid=? and state=?", new String[]{eid, "2"}, null, null, null);
	    int count = cursor.getCount();

    	cursor.close();
    	
		return String.valueOf(count);
		
	}
    
    //上传数据
    public StringBuffer getState(String authkey) {
    	SQLiteDatabase db = this.getReadableDatabase();
    	String eid = getEidByAuth(authkey);
    	Cursor cursor = db.query(TABLE_NAME, new String[]{"code, ptime"}, "eid=? and state=?", new String[]{eid, "2"}, null, null, null);
		StringBuffer buf = new StringBuffer();
		buf.append("[");
    	while( cursor.moveToNext() ){
    		if( cursor.moveToFirst() ){
    			buf.append("{\"code\": " + cursor.getString(0) + ",\"use_time\":" + cursor.getString(1) + "}");
    		}
    		int count = cursor.getCount();
    		if(count > 1){
	    		for(int i = 1; i < count; i++){
	    			cursor.moveToPosition(i);
	    			buf.append(",{\"code\": " + cursor.getString(0) + ",\"use_time\":" + cursor.getString(1) + "}");
	    		}
    		}
    	}
    	buf.append("]");
    	cursor.close();
		return buf;
    }
    
    //查看票是否被使用
    public boolean checkState(String pwd){
    	SQLiteDatabase db = this.getReadableDatabase();
    	Cursor cursor = db.query(TABLE_NAME, new String[]{"state"}, "pwd=?", new String[]{pwd}, null, null, null, "1");
    	boolean state = false;
    	if( cursor.moveToNext() ) {
    		String eid = cursor.getString(0);
    		if(eid.equals("2")){
    			state = false;
    		}else{
    			state = true;
    		}
        }
    	cursor.close();
    	return state;
    }
    
    //列出当前活动票种
    public List<Map<String, Object>> getAllTickname(String authkey){
    	SQLiteDatabase db = this.getReadableDatabase();
    	String eid = getEidByAuth(authkey);
    	//Cursor cursor = db.rawQuery( "SELECT DISTINCT " + FIELD_TICKET + ++ " FROM " + TABLE_NAME + " WHERE eid = ?", new String[]{eid} );
    	Cursor cursor = db.query(true, TABLE_NAME, new String[]{"ticket, ticket_id"}, "eid=?", new String[]{eid}, null, null, null, null);
    	List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
    	while( cursor.moveToNext() ){
    		for(int i = 0; i < cursor.getCount(); i++){
    			Map<String, Object> map = new HashMap<String, Object>();
    			cursor.moveToPosition(i);
    			map.put( "ticketname", cursor.getString(0) );
    			map.put( "ticket_id", cursor.getString(1) );
    			map.put( "cb", false );
    			list.add(map);
    		}
    	}
    	
    	cursor.close();
		return list;
    	
    }
    
    //更新票信息
    public boolean checkTicket(String eid, String title, String ticket, String pwd, String name, String email, String tel, String code, String state, String price, String ticket_id){
    	
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();    	
        cv.put(FIELD_EID, eid);
        cv.put(FIELD_TITLE, title);
        cv.put(FIELD_TICKET, ticket);
        cv.put(FIELD_PWD, pwd);
        cv.put(FIELD_NAME, name);
        cv.put(FIELD_EMAIL, email);
        cv.put(FIELD_TEL, tel);
        cv.put(FIELD_CODE, code);
        cv.put(FIELD_STATE, state);
        cv.put(FIELD_PRICE, price);
        cv.put(FIELD_TICKETID, ticket_id);
        long row = db.replace(TABLE_NAME, null, cv);    	
		return false;
    	
    }
    
    //格式化时间戳
	public String TimeStampDate(String timestampString) {
		String date ="";
		try{
		Long timestamp = (Long.parseLong(timestampString)+8*60*60) * 1000;
		 date = new java.text.SimpleDateFormat("MM-dd HH:mm")
				.format(new java.util.Date(timestamp));
		}catch(Exception e){
			
		}
		return date;
	}
	
	//手机格式验证
	public static boolean isTel(String name) {
		String strPattern = "^1[0-9]{10}$";

		Pattern p = Pattern.compile(strPattern);
		Matcher m = p.matcher(name);
		return m.matches();
	}

}