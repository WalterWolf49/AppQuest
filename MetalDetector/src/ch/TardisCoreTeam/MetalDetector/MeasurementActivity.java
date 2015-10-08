package ch.TardisCoreTeam.MetalDetector;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MeasurementActivity extends Activity implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mMagnet;

    private final float[] magneticFieldData = new float[3];
    private final float[] accelerationData = new float[3];

    Button reset;          // represents the reset button for the measurement
    TextView degrees;       // displays the the measured degrees.
    double currentRotationValue;    // represents the

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mMagnet = mSensorManager.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);

        reset = (Button) findViewById(R.id.reset);
        degrees = (TextView) findViewById(R.id.degrees);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerationData, 0, 3);
        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magneticFieldData, 0, 3);
        }

        currentRotationValue += getCurrentRotationValue();

        degrees.setTextColor((int) currentRotationValue);
    }

    private double getCurrentRotationValue() {
        float[] rotationMatrix = new float[16];

        if (SensorManager.getRotationMatrix(rotationMatrix, null, accelerationData, magneticFieldData)) {

            float[] orientation = new float[4];
            SensorManager.getOrientation(rotationMatrix, orientation);

            double neigung = Math.toDegrees(orientation[2]);

            return Math.abs(neigung);
        }

        return 0;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mSensorManager.registerListener(this, mMagnet, SensorManager.SENSOR_DELAY_NORMAL);
    }
}