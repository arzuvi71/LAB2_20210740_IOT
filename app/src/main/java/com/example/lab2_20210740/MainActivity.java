package com.example.lab2_20210740;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;
import android.widget.Toast;
import com.example.lab2_20210740.databinding.WelcomePageBinding;

public class MainActivity extends AppCompatActivity {

    private WelcomePageBinding binding;
    private boolean tieneInternet = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = WelcomePageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        configurarListeners();

        // Inicialmente ocultamos el campo de escribir texto
        binding.textView4.setVisibility(View.GONE);
        binding.inputTexto1.setVisibility(View.GONE);

        // Botón comenzar inicialmente deshabilitado
        binding.button2.setEnabled(false);
    }

    private void configurarListeners() {
        // Listener para el RadioGroup (Texto Si/No)
        binding.rgTexto.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rbSi) {
                    // Mostramos campo "Escribir texto"
                    binding.textView4.setVisibility(View.VISIBLE);
                    binding.inputTexto1.setVisibility(View.VISIBLE);
                    binding.inputTexto1.setEnabled(true);
                    binding.inputTexto1.setAlpha(1.0f);
                } else {
                    // Ocultamos campo "Escribir texto"
                    binding.textView4.setVisibility(View.GONE);
                    binding.inputTexto1.setVisibility(View.GONE);
                    binding.inputTexto1.setText("");
                }
            }
        });

        binding.button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                comprobarConexionInternet();
            }
        });

        binding.button2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                validarYComenzar();
            }
        });
    }

    private void comprobarConexionInternet() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connectivityManager != null) {
            NetworkCapabilities capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());

            if (capabilities != null) {
                if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {

                    tieneInternet = true;
                    binding.button2.setEnabled(true);
                    binding.button.setText("✅ Conectado");
                    binding.button.setBackgroundTintList(getColorStateList(android.R.color.holo_green_light));
                    Toast.makeText(this, "Conexión a internet exitosa", Toast.LENGTH_SHORT).show();
                    return;
                }
            }
        }

        tieneInternet = false;
        binding.button2.setEnabled(false);
        binding.button.setText("❌ Sin conexión");
        binding.button.setBackgroundTintList(getColorStateList(android.R.color.holo_red_light));
        Toast.makeText(this, "Error: No hay conexión a internet", Toast.LENGTH_SHORT).show();
    }

    private void validarYComenzar() {
        // Validamos que la cantidad no esté vacía
        String cantidadStr = binding.inputTexto.getText().toString().trim();
        if (cantidadStr.isEmpty()) {
            Toast.makeText(this, "Debe ingresar una cantidad", Toast.LENGTH_SHORT).show();
            return;
        }

        // Consideraremos un máximo de 10 imagenes a edir por lo cual validaremos un rango entre 1 y 10
        try {
            int cantidad = Integer.parseInt(cantidadStr);
            if (cantidad < 1 || cantidad > 10) {
                Toast.makeText(this, "La cantidad debe estar entre 1 y 10", Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Ingrese un número válido", Toast.LENGTH_SHORT).show();
            return;
        }

        // Validaremos que si se eligió "Si" en Texto, se haya escrito algo en "Escribir texto"
        if (binding.rbSi.isChecked()) {
            String escribirTexto = binding.inputTexto1.getText().toString().trim();
            if (escribirTexto.isEmpty()) {
                Toast.makeText(this, "Debe escribir texto cuando selecciona 'Si'", Toast.LENGTH_SHORT).show();
                return;
            }
        }

        // Validamos conexión a internet
        if (!tieneInternet) {
            Toast.makeText(this, "Necesita conexión a Internet para continuar", Toast.LENGTH_SHORT).show();
            return;
        }

        int cantidad = Integer.parseInt(cantidadStr);
        boolean hasText = binding.rbSi.isChecked();
        String customText = hasText ? binding.inputTexto1.getText().toString().trim() : "";

        // Iniciamos GameActivity
        Intent intent = new Intent(this, GameActivity.class);
        intent.putExtra("quantity", cantidad);
        intent.putExtra("hasText", hasText);
        intent.putExtra("customText", customText);
        startActivity(intent);

        Toast.makeText(this, "Proceso iniciado con: " + cantidad + " imágenes", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}