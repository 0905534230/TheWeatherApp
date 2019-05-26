package vn.edu.vnuk.theweatherapp.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.app.ProgressDialog;
import android.location.LocationManager;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.weatherapp.R;
import com.google.firebase.auth.FirebaseAuth;

import vn.edu.vnuk.theweatherapp.models.Weather;
import vn.edu.vnuk.theweatherapp.utils.Formatting;
import vn.edu.vnuk.theweatherapp.utils.UI;
import vn.edu.vnuk.theweatherapp.activities.BaseActivity;

public class MainActivity extends BaseActivity
        implements NavigationView.OnNavigationItemSelectedListener, LocationListener {

    private FirebaseAuth auth;
    protected static final int MY_PERMISSIONS_ACCESS_FINE_LOCATION = 1;

    // Time in milliseconds; only reload weather if last update is longer ago than this value
    private static final int NO_UPDATE_REQUIRED_THRESHOLD = 300000;

    private static Map<String, Integer> speedUnits = new HashMap<>(3);
    private static Map<String, Integer> pressUnits = new HashMap<>(3);
    private static boolean mappingsInitialised = false;

    private Weather todayWeather = new Weather();

    private TextView todayTemperature;
    private TextView todayDescription;
    private TextView todayWind;
    private TextView todayPressure;
    private TextView todayHumidity;
    private TextView todaySunrise;
    private TextView todaySunset;
    private TextView todayUvIndex;
    private TextView lastUpdate;
    private TextView todayIcon;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private SwipeRefreshLayout swipeRefreshLayout;

    private View appView;

    private LocationManager locationManager;
    private ProgressDialog progressDialog;

    private int theme;
    private boolean widgetTransparent;
    private boolean destroyed = false;

    private List<Weather> longTermWeather = new ArrayList<>();
    private List<Weather> longTermTodayWeather = new ArrayList<>();
    private List<Weather> longTermTomorrowWeather = new ArrayList<>();

    public String recentCityId = "";

    private Formatting formatting;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //Initialize  SharedPreferences
        PreferenceManager.setDefaultValues(this, R.xml.prefs, false);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);

        widgetTransparent = prefs.getBoolean("transparentWidget", false);
        setTheme(theme = UI.getTheme(prefs.getString("theme", "fresh")));
        boolean darkTheme = super.darkTheme;
        boolean blackTheme = super.blackTheme;
        formatting = new Formatting(this);




        // Initiate activity
        super.onCreate(savedInstanceState);
        appView = findViewById(R.id.viewApp);
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        AppBarLayout appBarLayout = findViewById(R.id.appBarLayout);

        progressDialog = new ProgressDialog(MainActivity.this);
        setContentView( R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        auth = FirebaseAuth.getInstance();
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
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

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_home) {

            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
            // Handle the camera action
        } else if (id == R.id.nav_weathermap) {
            Intent intent = new Intent(MainActivity.this , LoginActivity.class);
            startActivity(intent);
            finish();

        } else if (id == R.id.nav_graph) {

        } else if (id == R.id.nav_setting) {

        } else if (id == R.id.nav_register) {

            Intent intent = new Intent(MainActivity.this , RegisterActivity.class);
            startActivity(intent);
            finish();

        } else if (id == R.id.nav_logout) {

            auth.signOut();
            Intent intent = new Intent(MainActivity.this , LoginActivity.class);
            startActivity(intent);
            finish();

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onLocationChanged(Location location) {
        progressDialog.hide();
        try {
            locationManager.removeUpdates(this);
        } catch (SecurityException e) {
            Log.e("LocationManager", "Error while trying to stop listening for location updates. This is probably a permissions issue", e);
        }
        Log.i("LOCATION (" + location.getProvider().toUpperCase() + ")", location.getLatitude() + ", " + location.getLongitude());
        double latitude = location.getLatitude();
        double longitude = location.getLongitude();
        new ProvideCityNameTask(this, this, progressDialog).execute("coords", Double.toString(latitude), Double.toString(longitude));
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
