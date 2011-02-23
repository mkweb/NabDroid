/**
 * at.mkweb.android.nabdroid.NabDroid
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

import java.io.InputStream;
import java.net.URL;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import at.mkweb.android.nabcontrol.R;

public class NabDroid extends Activity implements OnClickListener {
    
	EditText text;
	
	Button button_say;
	Button button_sleep;
	Button button_wake;

	
	MenuItem menu_options;
	MenuItem menu_exit;
	
	SQLiteDatabase db;
	Nabaztag nab;
	
	String actionUrl;
	
	NabThread nabthread;
	ProgressDialog process;
	
	boolean disableButtons;
	
	String dialogTitleQueued = null;
	String dialogMessageQueued = null;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
    	
        super.onCreate(savedInstanceState);

    	setContentView(R.layout.main);
    	
    	text = (EditText) findViewById(R.id.text);
        
        button_say = (Button) findViewById(R.id.button_say);
        button_sleep = (Button) findViewById(R.id.button_sleep);
        button_wake = (Button) findViewById(R.id.button_wake);
        
        button_say.setOnClickListener(this);
        button_sleep.setOnClickListener(this);
        button_wake.setOnClickListener(this);
        
        menu_options = (MenuItem) findViewById(R.id.menu_options);
        menu_exit = (MenuItem) findViewById(R.id.menu_exit);
        
        loadDatabase();
        
        Registry.setDb(db);
        nab = Registry.getNabaztag();
        
        nabthread = new NabThread();
        nabthread.start();
        
        disableButtons();
        checkSleeping();
    }
    
    private void checkSleeping() {
    	
    	nabthread.checkSleeping();
    }
    
    private void disableButtons() {
    	
    	Log.e("disable buttons", "");
    	button_say.setEnabled(false);
        button_sleep.setEnabled(false);
        button_wake.setEnabled(false);
        Log.e("disable buttons", "finished");
    }
    
    private void setButtonsSleeping(boolean sleep) {
    	
    	if(sleep == true) {
    		
    		button_say.setEnabled(false);
            button_sleep.setEnabled(false);
            button_wake.setEnabled(true);
    	} else {
    		
    		button_wake.setEnabled(false);
    		button_sleep.setEnabled(true);
    		button_say.setEnabled(true);
    	}
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu){

    	MenuInflater inflater = getMenuInflater();
    	inflater.inflate(R.menu.menu, menu);
        
    	return true;
    }
    
    public boolean onOptionsItemSelected (MenuItem item){

    	if(item.getItemId() ==  R.id.menu_options) {

    		Intent myIntent = new Intent(this, Options.class);
            startActivityForResult(myIntent, 0);
    		
    		return true;
    	}

    	if(item.getItemId() == R.id.menu_exit) {

    		showConfirmAlert(getString(R.string.message_exit_title), getString(R.string.message_exit_message));
    		return true;
    	}

    	return false;
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	
    	checkSleeping();
    }
    
    private void loadDatabase() {
    	
    	db = openOrCreateDatabase("config", MODE_PRIVATE, null);
    	
        db.execSQL("CREATE TABLE IF NOT EXISTS config (id INT PRIMARY KEY, url VARCHAR(255), serial VARCHAR(50), token VARCHAR(50), voice);");
        db.execSQL("CREATE TABLE IF NOT EXISTS language (lang VARCHAR (255));");
        
        Cursor c = db.rawQuery("SELECT * FROM config;", null);
        
        if(c.getCount() < 1) {
        	
        	db.execSQL("INSERT INTO config VALUES (1, 'http://api.nabaztag.com/vl/FR/api.jsp', '', '', 'UK-Mistermuggles');");
        	
        	Log.i("database", "create initial config");
        }
    }
    
    //@Override
	public void onClick(View v) {
		
    	int id = v.getId();
    	
		actionUrl = null;
		
    	if(id == button_say.getId()) {
			
    		String say_text = text.getText().toString();
    		
			actionUrl = nab.getActionUrl(Nabaztag.ACTION_SAY, say_text);
			Log.e("action_detected", "speak: " + say_text);
		}
    	
		if(id == button_sleep.getId()) {
			
			actionUrl = nab.getActionUrl(Nabaztag.ACTION_SLEEP);
			Log.e("action_detected", "sleep");
		}
		
		if(id == button_wake.getId()) {
			
			actionUrl = nab.getActionUrl(Nabaztag.ACTION_WAKE);
			Log.e("action_detected", "wake");
		}

		if(actionUrl != null) {
		
			Log.d("URL-Connection", "Requesting connection to " + actionUrl);
			
			showProcess();

			try {
				
				nabthread.setUrl(actionUrl);
			} catch (Exception e) {
				Log.e("Exception", "Nabthread.setUrl()");
			}
			
			actionUrl = null;
		}
	}
    
    public void showConfirmAlert(String title, String message) {
    	
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	
    	builder.setPositiveButton(getString(R.string.confirm_ok), new DialogInterface.OnClickListener() {
    		
            public void onClick(DialogInterface dialog, int id) {
            	
                dialog.cancel();
                finish();
            }
        });
    	
    	builder.setNegativeButton(getString(R.string.confirm_cancel), new DialogInterface.OnClickListener() {
    		
            public void onClick(DialogInterface dialog, int id) {
            	
                dialog.cancel();
            }
        });
    	
    	AlertDialog dialog = builder.create();
    	
    	dialog.setTitle(title);
    	dialog.setMessage(message);
    	
    	dialog.show();
    }
    
    public void showErrorAlert(String title, String message) {
    	button_say.setVisibility(2);
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	
    	builder.setPositiveButton(getString(R.string.alert_ok), null);
    	
    	AlertDialog dialog = builder.create();
    	
    	dialog.setTitle(title);
    	dialog.setMessage(message);
    	
    	dialog.show();
    }
    
    public void showQueuedAlert() {
    	
    	showErrorAlert(dialogTitleQueued, dialogMessageQueued);
    }
    
    public String getConfigString(int id) {
    	
    	return getString(id);
    }
    
    public void showProcess() {
    	
    	showProcess((String) getText(R.string.loading));
    }
    
    public void showProcess(String string) {
    	
		process = new ProgressDialog(this);
		process.setTitle(string);
		process.setMessage(getText(R.string.process_send));
		process.show();
    }
    
    public void hideProcess() {
    	
    	if(process != null) {
    		
    		process.hide();
    	}
    }
    
    private Handler messageHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {  

			switch (msg.what) {
		    	  	
				case 0:
	    			showQueuedAlert();
			    	break;
			    	
				case 1:
	    			hideProcess();
			    	break;
			    	
				case 2:
	    			showProcess();
			    	break;
			    	
				case 3:
	    			setButtonsSleeping(true);
			    	break;
			    	
				case 4:
	    			setButtonsSleeping(false);
			    	break;
			    	
				case 5:
	    			disableButtons();
			    	break;
			}
		}
	};
    
    public class NabThread extends Thread {

    	private String actionUrl = null;
    	boolean checkSleeping = false;;
    	
    	public NabThread() {
    		
    		super();
    	}
    	
    	public void checkSleeping() {
    		
    		checkSleeping = true;
    	}
    	
    	public void setUrl(String url) {
    		
    		actionUrl = url;
    	}
    	
    	@Override
    	public void run() {

    		while(true) {
    			
    			if(disableButtons) {
    				
    				messageHandler.sendEmptyMessage(5);
    				disableButtons = false;
    			}
    			
    			if(checkSleeping) {

    				try {
    					
    					Thread.sleep(1000);
    				} catch (Exception e) {
    					// Nothing to do
    				}
    				
    				try {
	    				
    					String actionUrl = nab.getActionUrl(Nabaztag.ACTION_CHECK);
    					
	    				URL url = new URL(actionUrl);
	    				InputStream is = url.openStream();
	    				
	    				Document doc = null;
	    				
	    				try {
	    					
	    					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    					DocumentBuilder db = dbf.newDocumentBuilder();
	    					doc = db.parse(is);
	    					
	    					Log.d("XMLParser", "Parsed successfullly");
	    				} catch (Exception e) {
	    					
	    					Log.e("XMLParser-Exception", e.getMessage());
	    				}
	    				
	    				String short_result = null;
	    				
	    				try {
	    					
	    					short_result = doc.getElementsByTagName("rabbitSleep").item(0).getChildNodes().item(0).getNodeValue().trim();
	    					
	    					if(short_result.equals("NO")) {
	    						
	    						Log.d("sleeping", "no");
	    						messageHandler.sendEmptyMessage(4);
	    					}
	    					
	    					if(short_result.equals("YES")) {
	    						
	    						Log.d("sleeping", "yes");
	    						messageHandler.sendEmptyMessage(3);
	    					}
	    					
	    				} catch (NullPointerException npe) {
	    					
	    					short_result = doc.getElementsByTagName("message").item(0).getChildNodes().item(0).getNodeValue();
	    					
	    					disableButtons();
	    					
	    					sendErrorDialog(short_result);
	    				}
	    				
	    				Log.e("check", short_result);
	    				
    				} catch (Exception e) {
	    				
	    				Log.e("Nab-Exception", "");
	    			}

    				checkSleeping = false;
    			}
    			
	    		if(actionUrl != null) {
	    			
	    			try {
	    				
	    				URL url = new URL(actionUrl);
	    				InputStream is = url.openStream();
	    				
	    				Document doc = null;
	    				
	    				try {
	    					
	    					DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
	    					DocumentBuilder db = dbf.newDocumentBuilder();
	    					doc = db.parse(is);
	    					
	    					Log.d("XMLParser", "Parsed successfullly");
	    				} catch (Exception e) {
	    					
	    					Log.e("XMLParser-Exception", e.getMessage());
	    				}
	    				
	    				
	    				String short_result = doc.getElementsByTagName("message").item(0).getChildNodes().item(0).getNodeValue();
	    				
	    				Log.d("Short result", short_result);
	    				
	    				if(nab.isResultOk(short_result)) {
	    					
	    					// everything OK
	    					dialogTitleQueued = getString(R.string.message_sent_title); 
	    					dialogMessageQueued = getString(R.string.message_sent_message);
	    					
	    					messageHandler.sendEmptyMessage(0);
	    					
	    					checkSleeping = true;
	    					actionUrl = null;
	    					
	    				} else {
	    					
	    					sendErrorDialog(short_result);
	    				}
	    			} catch (Exception e) {
	    				
	    				Log.e("Nab-Exception", e.getMessage());
	    			}
	    			
	    			actionUrl = null;
	    			
		    		// Hiding Loader
		    		messageHandler.sendEmptyMessage(1);
	    		}
	    		
	    		try {
	    			
	    			Thread.sleep(100);
	    		} catch (Exception e) {
	    			
	    			Log.e("Thread-Exception", e.getMessage());
	    		}
	    	}
	    }
    	
    	public void sendErrorDialog(String short_result) {
    		
    		String errorMessage = null;
    		
    		if(short_result.equals("NOGOODSERIAL")) {
				
				errorMessage = (String) getString(R.string.response_NOGOODSERIAL);
				disableButtons = true;
			}
			if(short_result.equals("NOGOODTOKENORSERIAL")) {
				
				errorMessage = (String) getString(R.string.response_NOGOODTOKENORSERIAL);
				disableButtons = true;
			}
			if(short_result.equals("NOTAVAILABLE")) {
				
				errorMessage = (String) getString(R.string.response_NOTAVAILABLE);
				disableButtons = true;
			}
			if(short_result.equals("TTSNOTSENT")) {
				
				errorMessage = (String) getString(R.string.response_TTSNOTSENT);
			}
			
			if(errorMessage == null) {
				
				errorMessage = (String) getString(R.string.response_UNKNOWN);
				disableButtons = true;
			}
			
			dialogTitleQueued = getString(R.string.message_sent_error_title); 
			dialogMessageQueued = errorMessage;
			
			messageHandler.sendEmptyMessage(0);
    	}
    }
}