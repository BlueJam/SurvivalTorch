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
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.widget.Toast;

public class TorchActivityService extends IntentService {

	public static int NOTIFY_ID = 1337;

	private Camera cam = null;
	private Parameters p = null;

	private int loopXTimes;
	private int sosSpeed;
	private int timeBetweenSignals;
	private boolean sOLOff;
	private boolean notifications;

	private String click;
	private SharedPreferences prefs;

	private Handler handler;

	private screenTurnOff sTOff;

	public TorchActivityService() {
		super("TorchActivityService");
	}

	@Override
	public void onCreate() {
		// register the screen off broadcastreceivers
		IntentFilter filterOff = new IntentFilter(Intent.ACTION_SCREEN_OFF);
		sTOff = new screenTurnOff();
		registerReceiver(sTOff, filterOff);

		getCamera();
		// keepRunning = true;
		super.onCreate();
	}

	@Override
	public void onDestroy() {
		this.unregisterReceiver(sTOff);
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
		
		if(notifications){
			raiseNotification();
		}

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

	private void lightOn() {
		startFlash();
		while (TorchActivity.keepRunning) {
			//Keeping onHandleIntent alive until it's no longer needed.
		}
	}

	private void restartTorch() {
		stopFlash();
		// sleep to let the camera be turned off properly without this the light didn't always come back on
		try {
			Thread.sleep(200);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		startFlash();
	}

	private void sosLoop(int times) {
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

	private void getPreferences() {
		Context ctx = getApplicationContext();
		prefs = PreferenceManager.getDefaultSharedPreferences(ctx);
		loopXTimes = Integer.valueOf(prefs.getString("loopxtimes", "1"));
		sosSpeed = Integer.valueOf(prefs.getString("sosspeed", "500"));
		timeBetweenSignals = Integer.valueOf(prefs.getString("timebetweenloops", "5"));
		sOLOff = prefs.getBoolean("screenofflightoff", true);
		notifications = prefs.getBoolean("notifications", true);
	}

	public class screenTurnOff extends BroadcastReceiver {
		@Override
		public void onReceive(Context context, Intent intent) {
			if(sOLOff){
				if(click.equals("on")){
					restartTorch();
					//don't need to do anything to keep sos going
				}
			}else{
				TorchActivity.keepRunning = false;
			}
		}
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

	private void startFlash() {
		if (!TorchActivity.lightOn) {
			p.setFlashMode(Parameters.FLASH_MODE_TORCH);
			cam.setParameters(p);
			cam.startPreview();
			TorchActivity.lightOn = true;
		}
	}

	private void stopFlash() {
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
				Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
			}
		});
	}

}
