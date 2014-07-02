package io.endeios.allarmestrozzi;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

public class CasaStrozziAlarmUpdateService extends IntentService {

    private Notification n = null;
    private NotificationManager notificationManager;
    private RestTemplate restTemplate;
    private String url;
    private StrozziServiceBinder binder = new StrozziServiceBinder();
    public static String EXTRA_MESSENGER = "messenger";

    public CasaStrozziAlarmUpdateService() {
        super("CasaStrozziAlarmUpdateService");
        restTemplate = new RestTemplate();
        restTemplate.getMessageConverters().add(new StringHttpMessageConverter());
        url = "http://srvsvn/redmine/issues";
        url = "http://api.openweathermap.org/data/2.5/weather?q=London";

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //getResult();
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public IBinder onBind(Intent intent) {
        getResult(intent);
        return binder;
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        getResult(intent);

    }

    private void alarmOn(){
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        n = new Notification.Builder(this)
                .setContentTitle("CasaStrozzi")
                .setContentText("L'allarme è acceso")
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(false).build();
        n.flags = Notification.FLAG_ONGOING_EVENT;
        notificationManager.cancelAll();
        notificationManager.notify(0, n);
    }
    private void alarmOff(){
        notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        n = new Notification.Builder(this)
                .setContentTitle("CasaStrozzi")
                .setContentText("L'allarme è Spento")
                .setSmallIcon(R.drawable.ic_launcher)
                .setAutoCancel(false).build();
        n.flags = Notification.FLAG_ONGOING_EVENT;
        notificationManager.cancelAll();
        notificationManager.notify(0, n);
    }

    private void getResult(Intent intent) {
        String result = restTemplate.getForObject(url, String.class, "Android");
        Log.i("Service", "Result is " + result);
        try {
            JSONObject obj = new JSONObject(result);
            Log.i("Service", "Object is " + obj);
            Bundle extras = intent.getExtras();
            Integer londonId = (Integer) obj.get("id");
            if(londonId.compareTo(6058560)==0){
                alarmOn();
            } else {
                alarmOff();
            }
            if(extras!=null){
                Messenger messenger = (Messenger) extras.get(EXTRA_MESSENGER);
                Message message = Message.obtain();
                message.obj = obj;
                messenger.send(message);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (RemoteException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        notificationManager.cancelAll();
    }

    public class StrozziServiceBinder extends Binder {
        CasaStrozziAlarmUpdateService getService() {
            return CasaStrozziAlarmUpdateService.this;
        }
    }
}
