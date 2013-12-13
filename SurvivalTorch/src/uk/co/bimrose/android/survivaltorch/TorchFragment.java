package uk.co.bimrose.android.survivaltorch;

import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.AsyncTask;
import android.os.Bundle;
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

	Camera cam = null;
	Parameters p = null;
	boolean isFlashOn = false;;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent,
			Bundle savedInstanceState) {
		View result = inflater.inflate(R.layout.torchmain, parent, false);

		buttonFull = (Button) result.findViewById(R.id.button_full);
		buttonSos = (Button) result.findViewById(R.id.button_sos);
		buttonSosPreset = (Button) result.findViewById(R.id.button_sos_preset);

		buttonFull.setOnClickListener(this);
		buttonSos.setOnClickListener(this);
		buttonSosPreset.setOnClickListener(this);

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
		if (isFlashOn)
			turnOnFlash();
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

		switch (view.getId()) {
		case R.id.button_full:
			if (!isFlashOn) {
				turnOnFlash();
			} else {
				turnOffFlash();
			}
			break;
		case R.id.button_sos:
			new Sos().execute();
			break;
		case R.id.button_sos_preset:
			// sosPreset
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

			try {
				//dot dot dot, dash dash dash, dot dot dot
				for (int i = 0; i < 9; i++) {
					int flashOn = 500;
					int sleepTime = 500;
					if (i > 2 && i < 6) {
						flashOn = 1500;
					}
					turnOnFlash();
					Thread.sleep(flashOn);
					turnOffFlash();
					Thread.sleep(sleepTime);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			return null;
		}
	}
}
