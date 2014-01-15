package uk.co.bimrose.android.survivaltorch;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class TorchActivityService extends IntentService {

	NotificationCompat.Builder b;
	NotificationManager mgr;

	private static int NOTIFY_ID = 1337;
	private static int FOREGROUND_ID = 1338;

	Camera cam = null;
	Parameters p = null;

	PowerManager pm;
	boolean screenOn = true;

	Uri notification;
	Ringtone r;
	
	boolean stopnotification = false;

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
		releaseCamera();
		super.onDestroy();
	}

	@Override
	public void onHandleIntent(Intent i) {

		/**
		 * Intent intent = i; Bundle extras = intent.getExtras(); int loopXTimes
		 * = Integer.parseInt(extras.getString("lXTimes")); int
		 * timeBetweenSignals = Integer
		 * .parseInt(extras.getString("tBSignals")); int sosSpeed =
		 * Integer.parseInt(extras.getString("sSpeed")); int lightSensitivity =
		 * Integer.parseInt(extras .getString("lSensitivity")); int batteryPct =
		 * Integer.parseInt(extras.getString("bPct"));
		 **/

		raiseNotification();

		// loop through checking if the screen has been turned off
		PowerManager powerManager;

		while (true) {
			// build the torch here with the values above
			turnOnFlash();
			while (screenOn) {
				powerManager = (PowerManager) getSystemService(POWER_SERVICE);
				// if screen is off && screenOn = true
				if (!powerManager.isScreenOn()
						&& (powerManager.isScreenOn() != screenOn)) {
					screenOn = false;
					turnOffFlash();
					// we need to sleep for a moment here to let the camera be
					// turned off properly
					// without this the light didn't always come back on again.
					try {
						Thread.sleep(200);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					turnOnFlash();
				}
			}

			while (!screenOn) {
				powerManager = (PowerManager) getSystemService(POWER_SERVICE);
				// if screen is on && screenOn = false
				if (powerManager.isScreenOn()
						&& (powerManager.isScreenOn() != screenOn)) {
					screenOn = true;
					
					//stopForegroundService();
					//raiseNotification();
				}
			}
			stopForeground(stopnotification);
		}

		// this should send a silent notification that lets you get back to the
		// torch by clicking on it
		// raiseNotification();

	}


	private Notification buildForegroundNotification(String contextText) {
		
		NotificationCompat.Builder b = new NotificationCompat.Builder(this);

		b.setOngoing(true);
		
		b.setAutoCancel(true).setDefaults(Notification.DEFAULT_ALL)
		.setWhen(System.currentTimeMillis());

		b.setContentTitle(getString(R.string.app_name)) 
				.setContentText(contextText)
				.setSmallIcon(android.R.drawable.stat_sys_download)
				.setTicker(getString(R.string.turntorchoff));
		
		Intent outbound = new Intent(this, TorchActivity.class);

		b.setContentIntent(PendingIntent.getActivity(this, 0, outbound, 0));

		return (b.build());
	}

	private void raiseNotification() {
		NotificationCompat.Builder b = new NotificationCompat.Builder(this);

		b.setAutoCancel(true).setWhen(System.currentTimeMillis());
		b.setContentIntent(PendingIntent.getActivity(this, 0, new Intent(), 0));

		b.setContentTitle(getString(R.string.app_name)) 
		.setContentText("Grrrrrrrrrrrrrrrrrr")
		.setSmallIcon(android.R.drawable.stat_sys_download)
		.setTicker(getString(R.string.turntorchoff));

		NotificationManager mgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

		mgr.notify(NOTIFY_ID, b.build());

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
		p = cam.getParameters();
		p.setFlashMode(Parameters.FLASH_MODE_TORCH);
		cam.setParameters(p);
		cam.startPreview();
	}

	public void turnOffFlash() {
		p = cam.getParameters();
		p.setFlashMode(Parameters.FLASH_MODE_OFF);
		cam.setParameters(p);
		cam.stopPreview();

		/**
		 * Uri notification = RingtoneManager
		 * .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION); Ringtone r =
		 * RingtoneManager.getRingtone(getApplicationContext(), notification);
		 * r.play();
		 **/

	}

}
