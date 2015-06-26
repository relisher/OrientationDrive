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
    public float returnedSpeed;
    public float returnedHeading;
    public boolean buttonState = true;
    /** The Sphero Connection View */
    private SpheroConnectionView mSpheroConnectionView;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        runTheListener R1 = new runTheListener();
        R1.start();
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


    TextView pitchTextView;
    TextView rollTextView;
    SensorManager sensorManager;
    Sensor accelerometer;
    Sensor magnetometer;
    runTheListener run = new runTheListener();

    /** Called when the user comes back to this app */
    @Override
    protected void onResume() {
        super.onResume();
        // Refresh list of Spheros

        mSpheroConnectionView.startDiscovery();
    }


    /** Called when the user presses the back or home button */
    @Override
    protected void onPause() {
        super.onPause();
        // Disconnect Robot properly
        RobotProvider.getDefaultProvider().disconnectControlledRobots();
    }

    /**
     * When the user clicks "STOP", stop the Robot.
     *
     * @param v The View that had been clicked
     */
    public void onStopClick(View v) {
        if (mRobot != null) {
            // Stop robot
            buttonState = false;
            mRobot.stop();
        }
    }

    /**
     * When the user clicks a control button, roll the Robot in that direction
     *
     * @param v The View that had been clicked
     */

    public void onControlClick(View v) {
        // Find the heading, based on which button was clicked

        switch (v.getId()) {
            case R.id.go_button:
                    while(buttonState) {
                        setSphero(returnedHeading, returnedSpeed, 500);
                    }
                break;

            default:
                setSphero(1f, 0f, 10000);
                buttonState = true;
                break;
        }


    }
    public void setSphero(float heading, float speed, int milliSeconds)
    {
        final float direction = heading;
        final int time = milliSeconds;
        final float velocity = speed;
        mRobot.drive(direction, velocity);
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            e.printStackTrace();
            // handle the exception...
            // For example consider calling Thread.currentThread().interrupt(); here.
        }
    }
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

                float pitchAngle = orientation[1];
                float rollAngle = orientation[2];

                float pitchDegrees = (float)Math.toDegrees(pitchAngle);
                float rollDegrees = (float)Math.toDegrees(rollAngle);
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

                }

            }
        }
        public float getValue (float angleValue)
        {
            try {
                return Math.abs(angleValue/360f);
            } catch (ArithmeticException e) {
                return 0f;
            }
        }
        public float returnHeading()
        {
            return heading;
        }
        public float returnSpeed()
        {
            return speed;
        }

    }
    public class runTheListener implements Runnable
    {
        private Thread t;
        @Override
        public void run() {
            SensorListener sensorListener;
            sensorListener = new SensorListener();
            while(true) {
                returnedSpeed = sensorListener.returnSpeed();
                returnedHeading = sensorListener.returnHeading();
            }
        }
        public void start ()
        {
            sensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);
            accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            magnetometer = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
            if (t == null)
            {
                t = new Thread (this);
                t.start();
            }

        }
    }
}



