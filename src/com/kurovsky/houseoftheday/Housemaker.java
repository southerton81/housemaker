package com.kurovsky.houseoftheday;

import com.kurovsky.houseoftheday.options.OptionsActivity;
import com.kurovsky.houseoftheday.R;
import com.swarmconnect.SwarmActivity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Window;

public class Housemaker extends SwarmActivity /*implements SensorEventListener*/ {
	//private SensorManager mSensorManager;
	//private Sensor mOrientationSensor;
	private MainView View;
    
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        //mSensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        //mOrientationSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        setContentView(R.layout.main);
        View = (MainView)findViewById(R.id.MainView);
    }

    protected void onResume() {
        super.onResume();
        //mSensorManager.registerListener(this, mOrientationSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    protected void onPause() {
        super.onPause();
        //mSensorManager.unregisterListener(this);
        View.StoreGame();
    	Game.mRestoreGame = true;
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    //public void onSensorChanged(SensorEvent event) {
    	 //if (OptionsActivity.mRollAllowed && event.sensor.getType() == Sensor.TYPE_ORIENTATION){
             //float roll = event.values[2];    
             //View.OnRoll(roll);
    	 //}
    //}

}