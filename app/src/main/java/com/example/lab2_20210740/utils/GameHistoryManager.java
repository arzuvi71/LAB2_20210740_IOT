package com.example.lab2_20210740.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.lab2_20210740.models.GameInteraction;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class GameHistoryManager {
    private static final String PREFS_NAME = "game_history";
    private static final String KEY_INTERACTIONS = "interactions";
    private static final String KEY_INTERACTION_COUNT = "interaction_count";
    
    private SharedPreferences prefs;
    
    public GameHistoryManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }
    
    // Agregar nueva interacción
    public void addInteraction(int quantity, boolean hasText, String customText, int totalTime) {
        // Obtener número de interacción actual
        int interactionNumber = getNextInteractionNumber();
        
        // Crear nueva interacción
        GameInteraction interaction = new GameInteraction(interactionNumber, quantity, hasText, customText, totalTime);
        
        // Obtener interacciones existentes
        Set<String> existingInteractions = prefs.getStringSet(KEY_INTERACTIONS, new HashSet<>());
        Set<String> updatedInteractions = new HashSet<>(existingInteractions);
        
        // Agregar nueva interacción
        updatedInteractions.add(interaction.serialize());
        
        // Guardar en SharedPreferences
        SharedPreferences.Editor editor = prefs.edit();
        editor.putStringSet(KEY_INTERACTIONS, updatedInteractions);
        editor.putInt(KEY_INTERACTION_COUNT, interactionNumber);
        editor.apply();
    }
    
    // Obtener todas las interacciones
    public List<GameInteraction> getAllInteractions() {
        Set<String> interactionStrings = prefs.getStringSet(KEY_INTERACTIONS, new HashSet<>());
        List<GameInteraction> interactions = new ArrayList<>();
        
        for (String interactionString : interactionStrings) {
            GameInteraction interaction = GameInteraction.deserialize(interactionString);
            if (interaction != null) {
                interactions.add(interaction);
            }
        }
        
        // Ordenar por número de interacción
        interactions.sort((a, b) -> Integer.compare(a.getInteractionNumber(), b.getInteractionNumber()));
        
        return interactions;
    }
    
    // Obtener siguiente número de interacción
    private int getNextInteractionNumber() {
        int currentCount = prefs.getInt(KEY_INTERACTION_COUNT, 0);
        return currentCount + 1;
    }
    
    // Obtener número total de interacciones
    public int getTotalInteractions() {
        return prefs.getInt(KEY_INTERACTION_COUNT, 0);
    }
    
    // Limpiar historial (opcional)
    public void clearHistory() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.clear();
        editor.apply();
    }
    
    // Generar historial completo como String
    public String generateFullHistory() {
        List<GameInteraction> interactions = getAllInteractions();
        
        if (interactions.isEmpty()) {
            return "No hay interacciones registradas aún.\n¡Juega tu primera partida!";
        }
        
        StringBuilder fullHistory = new StringBuilder();
        fullHistory.append("=== HISTORIAL COMPLETO ===\n\n");
        
        for (GameInteraction interaction : interactions) {
            fullHistory.append(interaction.generateSummary());
            fullHistory.append("\n");
        }
        
        fullHistory.append("Total de partidas jugadas: ").append(interactions.size());
        
        return fullHistory.toString();
    }
}
