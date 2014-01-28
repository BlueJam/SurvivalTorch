package uk.co.bimrose.android.survivaltorch;

import android.app.Activity;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.actionbarsherlock.app.SherlockFragment;

public class TorchFragment extends SherlockFragment implements View.OnClickListener {

	private Button buttonFull;
	private Button buttonSos;
	private Button buttonSosPreset;
	private Button buttonStop;
	private TextView message;

	private int lightSensitivity = 1000000;
	private boolean isThereALightSensor;
	
	private SharedPreferences prefs;

	public static int serviceCount;

	private AlertResetListener alertResetListener;
	private ServiceListener sListener;

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
			serviceCount++;
			sListener.startService("on");
			break;
		case R.id.button_sos:
			serviceCount++;
			sListener.startService("sos");
			break;
		case R.id.button_sos_preset:
			serviceCount++;
			sListener.startService("sosPreset");
			break;
		case R.id.button_stop:
			sListener.stopNotification();
			break;
		default:
			throw new RuntimeException("Unknown button ID");
		}
	}

	private void getPreferences() {
		prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
		lightSensitivity = Integer.valueOf(prefs.getString("lightsensitivity", "1000000"));
		message.setText("");
		if (lightSensitivity < 1000) {
			if (!isThereALightSensor) {
				message.setText("Sorry, you don't have a light sensor");
				prefs.edit().remove("lightSensitivity").commit();
			}
		}
	}
	
	public void setMessage(float lux){
		message.setText("LUX = " + Float.toString(lux));
	}
	
	public void setIsThereALightSensor(boolean isThereALightSensor){
		this.isThereALightSensor = isThereALightSensor;
	}
	
	public boolean getIsThereALightSensor(){
		return isThereALightSensor;
	}
	
	// Container Activity must implement this interface
	public interface AlertResetListener {
		public void alertReset();
	}

	// Container Activity must implement this interface
	public interface ServiceListener {
		public void startService(String s);

		public void stopNotification();
	}
}