package com.example.dlhwidget;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.widget.RemoteViews;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;

public class backgroundUpdater extends AsyncTask<Void, Void, Void>
{
    String url = "https://www.dlhweather.com/current-details/";
    String windSpd, gustSpd;
    Context context;
    Intent nIntentUpdate;
    RemoteViews view;

    public backgroundUpdater(Context context, Intent intent, RemoteViews views) {
        this.context = context;
        this.nIntentUpdate = intent;
        this.view = views;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
//            progressDialog = new ProgressDialog(MainActivity.this);
//            progressDialog.show();
    }

    @Override
    protected Void doInBackground(Void... voids) {
        try {
            //Connect to the website
            Document document = Jsoup.connect(url).get();

            Elements table = document.select("table");
            Elements rows = table.select("tr");

            for (int i = 1; i < rows.size(); i++) { //first row is the col names so skip it.
                Element row = rows.get(i);
                Elements cols = row.select("td");

                if (cols.get(0).text().equals("Wind Speed:")) {
                    windSpd = cols.get(1).text();
                }
                if (cols.get(0).text().equals("Gust Speed:")) {
                    gustSpd = cols.get(1).text();
                }
            }


            //Get the logo source of the website
            //Element img = document.select("img").first();
            // Locate the src attribute
            //String imgSrc = img.absUrl("src");
            // Download image from URL
            //InputStream input = new java.net.URL(imgSrc).openStream();

            //Get the title of the website
            //title = document.title();

        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);

//                windSpd_wid = windSpd.split(" \\(")[0];
//                gustSpd_wid = gustSpd.split(" \\(")[0];



        //RemoteViews view = new RemoteViews(context.getPackageName(), R.layout.dlhwidget);
        view.setTextViewText(R.id.text1, windSpd.split(" \\(")[0]);
        view.setTextViewText(R.id.text1, gustSpd.split(" \\(")[0]);
        ComponentName theWidget = new ComponentName(context, backgroundUpdater.class);
        nIntentUpdate.setAction(AppWidgetManager.ACTION_APPWIDGET_UPDATE);

        AppWidgetManager manager = AppWidgetManager.getInstance(context);
        manager.updateAppWidget(theWidget, view);

        Toast.makeText(context, "Updater working", Toast.LENGTH_SHORT).show();


//            Intent intent = new Intent(context, DLHWidget.class);
//            intent.setAction("android.appwidget.action.APPWIDGET_UPDATE");
//            int ids[] = AppWidgetManager.getInstance(context).getAppWidgetIds(new ComponentName(context, DLHWidget.class));
//            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS,ids);
//            context.sendBroadcast(intent);

    }
}

