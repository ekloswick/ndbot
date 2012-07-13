package nd.reu.ndbot;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;

public class CompassService extends Service{
	
	
	private SensorManager mSensorManager;
	private Sensor compass;
	private float compassX;
	private float compassY;
	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void onCreate(){
		mSensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		compass = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		 mSensorManager.registerListener(listener,compass,SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	private SensorEventListener listener=new SensorEventListener() {
		
		@Override
		public void onAccuracyChanged(Sensor sensor, int accuracy) {
			// not used
			
		}

		@Override
		public void onSensorChanged(SensorEvent event) {  
			if(event.sensor.getType() != Sensor.TYPE_MAGNETIC_FIELD)
				return;
			compassX = event.values[0];
			compassY = event.values[1];
			
		}
	};
}
