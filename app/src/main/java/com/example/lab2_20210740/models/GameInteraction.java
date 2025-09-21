package com.example.lab2_20210740.models;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class GameInteraction {
    private int interactionNumber;
    private int quantity;
    private boolean hasText;
    private String customText;
    private int totalTime;
    private String dateTime;
    
    public GameInteraction(int interactionNumber, int quantity, boolean hasText, String customText, int totalTime) {
        this.interactionNumber = interactionNumber;
        this.quantity = quantity;
        this.hasText = hasText;
        this.customText = customText != null ? customText : "";
        this.totalTime = totalTime;
        
        // Generar fecha y hora actual
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
        this.dateTime = sdf.format(new Date());
    }
    
    // Constructor para deserialización
    public GameInteraction(int interactionNumber, int quantity, boolean hasText, String customText, int totalTime, String dateTime) {
        this.interactionNumber = interactionNumber;
        this.quantity = quantity;
        this.hasText = hasText;
        this.customText = customText != null ? customText : "";
        this.totalTime = totalTime;
        this.dateTime = dateTime;
    }
    
    // Getters
    public int getInteractionNumber() { return interactionNumber; }
    public int getQuantity() { return quantity; }
    public boolean hasText() { return hasText; }
    public String getCustomText() { return customText; }
    public int getTotalTime() { return totalTime; }
    public String getDateTime() { return dateTime; }
    
    // Método para formatear tiempo
    public String getFormattedTime() {
        int minutes = totalTime / 60;
        int seconds = totalTime % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }
    
    // Método para generar resumen de la interacción
    public String generateSummary() {
        StringBuilder summary = new StringBuilder();
        summary.append("Interacción ").append(interactionNumber).append(": ").append(quantity).append(" imágenes\n");
        summary.append("📅 ").append(dateTime).append("\n");
        summary.append("⏱️ Tiempo: ").append(getFormattedTime()).append("\n");
        
        if (hasText && !customText.isEmpty()) {
            summary.append("✏️ Texto personalizado: \"").append(customText).append("\"\n");
        } else {
            summary.append("✏️ Sin texto personalizado\n");
        }
        
        return summary.toString();
    }
    
    // Método para serializar a String (para SharedPreferences)
    public String serialize() {
        return interactionNumber + "|" + quantity + "|" + hasText + "|" + customText + "|" + totalTime + "|" + dateTime;
    }
    
    // Método estático para deserializar desde String
    public static GameInteraction deserialize(String data) {
        String[] parts = data.split("\\|");
        if (parts.length >= 6) {
            return new GameInteraction(
                Integer.parseInt(parts[0]),
                Integer.parseInt(parts[1]),
                Boolean.parseBoolean(parts[2]),
                parts[3],
                Integer.parseInt(parts[4]),
                parts[5]
            );
        }
        return null;
    }
}
