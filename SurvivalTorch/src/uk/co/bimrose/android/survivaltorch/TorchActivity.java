package uk.co.bimrose.android.survivaltorch;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.WindowManager;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;

public class TorchActivity extends SherlockFragmentActivity implements
		SensorEventListener {

	boolean keepScreenOn = false;
	private SensorManager mSensorManager;
	private Sensor mLight;
	private TorchFragment torchFrag = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// have they selected to keep the screen on?
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(this);
		keepScreenOn = Boolean.valueOf(prefs.getBoolean("keepscreenon", false));
		if (keepScreenOn) {
			// stops screen closing, should be an option
			getWindow()
					.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		}

		torchFrag = (TorchFragment) getSupportFragmentManager()
				.findFragmentById(R.id.torchfrag);

		if (torchFrag == null) {
			torchFrag = new TorchFragment();
			getSupportFragmentManager().beginTransaction()
					.add(R.id.torchfrag, torchFrag).commit();
		}

		// check for light sensor on the device
		sensorCheck();

	}

	@Override
	protected void onResume() {
		super.onResume();
		if (TorchFragment.isThereALightSensor) {
			mSensorManager.registerListener(this, mLight,
					SensorManager.SENSOR_DELAY_NORMAL);
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		if (TorchFragment.isThereALightSensor) {
			mSensorManager.unregisterListener(this);
		}

	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// Don't think there is anything to do here
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		float lux = event.values[0];
		torchFrag.message.setText(Float.toString(lux));
		
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
		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		if (mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null) {
			// Yep, light sensor present
			TorchFragment.isThereALightSensor = true;
			mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
			mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
		} else {
			// No light sensor
			TorchFragment.isThereALightSensor = false;
		}
	}

}
