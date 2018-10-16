package com.omelchenkoaleks.odometer;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Binder;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;

import java.util.Random;

public class OdometerService extends Service {

    // чтобы объект слушателя был доступен для других методов нужна эта переменная
    private LocationListener listener;

    // для менеджера создается переменная, чтобы к нему можно было обращаться из других методов
    private LocationManager locationManager;

    // строка разрешения добавляется в виде константы
    public static final String PERMISSION_STRING =
            android.Manifest.permission.ACCESS_FINE_LOCATION;

    /**
     * расстояине и последнее местонахождение хранится в статических переменных,
     * чтобы их значения сохранялись при уничтожении службы
     */
    private static double distanceInMeters;
    private static Location lastLocation = null;


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
        // это был тест со случайным числом
//        // получить случайное число типа Double
//        return random.nextDouble();

        // расстояние в метрах преобразуем в мили
        return this.distanceInMeters / 1609.344;
    }

    // реализуем слушатель в этом методе - он долже создаваться при создании OdometerService
    @Override
    public void onCreate() {
        super.onCreate();

        // создать LocationListener
        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                if (lastLocation == null) {
                    // задает исходное местонахождение пользователя
                    lastLocation = location;
                }
                // метод вычисляет расстояние между location и lastLocation
                distanceInMeters += location.distanceTo(lastLocation);
                // обновляет пройденное расстояние и последнее местонахождение пользователя
                lastLocation = location;
            }

            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {

            }

            @Override
            public void onProviderEnabled(String provider) {

            }

            @Override
            public void onProviderDisabled(String provider) {

            }
        };

        // получаем объект LocationManager = он нужен, чтобы получить
        // доступ к службе позиционирования
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        // проверяем наличие разрешения
        if (ContextCompat.checkSelfPermission(this, PERMISSION_STRING)
                == PackageManager.PERMISSION_GRANTED) {
            // получить самого точного провайдера
            String provider = locationManager.getBestProvider(new Criteria(), true);
            if (provider != null) {
                // запросить обновления от провайдера данных местонахождения
                locationManager.requestLocationUpdates(provider, 1000, 1, listener);
            }
        }
    }

    // в этом методе остановку получения обновлений слушателем
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (locationManager != null && listener != null) {
            // проверяем разрешение на их удаление и если есть - прекращаем обновления
            if (ContextCompat.checkSelfPermission(this, PERMISSION_STRING)
                    == PackageManager.PERMISSION_GRANTED) {
                locationManager.removeUpdates(listener);
            }
            locationManager = null;
            listener = null;
        }
    }
}
