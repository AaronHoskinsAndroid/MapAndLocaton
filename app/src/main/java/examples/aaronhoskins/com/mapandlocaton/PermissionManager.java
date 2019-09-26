package examples.aaronhoskins.com.mapandlocaton;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionManager {
    public static final int PERMISSION_INDEX_ID = 666;
    IPermissionManager manager;
    Context context;

    public PermissionManager(Context context) {
        this.context = context;
        this.manager = (IPermissionManager)context;
    }

    public void checkForPermission(){
        if (ContextCompat.checkSelfPermission(context,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            // Permission is not granted
            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity)context,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

            } else {
                // No explanation needed; request the permission
                requestPermission();
            }
        } else {
            manager.onPermissionResult(true);
        }
    }

    public void requestPermission() {
        ActivityCompat.requestPermissions(
                (Activity)context,
                new String[] {Manifest.permission.ACCESS_FINE_LOCATION},
                PERMISSION_INDEX_ID);
    }

    public void permissionResult(int requestCode,
                                 String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case PERMISSION_INDEX_ID: {
                // If request is cancelled, the result arrays are empty.
                manager.onPermissionResult(grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED);
            }

        }

    }

    public interface IPermissionManager{
        void onPermissionResult(boolean isGranted);
    }
}
