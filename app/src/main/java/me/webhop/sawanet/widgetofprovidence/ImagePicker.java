package me.webhop.sawanet.widgetofprovidence;

import android.app.Activity;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import static android.content.ContentValues.TAG;

public class ImagePicker extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Create a image picker intent
        Intent img_picker = new Intent();
        img_picker.setType("image/*");
        img_picker.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(img_picker, "Select Picture"), 0);
        setContentView(R.layout.activity_image_picker);
    }

    @Override
    public void onActivityResult(final int requestCode, final int resultCode, final Intent data) {
        try {
            // get the selected result (img uri)
            Uri uri = data.getData();
            Intent intent = getIntent();

            // get widget id and image uri
            int appWidgetId = intent.getIntExtra("appWidgetId", -1);

            // create intent to pass data back to widget (update)
            Intent widgetIntent = new Intent(this, Widget.class);
            widgetIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            widgetIntent.putExtra("uri", uri.toString());

            // Send bundle back to widget by using broadcast, widget's onReceive() can get it
            sendBroadcast(widgetIntent);
        }catch (NullPointerException ex){
            ex.printStackTrace();
        }
        finish();
    }
}
