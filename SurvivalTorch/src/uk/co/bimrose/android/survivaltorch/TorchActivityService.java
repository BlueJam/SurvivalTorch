package uk.co.bimrose.android.survivaltorch;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.Ringtone;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

public class TorchActivityService extends IntentService {

	NotificationCompat.Builder b;
	NotificationManager mgr;

	public static int NOTIFY_ID = 1337;

	Camera cam = null;
	Parameters p = null;
	PowerManager powerManager;

	boolean screenOn = true;

	int loopXTimes;
	int sosSpeed;
	int timeBetweenSignals;
	int lightSensitivity;
	int batteryPct;

	String click;
	SharedPreferences prefs;

	private Handler handler;

	public TorchActivityService() {
		super("TorchActivityService");
	}

	@Override
	public void onCreate() {
		getCamera();
		// keepRunning = true;
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		stopFlash();
		releaseCamera();
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		handler = new Handler();

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onHandleIntent(Intent i) {

		if (TorchFragment.serviceCount <= 1) {
			TorchActivity.keepRunning = true;
		}

		Bundle extras = i.getExtras();
		if (extras != null) {
			click = extras.getString("click");
		}

		getPreferences();
		stopFlash();
		raiseNotification();

		// how did we get here? button clicks.
		if (click.equals("on")) {
			lightOn();
		} else if (click.equals("sos")) {
			sosLoop(1);
		} else if (click.equals("sosPreset")) {
			sosLoop(loopXTimes);
		}
		TorchFragment.serviceCount--;
	}

	/**
	 * public void strobe(){ while (TorchActivity.keepRunning){ startFlash(); stopFlash(); } }
	 **/

	public void lightOn() {
		// loop through checking if the screen has been turned off
		while (TorchActivity.keepRunning) {
			// build the torch here with the values above
			startFlash();
			while (screenOn) {
				if (!TorchActivity.keepRunning)
					break;
				powerManager = (PowerManager) getSystemService(POWER_SERVICE);
				if (!powerManager.isScreenOn() && (powerManager.isScreenOn() != screenOn)) {
					screenOn = false;
					stopFlash();
					// sleep to let the camera be turned off properly
					// without this the light didn't always come back on
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					startFlash();
				}
			}
			while (!screenOn) {
				if (!TorchActivity.keepRunning)
					break;
				powerManager = (PowerManager) getSystemService(POWER_SERVICE);
				if (powerManager.isScreenOn() && (powerManager.isScreenOn() != screenOn)) {
					screenOn = true;
				}
			}
		}
	}

	public void sosLoop(int times) {
		for (int x = 0; x < times; x++) {
			if (!TorchActivity.keepRunning)
				break;
			try {
				for (int i = 0; i < 9; i++) {
					int flashOn = sosSpeed;
					int sleepTime = sosSpeed;
					if (i > 2 && i < 6) {
						flashOn = sosSpeed * 3;
					}
					if (!TorchActivity.keepRunning)
						break;
					startFlash();
					Thread.sleep(flashOn);
					if (!TorchActivity.keepRunning)
						break;
					stopFlash();
					Thread.sleep(sleepTime);
					if (!TorchActivity.keepRunning)
						break;
				}
				if (!TorchActivity.keepRunning)
					break;
				Thread.sleep(timeBetweenSignals * 1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void raiseNotification() {
		NotificationCompat.Builder b = new NotificationCompat.Builder(this);
		b.setAutoCancel(true).setWhen(System.currentTimeMillis());
		Intent i = new Intent(this, TorchActivity.class);
		Bundle bundle = new Bundle();
		bundle.putBoolean("closeActivity", true);
		// this clears the stack (I think!) and stops the TorchActivity opening up again once finish() has been called
		i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
		i.putExtras(bundle);
		// FLAG_CANCEL_CURRENT is needed to preserve the Extras added to the PendingIntent
		b.setContentIntent(PendingIntent.getActivity(this, 0, i, PendingIntent.FLAG_CANCEL_CURRENT));
		// The code: new Intent() opens a blank intent (I think)
		// b.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(),0));
		b.setContentTitle(getString(R.string.app_name)).setContentText("Grrrrrrrrrrrrrrrrrr")
				.setSmallIcon(android.R.drawable.stat_sys_download).setTicker(getString(R.string.turntorchoff));
		NotificationManager mgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		mgr.notify(NOTIFY_ID, b.build());
	}

	public void getPreferences() {
		Context ctx = getApplicationContext();
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		loopXTimes = Integer.valueOf(prefs.getString("loopxtimes", "1"));
		sosSpeed = Integer.valueOf(prefs.getString("sosspeed", "500"));
		timeBetweenSignals = Integer.valueOf(prefs.getString("timebetweenloops", "5"));
		lightSensitivity = Integer.valueOf(prefs.getString("lightsensitivity", "1000000"));
		batteryPct = Integer.valueOf(prefs.getString("batterypct", "50"));
	}

	// get camera parameters
	private void getCamera() {
		cam = Camera.open();
		p = cam.getParameters();
	}

	private void releaseCamera() {
		cam.release();
		cam = null;
	}

	public void startFlash() {
		if (!TorchActivity.lightOn) {
			p.setFlashMode(Parameters.FLASH_MODE_TORCH);
			cam.setParameters(p);
			cam.startPreview();
			TorchActivity.lightOn = true;
		}
	}

	public void stopFlash() {
		if (TorchActivity.lightOn) {
			p.setFlashMode(Parameters.FLASH_MODE_OFF);
			cam.setParameters(p);
			cam.stopPreview();
			TorchActivity.lightOn = false;
		}
	}

	public void toast(String tMsg) {
		final String msg = tMsg;
		handler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
			}
		});
	}

}
