package com.example.dlhwidget;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.w3c.dom.Text;

import java.io.IOException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    TextView text1, text2, lastRefreshed;
    ProgressDialog progressDialog;
    Button btnRefresh;
    ImageView imgView;
    int currentDegree = 0;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        text1 = findViewById(R.id.text1);
        text2 = findViewById(R.id.text2);
        lastRefreshed = findViewById(R.id.lastRefreshed);
        btnRefresh = findViewById(R.id.btnRefresh);
        imgView = findViewById(R.id.imageView);

        btnRefresh.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                new Content().execute();
            }
        });

        //update wind info
        new Content().execute();


    }

//    class btnClickListener{
//        if (Content!= null && Content.getStatus() == Content.Status.RUNNING) {
//            Content.cancel(true);
//        }
//        Content.execute();
//    }


    class Content extends AsyncTask<Void, Void, Void> {
        String url = "https://www.dlhweather.com/current-details/";
        String windSpd, gustSpd, condAsOf;
        int windDir;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = new ProgressDialog(MainActivity.this);
            progressDialog.show();
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                //Connect to the website
                Document document = Jsoup.connect(url).get();

                Elements table = document.select("table");
                Elements rows = table.select("tr");

                for (int i = 0; i < rows.size(); i++) { //first row is the col names so skip it.
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

            text1.setText(windSpd.split(" \\(")[0]);
            text2.setText(gustSpd.split(" \\(")[0]);
            lastRefreshed.setText(condAsOf);
            progressDialog.dismiss();

            //get bitmap
            Bitmap bmp = imageView2Bitmap(imgView);


//            Log.e("BITMAP", "got old bitmap");
//            Matrix matrix = new Matrix();
//            //matrix.postScale(scaleWidth, scaleHeight);
//            matrix.postRotate(windDir);
//            Bitmap newBmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
//
//            Log.e("BITMAP", "created");
//            imgView.setImageBitmap(newBmp);
//            Log.e("BITMAP", "new image set");


            // create a rotation animation (reverse turn degree degrees)
            RotateAnimation ra = new RotateAnimation(
                    currentDegree,
                    windDir,
                    Animation.RELATIVE_TO_SELF, 0.5f,
                    Animation.RELATIVE_TO_SELF,
                    0.5f);

            // how long the animation will take place
            ra.setDuration(210);

            // set the animation after the end of the reservation status
            ra.setFillAfter(true);

            // Start the animation
            imgView.startAnimation(ra);
            currentDegree = windDir;



            Toast.makeText(getApplicationContext(),"Wind direction: " + windDir, Toast.LENGTH_SHORT).show();

            //return [windSpd.split(" \\(")[0])
        }
    }

    private Bitmap imageView2Bitmap(ImageView view){
        Bitmap bitmap = ((BitmapDrawable)view.getDrawable()).getBitmap();
        return bitmap;
    }


}



