package uk.co.bimrose.android.survivaltorch;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class TorchFragment extends SherlockFragment implements View.OnClickListener {

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
	
	public static int z;

	AlertResetListener alertResetListener;
	ServiceListener sListener;

	int x;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// Makes sure that the container activity has implemented the callback interface
		try {
			alertResetListener = (AlertResetListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement AlertReset");
		}

		// start / stop service
		try {
			sListener = (ServiceListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement ServiceListener");
		}

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
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
		getPreferences();
	}

	@Override
	public void onClick(View view) {
		alertResetListener.alertReset();
		TorchActivity.keepRunning = false;
		switch (view.getId()) {
		case R.id.button_full:
			z++;
			sListener.startService("on");
			break;
		case R.id.button_sos:
			z++;
			sListener.startService("sos");
			break;
		case R.id.button_sos_preset:
			z++;TorchActivity.keepRunning = false;TorchActivity.keepRunning = false;
			sListener.startService("sosPreset");
			break;
		case R.id.button_stop:
			sListener.stopService();
			break;
		default:
			throw new RuntimeException("Unknown button ID");
		}
	}

	private void getPreferences() {
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		loopXTimes = Integer.valueOf(prefs.getString("loopxtimes", "1"));
		timeBetweenSignals = Integer.valueOf(prefs.getString("timebetweenloops", "5"));
		sosSpeed = Integer.valueOf(prefs.getString("sosspeed", "500"));
		lightSensitivity = Integer.valueOf(prefs.getString("lightsensitivity", "1000000"));
		message.setText("");
		if (lightSensitivity < 1000) {
			if (!isThereALightSensor) {
				message.setText("Sorry, you don't have a light sensor");
				prefs.edit().remove("lightSensitivity").commit();
			}
		}
		batteryPct = Integer.valueOf(prefs.getString("batterypct", "50"));
	}

	// Container Activity must implement this interface
	public interface AlertResetListener {
		public void alertReset();
	}

	// Container Activity must implement this interface
	public interface ServiceListener {
		public void startService(String s);

		public void stopService();
	}
}