package com.example.ftm_1;

import android.Manifest;
import android.Manifest.permission;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Log;
import android.view.View;

/**
 * This is a simple splash screen (activity) for giving more details on why the user should approve
 * fine location permissions. If they choose to move forward, the permission screen is brought up.
 * Either way (approve or disapprove), this will exit to the MainActivity after they are finished
 * with their final decision.
 */
public class LocationPermissionRequestActivity extends AppCompatActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    private static final String TAG = "LocationPermission";

    /* Id to identify Location permission request. */
    private static final int PERMISSION_REQUEST_FINE_LOCATION = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // If permissions granted, we start the main activity (shut this activity down).
        if (ActivityCompat.checkSelfPermission(this, permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            finish();
        }

        setContentView(R.layout.activity_location_permission_request);
    }

    public void onClickApprovePermissionRequest(View view) {
        Log.d(TAG, "onClickApprovePermissionRequest()");

        // On 23+ (M+) devices, fine location permission not granted. Request permission.
        ActivityCompat.requestPermissions(
                this,
                new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_REQUEST_FINE_LOCATION);
    }

    public void onClickDenyPermissionRequest(View view) {
        Log.d(TAG, "onClickDenyPermissionRequest()");
        finish();
    }

    /*
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {

        String permissionResult =
                "Request code: "
                        + requestCode
                        + ", Permissions: "
                        + permissions
                        + ", Results: "
                        + grantResults;
        Log.d(TAG, "onRequestPermissionsResult(): " + permissionResult);

        if (requestCode == PERMISSION_REQUEST_FINE_LOCATION) {
            // Close activity regardless of user's decision (decision picked up in main activity).
            finish();
        }
    }
}