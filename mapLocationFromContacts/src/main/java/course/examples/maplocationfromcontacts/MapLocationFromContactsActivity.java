/* 
 * Must have Google Maps application installed
 */

package course.examples.maplocationfromcontacts;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.widget.Button;

//Several Activity lifecycle methods are instrumented to emit LogCat output
//so you can follow this class' lifecycle

public class MapLocationFromContactsActivity extends AppCompatActivity {

	// These variables are shorthand aliases for data items in Contacts-related database tables 
	private static final String DATA_MIMETYPE = ContactsContract.Data.MIMETYPE;
	private static final Uri DATA_CONTENT_URI = ContactsContract.Data.CONTENT_URI;
	private static final String DATA_CONTACT_ID = ContactsContract.Data.CONTACT_ID;

	private static final String CONTACTS_ID = ContactsContract.Contacts._ID;
	private static final Uri CONTACTS_CONTENT_URI = ContactsContract.Contacts.CONTENT_URI;

	private static final String STRUCTURED_POSTAL_CONTENT_ITEM_TYPE = ContactsContract.CommonDataKinds.StructuredPostal.CONTENT_ITEM_TYPE;
	private static final String STRUCTURED_POSTAL_FORMATTED_ADDRESS = ContactsContract.CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS;

	private static final int PICK_CONTACT_REQUEST = 0;
	private static final int MY_PERMISSIONS_REQUEST_READ_CONTACTS = 1;
	static String TAG = "MapLocation";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		if (ContextCompat.checkSelfPermission(MapLocationFromContactsActivity.this, Manifest.permission.READ_CONTACTS)
				!= PackageManager.PERMISSION_GRANTED) {

			ActivityCompat.requestPermissions(MapLocationFromContactsActivity.this,new String[]{Manifest.permission.READ_CONTACTS},
					MY_PERMISSIONS_REQUEST_READ_CONTACTS);
		}

		final Button button = (Button) findViewById(R.id.mapButton);
		button.setOnClickListener(new Button.OnClickListener() {

			// Called when user clicks the Show Map button
			@Override
			public void onClick(View v) {
				try {
					// Create Intent object for picking data from Contacts database
					Intent intent = new Intent(Intent.ACTION_PICK,
							CONTACTS_CONTENT_URI);
					
					// Use intent to start Contacts application
					// Variable PICK_CONTACT_REQUEST identifies this operation 
					startActivityForResult(intent, PICK_CONTACT_REQUEST);
					
				} catch (Exception e) {
					// Log any error messages to LogCat using Log.e()
					Log.e(TAG, e.toString());
				}
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		// Ensure that this call is the result of a successful PICK_CONTACT_REQUEST request
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == Activity.RESULT_OK
				&& requestCode == PICK_CONTACT_REQUEST) {

			// These details are covered in the lesson on ContentProviders
			ContentResolver cr = getContentResolver();
			Cursor cursor = cr.query(data.getData(), null, null, null, null);

			if (null != cursor && cursor.moveToFirst()) {
				@SuppressLint("Range") String id = cursor
						.getString(cursor.getColumnIndex(CONTACTS_ID));
				String where = DATA_CONTACT_ID + " = ? AND " + DATA_MIMETYPE
						+ " = ?";
				String[] whereParameters = new String[]{id,
						STRUCTURED_POSTAL_CONTENT_ITEM_TYPE};
				Cursor addrCur = cr.query(DATA_CONTENT_URI, null, where,
						whereParameters, null);

				if (null != addrCur && addrCur.moveToFirst()) {
					@SuppressLint("Range") String formattedAddress = addrCur
							.getString(addrCur
									.getColumnIndex(STRUCTURED_POSTAL_FORMATTED_ADDRESS));

					if (null != formattedAddress) {

						// Process text for network transmission
						formattedAddress = formattedAddress.replace(' ', '+');

						// Create Intent object for starting Google Maps application 
						Intent geoIntent = new Intent(
								Intent.ACTION_VIEW,
								Uri.parse("geo:0,0?q=" + formattedAddress));

						// Use the Intent to start Google Maps application using Activity.startActivity()
						startActivity(geoIntent);
					}
				}
				if (null != addrCur)
					addrCur.close();
			}
			if (null != cursor)
				cursor.close();
		}
	}

	@Override
	protected void onRestart() {
		Log.i(TAG, "The activity is about to be restarted.");
		super.onRestart();
	}

	@Override
	protected void onStart() {
		super.onStart();
		Log.i(TAG, "The activity is about to become visible.");
	}

	@Override
	protected void onResume() {
		super.onResume();
		Log.i(TAG, "The activity has become visible (it is now \"resumed\")");
	}

	@Override
	protected void onPause() {
		super.onPause();
		Log.i(TAG,
				"Another activity is taking focus (this activity is about to be \"paused\")");
	}

	@Override
	protected void onStop() {
		super.onStop();
		Log.i(TAG, "The activity is no longer visible (it is now \"stopped\")");
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		Log.i(TAG, "The activity is about to be destroyed.");
	}

}
