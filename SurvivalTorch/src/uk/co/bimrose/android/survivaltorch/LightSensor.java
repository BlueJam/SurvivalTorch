package uk.co.bimrose.android.survivaltorch;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;

public class LightSensor extends Activity implements SensorEventListener {

	private SensorManager mSensorManager;
	private Sensor mLight;

	public static boolean isThereALightSensor;

	@Override
	public final void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
		mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
	}
	
	  @Override
	  protected void onResume() {
	    super.onResume();
	    mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);
	  }

	  @Override
	  protected void onPause() {
	    super.onPause();
	    mSensorManager.unregisterListener(this);
	  }
	


	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		//Don't think there is anything to do here
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		TorchFragment.lightLevel = event.values[0];
	}

}