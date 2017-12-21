package me.webhop.sawanet.widgetofprovidence;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import static android.content.ContentValues.TAG;

public class ImagePicker extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create a image picker intent
        Intent img_picker;
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            img_picker = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            img_picker.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            img_picker.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
        }else{
            img_picker = new Intent(Intent.ACTION_GET_CONTENT);
        }
        img_picker.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
        img_picker.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        img_picker.setType("image/*");
        startActivityForResult(Intent.createChooser(img_picker, "Select Picture"), 0);
        setContentView(R.layout.activity_image_picker);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        try {
            // get the selected result (img uri)
            Uri uri = data.getData();
            Intent intent = getIntent();
            final int takeFlags = data.getFlags() & Intent.FLAG_GRANT_READ_URI_PERMISSION;
            ContentResolver resolver = getContentResolver();
            assert uri != null;
            resolver.takePersistableUriPermission(uri, takeFlags);
            // get widget id and image uri
            int appWidgetId = intent.getIntExtra("appWidgetId", -1);

            // create intent to pass data back to widget (update)
            Intent widgetIntent = new Intent(this, Widget.class);
            widgetIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            widgetIntent.putExtra("uri", uri.toString());
            Log.d(TAG, uri.toString());
            Log.d(TAG, String.valueOf(appWidgetId));
            // Send bundle back to widget by using broadcast, widget's onReceive() can get it
            sendBroadcast(widgetIntent);
        }catch (NullPointerException ex){
            ex.printStackTrace();
        }
        finish();
    }
}
