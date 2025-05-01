package com.example.lab6;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NotesManager {
    private static final String PREFS_NAME = "notes_app_prefs";
    private static final String NOTES_KEY = "notes";

    private SharedPreferences sharedPreferences;
    private Gson gson;
    private Context context;

    public NotesManager(Context context) {
        this.context = context;
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public List<Note> getAllNotes() {
        String notesJson = sharedPreferences.getString(NOTES_KEY, "");

        if (notesJson.isEmpty()) {
            return new ArrayList<>();
        }

        Type type = new TypeToken<List<Note>>() {}.getType();
        List<Note> notesList = gson.fromJson(notesJson, type);
        Collections.sort(notesList, (note1, note2) ->
                note2.getUpdatedAt().compareTo(note1.getUpdatedAt()));

        return notesList;
    }

    public Note getNoteById(String id) {
        List<Note> notes = getAllNotes();
        for (Note note : notes) {
            if (note.getId().equals(id)) {
                return note;
            }
        }
        return null;
    }

    public void addNote(Note note) {
        List<Note> notes = getAllNotes();
        notes.add(note);
        saveNotesList(notes);
    }

    public void updateNote(Note updatedNote) {
        List<Note> notes = getAllNotes();
        for (int i = 0; i < notes.size(); i++) {
            if (notes.get(i).getId().equals(updatedNote.getId())) {
                notes.set(i, updatedNote);
                break;
            }
        }
        saveNotesList(notes);
    }

    public void deleteNote(String id) {
        List<Note> notes = getAllNotes();
        notes.removeIf(note -> note.getId().equals(id));
        saveNotesList(notes);
    }

    private void saveNotesList(List<Note> notes) {
        String notesJson = gson.toJson(notes);
        sharedPreferences.edit().putString(NOTES_KEY, notesJson).apply();
        notifyWidgetDataChanged();
    }

    public void notifyWidgetDataChanged() {
        if (context != null) {
            AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
            int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                    new ComponentName(context, NotesWidget.class));

            Intent updateIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
            context.sendBroadcast(updateIntent);

            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_listview);
        }
    }
}