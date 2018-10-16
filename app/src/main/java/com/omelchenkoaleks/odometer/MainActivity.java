package com.omelchenkoaleks.odometer;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.widget.TextView;

import java.util.Locale;

public class MainActivity extends Activity {

    // эти переменные нужны для сохранения ссылки на службу и признака связывания с активностью
    private OdometerService odometerService;
    private boolean bound = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        displayDistance();
    }

    // создание объекта ServiceConnection = это позволит активности связаться со службой
    private ServiceConnection connection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder binder) {
            // параметр IBinder используем для получения ссылки на службу,
            // с которой устанавливается связь (в данном случае нужно привести IBinder
            // к типу OdometerService.OdometerBinder
            OdometerService.OdometerBinder odometerBinder =
                    (OdometerService.OdometerBinder) binder;
            odometerService = odometerBinder.getOdometer();
            // т.к. активность связывается со службой = переменной присваивается true
            bound = true;
        }

        // метод решает только одну задачу = сохраняет информацию о том, что активность теперь не
        // связана со службой
        @Override
        public void onServiceDisconnected(ComponentName name) {
            bound = false;
        }
    };

    // этот метод выбран для связывания активности со службой =
    // наш сервис должен быть видим когда активность становится видимой
    @Override
    protected void onStart() {
        super.onStart();

        // создаем итент для службы с которой выполняется связывание
        Intent intent = new Intent(this, OdometerService.class);
        // привязываем
        bindService(intent, connection, Context.BIND_AUTO_CREATE);
    }

    // в этом методе будет отмена связывания
    @Override
    protected void onStop() {
        super.onStop();

        if (bound) {
            // отмена связывания осуществляется этим методом
            unbindService(connection);
            // переменной присваивается false
            bound = false;
        }
    }

    /**
     *  класс OdometerService будет вызываться каждую секунду, а надпись в MainActivity
     *  будет отображаться полученным значением в этом методе
     */
    private void displayDistance() {
        final TextView distanceView = findViewById(R.id.distance);
        final Handler handler = new Handler();
        handler.post(new Runnable() {
            @Override
            public void run() {
                double distance = 0.0;
                if (bound && odometerService != null) {
                    distance = odometerService.getDistance();
                }
                String distanceStr = String.format(Locale.getDefault(),
                        "%1$,.2f miles", distance);
                distanceView.setText(distanceStr);
                handler.postDelayed(this, 1000);
            }
        });
    }
}
