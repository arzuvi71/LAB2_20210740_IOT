package com.example.lab2_20210740;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;

import com.bumptech.glide.Glide;
import com.example.lab2_20210740.databinding.GameStartPageBinding;
import com.example.lab2_20210740.utils.GameHistoryManager;
import com.example.lab2_20210740.workers.CountdownWorker;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class GameActivity extends AppCompatActivity {

    private GameStartPageBinding binding;
    private int quantity;
    private boolean hasText;
    private String customText;
    private List<String> catImageUrls;
    private int currentImageIndex = 0;

    // Variables para el contador con WorkManager
    private int totalTimeSeconds;
    private WorkManager workManager;
    private OneTimeWorkRequest countdownWorkRequest;

    // Hilo para cargar imágenes
    private ExecutorService imageLoadExecutor;
    private Handler mainHandler;

    private BroadcastReceiver countdownReceiver;

    private GameHistoryManager historyManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = GameStartPageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializamos componentes
        workManager = WorkManager.getInstance(this);
        mainHandler = new Handler(Looper.getMainLooper());
        imageLoadExecutor = Executors.newSingleThreadExecutor();
        historyManager = new GameHistoryManager(this);

        setupBroadcastReceiver();

        getIntentData();


        setupUI();

        loadCatImages();

        // Iniciamos contador con WorkManager
        startCountdownWithWorkManager();
    }
    
    private void getIntentData() {
        Intent intent = getIntent();
        quantity = intent.getIntExtra("quantity", 1);
        hasText = intent.getBooleanExtra("hasText", false);
        customText = intent.getStringExtra("customText");
        
        if (customText == null) {
            customText = "";
        }
        
        // Determinamos el tiempo total del contador
        totalTimeSeconds = quantity * 4;

        Log.d("GameActivity", "Quantity: " + quantity + ", Total time: " + totalTimeSeconds + "s");
    }

    private void setupBroadcastReceiver() {
        countdownReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();

                if (CountdownWorker.ACTION_COUNTDOWN_UPDATE.equals(action)) {
                    int remainingTime = intent.getIntExtra(CountdownWorker.EXTRA_REMAINING_TIME, 0);
                    int currentImage = intent.getIntExtra(CountdownWorker.EXTRA_CURRENT_IMAGE, 0);
                    updateChronometerDisplay(remainingTime);
                    Log.d("GameActivity", "Contador actualizado: " + remainingTime + "s, imagen actual: " + currentImage);

                } else if (CountdownWorker.ACTION_COUNTDOWN_FINISHED.equals(action)) {
                    onCountdownFinished();

                } else if ("com.example.lab2_20210740.IMAGE_UPDATE".equals(action)) {
                    int newImageIndex = intent.getIntExtra(CountdownWorker.EXTRA_CURRENT_IMAGE, 0);
                    Log.d("GameActivity", "Recibido cambio de imagen a índice: " + newImageIndex);
                    currentImageIndex = newImageIndex;
                    showCurrentImage();
                }
            }
        };

        IntentFilter filter = new IntentFilter();
        filter.addAction(CountdownWorker.ACTION_COUNTDOWN_UPDATE);
        filter.addAction(CountdownWorker.ACTION_COUNTDOWN_FINISHED);
        filter.addAction("com.example.lab2_20210740.IMAGE_UPDATE");
        LocalBroadcastManager.getInstance(this).registerReceiver(countdownReceiver, filter);
    }
    
    private void setupUI() {
        // Configuramos la cantidad
        binding.tvCantidad.setText("Cantidad: " + quantity);

        // Configuramos el cronómetro inicial
        updateChronometerDisplay(totalTimeSeconds);

        //Habilitamos el boton de siguiente
        binding.button1.setEnabled(false);
        binding.button1.setOnClickListener(v -> goToResults());


        binding.imageView.setImageResource(android.R.drawable.ic_menu_gallery);
    }

    private void startCountdownWithWorkManager() {
        countdownWorkRequest = new OneTimeWorkRequest.Builder(CountdownWorker.class)
                .setInputData(CountdownWorker.createInputData(totalTimeSeconds, quantity))
                .build();

        workManager.enqueue(countdownWorkRequest);
        Log.d("GameActivity", "Contador iniciado con WorkManager");
    }
    
    private void updateChronometerDisplay(int remainingSeconds) {
        int minutes = remainingSeconds / 60;
        int seconds = remainingSeconds % 60;
        String timeText = String.format("%02d:%02d", minutes, seconds);
        binding.cronometro.setText(timeText);
    }

    private void onCountdownFinished() {
        binding.button1.setEnabled(true);
        binding.cronometro.setText("00:00");
        Toast.makeText(this, "¡Tiempo terminado! Puedes continuar", Toast.LENGTH_SHORT).show();
    }
    
    /** Para la parte de la implementación del API se empleo un LLM (Deepseek) ya que se observo problemas para que
     * las imagenes mostradas pasaran en el tiempo adecuado y completas antes de finalizar la interaccion.
     * Se empleo el prompt "Teniendo conocimientos sobre Android Studio con el desarrollo de una aplicación que incluye diferentes elementos
     * y componentes visuales, ViewBinding e hilos, quiero implementar una API a esta para cargar imagenes aleaorias de gatos y que si se ingresa
     * un texto estas se generen con todo y texto, cada una en un lapso de 4s connun contador en la cabecera*/

    private void loadCatImages() {
        imageLoadExecutor.execute(() -> {
            catImageUrls = new ArrayList<>();
            
            // Generar URLs según si tiene texto o no
            for (int i = 0; i < quantity; i++) {
                String imageUrl;
                if (hasText && !customText.isEmpty()) {
                    // URL con texto personalizado - agregar parámetro único para cada imagen
                    String encodedText = customText.replace(" ", "%20");
                    imageUrl = "https://cataas.com/cat/says/" + encodedText +
                              "?fontSize=50&fontColor=white&fontColorOutline=black&random=" +
                              System.currentTimeMillis() + "_" + i;
                } else {
                    // URL sin texto, agregar parámetro random para evitar cache
                    imageUrl = "https://cataas.com/cat?random=" + System.currentTimeMillis() + "_" + i;
                }
                catImageUrls.add(imageUrl);
                Log.d("GameActivity", "URL generada " + (i+1) + ": " + imageUrl);
            }
            
            // Actualizar UI en el hilo principal
            mainHandler.post(() -> {
                // Mostrar primera imagen inmediatamente
                currentImageIndex = 0;
                showCurrentImage();
                Toast.makeText(GameActivity.this, "¡" + quantity + " gatitos cargados! 🐱", Toast.LENGTH_SHORT).show();
            });
        });
    }
    
    private void showCurrentImage() {
        if (catImageUrls != null && !catImageUrls.isEmpty() && currentImageIndex < catImageUrls.size()) {
            String currentImageUrl = catImageUrls.get(currentImageIndex);

            Log.d("GameActivity", "Mostrando imagen " + (currentImageIndex + 1) + " de " + quantity + ": " + currentImageUrl);

            // Cargar imagen con Glide en el hilo principal
            mainHandler.post(() -> {
                Glide.with(GameActivity.this)
                        .load(currentImageUrl)
                        .placeholder(android.R.drawable.ic_menu_gallery)
                        .error(android.R.drawable.ic_menu_close_clear_cancel)
                        .into(binding.imageView);

                Toast.makeText(GameActivity.this, "Imagen " + (currentImageIndex + 1) + " de " + quantity, Toast.LENGTH_SHORT).show();
            });
        }
    }
    
    private void goToResults() {
        // Detener contador con WorkManager
        stopCountdownWorkManager();

        // Guardar interacción en el historial
        historyManager.addInteraction(quantity, hasText, customText, totalTimeSeconds);
        Log.d("GameActivity", "Interacción guardada en historial");

        // Crear intent para ResultsActivity
        Intent intent = new Intent(this, ResultsActivity.class);
        intent.putExtra("quantity", quantity);
        intent.putExtra("hasText", hasText);
        intent.putExtra("customText", customText);
        intent.putExtra("totalTime", totalTimeSeconds);
        intent.putStringArrayListExtra("imageUrls", new ArrayList<>(catImageUrls));

        startActivity(intent);
        finish();
    }

    private void stopCountdownWorkManager() {
        if (workManager != null && countdownWorkRequest != null) {
            workManager.cancelWorkById(countdownWorkRequest.getId());
            Log.d("GameActivity", "Contador detenido con WorkManager");
        }
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        // El contador continúa ejecutándose en background con WorkManager
        Log.d("GameActivity", "Activity pausada - contador sigue corriendo");
    }

    @Override
    protected void onResume() {
        super.onResume();
        // El contador sigue corriendo, no necesita reiniciarse
        Log.d("GameActivity", "Activity reanudada - contador sigue corriendo");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Desregistrar BroadcastReceiver
        if (countdownReceiver != null) {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(countdownReceiver);
        }

        // Detener contador
        stopCountdownWorkManager();

        // Limpiar recursos
        if (imageLoadExecutor != null) {
            imageLoadExecutor.shutdown();
        }
        if (mainHandler != null) {
            mainHandler.removeCallbacksAndMessages(null);
        }

        binding = null;
        Log.d("GameActivity", "Activity destruida - recursos limpiados");
    }
}
