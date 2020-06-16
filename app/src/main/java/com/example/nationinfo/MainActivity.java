package com.example.nationinfo;

import androidx.appcompat.app.AppCompatActivity;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.Charset;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {
    private Spinner sp;
    private TextView Name;
    private TextView Area;
    private TextView Population;
    private ImageView NationFlag;
    private JSONArray jsonArray;
    private int countrycount = 1;
    private String[] arraySpinner;
    private String countryname;
    private String countrycode;

    public class GetDataSync extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void... params) {
            try {
                getData();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(MainActivity.this, android.R.layout.simple_spinner_item, arraySpinner);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            sp.setAdapter(adapter);
            sp.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parentView, View selectedItemView, int position, long id) {
                    String selectedItem = sp.getSelectedItem().toString();
                    if (selectedItem == "--- Select country---"){
                        setBlankData();
                    }
                    else {
                        for (int i = 0; i < countrycount; i++) {
                            try {
                                JSONObject jsonobj = jsonArray.getJSONObject(i);
                                countryname = jsonobj.getString("countryName");
                                if (countryname == selectedItem) {
                                    Name.setText(countryname);
                                    NumberFormat nf = NumberFormat.getInstance(new Locale("vi", "VN"));
                                    nf.setMaximumFractionDigits(9);
                                    nf.setMinimumFractionDigits(0);
                                    double area = Double.valueOf(jsonobj.getString("areaInSqKm"));
                                    Area.setText(nf.format(area));
                                    int pop = Integer.valueOf(jsonobj.getString("population"));
                                    Population.setText(nf.format(pop));
                                    countrycode = jsonobj.getString("countryCode");
                                    countrycode = countrycode.toLowerCase();
                                    Picasso.get().load("http://www.geonames.org/flags/x/" + countrycode + ".gif").into(NationFlag);
                                    break;
                                } else {
                                    continue;
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parentView) {
                    setBlankData();
                }
            });
        }
    }

    private void setBlankData() {
        Name.setText("");
        Area.setText("");
        Population.setText("");
        NationFlag.setImageResource(android.R.color.transparent);
    }

    private void getData() throws IOException, JSONException {
        JSONObject json = readJsonFromUrl("http://api.geonames.org/countryInfoJSON?formatted=true&username=tuongdai252&style=full");
        try {
            jsonArray = json.getJSONArray("geonames");
            countrycount += jsonArray.length();
            arraySpinner = new String[countrycount];
            arraySpinner[0] = "--- Select country---";
            for (int i = 1; i < countrycount; i++) {
                JSONObject jsonobj = jsonArray.getJSONObject(i-1);
                countryname = jsonobj.getString("countryName");
                arraySpinner[i] = countryname;
            }
            Arrays.sort(arraySpinner);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private String readAll(Reader rd) throws IOException {
        StringBuilder sb = new StringBuilder();
        int cp;
        while ((cp = rd.read()) != -1) {
            sb.append((char) cp);
        }
        return sb.toString();
    }

    public JSONObject readJsonFromUrl(String url) throws IOException, JSONException {
        InputStream is = new URL(url).openStream();
        try {
            BufferedReader rd = new BufferedReader(new InputStreamReader(is, Charset.forName("UTF-8")));
            String jsonText = readAll(rd);
            JSONObject json = new JSONObject(jsonText);
            return json;
        } finally {
            is.close();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        sp = (Spinner) findViewById(R.id.spinner);
        Name = (TextView) findViewById(R.id.Name);
        Area = (TextView) findViewById(R.id.Area);
        Population = (TextView) findViewById(R.id.Population);
        NationFlag = (ImageView) findViewById(R.id.NationFlag);
        new GetDataSync().execute();
    }
}
