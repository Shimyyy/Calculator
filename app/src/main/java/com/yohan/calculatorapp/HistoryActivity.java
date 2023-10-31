package com.yohan.calculatorapp;

import android.database.Cursor;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import androidx.appcompat.app.AppCompatActivity;

public class HistoryActivity extends AppCompatActivity {
    DatabaseHelper myDb;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        myDb = new DatabaseHelper(this);
        listView = findViewById(R.id.listView);

        viewAll();
    }

    public void viewAll() {
        Cursor res = myDb.getAllData();
        String[] from = new String[]{"_id", "INPUT"};
        int[] to = new int[]{R.id.textViewId, R.id.textViewInput};
        SimpleCursorAdapter cursorAdapter = new SimpleCursorAdapter(this, R.layout.list_item, res, from, to, 0);
        listView.setAdapter(cursorAdapter);
    }
}
