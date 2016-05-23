package edu.tamu.csce315team11.weatherapp;

import android.content.ComponentCallbacks;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View.OnClickListener;
import android.webkit.WebView;
import android.webkit.WebSettings;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import edu.tamu.csce315team11.weatherapp.R;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.File;
import java.io.FileOutputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class MainActivity extends AppCompatActivity {
    MediaPlayer soundEffect;

    int mute = 1;

    private class Forecast {

        public int zipCode;
        public String city;
        public String state;
        public String currentTemp;
        public String highTemp;
        public String lowTemp;
        public String wind;
        public String weekday;
        public int code;

        public List<Forecast> future;

        public void setZip (int zip) { this.zipCode = zip; }
        public void setCity (String city) { this.city = city; }
        public void setState (String state) { this.state = state; }
        public void setCurrentTemp (String currentTemp) { this.currentTemp = currentTemp; }
        public void setHighTemp (String highTemp) { this.highTemp = highTemp; }
        public void setLowTemp (String lowTemp) { this.lowTemp = lowTemp; }
        public void setWind (String wind) { this.wind = wind; }
        public void setWeekday (String weekday) { this.weekday = weekday; }
        public void setCode (int code) { this.code = code; }

        public Forecast() {
            this.zipCode = -1;
            this.city = "Error";
            this.state = "";
            this.currentTemp = "";
            this.highTemp = "";
            this.lowTemp = "";
            this.code = 1;
            this.wind = "";
            this.weekday = "";

            future = new ArrayList<>();
        }

        public Forecast(int zip, String city, String state, String currentTemp, String highTemp, String lowTemp, int code, String wind, String weekday) {
            this.zipCode = zip;
            this.city = city;
            this.state = state;
            this.currentTemp = currentTemp;
            this.highTemp = highTemp;
            this.lowTemp = lowTemp;
            this.code = code;
            this.wind = wind;
            this.weekday = weekday;

            future = new ArrayList<>();
        }
    }

    private class FetchWeatherJob extends AsyncTask<String, Void, Forecast> {

        private Context context;

        public FetchWeatherJob(Context c) {
            context = c;
        }

        public String getStringFromDocument(Document doc)
        {
            try
            {
                DOMSource domSource = new DOMSource(doc);
                StringWriter writer = new StringWriter();
                StreamResult result = new StreamResult(writer);
                TransformerFactory tf = TransformerFactory.newInstance();
                Transformer transformer = tf.newTransformer();
                transformer.transform(domSource, result);
                return writer.toString();
            }
            catch(TransformerException ex)
            {
                ex.printStackTrace();
                return null;
            }
        }

        @Override
        protected Forecast doInBackground(String... params) {
            try {
                String weatherURL = "https://query.yahooapis.com/v1/public/yql?q=select%20*%20from%20weather.forecast%20where%20woeid%20in%20(select%20woeid%20from%20geo.places(1)%20where%20text%3D%22"
                        + params[0]
                        + "%22)&format=xml&env=store%3A%2F%2Fdatatables.org%2Falltableswithkeys";
                URL url = new URL(weatherURL);
                DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                DocumentBuilder db = dbf.newDocumentBuilder();
                Document doc = db.parse(new InputSource(url.openStream()));
                doc.getDocumentElement().normalize();

                // Find location info
                Node location = doc.getElementsByTagName("yweather:location").item(0);
                String locationCity = location.getAttributes().getNamedItem("city").getNodeValue();
                String locationState = location.getAttributes().getNamedItem("region").getNodeValue();

                // Find current conditions
                Node currentCondition = doc.getElementsByTagName("yweather:condition").item(0);
                String currentTemp = currentCondition.getAttributes().getNamedItem("temp").getNodeValue();
                String currentCode = currentCondition.getAttributes().getNamedItem("code").getNodeValue();

                Node highLow = doc.getElementsByTagName("yweather:forecast").item(0);
                String highTemp = highLow.getAttributes().getNamedItem("high").getNodeValue();
                String lowTemp = highLow.getAttributes().getNamedItem("low").getNodeValue();
                String weekday = highLow.getAttributes().getNamedItem("day").getNodeValue();

                Node wind = doc.getElementsByTagName("yweather:wind").item(0);
                String windspeed = "Wind: " + wind.getAttributes().getNamedItem("speed").getNodeValue() + " mph";

                Forecast f = new Forecast();
                f.setZip(Integer.parseInt(params[0]));
                f.setCity(locationCity);
                f.setState(locationState);
                f.setCurrentTemp(currentTemp);
                f.setHighTemp(highTemp);
                f.setLowTemp(lowTemp);
                f.setCode(Integer.parseInt(currentCode));
                f.setWind(windspeed);
                f.setWeekday(weekday);

                NodeList forecastNodes = doc.getElementsByTagName("yweather:forecast");
                for (int i = 1; i < forecastNodes.getLength(); i++) {

                    Node node = forecastNodes.item(i);

                    String fWeekday = node.getAttributes().getNamedItem("day").getNodeValue();
                    String fHigh = node.getAttributes().getNamedItem("high").getNodeValue();
                    String fLow = node.getAttributes().getNamedItem("low").getNodeValue();
                    int fCode = Integer.parseInt(node.getAttributes().getNamedItem("code").getNodeValue());

                    Forecast child = new Forecast();
                    child.setZip(Integer.parseInt(params[0]));
                    child.setCity(locationCity);
                    child.setState(locationState);
                    child.setHighTemp(fHigh);
                    child.setLowTemp(fLow);
                    child.setCode(fCode);
                    child.setWeekday(fWeekday);

                    f.future.add(child);
                }

                FileOutputStream outputStream;

                try {
                    outputStream = openFileOutput("weather.xml", Context.MODE_PRIVATE);
                    outputStream.write(getStringFromDocument(doc).getBytes());
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }

                return f;
            } catch (Exception e) {
                System.out.println("Exception while parsing: " + e);
                return new Forecast(-1, "Error Parsing", "TX", "69", "42", "0", 1, "", "");
            }
        }

        @Override
        protected void onPostExecute(Forecast result) {
            TextView location = (TextView) findViewById(R.id.place);
            location.setText(result.city + "," + result.state);

            TextView currentTemp = (TextView) findViewById(R.id.temp);
            currentTemp.setText(result.currentTemp + "ºF");

            TextView highLow = (TextView) findViewById(R.id.highLow);
            highLow.setText("High: " + result.highTemp + "ºF     Low: " + result.lowTemp + "ºF");

            TextView wind = (TextView) findViewById(R.id.wind);
            wind.setText(result.wind);

            // Update TableLayout
            TableLayout table = (TableLayout) findViewById(R.id.forecast);
            for (int i = 0; i < result.future.size(); i++){
                TableRow row = new TableRow(context);
                TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT);
                row.setLayoutParams(layoutParams);

                ImageView image = new ImageView(context);
                int futureCode = result.future.get(i).code;
                if ((futureCode >= 0 && futureCode <= 4) || (futureCode == 23) || (futureCode >= 37 && futureCode <= 39) || futureCode == 45 || futureCode == 47) {
                    image.setImageResource(R.drawable.ic_thunder);
                } else if (futureCode == 5 || futureCode == 7) {
                    image.setImageResource(R.drawable.ic_snow);
                } else if (futureCode == 6 || (futureCode >= 8 && futureCode <= 12) || futureCode == 40) {
                    image.setImageResource(R.drawable.ic_rainy);
                    soundEffect.setLooping(true);
                } else if ((futureCode >= 13 && futureCode <= 18) || futureCode == 25 || futureCode == 35 || (futureCode >= 41 && futureCode <= 43) || futureCode == 46){
                    image.setImageResource(R.drawable.ic_snow);
                } else if (futureCode == 19) {
                    image.setImageResource(R.drawable.ic_windy);
                } else if ((futureCode >= 20 && futureCode <= 22) || futureCode == 28) {
                    image.setImageResource(R.drawable.ic_cloudy);
                } else if (futureCode == 27 || futureCode == 29 || futureCode == 31 || futureCode == 33) {
                    image.setImageResource(R.drawable.ic_night);
                } else if (futureCode == 44 || futureCode == 30){
                    image.setImageResource(R.drawable.ic_partly_cloudy);
                } else {
                    image.setImageResource(R.drawable.ic_sunny);
                }
                image.setPadding(30, 15, 30, 15);
                row.addView(image);

                TextView day = new TextView(context);

                day.setText(result.future.get(i).weekday);
                day.setTypeface(null, Typeface.BOLD);
                day.setTextSize(15);
                day.setPadding(30, 15, 30, 15);
                row.addView(day);

                TextView high = new TextView(context);
                high.setText(result.future.get(i).highTemp + "ºF");
                high.setTypeface(null, Typeface.ITALIC);
                high.setPadding(30, 15, 30, 15);
                row.addView(high);

                TextView low = new TextView(context);
                low.setText(result.future.get(i).lowTemp + "ºF");
                low.setTypeface(null, Typeface.ITALIC);
                low.setPadding(30, 15, 30, 15);
                row.addView(low);

                table.addView(row);
            }
        }
    }

    private Forecast forecast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        WebView myWebView = (WebView) findViewById(R.id.webView);
        WebSettings webSettings = myWebView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setSupportMultipleWindows(false);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        myWebView.setScrollContainer(false);
        myWebView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return (event.getAction() == MotionEvent.ACTION_MOVE);
            }
        });

        myWebView.loadUrl("file:///android_asset/sunny.html");

        Intent intent = getIntent();
        int new_zip = intent.getIntExtra(ZipActivity.EXTRA_MESSAGE, 78681);

        FetchWeatherJob fetchWeatherJob = new FetchWeatherJob(this);
        try {
            forecast = fetchWeatherJob.execute(Integer.toString(new_zip)).get();
        } catch (Exception e) {
            System.out.println("Could not fetch weaather: " + e);
        }

        if(soundEffect != null) {
            soundEffect.stop();
            soundEffect.release();
        }

        System.out.println("Code is " + forecast.code);
        if ((forecast.code >= 0 && forecast.code <= 4) || (forecast.code == 23) || (forecast.code >= 37 && forecast.code <= 39) || forecast.code == 45 || forecast.code == 47) {
            myWebView.loadUrl("file:///android_asset/thunder.html");
            soundEffect = MediaPlayer.create(this, R.raw.thunder);
        } else if (forecast.code == 5 || forecast.code == 7) {
            myWebView.loadUrl("file:///android_asset/snow_rain.html");
            soundEffect = MediaPlayer.create(this, R.raw.snow);
        } else if (forecast.code == 6 || (forecast.code >= 8 && forecast.code <= 12) || forecast.code == 40) {
            myWebView.loadUrl("file:///android_asset/rainy.html");
            soundEffect = MediaPlayer.create(this, R.raw.thunder);
            soundEffect.setLooping(true);
        } else if ((forecast.code >= 13 && forecast.code <= 18) || forecast.code == 25 || forecast.code == 35 || (forecast.code >= 41 && forecast.code <= 43) || forecast.code == 46){
            myWebView.loadUrl("file:///android_asset/snow.html");
            soundEffect = MediaPlayer.create(this, R.raw.snow);
        } else if (forecast.code == 19) {
            myWebView.loadUrl("file:///android_asset/dust.html");
            soundEffect = MediaPlayer.create(this, R.raw.thunder);
        } else if ((forecast.code >= 20 && forecast.code <= 22) || forecast.code == 28) {
            myWebView.loadUrl("file:///android_asset/cloudy.html");
            soundEffect = MediaPlayer.create(this, R.raw.sunny);
        } else if (forecast.code == 27 || forecast.code == 29 || forecast.code == 31 || forecast.code == 33) {
            myWebView.loadUrl("file:///android_asset/night.html");
            soundEffect = MediaPlayer.create(this, R.raw.na);
        } else {
            myWebView.loadUrl("file:///android_asset/sunny.html");
            soundEffect = MediaPlayer.create(this, R.raw.sunny);
        }

        soundEffect.setLooping(true);
        if (mute == 0)
            soundEffect.start();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void openBox(View view){
        Intent intent = new Intent(this, ZipActivity.class);
        startActivity(intent);
    }

    public void toggleMute(View view) {
        mute = (mute == 1) ? 0 : 1;

        System.out.println("Mute now set to " + mute);

        ImageButton muteImageButton = (ImageButton) findViewById(R.id.music);
        if (mute == 0) {
            soundEffect.start();
            muteImageButton.setImageResource(android.R.drawable.ic_lock_silent_mode_off);
        } else {
            soundEffect.pause();
            muteImageButton.setImageResource(android.R.drawable.ic_lock_silent_mode);
        }
    }

    //http://stackoverflow.com/questions/9148615/android-stop-background-music
    public void onUserLeaveHint() {
        soundEffect.stop();
        super.onUserLeaveHint();
    }
}
