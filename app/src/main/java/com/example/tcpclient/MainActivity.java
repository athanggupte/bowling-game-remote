package com.example.tcpclient;

import android.graphics.drawable.GradientDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.nio.ByteBuffer;


public class MainActivity extends AppCompatActivity {

    EditText editTextMsg;
    Button sendButton, closeButton;
    ToggleButton toggleButton;
    TextView orientationText;

    String message = "", ip = "192.168.1.9";

    private CommunicationThread commThread;

    private SensorManager sensorManager;
    private Sensor sensorGravity, sensorMagnet, sensorRotVect;
    private SensorEventListener sensorEventListener;

    float[] orientation;
    int eventCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        editTextMsg = (EditText) findViewById(R.id.editTextMsg);
        sendButton = (Button) findViewById(R.id.buttonSend);
        closeButton = (Button) findViewById(R.id.buttonClose);
        orientationText = (TextView) findViewById(R.id.textOrientation);
        toggleButton = (ToggleButton) findViewById(R.id.toggleOrientation);

        commThread = new CommunicationThread(ip);
        commThread.start();

        sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        sensorGravity = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorMagnet = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        sensorRotVect = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);

        orientation = new float[3];

        sensorEventListener = new SensorEventListener() {
            float[] valGravity;
            float[] valGeomagnetic;
            float[] valRotVect;

            @Override
            public void onSensorChanged(SensorEvent event) {

                eventCount++;

                if(event.sensor.getType() == Sensor.TYPE_ROTATION_VECTOR)
                    valRotVect = event.values;

                long buf = (long) (valRotVect[3]*100000);
                commThread.send(buf);

                buf = (long) (valRotVect[0]*100000);
                commThread.send(buf);

                buf = (long) (valRotVect[1]*100000);
                commThread.send(buf);

                buf = (long) (valRotVect[2]*100000);
                commThread.send(buf);

                /*
                switch (event.sensor.getType()){
                    case Sensor.TYPE_ACCELEROMETER:
                        valGravity = event.values;
                        break;
                    case Sensor.TYPE_MAGNETIC_FIELD:
                        valGeomagnetic = event.values;
                        break;
                    default:
                }

                if(valGravity != null && valGeomagnetic != null){
                    float R[] = new float[9];
                    float I[] = new float[9];

                    if(SensorManager.getRotationMatrix(R, I, valGravity, valGeomagnetic)) {
                        SensorManager.getOrientation(R, orientation);
                    }

                    updateOrientation();

                    if(eventCount >= 20){
                        eventCount = 0;
                        Log.d("OrientationDetails", eventCount + "\t: " + orientation[0] + "\t: " + orientation[1] + "\t : " + orientation[2]);
                        try{
                            if(commThread == null) {
                                commThread = new CommunicationThread(ip);
                                commThread.start();
                            }

                         //   if(!commThread.isRunning()) return;


                        //    commThread.send(1);

                            long buf = (long) (orientation[0]*100000);
                            commThread.send(buf);

                        //    commThread.send(2);

                            buf = (long) (orientation[1]*100000);
                            commThread.send(buf);

                        //    commThread.send(3);

                            buf = (long) (orientation[2]*100000);
                            commThread.send(buf);

                        } catch (Exception e) {
                            e.printStackTrace();
                        }


                    }

                }

                */
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {

            }
        };

        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                message = editTextMsg.getText().toString();
                if(commThread == null) {
                    commThread = new CommunicationThread(ip);
                    commThread.start();
                }
              //  commThread.send(message);
            }
        });
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    if(commThread != null) {
                        commThread.close();
                        commThread = null;
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        toggleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try{
                    if(commThread == null) {
                        commThread = new CommunicationThread(ip);
                        commThread.start();
                    }
                    long buf = (long) (orientation[0]*100000);
                //    ByteBuffer.wrap(buf).putFloat(orientation[0]);
                    commThread.send(buf);
                    editTextMsg.setText(Long.toString(buf));
                    Log.d("Orientation",Long.toString(buf));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void updateOrientation() {
        orientationText.setText("Azimuth : " + orientation[0] + "\nPitch : " + orientation[1] + "\nRoll : " + orientation[2]);

    }

    @Override
    protected void onResume() {
        super.onResume();
    //    sensorManager.registerListener(sensorEventListener, sensorGravity, SensorManager.SENSOR_DELAY_GAME);
    //    sensorManager.registerListener(sensorEventListener, sensorMagnet, SensorManager.SENSOR_DELAY_GAME);
        sensorManager.registerListener(sensorEventListener, sensorRotVect, SensorManager.SENSOR_DELAY_GAME);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(sensorEventListener);
    }
}
