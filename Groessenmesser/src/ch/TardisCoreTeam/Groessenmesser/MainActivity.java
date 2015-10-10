package ch.TardisCoreTeam.Groessenmesser;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {
	//Request Codes
	private static final int SCAN_QR_CODE_REQUEST_CODE = 0;
	public final int CAMERA_RESULT_CODE = 1;

	Double mHeight;

	private EditText mAlphaAngleField;
	private EditText mBetaAngleField;
	private EditText mDistanceField;
	private TextView mHeightField;

	private Button mLaunchCameraButton;
	private Button mCalculateButton;

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		// Save the values you don't want to loose on orientation change
		if(mAlphaAngleField.getText().toString().length() > 0 && mBetaAngleField.getText().toString().length() > 0) {
			outState.putString("AngleAlpha", mAlphaAngleField.getText().toString());
			outState.putString("AngleBeta", mBetaAngleField.getText().toString());

		}
		if (mDistanceField.getText().toString().length() > 0) {
			outState.putString("Distance", mDistanceField.getText().toString());
		}
		if (mHeightField.getText().toString().length() > 0) {
			outState.putString("Height", mHeightField.getText().toString());
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		initializeUIElements();

		// If savedInstanceState != null, get the saved values back and set them to the textViews
		if(savedInstanceState != null){
			mAlphaAngleField.setText(savedInstanceState.getString("AngleAlpha", ""));
			mBetaAngleField.setText(savedInstanceState.getString("AngleBeta", ""));
			mDistanceField.setText(savedInstanceState.getString("Distance", ""));
			mHeightField.setText(savedInstanceState.getString("Height", ""));
		}
	}

	private void initializeUIElements() {
		mLaunchCameraButton = (Button) findViewById(R.id.launchCameraActivity);
		mLaunchCameraButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Start CameraActivity via Intent
				Intent startCameraIntent = new Intent(MainActivity.this, CameraActivity.class);
				startActivityForResult(startCameraIntent, CAMERA_RESULT_CODE);
			}
		});

		mCalculateButton = (Button) findViewById(R.id.resultButton);
		mCalculateButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				calculateHeight();
			}
		});

		mAlphaAngleField = (EditText) findViewById(R.id.alpha);
		mBetaAngleField = (EditText) findViewById(R.id.beta);
		mDistanceField = (EditText) findViewById(R.id.distance);
		mHeightField = (TextView) findViewById(R.id.height);
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

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == CAMERA_RESULT_CODE) {
			if (resultCode == Activity.RESULT_OK) {
				// Extract angles from intent
				Double alphaAngle = data.getDoubleExtra("capturedAngle0", 0);
				Double betaAngle = data.getDoubleExtra("capturedAngle1", 0)- alphaAngle;

				mAlphaAngleField.setText(AppUtils.formatDouble(alphaAngle));
				mBetaAngleField.setText(AppUtils.formatDouble(betaAngle));

				mAlphaAngleField.setText(mAlphaAngleField.getText().toString().replaceAll(",", "."));
				mBetaAngleField.setText(mBetaAngleField.getText().toString().replaceAll(",", "."));
			}
		}

		//LOGBUCH
		if (requestCode == SCAN_QR_CODE_REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				String QrMessage = data.getStringExtra("SCAN_RESULT");
				log(QrMessage);
			}
		}
	}

	private void calculateHeight() { // may have a calculation-error
		// get the distance from EditText-bar
		double distance;
		Double angleAlpha= null;
		Double angleBeta= null;

		try {
			angleAlpha = Double.parseDouble(mAlphaAngleField.getText().toString());
		} catch (NumberFormatException e) {
			Toast.makeText(getApplicationContext(), "You need to set an alpha angle!", Toast.LENGTH_SHORT).show();
		}
		try {
			angleBeta = Double.parseDouble(mBetaAngleField.getText().toString());
		} catch (NumberFormatException e) {
			Toast.makeText(getApplicationContext(), "You need to set a beta angle!", Toast.LENGTH_SHORT).show();
		}
		try {
			distance = Double.parseDouble(mDistanceField.getText().toString());
		} catch (NumberFormatException e) {
			Toast.makeText(getApplicationContext(), "You need to set a distance!", Toast.LENGTH_SHORT).show();
			distance = 0d;
		}

		if (angleBeta != null && angleAlpha != null && distance != 0d) {

			double angleTop;
			double angleBottom;

            /*
            if (angleAlpha > 90d && angleBeta > 90d) {
                angleTop = angleBeta - 90;
                angleBottom = angleAlpha - 90;
            } else if (angleAlpha < 90d && angleBeta < 90d) {
                angleTop = 90 - angleBeta;
                angleBottom = 90 - angleAlpha;
            } else {
                angleTop = angleBeta + angleAlpha - 90;
                angleBottom = 90 - angleAlpha;
            }
            */
			angleTop = angleBeta + angleAlpha - 90;

			if(angleAlpha + angleBeta < 90) {
				//   angleTop = Math.abs(angleTop);
				angleBottom = 90 - angleAlpha;
			}
			else if(angleAlpha > 90) {
				angleBottom = angleAlpha - 90;
				angleBottom = angleBottom * -1;
			}
			else {
				angleBottom = 90 - angleAlpha;
			}

			System.out.println("angleTop: " + angleTop + " angleBottom: " + angleBottom);

			//double radBottom = mAngleBeta * Math.PI/180;
			//double radTop = mAngleAlpha * Math.PI/180;
			double radTop = Math.toRadians(angleTop);
			double radBottom = Math.toRadians(angleBottom);

			double y1 = distance * Math.tan(radTop);
			double y2 = distance * Math.tan(radBottom);

			System.out.println("y1: " + y1 + " y2: " + y2);

			// round to 2 decimal
            /*
            if (angleAlpha > 90d && angleBeta + angleAlpha > 90d) {
                mHeight = y1 - y2;
            } else if (angleAlpha < 90d && angleBeta + angleAlpha < 90d) {
                mHeight = y2 - y1;
            } else {
                mHeight = y1 + y2;
            }
            */

			mHeight = y1 + y2;

			mHeightField.setText(AppUtils.formatDouble(mHeight));
			Log.w("RESULT", "HEIGHT IS " + mHeight);
		} else {
			Log.w("Calulation: ", distance + "");
		}
	}


	//LOGBUCH

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuItem menuItem = menu.add("Log");
		menuItem.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {

			@Override
			public boolean onMenuItemClick(MenuItem item) {
				if (mHeight != null) {
					Intent intent = new Intent("com.google.zxing.client.android.SCAN");
					intent.putExtra("SCAN_MODE", "QR_CODE_MODE");
					startActivityForResult(intent, SCAN_QR_CODE_REQUEST_CODE);
					return false;
				} else {
					Toast.makeText(getApplicationContext(), "Please enter a height!", Toast.LENGTH_LONG).show();
					return true;
				}
			}
		});

		return super.onCreateOptionsMenu(menu);
	}


	private void log(String qrCode) {

		Intent intent = new Intent("ch.appquest.intent.LOG");
		if (getPackageManager().queryIntentActivities(intent, 0x10000).isEmpty()) {
			Toast.makeText(this, "Logbook App not Installed", Toast.LENGTH_LONG).show();
			return;
		} else {
			intent.putExtra("ch.appquest.logmessage", (new StringBuilder()).append("{  \"task\": \"Groessenmesser\"," +
					"  \"object\": \"").append(qrCode).append("\",").append("\"height\": \"")
					.append(mHeight).append("\"").append("}").toString());
			startActivity(intent);
			return;
		}
	}
}


