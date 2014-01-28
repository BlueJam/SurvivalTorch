package uk.co.bimrose.android.survivaltorch;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

public class LightFragment extends SherlockFragment implements SensorEventListener {

	private SensorManager mSensorManager;
	private Sensor mLight;
	Context mContext;
	boolean isThereALightSensor;
	String loopUntilLight;

	SharedPreferences prefsEdit;

	DaytimeListener dTListener;
	LightSensorListener lSListener;

	SharedPreferences prefs;
	int lightSensitivity;
	
	AutoStopCleanup autoStopCleanup;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			dTListener = (DaytimeListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement DaytimeListener");
		}

		try {
			lSListener = (LightSensorListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement LightSensorListener");
		}
		
		try {
			autoStopCleanup = (AutoStopCleanup) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement AlertReset");
		}
		
		// check for light sensor on the device
		sensorCheck();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		registerLightSensor();
		return (null);
	}

	public void registerLightSensor() {
		if (isThereALightSensor) {
			mSensorManager.registerListener(this, mLight, SensorManager.SENSOR_DELAY_NORMAL);
		}
	}

	public void unregisterLightSensor() {
		if (isThereALightSensor) {
			mSensorManager.unregisterListener(this);
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		// gets the lightsensitivity in case the user has changed it in prefs
		prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		lightSensitivity = Integer.valueOf(prefs.getString("lightsensitivity", "1"));
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (isThereALightSensor) {
			mSensorManager.unregisterListener(this);
		}
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// Don't think there is anything to do here
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		// triggered whenever the light sensor value changes
		float lux = event.values[0];
		dTListener.onlightChanged(lux, lightSensitivity);
		if (lightSensitivity < 1000) {
			if (lux >= lightSensitivity) {
				autoStopCleanup.autoStopCleanup();
			}
		}
	}

	public void sensorCheck() {
		mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
		if (mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null) {
			// Yep, light sensor present
			isThereALightSensor = true;
			updateLightSensorStatus();
			mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
		} else {
			// No light sensor
			isThereALightSensor = false;
			updateLightSensorStatus();
		}
	}

	public void updateLightSensorStatus() {
		// let the TorchFragment know there is a light sensor
		lSListener.lightSensorCheck(isThereALightSensor);
	}

	// Container Activity must implement this interface
	public interface DaytimeListener {
		public void onlightChanged(float lux, int lightSensitivity);
	}

	// Container Activity must implement this interface
	public interface LightSensorListener {
		public void lightSensorCheck(boolean isThereALightSensor);
	}

	public void toast(String toastMsg) {
		Toast.makeText(getActivity(), toastMsg, Toast.LENGTH_SHORT).show();
	}
	
	// Container Activity must implement this interface
	public interface AutoStopCleanup {
		public void autoStopCleanup();
	}
	
}