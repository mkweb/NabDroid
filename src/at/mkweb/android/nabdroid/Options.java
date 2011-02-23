/**
 * at.mkweb.android.nabdroid.Options
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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import at.mkweb.android.nabcontrol.R;

public class Options extends Activity implements OnClickListener {
	
	Button button_back;
	Button button_save;
	Spinner spinner_lang;
	
	private Nabaztag nab;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		
		super.onCreate(savedInstanceState);
    	this.setContentView(R.layout.options);
    	
    	nab = Registry.getNabaztag();
    	
    	button_back = (Button) findViewById(R.id.button_back);
        button_save = (Button) findViewById(R.id.button_save);
        
        spinner_lang = (Spinner) findViewById(R.id.spinner);
        
    	button_back.setOnClickListener(this);
        button_save.setOnClickListener(this);
        
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.languages, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        spinner_lang.setAdapter(adapter);
        
        spinner_lang.setSelection(adapter.getPosition(nab.getVoice()));
        
        EditText token = (EditText) findViewById(R.id.edit_token);
        EditText serial = (EditText) findViewById(R.id.edit_serial);
        
        token.setText(nab.getToken());
        serial.setText(nab.getSerial());
	}

	// @Override
	public void onClick(View v) {
		
		int id = v.getId();
		
		if(id == button_back.getId()) {
			
			back();
		}
	
		if(id == button_save.getId()) {
			
			EditText field_token = (EditText) findViewById(R.id.edit_token);
			EditText field_serial = (EditText) findViewById(R.id.edit_serial);
			spinner_lang = (Spinner) findViewById(R.id.spinner);
            
			nab.setToken(field_token.getText().toString());
			nab.setSerial(field_serial.getText().toString());
			nab.setVoice(spinner_lang.getSelectedItem().toString());
			
			
			if(nab.save()) {
				
				back();
			} else {
			
				this.showErrorAlert(getString(R.string.message_saved_error_title), getString(R.string.message_saved_error_message));
			}
		}
	}
	
	public void back() {
		
		Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        
        finish();
        
		Log.e("action_detected", "back");
	}
	
	private void showErrorAlert(String title, String message) {

    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	
    	builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
    		
            public void onClick(DialogInterface dialog, int id) {
            	
                dialog.cancel();
            }
        });
    	
    	AlertDialog dialog = builder.create();
    	
    	dialog.setTitle(title);
    	dialog.setMessage(message);
    	
    	dialog.show();
    }
}
