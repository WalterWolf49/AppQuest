package ch.TardisCoreTeam.Groessenmesser;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.hardware.Camera;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;

public class CameraActivity extends Activity implements SensorEventListener, View.OnClickListener {

    private final static String TAG= "CameraActivity";

    private Camera mCamera;
    private CameraPreview mCameraPreview;
    private Intent resultIntent = new Intent();
    private int counter = 0;
    private FrameLayout mPreviewLayout;

    private final float[] magneticFieldData = new float[3];
    private final float[] accelerationData = new float[3];

    private SensorManager sensorManager;
    private Sensor accSensor;
    private Sensor magSensor;


    private double angleBottom;
    private double angleTop;

    double currentRotationValue;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "onCreate called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        final Button capture_angle = (Button) findViewById(R.id.button_capture);
        capture_angle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resultIntent.putExtra("capturedAngle" + counter, currentRotationValue);
                counter++;

                if (counter == 2) {
                    setResult(RESULT_OK, resultIntent);
                    finish();
                }
            }
        });

        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accSensor = sensorManager.getSensorList(Sensor.TYPE_ACCELEROMETER).get(0);
        magSensor = sensorManager.getSensorList(Sensor.TYPE_MAGNETIC_FIELD).get(0);
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart called");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume called");
        mCamera = getCameraInstance();

        // Create our Preview view and set it as the content of our activity.
        mCameraPreview = new CameraPreview(this, mCamera);
        mPreviewLayout = (FrameLayout) findViewById(R.id.camera_preview);
        mPreviewLayout.addView(mCameraPreview);

        sensorManager.registerListener(this, accSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(this, magSensor, SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause called");
        releaseCamera();
        mPreviewLayout.removeView(mCameraPreview);
        mCameraPreview= null;
        sensorManager.unregisterListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i(TAG, "onStop called");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "onRestart called");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG, "onDestroy called");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_camera, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            System.arraycopy(event.values, 0, accelerationData, 0, 3);
        }
        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            System.arraycopy(event.values, 0, magneticFieldData, 0, 3);
        }

        currentRotationValue = getCurrentRotationValue();
        ((Button) findViewById(R.id.button_capture)).setText(AppUtils.formatDouble(currentRotationValue));
    }


    private double getCurrentRotationValue() {
        float[] rotationMatrix = new float[16];
        if (SensorManager.getRotationMatrix(rotationMatrix, null, accelerationData, magneticFieldData)) {
            float[] orientation = new float[4];
            SensorManager.getOrientation(rotationMatrix, orientation);

            /**orientationValue
             * 1 = portrait
             * 2 = landscape
             **/
            int orientationValue = 1;
            if(isLandscape()) {
                orientationValue = 2;
            }
            double tilt = Math.toDegrees(orientation[orientationValue]);
            return Math.abs(tilt);
        }
        return 0;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    // check if the phone is in landscape
    public boolean isLandscape() {
        return getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
    }

    /**
     * A safe way to get an instance of the Camera object.
     */
    public Camera getCameraInstance() {
        Camera c = null;
        try {
            c = Camera.open(Camera.CameraInfo.CAMERA_FACING_BACK); // attempt to get a Camera instance
        } catch (Exception e) {
            Log.e(TAG, "Couldn't initialize BackFacing camera" + e);
        }
        return c; // returns null if camera is unavailable
    }



    private void releaseCamera(){
        if (mCamera != null){
            mCamera.stopPreview();
            mCamera.release(); // release the camera for other applications and to be able to initialize it again after an orientation change
            mCamera = null;
        }
    }

    @Override
    public void onClick(View v) {

    }
}
