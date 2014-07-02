package io.endeios.allarmestrozzi;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

import java.util.Calendar;


public class OverviewActivity extends Activity {

    private Boolean status = false;

    private AlarmManager alarm;
    private PendingIntent myPintent;
    private Intent serviceIntent;
    private Messenger messenger;



    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Log.d(OverviewActivity.class.getName(),"Ricevuto "+msg.obj);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        Switch enable_switch = (Switch) findViewById(R.id.enable_switch);
        enable_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

                Log.i(OverviewActivity.class.getName(), "Getting " + compoundButton);
                Log.i(OverviewActivity.class.getName(), "Id is " + compoundButton.getId());
                if (compoundButton.isChecked()) {
                    status = true;
                } else {
                    status = false;
                }

                Log.i(OverviewActivity.class.getName(), "Status is " + status);

            }
        });


        serviceIntent = new Intent(this,CasaStrozziAlarmUpdateService.class);
        messenger = new Messenger(handler);
        serviceIntent.putExtra(CasaStrozziAlarmUpdateService.EXTRA_MESSENGER,messenger);
        myPintent = PendingIntent.getService(this, 0, serviceIntent, 0);
        alarm = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        startTimerService();

    }

    public boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        // if no network is available networkInfo will be null
        // otherwise check if we are connected
        if (networkInfo != null && networkInfo.isConnected()) {
            return true;
        }
        return false;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.overview, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void refreshStatus(View v) {
        Switch enable_switch = (Switch) findViewById(R.id.enable_switch);
        Log.i(OverviewActivity.class.getName(), "Id is " + enable_switch.getId());
        Log.i(OverviewActivity.class.getName(), "Status is " + status);
        if (status == true) {
            enable_switch.setChecked(false);
        } else {
            enable_switch.setChecked(true);
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        alarm.cancel(myPintent);
        startTimerService();
        Log.d(OverviewActivity.class.getName(),"Resuming service scheduling");
    }

    @Override
    public void onDestroy() {
        Log.d(OverviewActivity.class.getName(),"Stopping timer on service");
        alarm.cancel(myPintent);
    }

    private void startTimerService() {
        Calendar calendar = Calendar.getInstance();
        alarm.setRepeating(AlarmManager.RTC_WAKEUP,calendar.getTimeInMillis(),3*1000,myPintent);
    }
}
