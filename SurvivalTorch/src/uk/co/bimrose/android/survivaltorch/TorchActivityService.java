package uk.co.bimrose.android.survivaltorch;

import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Intent;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

public class TorchActivityService extends IntentService {

	NotificationCompat.Builder b;
	NotificationManager mgr;

	Camera cam = null;
	Parameters p = null;

	PowerManager pm;
	boolean screenOn = true;
	
	ScreenCheck screenCheck;

	public TorchActivityService() {
		super("TorchActivityService");
	}

	@Override
	public void onCreate() {
		super.onCreate();
		getCamera();
		screenCheck = (ScreenCheck) new ScreenCheck().execute();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		if (cam != null) {
			cam.release();
			cam = null;
		}
	}
	
	
	
	private class ScreenCheck extends AsyncTask<Void, Void, Void> {
		@Override
		protected Void doInBackground(Void... args) {
			
			turnOnFlash();
			PowerManager powerManager;
			while (screenOn) {
				powerManager = (PowerManager) getSystemService(POWER_SERVICE);
				//if screen is off && screenOn = true
				if (!powerManager.isScreenOn() && (powerManager.isScreenOn() != screenOn)) {
					screenOn = false;
					turnOffFlash();
					turnOnFlash();
				}
			}
			
			return null;
		}
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

		// build the torch here with the values above
		
		

		
		while(true){
			
		}
		
		// this should send a silent notification that lets you get back to the
		// torch by clicking on it
		// raiseNotification();

		/**
		 * Uri notification = RingtoneManager
		 * .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION); Ringtone r =
		 * RingtoneManager.getRingtone(getApplicationContext(), notification);
		 * for(int x = 0;x <= 10;x++){ r.play(); try { Thread.sleep(1000); }
		 * catch (InterruptedException e) { e.printStackTrace(); } }
		 **/

	}

	/**
	 * private void raiseNotification() { b = new
	 * NotificationCompat.Builder(this);
	 * 
	 * b.setAutoCancel(true).setDefaults(Notification.DEFAULT_ALL)
	 * .setWhen(System.currentTimeMillis());
	 * 
	 * b.setContentTitle(getString(R.string.app_name))
	 * .setContentText(getString(R.string.keepscreenontitle))
	 * .setSmallIcon(android.R.drawable.stat_sys_download_done)
	 * .setTicker(getString(R.string.app_name));
	 * 
	 * Intent outbound = new Intent(Intent.ACTION_VIEW);
	 * 
	 * b.setContentIntent(PendingIntent.getActivity(this, 0, outbound, 0));
	 * 
	 * mgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
	 * 
	 * mgr.notify(NOTIFY_ID, b.build()); }
	 **/

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

	private void turnOnFlash() {
		if (cam == null || p == null) {
			return;
		}
		p = cam.getParameters();
		p.setFlashMode(Parameters.FLASH_MODE_TORCH);
		cam.setParameters(p);
		cam.startPreview();
	}

	public void turnOffFlash() {
		if (cam == null || p == null) {
			return;
		}
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
