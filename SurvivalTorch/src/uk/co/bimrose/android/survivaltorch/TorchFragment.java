package uk.co.bimrose.android.survivaltorch;

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

	Camera cam = null;
	Parameters p = null;
	boolean isFlashOn = false;
	boolean loopForever = false;
	boolean single = true;
	boolean loopUntilLight = false;
	int timeBetweenSignals = 5;
	boolean stopOnLowBattery = true;
	boolean keepScreenOn = false;
	int list = 50;
	int loopXTimes = 1;
	public static boolean stop = false;
	int sosSpeed = 500;
	
	public static float lightLevel;
	public static boolean isThereALightSensor;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState) {
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

		// getSherlockActivity().getSupportActionBar().setHomeButtonEnabled(true);

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
		
		if (isFlashOn) {
			turnOnFlash();
		}

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(getActivity());

		loopForever = Boolean.valueOf(prefs.getBoolean("loopforever", false));
		loopXTimes = Integer.valueOf(prefs.getString("loopxtimes", "1"));
		timeBetweenSignals = Integer.valueOf(prefs.getString(
				"timebetweenloops", "5"));
		sosSpeed = Integer.valueOf(prefs.getString("sosspeed", "500"));
		
		message.setText("");
		loopUntilLight = Boolean.valueOf(prefs.getBoolean("loopuntillight",
				false));
		//If the light sensor has been checked then we need to check if we have one
		//If there isn't one, we reset the value and send the user a message
		if(loopUntilLight){
			// if there is no light sensor then clear the preference and post a message
			if (!isThereALightSensor) {
				prefs.edit().remove("loopuntillight").commit();
				message.setText("Sorry, you don't have a light sensor");
			}

		}
		
		stopOnLowBattery = Boolean.valueOf(prefs.getBoolean("stoponlowbattery",
				true));
		list = Integer.valueOf(prefs.getString("list", "5"));

		keepScreenOn = Boolean.valueOf(prefs.getBoolean("keepscreenon", false));

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

		stop = false;

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
			new Sos().execute();
			break;
		case R.id.button_sos_preset:
			single = false;
			new Sos().execute();
			break;
		case R.id.button_stop:
			turnOffFlash();
			stop = true;
			break;
		default:
			throw new RuntimeException("Unknow button ID");
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
			cam.startPreview();
			isFlashOn = true;
		}

	}

	private void turnOffFlash() {
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
				if (loopForever) {
					while (!stop) {
						sos();
					}
				} else if (loopXTimes > 0) {
					for (int i = 0; i < loopXTimes; i++) {
						sos();
						if (stop) {
							break;
						}
					}
				}

			}

			return null;
		}

		private void sos() {
			try {
				// dot dot dot, dash dash dash, dot dot dot
				for (int i = 0; i < 9; i++) {
					int flashOn = sosSpeed;
					int sleepTime = sosSpeed;
					if (i > 2 && i < 6) {
						flashOn = sosSpeed * 3;
					}
					turnOnFlash();
					Thread.sleep(flashOn);
					turnOffFlash();
					Thread.sleep(sleepTime);
					if (stop) {
						break;
					}
				}
				Thread.sleep(timeBetweenSignals * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

}
