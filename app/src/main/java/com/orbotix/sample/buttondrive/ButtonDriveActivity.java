package com.orbotix.sample.buttondrive;

import android.app.Activity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import orbotix.robot.base.Robot;
import orbotix.robot.base.RobotProvider;
import orbotix.robot.base.RollCommand;
import orbotix.sphero.ConnectionListener;
import orbotix.sphero.Sphero;
import orbotix.view.connection.SpheroConnectionView;

/** Activity for controlling the Sphero with five control buttons. */
public class ButtonDriveActivity extends Activity {

    private Sphero mRobot;

    public float heading;
    public float speed;
    private SpheroConnectionView mSpheroConnectionView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        findViews();
        mSpheroConnectionView = (SpheroConnectionView) findViewById(R.id.sphero_connection_view);
        mSpheroConnectionView.addConnectionListener(new ConnectionListener() {

            @Override
            public void onConnected(Robot robot) {
                //SpheroConnectionView is made invisible on connect by default
                mRobot = (Sphero) robot;
            }

            @Override
            public void onConnectionFailed(Robot sphero) {
                // let the SpheroConnectionView handle or hide it and do something here...
            }

            @Override
            public void onDisconnected(Robot sphero) {
                mSpheroConnectionView.startDiscovery();
            }
        });
    }

    private void findViews() {
        azimuthTextView = (TextView) findViewById(R.id.azimuth);
        pitchTextView = (TextView) findViewById(R.id.pitch);
        rollTextView = (TextView) findViewById(R.id.roll);
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(sensorListener, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorListener, magnetometer, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(sensorListener);
    }

    TextView azimuthTextView;
    TextView pitchTextView;
    TextView rollTextView;

    SensorManager sensorManager;
    SensorListener sensorListener = new SensorListener();
    Sensor accelerometer;
    Sensor magnetometer;

    class SensorListener implements SensorEventListener {

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {} // Must be implemented but nothing todo here

        float[] gravity;
        float[] geomagnetic;

        @Override
        public void onSensorChanged(SensorEvent event) {
            float[] rotationMatrixR = new float[9];
            float[] rotationMatrixI = new float[9];
            float[] orientation = new float[3];
            if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
                gravity = event.values.clone();
            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
                geomagnetic = event.values.clone();
            if (gravity == null || geomagnetic == null) return; // Not all data

            boolean success =  SensorManager.getRotationMatrix(rotationMatrixR, rotationMatrixI, gravity, geomagnetic);
            if (success) {
                sensorManager.getOrientation(rotationMatrixR, orientation);
                float azimuthAngle = orientation[0];
                float pitchAngle = orientation[1];
                float rollAngle = orientation[2];
                double azimuthDegrees = Math.toDegrees(azimuthAngle);
                double pitchDegrees = Math.toDegrees(pitchAngle);
                double rollDegrees = Math.toDegrees(rollAngle);

                if (Math.abs(rollDegrees) > Math.abs(pitchDegrees))
                {
                    if(rollDegrees > 0f)
                    {
                        heading = 90f;
                    }
                    else
                    {
                        heading = 270f;
                    }
                    speed = getValue(rollDegrees);
                    pitchTextView.setText(Double.toString(pitchDegrees));
                    rollTextView.setText(Double.toString(rollDegrees));
                    if (mRobot != null) {
                        mRobot.drive(heading, speed);
                    }

                }

                else
                {
                    if(pitchDegrees > 0f)
                    {
                        heading = 180f;
                    }
                    else
                    {
                        heading = 0f;
                    }
                    speed = getValue(pitchDegrees);
                    pitchTextView.setText(Double.toString(pitchDegrees));
                    rollTextView.setText(Double.toString(rollDegrees));
                    if (mRobot != null) {
                        mRobot.drive(heading, speed);
                    }

                }

            }
        }
        public float getValue (double angleValue)
       {

         try {

            return (float)Math.abs(angleValue/360f);
         } catch (ArithmeticException e) {
            return 0f;
          }
     }






            }
        }



