package uk.co.bimrose.android.survivaltorch;

import android.app.Activity;
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

	private Sos sosLight;

	public static float lightLevel;

	AlertResetListener alertResetListener;
	BatteryLowListener bLListener;

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
	public void onPause() {
		super.onPause();
		turnOffFlash();
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
		lightSensitivity = Integer.valueOf(prefs.getString("lightsensitivity", "1000000"));

		if (lightSensitivity<1000) {
			if (!isThereALightSensor) {
				message.setText("Sorry, you don't have a light sensor");
				prefs.edit().remove("lightSensitivity").commit();
			}
		}

		batteryPct = Integer.valueOf(prefs.getString("batterypct", "50"));
	}

	@Override
	public void onStart() {
		super.onStart();
		getCamera();
	}

	@Override
	public void onStop() {
		super.onStop();
		if (cam != null) {
			cam.release();
			cam = null;
		}
	}

	@Override
	public void onClick(View view) {

		cancelSosAsynchTask();

		switch (view.getId()) {
		case R.id.button_full:
			if (!isFlashOn) {
				turnOnFlash();
			} else {
				turnOffFlash();
			}
			break;
		case R.id.button_sos:
			single = true;
			initialiseSos();
			break;
		case R.id.button_sos_preset:
			single = false;
			initialiseSos();
			break;
		case R.id.button_stop:
			turnOffFlash();
			break;
		default:
			throw new RuntimeException("Unknown button ID");
		}

	}

	public void initialiseSos() {
		alertResetListener.alertReset();
		running = true;
		sosLight = (Sos) new Sos().execute();
	}

	public void cancelSosAsynchTask() {
		if (running) {
			if (sosLight != null && sosLight.getStatus() != AsyncTask.Status.FINISHED){
				sosLight.cancel(true);
			}
		}
	}

	// get camera parameters
	private void getCamera() {
		if (cam == null) {
			try {
				cam = Camera.open();
				p = cam.getParameters();
			} catch (RuntimeException e) {
				Log.e("Camera Error. Failed to Open. Error: ", e.getMessage());
			}
		}
	}

	// Turn on flash
	private void turnOnFlash() {
		if (!isFlashOn) {
			if (cam == null || p == null) {
				return;
			}
			p = cam.getParameters();
			p.setFlashMode(Parameters.FLASH_MODE_TORCH);
			cam.setParameters(p);
			isFlashOn = true;
			cam.startPreview();
		}

	}

	public void turnOffFlash() {
		if (isFlashOn) {
			if (cam == null || p == null) {
				return;
			}
			p = cam.getParameters();
			p.setFlashMode(Parameters.FLASH_MODE_OFF);
			cam.setParameters(p);
			cam.stopPreview();
			isFlashOn = false;
		}
	}

	private class Sos extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... args) {

			if (isFlashOn) {
				turnOffFlash();
			}

			if (single) {
				sos();
			} else {
				if (loopXTimes > 0) {
					for (int i = 0; i < loopXTimes; i++) {
						if (isCancelled()) break;
						sos();
					}
				}
			}
			return null;
		}

		private void sos() {
			try {
				// dot dot dot, dash dash dash, dot dot dot
				for (int i = 0; i < 9; i++) {
					if (isCancelled()) break;
					int flashOn = sosSpeed;
					int sleepTime = sosSpeed;
					if (i > 2 && i < 6) {
						flashOn = sosSpeed * 3;
					}
					turnOnFlash();
					Thread.sleep(flashOn);
					turnOffFlash();
					Thread.sleep(sleepTime);
				}
				Thread.sleep(timeBetweenSignals * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		protected void onPostExecute(Void args) {
			running = false;
			turnOffFlash();
		}

		@Override
		protected void onCancelled() {
			turnOffFlash();
		}

	}

	public void setBatteryMessage(int pct) {
		batteryMessage.setText(Integer.toString(pct));
		if (pct <= batteryPct) {
			cancelSosAsynchTask();
			bLListener.onLowBattery();
		}
	}

	public interface AlertResetListener {
		public void alertReset();
	}

	// Container Activity must implement this interface
	public interface BatteryLowListener {
		public void onLowBattery();
	}

}