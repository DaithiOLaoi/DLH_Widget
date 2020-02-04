package com.example.dlhwidget;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.SystemClock;
import android.util.Log;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.drawable.RoundedBitmapDrawable;
import androidx.core.graphics.drawable.RoundedBitmapDrawableFactory;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.Random;
import java.util.concurrent.ExecutionException;

import static java.lang.Math.round;
import static java.lang.Math.sin;

/**
 * Implementation of App Widget functionality.
 */
public class DLHWidget extends AppWidgetProvider {

    static String windSpd_wid = "noWind";
    static String gustSpd_wid = "noWind";
    static String condAsOf_wid = "noTime";
    static SharedPreferences sharedpreferences;
    static int windDir_wid = 0;
    static Bitmap bmp_wid;
    //static int numRuns;


    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager,
                                int appWidgetId) throws ExecutionException, InterruptedException {


        String wind2 = windSpd_wid;
        String gust2 = gustSpd_wid;
        //timestamp = System.currentTimeMillis();

            //get wind info
            new Content_wid(context).execute().get();

            //--------------------------------------------------------------
            //rotate image and create bitmap
            //TODO: bitmap scaling for some reason

//            RoundedBitmapDrawable roundedBitmapDrawable= RoundedBitmapDrawableFactory.create(context.getResources(), bmp_wid);
//            //setting radius
//            roundedBitmapDrawable.setCornerRadius(50.0f);
//            roundedBitmapDrawable.setAntiAlias(true);


            Matrix matrix = new Matrix();
            //matrix.postScale(1, 1);
            Random r = new Random();
            int test = r.nextInt(360);
            matrix.postRotate(test);
            Bitmap bmpNew = null;
            Bitmap bmpNew2 = null;
            if(bmp_wid != null){
                //scale bitmap as otherwise it's too large and falls outside widget
                int temp = (int) (round(1 - sin(test)));
                int widthHeight = 150 *  (1 + temp);
                bmp_wid = Bitmap.createScaledBitmap(bmp_wid, 150,150,true);
                bmpNew = Bitmap.createBitmap(bmp_wid, 0, 0,  150, 150, matrix, true);

                if(bmpNew2 == null){
                    bmpNew2 = ThumbnailUtils.extractThumbnail(bmpNew, 150, 150);
                }

                Log.e("BITMAP", "bitmap is not null");
            }else{
                Log.e("BITMAP", "bitmap is null");
            }
            //--------------------------------------------------------------


            // Construct the RemoteViews object
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.dlhwidget);
            views.setTextViewText(R.id.txtWindSpd, wind2);
            views.setTextViewText(R.id.txtGustSpd, gust2);
            //if(bmp_wid != null){
                views.setImageViewBitmap(R.id.imageView2, bmpNew);
            //}


//        AppWidgetManager manager=AppWidgetManager.getInstance(context);
//        manager.updateAppWidget(appWidgetId,views);


            //Create an Intent with the AppWidgetManager.ACTION_APPWIDGET_UPDATE action//

            Intent intentUpdate = new Intent(context, DLHWidget.class);
            intentUpdate.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);


//Update the current widget instance only, by creating an array that contains the widget’s unique ID//

            int[] idArray = new int[]{appWidgetId};
            intentUpdate.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, idArray);


//Wrap the intent as a PendingIntent, using PendingIntent.getBroadcast()//

            PendingIntent pendingUpdate = PendingIntent.getBroadcast(context, appWidgetId, intentUpdate,
                    PendingIntent.FLAG_UPDATE_CURRENT);

//Send the pending intent in response to the user tapping the ‘Update’ TextView//


            //--------------------------------------------------------------
//        backgroundUpdater bgUpdate = new backgroundUpdater(context, intentUpdate, views);
//        bgUpdate.execute().get();

            //--------------------------------------------------------------


            views.setOnClickPendingIntent(R.id.txtWindSpd, pendingUpdate);
//        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://code.tutsplus.com/"));
//        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
//        views.setOnClickPendingIntent(R.id.appwidget_text2, pendingIntent);
            // Instruct the widget manager to update the widget
            appWidgetManager.updateAppWidget(appWidgetId, views);


    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // There may be multiple widgets active, so update all of them

        //calling getSharedPreferences method returns shared preferences instance containing values of preferences
        sharedpreferences = context.getApplicationContext().getSharedPreferences("DLHPref", Context.MODE_PRIVATE);
        int numRuns = sharedpreferences.getInt("NUMRUNS", 0);

        SharedPreferences.Editor editor = sharedpreferences.edit();
        //refresh on loop
        if(numRuns < 5 ) {
            numRuns++;
            editor.putInt("NUMRUNS", numRuns);  //int incremented and stored to track loop
            editor.commit();

            for (int appWidgetId : appWidgetIds) {
                try {
                    updateAppWidget(context, appWidgetManager, appWidgetId);
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }else{
            editor.putInt("NUMRUNS", 0);
            editor.commit();
            Toast.makeText(context, "DLHWidget updated", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onEnabled(Context context) {
        // Enter relevant functionality for when the first widget is created
    }

    @Override
    public void onDisabled(Context context) {
        // Enter relevant functionality for when the last widget is disabled
    }



    static class Content_wid extends AsyncTask<Void, Void, Void> {
        String url = "https://www.dlhweather.com/current-details/";
        String windSpd, gustSpd, condAsOf;
        Context ctx;
        int windDir;

        public Content_wid(Context context){
            this.ctx = context;
        }


        @Override
        protected Void doInBackground(Void... voids) {
            try {
                //Connect to the website
                Document document = Jsoup.connect(url).get();

                Elements table = document.select("table");
                Elements rows = table.select("tr");

                for (int i = 0; i < rows.size(); i++) {
                    Element row = rows.get(i);
                    Elements cols = row.select("td");

                    if (cols.get(0).text().equals("Wind Speed:")) {
                        windSpd = cols.get(1).text();
                    }
                    if (cols.get(0).text().equals("Gust Speed:")) {
                        gustSpd = cols.get(1).text();
                    }
                    if (cols.get(0).text().equals("Conditions As Of:")) {
                        condAsOf = cols.get(1).text();
                        Log.d("DLHWidget: ", condAsOf);
                    }
                    if (cols.get(0).text().equals("Wind Direction:")) {
                        windDir = Integer.parseInt(cols.get(1).text().split("\\(")[1].split("º\\)")[0]);
                        Log.d("DLHWidget: windDir ", Integer.toString(windDir));
                    }
                }


            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            bmp_wid = BitmapFactory.decodeResource(ctx.getResources(), R.drawable.blue_pointer);

            windSpd_wid = windSpd.split(" kts")[0];
            gustSpd_wid = gustSpd.split(" kts")[0];
            condAsOf_wid = condAsOf.split("2019")[1];
            windDir_wid = windDir;


            
            //https://stackoverflow.com/questions/3455123/programmatically-update-widget-from-activity-service-receiver
            Intent intent = new Intent(ctx, DLHWidget.class);
            intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
            int ids[] = AppWidgetManager.getInstance(ctx).getAppWidgetIds(new ComponentName(ctx, DLHWidget.class));
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,ids);
            ctx.sendBroadcast(intent);


        }
    }
}

