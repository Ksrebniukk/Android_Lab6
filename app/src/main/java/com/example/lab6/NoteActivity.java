package com.example.lab6;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.UUID;

public class NoteActivity extends AppCompatActivity {

    private EditText titleEditText, contentEditText;
    private NotesManager notesManager;
    private String noteId;
    private boolean isNewNote = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        titleEditText = findViewById(R.id.editTextTitle);
        contentEditText = findViewById(R.id.editTextContent);
        notesManager = new NotesManager(this);

        noteId = getIntent().getStringExtra("note_id");

        if (noteId != null) {
            isNewNote = false;
            loadNote(noteId);
            setTitle("Редагувати нотатку");
        } else {
            setTitle("Нова нотатка");
        }
    }

    private void loadNote(String id) {
        Note note = notesManager.getNoteById(id);
        if (note != null) {
            titleEditText.setText(note.getTitle());
            contentEditText.setText(note.getContent());
        }
    }

    private void saveNote() {
        String title = titleEditText.getText().toString().trim();
        String content = contentEditText.getText().toString().trim();

        if (title.isEmpty()) {
            Toast.makeText(this, "Введіть заголовок", Toast.LENGTH_SHORT).show();
            return;
        }

        String currentTime = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                .format(new Date());

        Note note;
        if (isNewNote) {
            note = new Note(UUID.randomUUID().toString(), title, content, currentTime, currentTime);
            notesManager.addNote(note);
            Toast.makeText(this, "Нотатку створено", Toast.LENGTH_SHORT).show();
        } else {
            Note oldNote = notesManager.getNoteById(noteId);
            note = new Note(noteId, title, content, oldNote.getCreatedAt(), currentTime);
            notesManager.updateNote(note);
            Toast.makeText(this, "Нотатку оновлено", Toast.LENGTH_SHORT).show();
        }

        updateWidgets();

        setResult(RESULT_OK);
        finish();
    }

    private void deleteNote() {
        if (!isNewNote) {
            new AlertDialog.Builder(this)
                    .setTitle("Видалити нотатку")
                    .setMessage("Ви впевнені, що хочете видалити цю нотатку?")
                    .setPositiveButton("Видалити", (dialog, which) -> {
                        notesManager.deleteNote(noteId);
                        Toast.makeText(NoteActivity.this, "Нотатку видалено", Toast.LENGTH_SHORT).show();

                        updateWidgets();

                        setResult(RESULT_OK);
                        finish();
                    })
                    .setNegativeButton("Скасувати", null)
                    .show();
        }
    }

    private void updateWidgets() {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        int[] appWidgetIds = appWidgetManager.getAppWidgetIds(
                new ComponentName(this, NotesWidget.class));

        Intent updateIntent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        sendBroadcast(updateIntent);

        appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetIds, R.id.widget_listview);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_note, menu);
        MenuItem deleteItem = menu.findItem(R.id.action_delete);
        deleteItem.setVisible(!isNewNote);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.action_save) {
            saveNote();
            return true;
        } else if (id == R.id.action_delete) {
            deleteNote();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        String title = titleEditText.getText().toString().trim();
        String content = contentEditText.getText().toString().trim();

        if (!title.isEmpty() || !content.isEmpty()) {
            new AlertDialog.Builder(this)
                    .setTitle("Зберегти зміни?")
                    .setMessage("Хочете зберегти зміни перед виходом?")
                    .setPositiveButton("Зберегти", (dialog, which) -> saveNote())
                    .setNegativeButton("Відхилити", (dialog, which) -> {
                        super.onBackPressed();
                    })
                    .setCancelable(true)
                    .show();
        } else {
            super.onBackPressed();
        }
    }
}