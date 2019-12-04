package com.currency.currencyconverter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;

import org.json.JSONException;
import org.json.JSONObject;


import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

public class CurrencyConverterActivity extends AppCompatActivity {
	Spinner baseSpinner;
	Spinner finalSpinner;

	Button convertButton;

	EditText etAmount;
	TextView baseAmount;
	TextView finalAmount;

	double amount;
	double value;

	// This ensures that all currencies are shown to 2 decimal places
	DecimalFormat currency = new DecimalFormat("###,###.00");

	ProgressBar status;

	// Items to display - might need to modify this
	ArrayList<String> convertList = new ArrayList<>(Arrays.asList("CAD","USD","EUR"));

	BaseAdapter currAdapter;

	public int to;
	public int from;
	public String [] val;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
		// Add a Toolbar to the Activity Layout
		Toolbar toolbar = (Toolbar) findViewById(R.id.main_toolbar);
		setSupportActionBar(toolbar);

		baseSpinner = (Spinner) findViewById(R.id.baseSymbol);
		finalSpinner = (Spinner) findViewById(R.id.finalSymbol);

		etAmount = (EditText) findViewById(R.id.etAmount);
		baseAmount = (TextView) findViewById(R.id.baseAmount);
		finalAmount = (TextView) findViewById(R.id.finalAmount);

		status = (ProgressBar) findViewById(R.id.status);

		convertButton = (Button) findViewById(R.id.convertButton);

		// Create an array adapter for the spinners
		ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(
				this, R.array.name, android.R.layout.simple_spinner_item);

		adapter.setDropDownViewResource(android.R.layout.select_dialog_singlechoice);

		val = getResources().getStringArray(R.array.value);

		baseSpinner.setAdapter(adapter);
		finalSpinner.setAdapter(adapter);

		baseSpinner.setOnItemSelectedListener(new spinOne(1));
		finalSpinner.setOnItemSelectedListener(new spinOne(2));

		status.setVisibility(View.VISIBLE);

		// Convert Button Code
		convertButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {

				if(etAmount.getText().toString().trim().equals("")) {
					Toast.makeText(CurrencyConverterActivity.this, "Input a number", Toast.LENGTH_SHORT).show();
				}
				if (from == to){
					Toast.makeText(getApplicationContext(), "Invalid: Select different currencies", Toast.LENGTH_SHORT).show();
				}
				else{

					amount = Double.parseDouble(etAmount.getText().toString());
					runQuery(amount);
				}
			}
		});

		/**
		 * Code for manipulating ListView
		 */
		ListView theList = findViewById(R.id.theList);
		theList.setAdapter(currAdapter = new CurrencyListAdapter());
		theList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
				Toast.makeText(CurrencyConverterActivity.this, "You clicked on: " + convertList.get(i) + "\nLocated at index: " + i, Toast.LENGTH_LONG).show();
			}
		});

		// Button code for adding to the list
		Button addButton = findViewById(R.id.addToListButton);
		addButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
//                convertList.add("Currency " + (1+convertList.size()));
				convertList.add(val[from]);
				currAdapter.notifyDataSetChanged();
			}
		});

		// Code for SwipeRefreshLayout
		final SwipeRefreshLayout refresher = findViewById(R.id.refresher);
		refresher.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
			@Override
			public void onRefresh() {
//                convertList.add("Currency " + (1+convertList.size()));
				convertList.add(val[from]);
				currAdapter.notifyDataSetChanged();
				refresher.setRefreshing(false);
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
				Toast.makeText(this, "You are already in the Currency Activity", Toast.LENGTH_SHORT).show();
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
				// Load a custom help menu dialog
				currencyCustomDialog();
				break;
		}
		return true;
	}

	/**
	 *
	 * @param amount
	 */
	private void runQuery(double amount){
		CurrencyConvertQuery theQuery = new CurrencyConvertQuery(amount);
		theQuery.execute();
	}

	/**
	 * This class performs network access to the API on behalf of the CurrencyConversion Activity
	 *
	 * @author Samuel Ebba
	 */
	private class CurrencyConvertQuery extends AsyncTask<String, Integer, String> {

		String baseCurrency;
		String currentDate;
		double rate;

		double amount;

		public CurrencyConvertQuery(double amount) {
			this.amount = amount;
		}

		String base = baseSpinner.toString();

		@Override
		protected String doInBackground(String... strings) {
			String retVal = null;

//            String queryURL = "https://api.exchangeratesapi.io/latest?base=USD&symbols=EUR";
			String queryURL = "https://api.exchangeratesapi.io/latest?base=" + val[from] + "&symbols=" + val[to];

			try {
				URL url = new URL(queryURL);
				HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
				InputStream inputStream = urlConnection.getInputStream();

				// Set up JSON object parser
				BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 8);
				StringBuilder sb = new StringBuilder();

				String line = null;
				while((line = reader.readLine()) != null)
				{
					sb.append(line + "\n");
				}
				String result = sb.toString();
				JSONObject jsonObject = new JSONObject(result);
				baseCurrency = jsonObject.getString("base");
				publishProgress(15);
				currentDate = jsonObject.getString("date");
				publishProgress(30);
				JSONObject rates = jsonObject.getJSONObject("rates");
				publishProgress(75);

				rate = rates.getDouble(val[to]);
				// get value from EditText
				value = amount * rate;

			}
			catch (MalformedURLException mue){ retVal = "Malformed URL Exception"; }
			catch (IOException ioe) { retVal = "IOException. Is the wifi connected?"; }
			catch (JSONException e) { retVal = "JSON Exception";}


			return retVal;
		}

		@Override
		protected void onPostExecute(String s) {
			super.onPostExecute(s);

			status.setVisibility(View.INVISIBLE);

			// set the base amount
			baseAmount.setText(String.valueOf(currency.format(amount)));

			// set the final amount
			finalAmount.setText(String.valueOf(currency.format(value)));

		}

		@Override
		protected void onProgressUpdate(Integer... values) {
			super.onProgressUpdate(values);

			//Update GUI stuff only:
			status.setVisibility(View.VISIBLE);
			status.setProgress(values[0]);
		}
	}

	/**
	 * This class manages the currency list display
	 *
	 * @author Samuel Ebba
	 */
	private class CurrencyListAdapter extends BaseAdapter{

		// Tells the size of the list
		@Override
		public int getCount() {
			return convertList.size();
		}

		//This returns the string at position i
		@Override
		public String getItem(int i) {
			return convertList.get(i);
		}

		// This returns the database id at position i
		@Override
		public long getItemId(int i) {
			return i;
		}

		// Displays the custom view to be inserted into each row of the list
		@Override
		public View getView(int i, View view, ViewGroup viewGroup) {
			View thisRow = view;

			if(view == null){
				thisRow = getLayoutInflater().inflate(R.layout.currency_row_layout, null);
			}

			TextView fromText = thisRow.findViewById(R.id.from);
            fromText.setText("Array at: " + i + " is " + getItem(i));


			TextView toText = thisRow.findViewById(R.id.to);
			toText.setText("Index is: " + i);

			return thisRow;
		}
	}

	/**
	 * This method builds an AlertDialog using the builder pattern.
	 * The method get called when a user clicks the overflow menu option
	 * on the Currency Conversion Activity
	 */
	public void currencyCustomDialog(){
		LayoutInflater inflater = getLayoutInflater();
		View v = inflater.inflate(R.layout.currency_dialog, null);

		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage("Currency Conversion Help Menu")
				.setPositiveButton("OK", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {

					}
				})
				.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {

					}
				}).setView(v);
		builder.create().show();
	}

	/**
	 * A class that handles items selected from spinner
	 *
	 * @author Samuel Ebba
	 */
	private class spinOne implements AdapterView.OnItemSelectedListener{

		int ide;
		spinOne(int i) { ide = i; }

		@Override
		public void onItemSelected(AdapterView<?> adapterView, View view, int index, long l) {
			if (ide == 1){
				from = index;
			}
			else if (ide == 2){
				to = index;
			}
		}

		@Override
		public void onNothingSelected(AdapterView<?> adapterView) {

		}
	}
}

