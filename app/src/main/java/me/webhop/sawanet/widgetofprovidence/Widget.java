package me.webhop.sawanet.widgetofprovidence;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.net.Uri;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.AppWidgetTarget;
import com.bumptech.glide.request.transition.Transition;

import static android.content.ContentValues.TAG;

/**ex
 * Implementation of App Widget functionality.
 */
public class Widget extends AppWidgetProvider {
    private static DBhelper helper;
    private AppWidgetTarget appWidgetTarget;

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        boolean exist = false;
        if (helper == null)
            helper = new DBhelper(context,"widgetOfProvidence.db",null,1);
        for (int appWidgetId : appWidgetIds) {
            Intent intent = new Intent(context, ImagePicker.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra("appWidgetId", appWidgetId); // pass widget id (home screen can have many)
            if (helper.query(appWidgetId)) { // appWidgetId exist, load image
                exist = true;
                String uri = helper.getPath(appWidgetId);
                if (uri != null)
                    intent.putExtra("uri", uri);
                intent.putExtra("exist",true);
            }else{
                helper.addId(appWidgetId); // Add created id to database
            }
            Log.d(TAG, "onUpdate called, widget id: " + appWidgetId + " Bind to onclick event.");
            // Pending intent is a intent that will not execute at once, but interact with onclick to the widget
            PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent, 0);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
            views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);
            if (exist) onReceive(context, intent);
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        super.onReceive(context, intent);
        if (helper == null)
            helper = new DBhelper(context,"widgetOfProvidence.db",null,1);
        if (intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
            try {
                // Get back the selected image's uri and clicked widget's id
                int widgetId = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
                // If record not find, add this new path
                String receiveData = intent.getExtras().getString("uri");
                Log.d(TAG, "onReceive called, unpacking data");
                helper.addPath(widgetId,receiveData);
                if (receiveData != null) {
                    Uri imgUri = Uri.parse(receiveData);
                    // Create a new remote view, change the image of it then update to the widget
                    RemoteViews control = new RemoteViews(context.getPackageName(), R.layout.widget);
                    Log.d(TAG, "[Receive] widget id : " + String.valueOf(widgetId) + " Uri= " + imgUri);
                    appWidgetTarget = new AppWidgetTarget(context, R.id.widget_image, control, widgetId) {
                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            WindowManager wm = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
                            assert wm != null;
                            Display display = wm.getDefaultDisplay();
                            Point size = new Point();
                            display.getSize(size);
                            int width = size.x;
                            int height = size.y;
                            if (resource.getByteCount()>width*height*4*1.5){
                                // compress or resize here
                                resource = scaleBitmap(resource, resource.getWidth()/2, resource.getHeight()/2);
                            }
                            super.onResourceReady(resource, transition);
                        }
                    };
                    Glide.with(context).asBitmap().load(imgUri).into(appWidgetTarget);
                    if (intent.getExtras().getBoolean("exist")){
                        PendingIntent pendingIntent = PendingIntent.getActivity(context, widgetId, intent, 0);
                        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
                        views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);
                    }
                    AppWidgetManager.getInstance(context).updateAppWidget(widgetId, control);
                }
            }catch (Exception ex){
               ex.printStackTrace();
            }
        }
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
