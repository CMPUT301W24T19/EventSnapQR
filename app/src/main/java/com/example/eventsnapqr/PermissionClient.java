package com.example.eventsnapqr;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.PermissionChecker;

public class PermissionClient {
    private static PermissionClient instance = null;
    private Context context;
    private PermissionClient(){

    }
    public static PermissionClient getInstance(Context context){
        if(instance == null){
            instance = new PermissionClient();
        }
        instance.init(context);
        return instance;
    }
    private void init(Context context){
        this.context = context;
    }
    boolean checkPermission(String[] permissions){
        for(String permission: permissions){
            if(ContextCompat.checkSelfPermission(context,permission) == PermissionChecker.PERMISSION_DENIED){
                return false;
            }
        }
        return true;
    }
    void askPermissions(Activity activity, String[] permissions, int requestCode){
        ActivityCompat.requestPermissions(activity,permissions,requestCode);
    }
    boolean permissionResult(Activity activity, String[] permissions, int[] grantedResults, int requestCode){
        boolean allPermissionsGranted = true;
        if(grantedResults.length > 0){
            for(int i = 0; i<grantedResults.length; i++){
                if(grantedResults[i] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(activity, "Permission granted", Toast.LENGTH_LONG).show();
                }else{
                    allPermissionsGranted = false;
                    Toast.makeText(activity, "Permission denied", Toast.LENGTH_LONG).show();
                    // ask for permission
                    permissionRationale(activity, requestCode,permissions,permissions[i]);
                    break;

                }
            }
        }
        else{
            allPermissionsGranted = false;
        }
        return allPermissionsGranted;
    }
    private void permissionRationale(Activity activity, int requestCode, String[] permissions, String deniedPermission){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if(ActivityCompat.shouldShowRequestPermissionRationale(activity, deniedPermission)){
                showMessageOKCancel("Please allow access to the permissions", new DialogInterface.OnClickListener(){

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
                            askPermissions(activity,permissions,requestCode);
                        }
                    }
                });

            }
        }
    }

    private void showMessageOKCancel(String msg, DialogInterface.OnClickListener onClickListener) {
        new AlertDialog.Builder(context)
                .setMessage(msg)
                .setPositiveButton("Ok", onClickListener)
                .setNegativeButton("Cancel", onClickListener)
                .create()
                .show();
    }


}
