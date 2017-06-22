package me.webhop.sawanet.widgetofprovidence;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.Toast;

import java.io.InputStream;
import java.util.Random;

import static android.content.ContentValues.TAG;
import static android.support.v4.app.ActivityCompat.startActivityForResult;

/**
 * Created by sawa on 6/22/17.
 */


/**
 * Implementation of App Widget functionality.
 */
public class Widget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
            // Widget provider init
            for (int appWidgetId : appWidgetIds) {
                Log.d(TAG, "onUpdate called, widget id: " + appWidgetId);
                Intent intent = new Intent(context, ImagePicker.class);
                intent.putExtra("appWidgetId", appWidgetId); // pass widget id (home screen can have many)

                // Pending intent is a intent that will not execute at once, but interact with onclick to the widget
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
                views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
            try {
                // Get back the selected image's uri and clicked widget's id
                int widgetId = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
                Uri imgUri = Uri.parse(intent.getExtras().getString("uri"));

                // Create a new remote view, change the image of it then update to the widget
                RemoteViews control = new RemoteViews(context.getPackageName(), R.layout.widget);

                // Get uri's image and render to bitmap (idk why it;s not work by using setImageViewUri()
                Bitmap raw_bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imgUri);
                Bitmap bitmap = this.scaleBitmap(raw_bitmap, raw_bitmap.getWidth()/2, raw_bitmap.getHeight()/2);

                // Update to the widget
                control.setImageViewBitmap(R.id.widget_image, bitmap);
                AppWidgetManager.getInstance(context).updateAppWidget(widgetId, control);

            }catch (Exception ex){
               ex.printStackTrace();
            }
        }else{
            Log.d(TAG, "[Receive] Action is not Update (Move action? etc.)");
        }
        super.onReceive(context, intent);
    }

    public Bitmap scaleBitmap(Bitmap bitmap, int wantedWidth, int wantedHeight) {
        Bitmap output = Bitmap.createBitmap(wantedWidth, wantedHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);
        // Using matrix as parameter
        Matrix m = new Matrix();
        m.setScale((float) wantedWidth / bitmap.getWidth(), (float) wantedHeight / bitmap.getHeight());
        // Redraw bitmap using canvas
        canvas.drawBitmap(bitmap, m, new Paint(6));
        return output;
    }
}
