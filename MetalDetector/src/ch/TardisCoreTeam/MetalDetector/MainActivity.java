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

public class MainActivity extends Activity implements SensorEventListener {
    private SensorManager mSensorManager;
    private Sensor mMagnet;

    Button saveButton;
    ProgressBar progressBar;
    TextView textView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mMagnet = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);

        saveButton = (Button) findViewById(R.id.savebutton);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        textView = (TextView) findViewById(R.id.textView);

        progressBar.setMax(1474);

    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        float[] mag = event.values;
        double betrag = Math.sqrt(mag[0] * mag[0] + mag[1] * mag[1] + mag[2] * mag[2]);
        textView.setText((int) betrag);
        progressBar.setProgress((int) betrag);
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