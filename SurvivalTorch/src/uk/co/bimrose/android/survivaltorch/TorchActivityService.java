package uk.co.bimrose.android.survivaltorch;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
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
import android.util.Log;
import android.widget.Toast;

public class TorchActivityService extends IntentService {

	NotificationCompat.Builder b;
	NotificationManager mgr;

	public static int NOTIFY_ID = 1337;
	private static int FOREGROUND_ID = 1338;

	Camera cam = null;
	boolean camRegistered = false;
	Parameters p = null;
	PowerManager powerManager;
	boolean screenOn = true;
	Uri notification;
	Ringtone r;
	public static boolean keepRunning = true;
	int loopXTimes;
	int sosSpeed = 500;
	int timeBetweenSignals = 5;
	int lightSensitivity = 1000000;
	int batteryPct = 50;

	String click;
	SharedPreferences prefs;

	private Handler handler;

	public TorchActivityService() {
		super("TorchActivityService");
	}

	@Override
	public void onCreate() {
		getCamera();
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		keepRunning = false;
		turnOffFlash();
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

		getPreferences();

		Bundle extras = i.getExtras();
		if (extras != null) {
			click = extras.getString("click");
		}

		raiseNotification();

		keepRunning = true;
		// how did we get here? button clicks.
		if (click.equals("on")) {
			lightOn();
		} else if (click.equals("sos")) {
			sosLoop(1);
		} else if (click.equals("sosPreset")) {
			sosLoop(loopXTimes);
		}
	}

	public void lightOn() {
		// loop through checking if the screen has been turned off
		while (keepRunning) {
			// build the torch here with the values above
			turnOnFlash();
			while (screenOn) {
				if (!keepRunning)
					break;
				powerManager = (PowerManager) getSystemService(POWER_SERVICE);
				if (!powerManager.isScreenOn()
						&& (powerManager.isScreenOn() != screenOn)) {
					screenOn = false;
					turnOffFlash();
					// sleep to let the camera be turned off properly
					// without this the light didn't always come back on
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					turnOnFlash();
				}
			}
			while (!screenOn) {
				if (!keepRunning)
					break;
				powerManager = (PowerManager) getSystemService(POWER_SERVICE);
				if (powerManager.isScreenOn()
						&& (powerManager.isScreenOn() != screenOn)) {
					screenOn = true;
				}
			}
		}
	}
	
	public void sosLoop(int times) {
		for (int x = 0; x <= times; x++) {
			if (!keepRunning)
				break;
			try {
				for (int i = 0; i < 9; i++) {
					int flashOn = sosSpeed;
					int sleepTime = sosSpeed;
					if (i > 2 && i < 6) {
						flashOn = sosSpeed * 3;
					}
					if (!keepRunning)
						break;
					turnOnFlash();
					Thread.sleep(flashOn);
					if (!keepRunning)
						break;
					turnOffFlash();
					Thread.sleep(sleepTime);
					if (!keepRunning)
						break;
				}
				if (!keepRunning)
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
		// this clears the stack (I think!) and stops the TorchActivity opening
		// up again once finish() has been called on it
		i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		i.putExtras(bundle);
		// FLAG_CANCEL_CURRENT is needed to preserve the Extras added to the
		// PendingIntent
		b.setContentIntent(PendingIntent.getActivity(this, 0, i,
				PendingIntent.FLAG_CANCEL_CURRENT));
		// The code: new Intent() opens a blank intent (I think)
		// b.setContentIntent(PendingIntent.getActivity(this, 0, new
		// Intent(),0));

		b.setContentTitle(getString(R.string.app_name))
				.setContentText("Grrrrrrrrrrrrrrrrrr")
				.setSmallIcon(android.R.drawable.stat_sys_download)
				.setTicker(getString(R.string.turntorchoff));

		NotificationManager mgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		mgr.notify(NOTIFY_ID, b.build());

	}

	public void getPreferences() {
		Context ctx = getApplicationContext();
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		loopXTimes = Integer.valueOf(prefs.getString("loopxtimes", "1"));
		sosSpeed = Integer.valueOf(prefs.getString("sosspeed", "500"));
		timeBetweenSignals = Integer.valueOf(prefs.getString(
				"timebetweenloops", "5"));
		lightSensitivity = Integer.valueOf(prefs.getString("lightsensitivity",
				"1000000"));
		batteryPct = Integer.valueOf(prefs.getString("batterypct", "50"));
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

	private void releaseCamera() {
		if (cam != null) {
			cam.release();
			cam = null;
		}
	}

	private void turnOnFlash() {
		if (cam != null && !camRegistered) {
			p.setFlashMode(Parameters.FLASH_MODE_TORCH);
			// toast(Boolean.toString(camRegistered));
			cam.setParameters(p);
			cam.startPreview();
			camRegistered = true;
		}
	}

	public void turnOffFlash() {
		if (cam != null && camRegistered) {
			p.setFlashMode(Parameters.FLASH_MODE_OFF);
			toast(Boolean.toString(camRegistered));
			cam.setParameters(p);
			cam.stopPreview();
			camRegistered = false;
		}
	}

	public void toast(String tMsg) {
		final String msg = tMsg;
		handler.post(new Runnable() {
			@Override
			public void run() {
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT)
						.show();
			}
		});
	}

}
