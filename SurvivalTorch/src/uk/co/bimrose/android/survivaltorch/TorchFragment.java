package uk.co.bimrose.android.survivaltorch;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragment;

public class TorchFragment extends SherlockFragment implements
		View.OnClickListener {

	Button buttonFull;
	Button buttonSos;
	Button buttonSosPreset;
	Button buttonStop;
	TextView message;
	TextView batteryMessage;

	Camera cam = null;
	Parameters p = null;
	boolean isFlashOn = false;
	boolean single = true;
	int lightSensitivity = 1000000;
	int timeBetweenSignals = 5;
	boolean keepScreenOn = false;
	int batteryPct = 50;
	int loopXTimes = 1;
	int sosSpeed = 500;
	boolean isThereALightSensor;
	boolean running = false;

	public static float lightLevel;

	AlertResetListener alertResetListener;
	BatteryLowListener bLListener;
	ServiceListener sListener;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// This makes sure that the container activity has implemented
		// the callback interface. If not, it throws an exception
		try {
			alertResetListener = (AlertResetListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement AlertReset");
		}

		// and the battery low listener as well
		try {
			bLListener = (BatteryLowListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement BatteryLowListener");
		}
		
		//start / stop service
		try {
			sListener = (ServiceListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement ServiceListener");
		}
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState) {
		View result = inflater.inflate(R.layout.torchfrag, parent, false);

		buttonFull = (Button) result.findViewById(R.id.button_full);
		buttonSos = (Button) result.findViewById(R.id.button_sos);
		buttonSosPreset = (Button) result.findViewById(R.id.button_sos_preset);
		buttonStop = (Button) result.findViewById(R.id.button_stop);
		message = (TextView) result.findViewById(R.id.message);
		batteryMessage = (TextView) result.findViewById(R.id.batterymessage);

		buttonFull.setOnClickListener(this);
		buttonSos.setOnClickListener(this);
		buttonSosPreset.setOnClickListener(this);
		buttonStop.setOnClickListener(this);

		return (result);
	}


	@Override
	public void onResume() {
		super.onResume();

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getActivity());

		loopXTimes = Integer.valueOf(prefs.getString("loopxtimes", "1"));
		timeBetweenSignals = Integer.valueOf(prefs.getString(
				"timebetweenloops", "5"));
		sosSpeed = Integer.valueOf(prefs.getString("sosspeed", "500"));

		message.setText("");
		lightSensitivity = Integer.valueOf(prefs.getString("lightsensitivity",
				"1000000"));

		if (lightSensitivity < 1000) {
			if (!isThereALightSensor) {
				message.setText("Sorry, you don't have a light sensor");
				prefs.edit().remove("lightSensitivity").commit();
			}
		}

		batteryPct = Integer.valueOf(prefs.getString("batterypct", "50"));
	}

	@Override
	public void onClick(View view) {

		switch (view.getId()) {
		case R.id.button_full:
			//stop service
			sListener.stopService();
			break;
		case R.id.button_sos:
			single = true;
			break;
		case R.id.button_sos_preset:
			single = false;
			break;
		case R.id.button_stop:
			//start service
			sListener.startService();
			break;
		default:
			throw new RuntimeException("Unknown button ID");
		}

	}


	public void setBatteryMessage(int pct) {
		batteryMessage.setText(Integer.toString(pct));
		if (pct <= batteryPct) {
			bLListener.onLowBattery();
		}
	}

	// Container Activity must implement this interface
	public interface AlertResetListener {
		public void alertReset();
	}

	// Container Activity must implement this interface
	public interface BatteryLowListener {
		public void onLowBattery();
	}
	
	// Container Activity must implement this interface
	public interface ServiceListener {
		public void startService();
		public void stopService();
	}

}