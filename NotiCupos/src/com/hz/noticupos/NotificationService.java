package com.hz.noticupos;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class NotificationService extends Service{

	private int val;
	private int multiplier; //milliseconds
	
	public NotificationService(int value, String time){
		this.val = value;
		
		if (time.equals("Minutos"))multiplier = 60000;
		else if(time.equals("Horas"))multiplier = 3600000;
		
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

}
