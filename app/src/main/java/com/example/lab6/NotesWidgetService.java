package com.example.lab6;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.AdapterView;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;

import java.util.ArrayList;
import java.util.List;

public class NotesWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new NotesRemoteViewsFactory(this.getApplicationContext(), intent);
    }

    class NotesRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
        private Context context;
        private List<Note> notes = new ArrayList<>();
        private NotesManager notesManager;

        public NotesRemoteViewsFactory(Context context, Intent intent) {
            this.context = context;
        }

        @Override
        public void onCreate() {
            notesManager = new NotesManager(context);
        }

        @Override
        public void onDataSetChanged() {
            notes = notesManager.getAllNotes();
        }

        @Override
        public void onDestroy() {
            notes.clear();
        }

        @Override
        public int getCount() {
            return notes.size();
        }

        @Override
        public RemoteViews getViewAt(int position) {
            if (position == AdapterView.INVALID_POSITION || notes.size() <= position) {
                return null;
            }

            Note note = notes.get(position);
            RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_note_item);

            rv.setTextViewText(R.id.widget_note_title, note.getTitle());

            String previewContent = note.getContent();
            if (previewContent.length() > 30) {
                previewContent = previewContent.substring(0, 30) + "...";
            }
            rv.setTextViewText(R.id.widget_note_content, previewContent);

            Intent fillInIntent = new Intent();
            fillInIntent.putExtra(NotesWidget.EXTRA_NOTE_ID, note.getId());

            rv.setOnClickFillInIntent(R.id.widget_note_item, fillInIntent);

            return rv;
        }

        @Override
        public RemoteViews getLoadingView() {
            return null;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }
    }
}