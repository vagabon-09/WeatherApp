package com.example.learning;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivityDebug";
    private final int PERMISSION_CODE = 1;
    private RelativeLayout homeRL;
    private ProgressBar loadingPB;
    private TextView cityNameTV, temperatureTV, conditionTV;
    private RecyclerView weatherRV;
    private TextInputEditText cityEdt;
    private ImageView backIV, iconIV, searchIV;
    private ArrayList<WeatherRVModal> weatherRVModalArrayList;
    private WeatherRVAdapter weatherRVAdapter;
    private LocationManager locationManager;
    private String cityName;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_main);

        homeRL = findViewById(R.id.idRLHome);
        loadingPB = findViewById(R.id.idPBLoading);
        cityNameTV = findViewById(R.id.idTVCityName);
        temperatureTV = findViewById(R.id.idTVTemperature);
        conditionTV = findViewById(R.id.idTVCondition);
        weatherRV = findViewById(R.id.idRVWeather);
        cityEdt = findViewById(R.id.idEdtCity);
        backIV = findViewById(R.id.idIBBack);
        iconIV = findViewById(R.id.idIVIcon);
        searchIV = findViewById(R.id.idIVSearch);
        weatherRVModalArrayList = new ArrayList<>();
        weatherRVAdapter = new WeatherRVAdapter(this, weatherRVModalArrayList);
        weatherRV.setAdapter(weatherRVAdapter);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, PERMISSION_CODE);
        }

        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        String cityName = getCityName(location.getLongitude(), location.getLatitude());
        getWeatherInfo(cityName);


        searchIV.setOnClickListener(view -> {
            String city = cityEdt.toString();
            if (city.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please Enter City Name", Toast.LENGTH_SHORT).show();
            } else {
                cityNameTV.setText(cityName);
                getWeatherInfo(city);
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Please Provide Permissions", Toast.LENGTH_SHORT).show();
                finish();
            }
        }
    }

    private String getCityName(double longitude, double latitude) {

        String cityName = null;
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());
        try {
            List<Address> address = gcd.getFromLocation(latitude, longitude, 10);
            // new code start
            if (address != null) cityName = address.get(0).getLocality();
            // new code end
//            for (Address adr : addresses) {
//                if (adr != null) {
//                    String city = adr.getLocality();
//                    if (city != null && !city.equals("")) {
//                        cityName = city;
//                    } else {
//                        Log.d("TAG", "City Not Found");
//                        Toast.makeText(this, "City Not Found", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        Log.d(TAG, "getCityName: here i am ");
        return cityName;
    }

    private void getWeatherInfo(String cityName) {
        Log.d(TAG, "getWeatherInfo: " + cityName);
        RequestQueue requestQueue = Volley.newRequestQueue(this);
        String url = "http://api.weatherapi.com/v1/forecast.json?key=16a93b04712248b987d163622232812&q=" + cityName + "&days=1&aqi=yes&alerts=yes";
        cityNameTV.setText(cityName);
        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, response -> {
            if (response != null) {
                loadingPB.setVisibility(View.GONE);
                homeRL.setVisibility(View.VISIBLE);
                weatherRVModalArrayList.clear();
            }
            try {
                String temperature = response.getJSONObject("current").getString("temp_c");
                temperatureTV.setText(temperature + "°c");
                int isDay = response.getJSONObject("current").getInt("is_day");
                String condition = response.getJSONObject("current").getJSONObject("condition").getString("text");
                String conditionIcon = response.getJSONObject("current").getJSONObject("condition").getString("icon");
                Picasso.get().load("http:".concat(conditionIcon)).into(iconIV);
                conditionTV.setText(condition);
                if (isDay == 1) {
                    Picasso.get().load("https://www.google.com/url?sa=i&url=https%3A%2F%2Fwww.freepik.com%2Fpremium-vector%2Fday-sun-weather-app-screen-mobile-interface-design-forecast-weather-background-time-concept-vector-banner_34671984.htm&psig=AOvVaw2sf3n35EN0E02C-tdjbtI2&ust=1704006128539000&source=images&cd=vfe&opi=89978449&ved=0CBIQjRxqFwoTCLi5qLvLtoMDFQAAAAAdAAAAABAw").into(backIV);
                } else {
                    Picasso.get().load("https://www.google.com/url?sa=i&url=https%3A%2F%2Fwww.freepik.com%2Fpremium-vector%2Fnight-with-clouds-weather-app-screen-mobile-interface-design-forecast-weather-background-time-concept-vector-banner_35828856.htm&psig=AOvVaw2sf3n35EN0E02C-tdjbtI2&ust=1704006128539000&source=images&cd=vfe&opi=89978449&ved=0CBIQjRxqFwoTCLi5qLvLtoMDFQAAAAAdAAAAABAI").into(backIV);
                }

                JSONObject forecastObj = response.getJSONObject("forecast");
                JSONObject forecastO = forecastObj.getJSONArray("forecastday").getJSONObject(0);
                JSONArray hourArray = forecastO.getJSONArray("hour");
                for (int i = 0; i < hourArray.length(); i++) {
                    JSONObject hourObj = hourArray.getJSONObject(i);
                    String time = hourObj.getString("time");
                    String temper = hourObj.getString("temp_c");
                    String img = hourObj.getJSONObject("condition").getString("icon");
                    String wind = hourObj.getString("wind_kph");
                    // weatherRVModalArrayList.add(new WeatherRVModal(time, temper, img, wind)); // old code (wrong code)
                    weatherRVModalArrayList.add(new WeatherRVModal(wind, time, temperature, img)); // new Code
                }
                weatherRVAdapter.notifyDataSetChanged();
            } catch (JSONException e) {
                throw new RuntimeException(e);
            }
        }, (Response.ErrorListener) error -> {
            // Handle errors
            if (error != null && error.getMessage() != null) {
                Log.e(TAG, "API Error: " + error.getMessage());
                Toast.makeText(MainActivity.this, "API Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            } else {
                Log.e(TAG, "API Error: Unknown error");
                Toast.makeText(MainActivity.this, "API Error: Unknown error", Toast.LENGTH_SHORT).show();
            }

        });

        // Add the request to the RequestQueue
        requestQueue.add(jsonObjectRequest);
    }

}