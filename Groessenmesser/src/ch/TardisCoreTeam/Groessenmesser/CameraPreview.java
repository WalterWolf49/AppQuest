package ch.TardisCoreTeam.Groessenmesser;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;

import java.io.IOException;

/**
 * A basic Camera preview class
 */
public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "CameraPreview";

    private SurfaceHolder mHolder;
    private Camera mCamera;
    private int viewWidth;
    private int viewHeight;

    public CameraPreview(Context context, Camera camera) {
        super(context);

        // If this isn't set, the onDraw method is not called
        setWillNotDraw(false);

        mCamera = camera;

        // Install a SurfaceHolder.Callback so we get notified when the
        // underlying surface is created and destroyed.
        mHolder = getHolder();
        mHolder.addCallback(this);
        // deprecated setting, but required on Android versions prior to 3.0
        mHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);

        // Get Cameras parameters and set the focus mode. Set the modified parameters as the cameras parameters again.
        Camera.Parameters cameraParameters = mCamera.getParameters();
        cameraParameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        mCamera.setParameters(cameraParameters);

    }

    public void surfaceCreated(SurfaceHolder holder) {
        // The Surface has been created, now tell the camera where to draw the preview.
        try {
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        // empty. Take care of releasing the Camera preview in your activity.
        if (mCamera != null) {
            mCamera.release();
        }
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int w, int h) {
        // If your preview can change or rotate, take care of those events here.
        // Make sure to stop the preview before resizing or reformatting it.
        setCameraDisplayOrientation(mCamera);

        if (mHolder.getSurface() == null) {
            // preview surface does not exist
            return;
        }

        // stop preview before making changes
        try {
            mCamera.stopPreview();
        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
        }

        // set preview size and make any resize, rotate or
        // reformatting changes here

        // start preview with new settings
        try {
            mCamera.setPreviewDisplay(mHolder);
            mCamera.startPreview();
        } catch (Exception e) {
            Log.d(TAG, "Error starting camera preview: " + e.getMessage());
        }
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Create paint object
        Paint paintObject = new Paint();
        paintObject.setColor(getResources().getColor(android.R.color.holo_green_light));
        paintObject.setStrokeWidth(10); // Line thickness


        //have to go to parent to get dimensions
        View parent = (View) this.getParent();
        View view = parent.findViewById(R.id.camera_preview);
        viewHeight = view.getHeight();
        viewWidth = view.getWidth();

        // Draw line with paint object (leftStartingPoint, heightStartingPoint, rightEndingPoint, heightEndingPoint, Paint)
        canvas.drawLine(0, viewHeight / 2, viewWidth, viewHeight / 2, paintObject);
    }

    /**
     * Calling this method makes the camera image show in the same orientation as the display.
     * NOTE: This method is not allowed to be called during preview.
     *
     * @param camera
     */
    @SuppressLint("NewApi")
    private void setCameraDisplayOrientation(Camera camera) {
        int cameraId = findBackFacingCameraID();
        int rotation = ((WindowManager) getContext().getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (Build.VERSION.SDK_INT > 8) {
            Camera.CameraInfo info =
                    new Camera.CameraInfo();
            Camera.getCameraInfo(cameraId, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                result = (info.orientation + degrees) % 360;
                result = (360 - result) % 360;  // compensate the mirror
            } else {  // back-facing
                result = (info.orientation - degrees + 360) % 360;
            }
        } else {
            // on API 8 and lower devices
            if (getContext().getResources().getConfiguration().orientation != Configuration.ORIENTATION_LANDSCAPE) {
                result = 90;
            } else {
                result = 0;
            }
        }
        try {
            camera.setDisplayOrientation(result);
        } catch (Exception e) {
            // may fail on old OS versions. ignore it.
            e.printStackTrace();
        }
    }

    private int findBackFacingCameraID() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                Log.d(TAG, "BackFacing Camera found");
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }
}
