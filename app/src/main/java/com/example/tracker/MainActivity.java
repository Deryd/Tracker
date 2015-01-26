package com.example.tracker;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

import android.R.integer;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Camera.Size;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.telephony.SmsMessage;
import android.telephony.gsm.SmsManager;
import android.text.AlteredCharSequence;
import android.text.method.DialerKeyListener;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;


public class MainActivity extends Activity implements SurfaceHolder.Callback
{
  Camera camera;
  MediaRecorder mediaRecorder;
  SurfaceHolder holder;
  SurfaceView surfaceView;
  ToggleButton startStopButton;
  RelativeLayout mainLayout;
  File videoFile;
  VideoRecorder recorder;
  CheckBox checkBox;
  SharedPreferences prefs;
  SmsManager smsManager;
  TextView textView;
  AlertDialog.Builder alertDialogB;
  boolean onOffCheck;
  
  SensorManager sensorManager;
  Sensor sensorAccel;
  Sensor sensorMagnet;
  Timer timer;
  CountDownTimer countDownTimer;
  TimerTask task;
  boolean weCrashed = false;
  
  private static final int IDM_SETTINGS = 101;
  private static final int IDM_VIDEO = 102;
  private static final int IDM_ABOUT = 103;
  
  private int rotation;
  StringBuilder sb = new StringBuilder();
  
  public static final String PREF_ON_OFF = "PREF_ON_OFF";
  private String SMS_TEXT_PATH = "smsText.txt";
  private String sms_TextString = null;
  private String PositioString;

  float[] r = new float[9];
  float[] inR = new float[9];
  float[] outR = new float[9];
  float[] valuesAccel = new float[3];
  float[] valuesMagnet = new float[3];
  //float[] valuesResult = new float[3];
  float[] valuesResult2 = new float[3];
  
@SuppressLint("InlinedApi")
	@Override
	  public void onCreate(Bundle savedInstanceState)
	  {
		  super.onCreate(savedInstanceState);
		  
		  setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT);
		  getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
		  requestWindowFeature(Window.FEATURE_NO_TITLE);
		  
		  setContentView(R.layout.activity_main);

		  surfaceView = (SurfaceView) findViewById(R.id.surfaceView);
		  holder = surfaceView.getHolder();
		  holder.addCallback(this);
		  holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		 
		    sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		    sensorAccel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		    sensorMagnet = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		    
		  mainLayout = (RelativeLayout) findViewById(R.id.menu);
		  checkBox = (CheckBox) findViewById(R.id.checkBox1);
		  //mainLayout.setOnTouchListener(this);
		  setContentView(mainLayout);
		  
		  recorder = new VideoRecorder();
		  updateUIFromPreferences();
		  
		  LocationManager locMgr = (LocationManager) 
				  getSystemService(Context.LOCATION_SERVICE);
		  LocationListener locationListener = new LocationListener() {
			
			@Override
			public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProviderEnabled(String arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onProviderDisabled(String arg0) {
				// TODO Auto-generated method stub
				
			}
			
			@Override
			public void onLocationChanged(Location arg0) {
				updateWithNewLocation(arg0);
			}
		};

		  locMgr.requestLocationUpdates(LocationManager.GPS_PROVIDER, 10000, 100, locationListener);
		  textView = (TextView)findViewById(R.id.textView1);
		  
	  }

	private void updateWithNewLocation(Location location)
	{
		
		TextView myLocTextView;
		myLocTextView = (TextView)findViewById(R.id.textView1);
		
		if(location != null)
		{
			double lat = location.getLatitude();
			double lng = location.getLongitude();
			PositioString = "Lat:" + lat +"\nLong:" + lng;//������ � ������� ������� ����� �����!!!
		}
		else 
		{
			PositioString = "No location found";
		}

		myLocTextView.setText("Your Current Position is:\n" + PositioString);
	}
	
	public void onToggleClicked(View view)
	{
		boolean on = ((ToggleButton) view).isChecked();
		
		if(on)
		{
            camera.stopPreview();

            camera.unlock();

            recorder.setCamera(camera);
            
            recorder.setRecorderParams();
            recorder.setPreview(holder.getSurface());
            //recorder.start(String.format("/sdcard/RoadRec/%d.mp4", System.currentTimeMillis()));
            recorder.start("/sdcard/RoadRec/File_1.mp4");
		}
		else 
		{
			recorder.stop();
	            try
	            {
	                camera.reconnect();
	            }
	            catch (Exception e)
	            {
	                e.printStackTrace();
	            }
            camera.startPreview();
		}
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		camera = Camera.open();
		recorder.open();
		updateUIFromPreferences();

		    sensorManager.registerListener(listener, sensorAccel, SensorManager.SENSOR_DELAY_NORMAL);
		    sensorManager.registerListener(listener, sensorMagnet, SensorManager.SENSOR_DELAY_NORMAL);
		    
		    timer = new Timer();
		    task = new TimerTask() {
		      @Override
		      public void run() {
		        runOnUiThread(new Runnable() {
		          @Override
		          public void run() {
		            //getDeviceOrientation();
		        	if(onOffCheck)
		        	{
			            getActualDeviceOrientation();
			            if(!weCrashed)
			            {
			            	checkCrash();
			            }
		            }  
		          }
		        });
		      }
		    };
		    timer.schedule(task, 0, 400);
		 
		    WindowManager windowManager = ((WindowManager) getSystemService(Context.WINDOW_SERVICE));
		    Display display = windowManager.getDefaultDisplay();
		    rotation = display.getRotation();
	
	    
	}

	@Override
  	protected void onPause()
	{
		super.onPause();
		recorder.close();
		if(camera != null)
		{
			camera.setPreviewCallback(null);
			camera.stopPreview();
			camera.release();
			camera = null;
		}
		
		sensorManager.unregisterListener(listener);
	    timer.cancel();
	}

	@Override
    protected void onDestroy()
    {
        super.onDestroy();
    }
	
  	public void surfaceCreated(SurfaceHolder holder)
  {
      try
      {
          camera.setPreviewDisplay(holder);
      }
      catch (IOException e)
      {
          e.printStackTrace();
      }

      Size previewSize = camera.getParameters().getPreviewSize();
      float aspect = (float) previewSize.width / previewSize.height;

      int previewSurfaceWidth = surfaceView.getWidth();
      int previewSurfaceHeight = surfaceView.getHeight();

      LayoutParams lp = surfaceView.getLayoutParams();

      if (this.getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE)
      {
          camera.setDisplayOrientation(90);
          lp.height = previewSurfaceHeight;
          lp.width = (int) (previewSurfaceHeight / aspect);
      }
      else
      {
          camera.setDisplayOrientation(0);
          lp.width = previewSurfaceWidth;
          lp.height = (int) (previewSurfaceWidth / aspect);
      }

      surfaceView.setLayoutParams(lp);
      camera.startPreview();
  }
  
  	public void surfaceDestroyed(SurfaceHolder holder){}
  
  	public void surfaceChanged( SurfaceHolder holder, int format, int width, int heigth) {}

  	@Override 	
  	public boolean onCreateOptionsMenu(Menu menu)
  	{
  		menu.add(0,IDM_SETTINGS,0,"Settings");
  		menu.add(0,IDM_VIDEO,0,"Video");
  		menu.add(0,IDM_ABOUT,0,"About");
  		
  		return super.onCreateOptionsMenu(menu);
  	}
 
  	@Override
  	public boolean onOptionsItemSelected(MenuItem item)
  	{
  		switch (item.getItemId()) {
		case IDM_SETTINGS:
	  		Intent intent = new Intent(this, ActivityMenu.class);
	  		startActivity(intent);
	  		return super.onOptionsItemSelected(item);
		/*case IDM_VIDEO:
			Intent intent2 = new Intent(Intent.ACTION_PICK);
			intent2.setType("sdcard/");
			startActivityForResult(intent2, 1);
			return true;
		case IDM_ABOUT:
			
			break;*/
		}
		return false;
  	}
  	
  	public void updateUIFromPreferences()
	{
  		Context context = getApplicationContext();
		prefs = PreferenceManager.getDefaultSharedPreferences(context);
		onOffCheck = prefs.getBoolean(PREF_ON_OFF, false);
		checkBox.setChecked(onOffCheck);
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
				 		buffer.append(str + "\n");
			 		
				 		inStream.close();
				 		sms_TextString = buffer.toString();
				 		sms_TextString += PositioString;	
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
  	
  	private void SendSms(String sendTo) 
	{
		loadSmsText();
		alertDialogB = new AlertDialog.Builder(MainActivity.this);
		alertDialogB.setTitle("SEND MESSAGE...")
		.setMessage("Cancel sending message")
		.setNegativeButton( "Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id)
			{
				dialog.cancel();
				weCrashed = false;
				countDownTimer.cancel();
			}
		});
		final AlertDialog alert = alertDialogB .create();
		alert.show();
		
		countDownTimer = new CountDownTimer(5000, 1000) {
	        @Override
	        public void onTick(long millisUntilFinished) {}

	        @Override
	        public void onFinish() 
	        { 
	        	alert.dismiss();
	        	timer.cancel();
	    		//smsManager = SmsManager.getDefault();
	    		//smsManager.sendTextMessage(sendTo, null, sms_TextString, null, null);
	        	textView.setText(sms_TextString);
	        }
	    }.start();

	}
  	
  	private void checkCrash()
  	{
  		if(valuesResult2[1] > 0)
  		{
  			weCrashed = true;
			//textView.setText("Crash");
			SendSms("0665217475");
  		}
  	}

    void getActualDeviceOrientation() {
        SensorManager.getRotationMatrix(inR, null, valuesAccel, valuesMagnet);
        int x_axis = SensorManager.AXIS_X;
        int y_axis = SensorManager.AXIS_Y;
        
        switch (rotation) {
        	case (Surface.ROTATION_0): 
        		break;
        	case (Surface.ROTATION_90):
        		x_axis = SensorManager.AXIS_Y;
        		y_axis = SensorManager.AXIS_MINUS_X;
        		break;
        	case (Surface.ROTATION_180):
        		y_axis = SensorManager.AXIS_MINUS_Y;
        		break;
        	case (Surface.ROTATION_270):
        		x_axis = SensorManager.AXIS_MINUS_Y;
        		y_axis = SensorManager.AXIS_X;
        		break;
        default: 
        	break;
        }
        SensorManager.remapCoordinateSystem(inR, x_axis, y_axis, outR);
        SensorManager.getOrientation(outR, valuesResult2);
        valuesResult2[0] = (float) Math.toDegrees(valuesResult2[0]); 
        valuesResult2[1] = (float) Math.toDegrees(valuesResult2[1]); 
        valuesResult2[2] = (float) Math.toDegrees(valuesResult2[2]); 
        return;
      }  
    
    SensorEventListener listener = new SensorEventListener() {

  	    @Override
  	    public void onAccuracyChanged(Sensor sensor, int accuracy) {
  	    }

  	    @Override
  	    public void onSensorChanged(SensorEvent event) {
  	      switch (event.sensor.getType()) {
  	      case Sensor.TYPE_ACCELEROMETER:
  	        for (int i=0; i < 3; i++){
  	          valuesAccel[i] = event.values[i];
  	        }        
  	        break;
  	      case Sensor.TYPE_MAGNETIC_FIELD:
  	        for (int i=0; i < 3; i++){
  	          valuesMagnet[i] = event.values[i];
  	        }  
  	        break;
  	      }
  	    }
  	  };
 
  	  /*	void getDeviceOrientation() {
        SensorManager.getRotationMatrix(r, null, valuesAccel, valuesMagnet);
        SensorManager.getOrientation(r, valuesResult);

        valuesResult[0] = (float) Math.toDegrees(valuesResult[0]); 
         valuesResult[1] = (float) Math.toDegrees(valuesResult[1]);
         valuesResult[2] = (float) Math.toDegrees(valuesResult[2]);
        return;
      }*/
      
/*      String format(float values[]) {
    	    return String.format("%1$.1f\t\t%2$.1f\t\t%3$.1f", values[0], values[1], values[2]);
    	  }*/
      
  	/*void showInfo() {
  		sb.setLength(0);
  	    sb.append("\nOrientation 2: " + format(valuesResult2))
  	    ;
  		textView.setText(sb);
  	  }*/
 
  	/*@Override
  	public boolean onTouch(View v, MotionEvent event)
  	{
  		LinearLayout layout = (LinearLayout) findViewById(R.id.llMenu);
  		layout.setVisibility(View.VISIBLE);
  		
  		Toast toast = Toast.makeText(getApplicationContext(), "gdfgd", Toast.LENGTH_SHORT);
  		toast.show();
  		
  		
  		return true;
  	}*/
}


