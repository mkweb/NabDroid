/**
 * at.mkweb.android.nabdroid.Nabaztag
 * 
 * LICENSE:
 *
 * This file is part of NabDroi, an Android app for communicating with your Nabaztag (http://android.mk-web.at/app/nabdroid.html).
 *
 * NabDroid is free software: you can redistribute it and/or modify it under the terms of the GNU General Public
 * License as published by the Free Software Foundation, either version 2 of the License, or (at your option) any
 * later version.
 *
 * NabDroi is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with software.  If not, see
 * <http://www.gnu.org/licenses/>.
 *
 * @author Mario Klug <mario.klug@mk-web.at>
 * @package at.mkweb.android.nabdroid
 * 
 * @license http://www.gnu.org/licenses/gpl.html
 */

package at.mkweb.android.nabdroid;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;


public class Nabaztag {

	private SQLiteDatabase db;
	
	private String config_url;
	private String serial;
	private String token;
	private String voice;

	public static final int ACTION_SAY = 0;
	public static final int ACTION_SLEEP = 1;
	public static final int ACTION_WAKE = 2;
	public static final int ACTION_CHECK = 3;
	
	String url;
	String actionUrl = null;
	
	public Nabaztag(SQLiteDatabase db) {
		
		this.db = db;
		
		readConfig();
	}
	
	public boolean isResultOk(String result) {
		
		if(	result.equals("TTSSENT")
			|| 	result.equals("EARPOSITIONSENT")
			|| 	result.equals("EARPOSITIONSENT")
			|| 	result.equals("POSITIONEAT")
			|| 	result.equals("MESSAGESENT")
			|| 	result.equals("COMMANDSENT")
			) {
			
			return true;
		}
		
		return false;
	}
	
	public String getToken() {
		
		return token;
	}
	
	public String getSerial() {
		
		return serial;
	}
	
	public String getVoice() {
		
		return voice;
	}
	
	public void setToken(String t) {
		
		token = t;
	}
	
	public void setSerial(String s) {
		
		serial = s;
	}
	
	public void setVoice(String v) {
		
		voice = v;
	}
	
	public boolean save() {
		
		String sql = "UPDATE config SET token = '" + token + "', serial = '" + serial + "', voice = '" + voice + "' WHERE id = 1;";
		
		try {
			
			db.execSQL(sql);
			
			readConfig();
			return true;
			
		} catch (Exception e) {
			
			return false;
		}
	}
	
	private void readConfig() {
		
		try {
		
			Cursor c = db.rawQuery("SELECT url, serial, token, voice FROM config WHERE id = 1;", null);
			
			c.moveToFirst();
			
			config_url = c.getString(c.getColumnIndex("url"));
			serial = c.getString(c.getColumnIndex("serial"));
			token = c.getString(c.getColumnIndex("token"));
			voice = c.getString(c.getColumnIndex("voice"));
			
			actionUrl = config_url + "?token=" + token + "&sn=" + serial + "&voice=" + voice + "&";
			
			Log.d("prepared Action-Url", actionUrl);
			
		} catch (Exception e) {
			
			Log.e("DB-Exception", e.getMessage());
		}
	}
	
	public boolean isConfigValid() {
		
		if(token.length() < 1 || serial.length() < 1) {
			
			return false;
		}
		
		return true;
	}
	
	public String getActionUrl(int action) {
		
		url = "";
		
		switch(action) {
		
			case ACTION_SLEEP:
				
				url = actionUrl + "action=13";
				break;
				
			case ACTION_WAKE:
				
				url = actionUrl + "action=14";
				break;
				
			case ACTION_CHECK:
				
				url = actionUrl + "action=7";
				break;
		}
		
		return url;
	}
	
	public String getActionUrl(int action, String text) {
		
		url = "";
		
		switch(action) {
		
			case ACTION_SAY:
				
				url = actionUrl + "tts=" + text.replace(" ", "%20").replace("\n", ",%20") + "&voice=" + voice;
				break;
		}
		
		return url;
	}
}
