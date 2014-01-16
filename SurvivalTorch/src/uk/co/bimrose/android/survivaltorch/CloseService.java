package uk.co.bimrose.android.survivaltorch;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

public class CloseService extends Activity {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		boolean closeActivity = false;
		String toastMsg = "false";
		
		Bundle extras = getIntent().getExtras();
		if(extras != null){
			closeActivity = extras.getBoolean("closeActivity");
			if(closeActivity){
				toastMsg = "true";
			}
		}
		
		TorchActivityService.keepRunning = false;
		Toast.makeText(getApplicationContext(), toastMsg,
				   Toast.LENGTH_LONG).show();
		
		
		
		
		finish();
	}
	
}