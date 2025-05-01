package com.example.lab6;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.widget.RemoteViews;

public class NotesWidget extends AppWidgetProvider {

    public static final String EXTRA_NOTE_ID = "com.example.lab6.EXTRA_NOTE_ID";
    public static final String ACTION_VIEW_NOTE = "com.example.lab6.ACTION_VIEW_NOTE";

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (ACTION_VIEW_NOTE.equals(intent.getAction())) {
            String noteId = intent.getStringExtra(EXTRA_NOTE_ID);
            if (noteId != null) {
                Intent noteIntent = new Intent(context, NoteActivity.class);
                noteIntent.putExtra("note_id", noteId);
                noteIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(noteIntent);
            }
        }
        super.onReceive(context, intent);
    }

    static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.notes_widget);

        Intent intent = new Intent(context, NotesWidgetService.class);
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        intent.setData(Uri.parse(intent.toUri(Intent.URI_INTENT_SCHEME)));

        views.setRemoteAdapter(R.id.widget_listview, intent);
        views.setEmptyView(R.id.widget_listview, R.id.widget_empty_view);

        Intent viewNoteIntent = new Intent(context, NotesWidget.class);
        viewNoteIntent.setAction(ACTION_VIEW_NOTE);
        PendingIntent viewNotePendingIntent = PendingIntent.getBroadcast(
                context, 0, viewNoteIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE);
        views.setPendingIntentTemplate(R.id.widget_listview, viewNotePendingIntent);

        Intent newNoteIntent = new Intent(context, NoteActivity.class);
        newNoteIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent newNotePendingIntent = PendingIntent.getActivity(context, 1, newNoteIntent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        views.setOnClickPendingIntent(R.id.widget_add_button, newNotePendingIntent);

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}