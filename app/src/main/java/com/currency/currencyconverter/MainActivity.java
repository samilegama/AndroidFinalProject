package com.currency.currencyconverter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    ImageButton currency;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Add a Toolbar to the Activity Layout
        Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
        setSupportActionBar(toolbar);

        currency = (ImageButton) findViewById(R.id.currencyButton);

        // Implement click function for currency conversion
        currency.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent currencyActivity = new Intent(MainActivity.this, CurrencyConverterActivity.class);
                startActivity(currencyActivity);
            }
        });
    }

    /**
     * The call to setActionBar(toolbar) calls this method
     *
     * @param menu
     * @return
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_toolbar_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            // Choice 1 is the currency converter
            case R.id.choice1:
                Intent currencyActivity = new Intent(MainActivity.this, CurrencyConverterActivity.class);
                startActivity(currencyActivity);
                break;
            // Choice 2 is the Car Charger
            case R.id.choice2:
                Toast.makeText(this, "Goes to Car Charger Activity", Toast.LENGTH_LONG).show();
                break;
            // Choice 3 is the Recipe Search
            case R.id.choice3:
                Toast.makeText(this, "Goes to Recipe Activity", Toast.LENGTH_LONG).show();
                break;
            // Choice 4 is the News Activity
            case R.id.choice4:
                Toast.makeText(this, "Goes to News Activity", Toast.LENGTH_LONG).show();
                break;
            // about is the overflow menu
            case R.id.about:
                Toast.makeText(this, "You clicked the Overflow Menu", Toast.LENGTH_LONG).show();
                break;
        }
        return true;
    }
}
