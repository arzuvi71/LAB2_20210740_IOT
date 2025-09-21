package com.example.lab2_20210740;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.lab2_20210740.databinding.ActivityResultsBinding;
import com.example.lab2_20210740.utils.GameHistoryManager;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class ResultsActivity extends AppCompatActivity {
    
    private ActivityResultsBinding binding;
    private int quantity;
    private boolean hasText;
    private String customText;
    private int totalTime;
    private int remainingTime;
    private ArrayList<String> imageUrls;
    private GameHistoryManager historyManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        binding = ActivityResultsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Inicializamos historyManager
        historyManager = new GameHistoryManager(this);

        getIntentData();

        // Configurar UI
        setupUI();
    }
    
    private void getIntentData() {
        Intent intent = getIntent();
        quantity = intent.getIntExtra("quantity", 0);
        hasText = intent.getBooleanExtra("hasText", false);
        customText = intent.getStringExtra("customText");
        totalTime = intent.getIntExtra("totalTime", 0);
        remainingTime = intent.getIntExtra("remainingTime", 0);
        imageUrls = intent.getStringArrayListExtra("imageUrls");
        
        if (customText == null) customText = "";
        if (imageUrls == null) imageUrls = new ArrayList<>();
    }
    
    private void setupUI() {

        binding.tvTitle.setText("Historial");

        // Mostraremos un historial completo de todas las interacciones
        String historial = historyManager.generateFullHistory();
        binding.tvSummary.setText(historial);

        binding.btnNewGame.setText("Volver a Jugar");
        binding.btnNewGame.setOnClickListener(v -> showPlayAgainDialog());
    }


    
    private String formatTime(int seconds) {
        int minutes = seconds / 60;
        int secs = seconds % 60;
        return String.format("%02d:%02d", minutes, secs);
    }
    
    private void showPlayAgainDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Confirmar")
                .setMessage("¿Está seguro que desea volver a jugar?")
                .setPositiveButton("Sí", (dialog, which) -> {
                    // Redirigimos al menú principal y reiniciamos el juego
                    Intent intent = new Intent(this, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    finish();
                    Toast.makeText(this, "¡Iniciando nuevo juego!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("No", (dialog, which) -> {
                    // Permanecemos en la misma pantalla
                    dialog.dismiss();
                    Toast.makeText(this, "Continuando en resultados", Toast.LENGTH_SHORT).show();
                })
                .setCancelable(false)
                .show();
    }
    

    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
