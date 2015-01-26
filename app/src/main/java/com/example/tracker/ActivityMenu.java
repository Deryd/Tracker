package com.example.tracker;

import java.io.*;

import android.app.Activity;
import android.app.Fragment.SavedState;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.opengl.Visibility;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

public class ActivityMenu extends Activity {
	
	public static final String PREF_ON_OFF = "PREF_ON_OFF";
	private String SMS_TEXT_PATH = "smsText.txt";
	private String readString;
	
	ToggleButton toggleButtonOnOff;
	RelativeLayout ralLayoutSettings;
	EditText smsText;
	SharedPreferences prefs;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);
		
		toggleButtonOnOff = (ToggleButton) findViewById(R.id.tbOnOffSMS);
		ralLayoutSettings = (RelativeLayout) findViewById(R.id.rlSettings);
		smsText = (EditText) findViewById(R.id.SmsText);
		loadSmsText();
		updateUIFromPreferences();
		isToogleChecked();
	}
	
	public void updateUIFromPreferences()
	{
		Context context = getApplicationContext();
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		boolean onOffCheck = prefs.getBoolean(PREF_ON_OFF, false);
		toggleButtonOnOff.setChecked(onOffCheck);
	}
	
	public void onBuutonClick(View v)
  	{
		Intent pickIntent = new Intent(Intent.ACTION_GET_CONTENT);
		pickIntent.setType(ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
		startActivityForResult(pickIntent, RESULT_OK);
  	}
	
	@Override
	public void onPause()
	{
		super.onPause();
		savePreferences();
		saveSmsText();

	}
	
	private void savePreferences()
	{
		boolean check = toggleButtonOnOff.isChecked();
		
		Editor editor = prefs.edit();
		editor.putBoolean(PREF_ON_OFF, check);
		editor.commit();
	}
	
 	public void onToggleClicked(View view)
	{
		boolean on = ((ToggleButton) view).isChecked();
		
		if(on)
		{
           ralLayoutSettings.setVisibility(View.VISIBLE);
		}
		else 
		{
			ralLayoutSettings.setVisibility(View.INVISIBLE);
		}
	}

 	private void isToogleChecked()
 	{
 		if(toggleButtonOnOff.isChecked())
 			ralLayoutSettings.setVisibility(View.VISIBLE);
 		else 
 			ralLayoutSettings.setVisibility(View.INVISIBLE);
		
 	}
 	
 	private void loadSmsText() {
 		File file = getBaseContext().getFileStreamPath(SMS_TEXT_PATH);
 		
 		if(file.exists())
 		{
	 		try {
	 			InputStream inStream = openFileInput(SMS_TEXT_PATH);
	
		 		if (inStream != null) 
		 		{
			 		InputStreamReader tmp =
			 		new InputStreamReader(inStream);
			 		BufferedReader reader = new BufferedReader(tmp);
			 		String str;
			 		StringBuffer buffer = new StringBuffer();
			
			 		while ((str = reader.readLine()) != null) 
			 		{
				 		buffer.append(str + "\n");
				 	}
				 		inStream.close();
				 		smsText.setText(buffer.toString());
			 		}
			 	}
			 		catch (Throwable t) 
			 		{
				 		Toast.makeText(getApplicationContext(),
				 		"Exception: " + t.toString(), Toast.LENGTH_LONG)
				 		.show();
			 		}
 			}
 	}

 	private void saveSmsText() 
 		{
	 		try 
	 		{
		 		OutputStreamWriter outStream =
		 		new OutputStreamWriter(openFileOutput(SMS_TEXT_PATH, 0));
		
		 		outStream.write(smsText.getText().toString());
		 		outStream.close();
	 		}
	 		catch (Throwable t) 
	 		{
		 		Toast.makeText(getApplicationContext(),
		 		"Exception: " + t.toString(), Toast.LENGTH_LONG)
		 		.show();
	 		}
 		}

}
