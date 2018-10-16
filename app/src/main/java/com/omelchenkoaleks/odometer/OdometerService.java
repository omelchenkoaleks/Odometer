package com.omelchenkoaleks.odometer;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.util.Random;

public class OdometerService extends Service {

    /**
     *  т.к. экземпляр OdometerBinder должен возвращаться методом onBind() класса OdometerService -
     *  создадим эту приватную переменную... В ней созданим экземпляр объекта и вернем его в методе
     */
    private final IBinder binder = new OdometerBinder();

    // для получения случайных чисел используется метод Random()
    private final Random random = new Random();

    // метод вызывается, когда компонент(н-р активность) выдаст запрос на связывание со службой
    // интерфейс IBinder используется для связывания службы с активностью - его нужно реализовать
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    // при создании связанной службы необходимо предоставить реализацию Binder
    public class OdometerBinder extends Binder {

        // этот метод будет использоваться аткивностью для получения ссылки на OdometerService
        OdometerService getOdometer() {
            return OdometerService.this;
        }
    }

    // это метод будет вызываться нашей активностью - он будет использовать
    // службу позиционирования Андроид
    public double getDistance() {
        // получить случайное число типа Double
        return random.nextDouble();
    }
}
