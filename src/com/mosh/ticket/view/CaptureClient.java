package com.mosh.ticket.view;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONObject;
import android.util.Log;


public class CaptureClient extends common{
	protected HttpClient http = null;
	public CaptureClient(){
		 http = new HttpClient();
	}
    public HttpClient getHttpClient(){
    	return http;
    }
    
    //联机验票
    public Map<String, String> exchangeStar(String pass, String ticket_id, db db) throws Exception{
    	Map<String, String> hmap = new HashMap<String, String>();
    	if(pass.length() == 11 || pass.length() == 13){
	    	String eid = Integer.valueOf(pass.substring(0, 5)) + "";
	    	String param = db.selectAuth(eid); //!!!!!!!!这里有问题
	    	db.close();
	    	if(param.equals("0")){
	    		hmap.put("error", "无效票");
	    	}else{
		    	Response res = http.get(BASEURL + "mobile/validate/?password=" + pass + param + "&ticket_id=" + ticket_id, true);
		    	JSONObject array = res.asJSONObject();
		    	if(null != array){
		    		if(array.length()>0){
		    			try{
		    				hmap.put("ispost", array.get("ispost").toString());
		    				if(array.getString("ispost").equals("1") || array.getString("ispost").equals("2")){
		    					db.update(Integer.valueOf(array.getString("t_id")), 2, array.getString("use_date"));
		    					db.close();
		    				}
		    				hmap.put("error", array.getString("error"));   				
		    				hmap.put("ticket", array.getString("ticket"));
		    				hmap.put("password", array.getString("t_password"));
		    				hmap.put("price", array.getString("price"));
		    				hmap.put("tel", array.getString("tel"));
		    				hmap.put("email", array.getString("email"));
		    				hmap.put("name", array.getString("name"));
		    				hmap.put("checktime", array.getString("use_date"));
		    			}catch(Exception e){
		    				Log.e("http", e.getMessage());
		    			}
		    		}
		    	}
		    	array =null;
		    	res = null;
	    	}
    	}else{
    		hmap.put("error","无效票");
    	}
    	return hmap;
    }
 
    //单机验票
    public Map<String, String> exchangeGhost(String pass, String ticket_id, db db) throws Exception{
    	Map<String, String> hmap = new HashMap<String, String>();
    	if(pass.length() == 11 || pass.length() == 13){
	    	String eid = Integer.valueOf(pass.substring(0, 5)) + "";
	    	String param = db.selectAuth(eid);
	    	db.close();
	    	if(param.equals("0")){
	    		hmap.put("error", "无效票");
	    	}else{
	        	hmap = db.getTicketByPwd(pass, ticket_id);
	        	db.close();
	    		if(hmap.get("ispost").equals("0")){
	    			hmap.put("error","无效票");
	    		}else{
	    			boolean s = db.checkState(pass);
	    			if(s){
						String ctime = System.currentTimeMillis()/1000-28800 + "";
						db.update(Integer.valueOf(hmap.get("code")), 2, ctime);
						db.close();
						hmap.put("error","验票通过");	    				
	    			}else{
	    				hmap.put("error","票已经被使用"); 
	    			}
	    		}
		    }
    	}else{
    		hmap.put("error","无效票");
    	}
    	return hmap;
    }    
//    private MediaPlayer ring() throws Exception, IOException {  
//        // TODO Auto-generated method stub  
//        Uri alert = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);  
//        MediaPlayer player = new MediaPlayer();  
//        player.setDataSource(this, alert);  
//        final AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);  
//        if (audioManager.getStreamVolume(AudioManager.STREAM_NOTIFICATION) != 0) {  
//            player.setAudioStreamType(AudioManager.STREAM_NOTIFICATION);  
//            player.setLooping(true);  
//            player.prepare();  
//            player.start();  
//        }  
//        return player;  
//    } 
    
}
