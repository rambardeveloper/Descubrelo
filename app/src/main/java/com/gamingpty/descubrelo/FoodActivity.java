package com.gamingpty.descubrelo;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;

public class FoodActivity extends AppCompatActivity {

    String placeId;
    String placeTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_food);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        placeId = intent.getStringExtra("place_id");
        placeTitle = intent.getStringExtra("title");
        getSupportActionBar().setTitle(placeTitle);
        getSupportActionBar().setElevation(0);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }
}
