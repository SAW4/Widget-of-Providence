package me.webhop.sawanet.widgetofprovidence;

import android.appwidget.AppWidgetManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import static android.content.ContentValues.TAG;

public class ImagePicker extends AppCompatActivity {

    // appWidgetId init: first launch didnt have any image path bind to widget
    private int appWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    // user defined final request code: identify result intent type
    private int REQUEST_CODE = 8993;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create a image picker intent
        Intent img_picker;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            img_picker = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            img_picker.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            img_picker.addCategory(Intent.CATEGORY_OPENABLE);
            img_picker.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        }else{
            img_picker = new Intent(Intent.ACTION_GET_CONTENT);
        }
        img_picker.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        img_picker.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        img_picker.setType("image/*");

        // Request code 8993 -> image picker result intent
        startActivityForResult(Intent.createChooser(img_picker, "Select Picture"), REQUEST_CODE);
        setContentView(R.layout.activity_image_picker);

        Bundle extras = getIntent().getExtras();
        assert extras != null;
        this.appWidgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        Log.v(TAG, "ImagePiceker -> this.appWidgetId: " + appWidgetId);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {

        // if it is not called by image picker, end
        if (requestCode != REQUEST_CODE || resultCode == RESULT_CANCELED) {
            finish();
            return;
        }

        // if everything OKAY, update image
        try {
            // get the selected result (img uri)
            Uri uri = data.getData();
            Intent intent = getIntent();
            assert uri != null;
            // get widget id and image uri
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
            // create intent to pass data back to widget (update)
            Intent widgetIntent = new Intent(this, Widget.class);
            widgetIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            widgetIntent.putExtra("uri", uri.toString());

            // make selected Uri grant persist permission, even reboot
            final int takeFlags = data.getFlags()
                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            ContentResolver resolver = getContentResolver();
            resolver.takePersistableUriPermission(uri, takeFlags);

            Log.v(TAG, "Persist-Permission => " + uri);
            Log.v(TAG, "widgetIntent uri Extra => " + uri );

            // update shared preference, auto load after reboot
            SharedPreferences pref = getSharedPreferences(getPackageName(), Context.MODE_PRIVATE);
            pref.edit().putString(Integer.toString(appWidgetId), uri.toString()).apply();

            // Send bundle back to widget by using broadcast, widget's onReceive() can get it
            sendBroadcast(widgetIntent);
        } catch (NullPointerException ex) {
            ex.printStackTrace();
        }
        finish();
    }
}
