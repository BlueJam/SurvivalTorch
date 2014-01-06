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

import com.actionbarsherlock.app.SherlockFragment;

public class LightFragment extends SherlockFragment implements
		SensorEventListener {

	private SensorManager mSensorManager;
	private Sensor mLight;
	Context mContext;
	boolean isThereALightSensor;

	DaytimeListener dTListener;
	IsThereALightSensor lSListener;
	GetIsThereALightSensor gLSListener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			dTListener = (DaytimeListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement DaytimeListener");
		}

		try {
			lSListener = (IsThereALightSensor) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement IsThereALightSensor");
		}

		try {
			gLSListener = (GetIsThereALightSensor) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement GetIsThereALightSensor");
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState) {
		View result = inflater.inflate(R.layout.torchfrag, parent, false);

		// check for light sensor on the device
		sensorCheck();
		return (result);
	}

	@Override
	public void onResume() {
		super.onResume();
		if (gLSListener.getIsThereALightSensor()) {
			mSensorManager.registerListener(this, mLight,
					SensorManager.SENSOR_DELAY_NORMAL);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		if (gLSListener.getIsThereALightSensor()) {
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
		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getActivity());
		int lightSensitivity = Integer.valueOf(prefs.getString(
				"lightsensitivity", "1"));

		float lux = event.values[0];
		// Sends a message to the hosting Activity including the light value
		// and the value the user has chosen
		dTListener.onlightChanged(lux, lightSensitivity);
	}

	public void sensorCheck() {

		mSensorManager = (SensorManager) getActivity().getSystemService(
				Context.SENSOR_SERVICE);
		if (mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null) {
			// Yep, light sensor present
			lSListener.sensorCheck(true);
			mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
		} else {
			// No light sensor
			lSListener.sensorCheck(false);
		}
	}

	// Container Activity must implement this interface
	public interface DaytimeListener {
		public void onlightChanged(float lux, int lightSensitivity);
	}

	// Container Activity must implement this interface
	public interface IsThereALightSensor {
		public void sensorCheck(boolean isThereALightSensor);
	}

	// Container Activity must implement this interface
	public interface GetIsThereALightSensor {
		public boolean getIsThereALightSensor();
	}

	/**
	 * Need to decouple this from the TorchFragment easy enough to do, just
	 * create it's own boolean value to check on for the stuff in this fragment,
	 * then pass the value via an interface to the Avtivity which then passes
	 * the value up to the TorchFragment for use later.
	 */

}