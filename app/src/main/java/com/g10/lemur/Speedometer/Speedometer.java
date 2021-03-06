package com.g10.lemur.Speedometer;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.g10.lemur.Accelerometer.Accelerometer;
import com.g10.lemur.Altimeter.Altimeter;
import com.g10.lemur.Decibel.Decibel;
import com.g10.lemur.Help.Help;
import com.g10.lemur.MainActivity;
import com.g10.lemur.R;
import com.g10.lemur.Settings.Settings;
import com.g10.lemur.Vision.Vision;
import com.jjoe64.graphview.DefaultLabelFormatter;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.series.DataPoint;
import com.jjoe64.graphview.series.LineGraphSeries;

import java.text.DecimalFormat;
import java.util.Random;

public class Speedometer extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener, LocationListener
{
    NavigationView navigationView;

    private final Handler mHandler = new Handler();
    private Runnable mTimer;


    static GraphView graph;
    static LineGraphSeries<DataPoint> series;
    long activityCreateTime;

    TextView currentValueTextView;
    TextView highValueTextView;
    TextView lowValueTextView;

    float speed = 0;
    float highestSpeed = 0;
    float lowestSpeed = 0;

    LocationManager locationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_speedometer);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // Set the current activity as marked in the menu
        navigationView.setCheckedItem(R.id.menuSpeed);

        //Graph stuff
        graph = (GraphView) findViewById(R.id.graph);
        series = new LineGraphSeries<>();
        graph.addSeries(series);

        graph.getViewport().setXAxisBoundsManual(true);
        graph.getViewport().setMinX(0);
        graph.getViewport().setMaxX(10000);
        graph.getGridLabelRenderer().setNumHorizontalLabels(4);

        graph.getGridLabelRenderer().setLabelFormatter(new DefaultLabelFormatter()
        {
            @Override
            public String formatLabel(double value, boolean isValueX)
            {
                if (isValueX) {
                    DecimalFormat df = new DecimalFormat("#.#");
                    return df.format(value/1000)+"s";
                } else {
                    return super.formatLabel(value, isValueX);
                }
            }
        });

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        try
        {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
            speed = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getSpeed();
        }
        catch (SecurityException e)
        {
            Log.i("Exception", "location security exception");
        }

        final ImageView mRefreshImage = (ImageView) findViewById(R.id.refreshImageView);
        mRefreshImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(view.getContext(), "Resetting Values",
                        Toast.LENGTH_SHORT).show();
                highestSpeed = speed;
                lowestSpeed = speed;
                currentValueTextView.setText(String.valueOf(speed) + " m/s");
                highValueTextView.setText(String.valueOf(speed) + " m/s");
                lowValueTextView.setText(String.valueOf(speed) + " m/s");


                series.resetData(new DataPoint[]{newDatapoint(speed)});
            }
        });


        currentValueTextView = (TextView) findViewById(R.id.currentValueText);
        highValueTextView = (TextView) findViewById(R.id.highValueText);
        lowValueTextView = (TextView) findViewById(R.id.lowValueText);

        lowestSpeed = speed;
        highestSpeed = speed;
        currentValueTextView.setText(String.valueOf(speed) + " m/s");
        highValueTextView.setText(String.valueOf(speed) + " m/s");
        lowValueTextView.setText(String.valueOf(speed) + " m/s");

        activityCreateTime = System.currentTimeMillis();
    }

    public void onRestoreInstanceState(Bundle savedInstanceState)
    {
        super.onRestoreInstanceState(savedInstanceState);

        navigationView.setCheckedItem(R.id.menuSpeed);
    }

    @Override
    public void onResume()
    {
        super.onResume();

        navigationView.setCheckedItem(R.id.menuSpeed);

        mTimer = new Runnable()
        {
            @Override
            public void run()
            {

                if(speed>highestSpeed){
                    highValueTextView.setText(String.valueOf(speed) + " m/s");
                    highestSpeed = speed;
                }

                if(speed<lowestSpeed){
                    lowValueTextView.setText(String.valueOf(speed) + " m/s");
                    lowestSpeed = speed;
                }

                currentValueTextView.setText(String.valueOf(speed) + " m/s");

                double timeSince = System.currentTimeMillis() - activityCreateTime;
                series.appendData(new DataPoint(timeSince, speed), true, 100);

                mHandler.postDelayed(this, 1000);
            }
        };
        mHandler.postDelayed(mTimer, 1000);
    }

    @Override
    public void onPause()
    {
        mHandler.removeCallbacks(mTimer);
        super.onPause();
    }

    @Override
    public void onBackPressed()
    {
        // Physical back button pressed

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START))
        {
            drawer.closeDrawer(GravityCompat.START);
        } else
        {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Right sub-menu.
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item)
    {
        Intent intent;
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.menuHome)
        {
            intent = new Intent(this, MainActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.menuVision)
        {
            // Go to Google Vision
            intent = new Intent(this, Vision.class);
            startActivity(intent);
        }
        else if (id == R.id.menuAlti)
        {
            // Go to altimeter
            intent = new Intent(this, Altimeter.class);
            startActivity(intent);
        }
        else if (id == R.id.menuSpeed)
        {
            // Stay Here
        }
        else if (id == R.id.menuAcc)
        {
            // Go to accelerometer
            intent = new Intent(this, Accelerometer.class);
            startActivity(intent);
        }
        else if (id == R.id.menuSound)
        {
            // Go to decibel
            intent = new Intent(this, Decibel.class);
            startActivity(intent);
        }
        else if (id == R.id.menuHelp)
        {
            // Go to help
            intent = new Intent(this, Help.class);
            startActivity(intent);
        }
        else if (id == R.id.menuSettings)
        {
            // Go to Settings
            intent = new Intent(this, Settings.class);
            startActivity(intent);
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private int randomYValue()
    {
        Random random = new Random();
        return random.nextInt(19);
    }

    private DataPoint newDatapoint(float y)
    {
        double timeSince = System.currentTimeMillis() - activityCreateTime;
        return new DataPoint(timeSince, y);
    }



    //GPS Speed stuff
    @Override
    public void onLocationChanged(Location location)
    {
        this.speed = location.getSpeed();
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
