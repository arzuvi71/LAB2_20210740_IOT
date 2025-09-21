package com.example.lab2_20210740.workers;

import android.content.Context;
import android.content.Intent;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.Data;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

public class CountdownWorker extends Worker {
    
    public static final String ACTION_COUNTDOWN_UPDATE = "com.example.lab2_20210740.COUNTDOWN_UPDATE";
    public static final String ACTION_COUNTDOWN_FINISHED = "com.example.lab2_20210740.COUNTDOWN_FINISHED";
    public static final String EXTRA_REMAINING_TIME = "remaining_time";
    public static final String EXTRA_CURRENT_IMAGE = "current_image";
    
    private static final String KEY_TOTAL_TIME = "total_time";
    private static final String KEY_QUANTITY = "quantity";
    
    public CountdownWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
        super(context, workerParams);
    }
    
    @NonNull
    @Override
    public Result doWork() {
        Data inputData = getInputData();
        int totalTimeSeconds = inputData.getInt(KEY_TOTAL_TIME, 12);
        int quantity = inputData.getInt(KEY_QUANTITY, 3);
        
        int remainingTime = totalTimeSeconds;
        int currentImageIndex = 0;
        int timePerImage = 4; // 4 segundos por imagen
        int elapsedTime = 0; // Tiempo transcurrido total

        try {
            // Enviar primera imagen al inicio
            sendImageUpdate(currentImageIndex);

            while (remainingTime > 0 && !isStopped()) {
                // Enviar actualización del contador
                sendCountdownUpdate(remainingTime, currentImageIndex);

                // Esperar 1 segundo
                Thread.sleep(1000);
                remainingTime--;
                elapsedTime++;

                // Calcular qué imagen debería mostrarse basado en el tiempo transcurrido
                int expectedImageIndex = elapsedTime / timePerImage;

                // Cambiar imagen si es necesario y no hemos llegado al final
                if (expectedImageIndex != currentImageIndex && expectedImageIndex < quantity) {
                    currentImageIndex = expectedImageIndex;
                    Log.d("CountdownWorker", "Cambiando a imagen " + (currentImageIndex + 1) + " de " + quantity +
                          " (tiempo transcurrido: " + elapsedTime + "s)");
                    sendImageUpdate(currentImageIndex);
                }
            }
            
            // Enviar señal de finalización
            sendCountdownFinished();
            
        } catch (InterruptedException e) {
            return Result.failure();
        }
        
        return Result.success();
    }
    
    private void sendCountdownUpdate(int remainingTime, int currentImage) {
        Intent intent = new Intent(ACTION_COUNTDOWN_UPDATE);
        intent.putExtra(EXTRA_REMAINING_TIME, remainingTime);
        intent.putExtra(EXTRA_CURRENT_IMAGE, currentImage);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }
    
    private void sendImageUpdate(int currentImage) {
        Intent intent = new Intent("com.example.lab2_20210740.IMAGE_UPDATE");
        intent.putExtra(EXTRA_CURRENT_IMAGE, currentImage);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }
    
    private void sendCountdownFinished() {
        Intent intent = new Intent(ACTION_COUNTDOWN_FINISHED);
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent);
    }
    
    public static Data createInputData(int totalTime, int quantity) {
        return new Data.Builder()
                .putInt(KEY_TOTAL_TIME, totalTime)
                .putInt(KEY_QUANTITY, quantity)
                .build();
    }
}
