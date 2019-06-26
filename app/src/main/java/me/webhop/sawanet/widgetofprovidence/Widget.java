package me.webhop.sawanet.widgetofprovidence;

import android.app.DownloadManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.util.Log;
import android.widget.RemoteViews;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.AppWidgetTarget;
import com.bumptech.glide.request.transition.Transition;

import java.net.URI;
import java.util.List;

import static android.content.ContentValues.TAG;




/**ex
 * Implementation of App Widget functionality.
 */
public class Widget extends AppWidgetProvider {
    private AppWidgetTarget appWidgetTarget;
    private SharedPreferences pref;
    /**
     * @param bitmap the Bitmap to be scaled
     * @param threshold the maxium dimension (either width or height) of the scaled bitmap
     * @param isNecessaryToKeepOrig is it necessary to keep the original bitmap? If not recycle the original bitmap to prevent memory leak.
     * */
    public static Bitmap getScaledDownBitmap(Bitmap bitmap, int threshold, boolean isNecessaryToKeepOrig){
        int width = bitmap.getWidth();
        int height = bitmap.getHeight();
        int newWidth = width;
        int newHeight = height;

        if(width > height && width > threshold){
            newWidth = threshold;
            newHeight = (int)(height * (float)newWidth/width);
        }

        if(width > height && width <= threshold){
            //the bitmap is already smaller than our required dimension, no need to resize it
            return bitmap;
        }

        if(width < height && height > threshold){
            newHeight = threshold;
            newWidth = (int)(width * (float)newHeight/height);
        }

        if(width < height && height <= threshold){
            //the bitmap is already smaller than our required dimension, no need to resize it
            return bitmap;
        }

        if(width == height && width > threshold){
            newWidth = threshold;
            newHeight = newWidth;
        }

        if(width == height && width <= threshold){
            //the bitmap is already smaller than our required dimension, no need to resize it
            return bitmap;
        }

        return getResizedBitmap(bitmap, newWidth, newHeight, isNecessaryToKeepOrig);
    }

    private static Bitmap getResizedBitmap(Bitmap bm, int newWidth, int newHeight, boolean isNecessaryToKeepOrig) {
        int width = bm.getWidth();
        int height = bm.getHeight();
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // CREATE A MATRIX FOR THE MANIPULATION
        Matrix matrix = new Matrix();
        // RESIZE THE BIT MAP
        matrix.postScale(scaleWidth, scaleHeight);

        // "RECREATE" THE NEW BITMAP
        Bitmap resizedBitmap = Bitmap.createBitmap(bm, 0, 0, width, height, matrix, false);
        if(!isNecessaryToKeepOrig){
            bm.recycle();
        }
        return resizedBitmap;
    }
    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {

        pref = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
        for (int appWidgetId : appWidgetIds) {
            String pref_str = pref.getString(Integer.toString(appWidgetId), null);
            Intent intent = new Intent(context, ImagePicker.class);
            intent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId); // pass widget id (home screen can have many)
            Log.v(TAG, "onUpdate called, widget id: " + appWidgetId + " Bind to onclick event.");
            Log.v(TAG, "onUpdate called, pref_str: " + pref_str );

            // Pending intent is a intent that will not execute at once, but interact with onclick to the widget
            PendingIntent pendingIntent = PendingIntent.getActivity(context, appWidgetId, intent, 0);
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
            views.setOnClickPendingIntent(R.id.widget_layout, pendingIntent);

            // if this appwidget already has image, load it with memorized path in shared preference
            if (pref_str != null){
                // create intent to pass data back to widget (update)
                Intent  widgetIntent = new Intent(context, Widget.class);
                widgetIntent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                widgetIntent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION);
                widgetIntent.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                widgetIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                widgetIntent.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                widgetIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
                widgetIntent.putExtra("uri", pref_str);
                // call onReceive with current param
                context.sendBroadcast(widgetIntent);
            }
            appWidgetManager.updateAppWidget(appWidgetId, views);

        }
    }

    @Override
    public void onReceive(final Context context, Intent intent) {
        super.onReceive(context, intent);
        if (intent.getAction().equals(AppWidgetManager.ACTION_APPWIDGET_UPDATE)) {
            try {
                // Get back the selected image's uri and clicked widget's id
                int widgetId = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
                // If record not find, add this new path
                String receiveData = intent.getExtras().getString("uri");
                int appWidgetId = intent.getExtras().getInt(AppWidgetManager.EXTRA_APPWIDGET_ID);
                pref = context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);

                Log.v(TAG, "onReceive called: Image is picked");
                Log.v(TAG, "onReceive -> receiveData := " + receiveData);
                Log.v(TAG, "onReceive -> appWidgetId := " + appWidgetId);

                if (receiveData != null) {
                    Uri imgUri = Uri.parse(receiveData);
                    // Create a new remote view, change the image of it then update to the widget
                    // each time we need to create a new remote views
                    RemoteViews control = new RemoteViews(context.getPackageName(), R.layout.widget);
                    appWidgetTarget = new AppWidgetTarget(context, R.id.widget_image, control, widgetId) {
                        @Override
                        public void onResourceReady(Bitmap resource, Transition<? super Bitmap> transition) {
                            // compress or resize here
                            int threshold = 1024;
                            resource = getScaledDownBitmap(resource, threshold, false);
                            super.onResourceReady(resource, transition);
                        }
                    };
//                    final int takeFlags = (Intent.FLAG_GRANT_READ_URI_PERMISSION
//                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//                    ContentResolver resolver = context.getContentResolver();
//                    resolver.takePersistableUriPermission(imgUri, takeFlags);

                    // use Glide for drawing bitmap, much more easier method
                    RequestOptions requestOptions = new RequestOptions();
                    requestOptions.diskCacheStrategy(DiskCacheStrategy.NONE);
                    requestOptions.skipMemoryCache(true);
                    requestOptions.fitCenter();
                    Glide.with(context).asBitmap().load(imgUri).apply(requestOptions).into(appWidgetTarget);
                    Log.v(TAG, "imgUri => " + imgUri);
                    AppWidgetManager.getInstance(context).updateAppWidget(widgetId, control);
                }
            }catch (Exception ex){
               ex.printStackTrace();
            }
        }
    }
}
