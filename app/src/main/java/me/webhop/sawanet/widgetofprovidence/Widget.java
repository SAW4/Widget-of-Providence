package me.webhop.sawanet.widgetofprovidence;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.RemoteViews;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import static android.content.ContentValues.TAG;

/**
 * Created by sawa on 6/22/17.
 */


/**ex
 * Implementation of App Widget functionality.
 */
public class Widget extends AppWidgetProvider {
    private static DBhelper helper;
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        if (helper == null)
            helper = new DBhelper(context,"widgetOfProvidence.db",null,1);
        for (int appWidgetId : appWidgetIds) {
            // Check database, if records find then add set the image
            if (helper.query(appWidgetId)){ // appWidgetId exist, load image
                Intent widgetIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                widgetIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
//                widgetIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                String uri = helper.getPath(appWidgetId);
                if (uri != null) {
                    widgetIntent.putExtra("uri", uri);
                    // Send bundle back to widget by using broadcast, widget's onReceive() can get it
                    context.sendBroadcast(widgetIntent);
                    // Pending intent is a intent that will not execute at once, but interact with onclick to the widget
                    RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
                    appWidgetManager.updateAppWidget(appWidgetId, views);
                    Log.d(TAG, "update already existed widget.");
                }
            }
            else {
                Intent intent = new Intent(context, ImagePicker.class);
                intent.putExtra("appWidgetId", appWidgetId); // pass widget id (home screen can have many)
                helper.addId(appWidgetId); // Add created id to database
                Log.d(TAG, "onUpdate called, widget id: " + appWidgetId + " Bind to onclick event.");

                // Pending intent is a intent that will not execute at once, but interact with onclick to the widget
                PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent, 0);
                RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
                views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);
                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (helper == null)
            helper = new DBhelper(context,"widgetOfProvidence.db",null,1);
        if (intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
            try {
                // Get back the selected image's uri and clicked widget's id
                int widgetId = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
                // If record not find, add this new path
                String receiveData = intent.getExtras().getString("uri");
                helper.addPath(widgetId,receiveData);
                if (receiveData != null) {
                    Uri imgUri = Uri.parse(receiveData);
                    // Create a new remote view, change the image of it then update to the widget
                    RemoteViews control = new RemoteViews(context.getPackageName(), R.layout.widget);
                    Log.d(TAG, "[Receive] widget id : " + String.valueOf(widgetId) + " Uri= " + imgUri);
                    // Get uri's image and render to bitmap (idk why it;s not work by using setImageViewUri()
                    Bitmap raw_bitmap = MediaStore.Images.Media.getBitmap(context.getContentResolver(), imgUri);
                    WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
                    assert wm != null;
                    Display display = wm.getDefaultDisplay();
                    Point size = new Point();
                    display.getSize(size);
                    int width = size.x;
                    int height = size.y;
                    if (raw_bitmap.getByteCount()>width*height*4*1.5){
                        // compress here
                        raw_bitmap = this.scaleBitmap(raw_bitmap, raw_bitmap.getWidth()/2, raw_bitmap.getHeight()/2);
                    }
//                    Log.d(TAG, "Image size (byte) : " + raw_bitmap.getByteCount());
//                    Log.d(TAG, "Bitmap Byte Limit : " + String.format("%.2f", width*height*4*1.5));
                    control.setImageViewBitmap(R.id.widget_image, raw_bitmap);
                    AppWidgetManager.getInstance(context).updateAppWidget(widgetId, control);
                }
            }catch (Exception ex){
               ex.printStackTrace();
            }
        }else{
//            Log.d(TAG, "[Receive] Action is not Update (Move action? etc.)");
        }
        super.onReceive(context, intent);
    }

    @Override
    public void onEnabled(Context context){
        helper = new DBhelper(context,"widgetOfProvidence.db",null,1);;
        Log.d(TAG, "onEnabled called, SQLite database created : " + helper.getDatabaseName() +  helper.getWritableDatabase().getPath());
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        if(helper == null)
            helper = new DBhelper(context,"widgetOfProvidence.db",null,1);;
        for (int appWidgetId : appWidgetIds) {
            helper.removeWidget(appWidgetId);
        }
    }

    @Override
    public void onDisabled(Context context) {
        super.onDisabled(context);
        helper.dropAllRecords();
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
