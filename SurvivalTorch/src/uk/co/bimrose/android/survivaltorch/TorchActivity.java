package uk.co.bimrose.android.survivaltorch;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.WindowManager;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class TorchActivity extends SherlockFragmentActivity {
	
	boolean keepScreenOn = false;
	
	  @Override
	  public void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    
	    
	    //have they selected to keep the screen on?
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		keepScreenOn = Boolean.valueOf(prefs.getBoolean("keepscreenon", false));
		if(keepScreenOn){
			//stops screen closing, should be an option
		    getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}
		
		//check for light sensor on the device
		sensorCheck();
	    
	    if (getSupportFragmentManager().findFragmentById(android.R.id.content) == null) {
	      getSupportFragmentManager().beginTransaction()
	                                 .add(android.R.id.content,
	                                      new TorchFragment()).commit();
	    }
	  }
	  
		@Override
		public boolean onCreateOptionsMenu(Menu menu) {
			new MenuInflater(this).inflate(R.menu.options, menu);
			return (super.onCreateOptionsMenu(menu));
		}
	  
		@Override
		public boolean onOptionsItemSelected(MenuItem item) {
			switch (item.getItemId()) {
			case R.id.prefs:
				TorchFragment.stop = true;
				startActivity(new Intent(this, Preferences.class));
				return (true);
			}
			return (super.onOptionsItemSelected(item));
		}
	  
		private void sensorCheck() {
			SensorManager mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
			if (mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null) {
				// Yep, light sensor present
				LightSensor.isThereALightSensor = true;
			} else {
				// No light sensor
				LightSensor.isThereALightSensor = false;
			}
		}
	  
	}
