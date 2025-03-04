package com.dariya.taxapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private EditText etIncome, etRRSP;
    private TextView tvTax, tvRRSP, tvSlider;
    private SeekBar rrspSlider;
    private Button btnCalculate, btnRefresh, btnReset;
    private SharedPreferences sharedPreferences;
    private static final double MAX_RRSP_LIMIT = 27230; // Per instructions given even though for 2024 it is 31560

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        etIncome = findViewById(R.id.etIncome);
        etRRSP = findViewById(R.id.etRRSP);
        tvTax = findViewById(R.id.tvTax);
        tvRRSP = findViewById(R.id.tvRRSP);
        tvSlider = findViewById(R.id.tvSlider);
        rrspSlider = findViewById(R.id.sliderRRSP);
        btnCalculate = findViewById(R.id.bt1);
        btnRefresh = findViewById(R.id.bt2);
        btnReset = findViewById(R.id.bt3);

        // Sets up shared preferences
        sharedPreferences = getSharedPreferences("TaxAppData", MODE_PRIVATE);

        // Sets maximum value for RRSP slider
        rrspSlider.setMax((int) MAX_RRSP_LIMIT);

        // Loads saved preferences
        loadPreferences();

        // Sets up button click listeners
        btnCalculate.setOnClickListener(v -> calculateTaxes());

        btnRefresh.setOnClickListener(v -> {
            refreshData();
            Toast.makeText(this, "Data refreshed and recalculated.", Toast.LENGTH_LONG).show();
        });

        btnReset.setOnClickListener(v -> {
            resetData();
            Toast.makeText(this, "Data reset to default values.", Toast.LENGTH_LONG).show();
        });

        // Sets up RRSP slider change listener ( based on how the user will slide the slider)
        rrspSlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                tvSlider.setText("RRSP Adjustment: $" + progress);
                calculateTaxes();
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void calculateTaxes() {
        String incomeText = etIncome.getText().toString();
        String rrspText = etRRSP.getText().toString();

        // Validates input fields if they are empty
        if (incomeText.isEmpty() || rrspText.isEmpty()) {
            Toast.makeText(this, "Please enter valid income and RRSP values.", Toast.LENGTH_LONG).show();
            return;
        }

        double income;
        double baseRRSP;

        try {
            income = Double.parseDouble(incomeText);
            baseRRSP = Double.parseDouble(rrspText);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid number format. Please enter numeric values.", Toast.LENGTH_LONG).show();
            return;
        }

        double rrspAdjustment = rrspSlider.getProgress();
        double totalRRSP = baseRRSP + rrspAdjustment;

        // Ensures total RRSP does not exceed maximum limit, because it should not
        if (totalRRSP > MAX_RRSP_LIMIT) {
            totalRRSP = MAX_RRSP_LIMIT;
            rrspAdjustment = MAX_RRSP_LIMIT - baseRRSP;
            rrspSlider.setProgress((int) rrspAdjustment);
            tvSlider.setText("RRSP Adjustment: $" + rrspAdjustment);
            Toast.makeText(this, "Total RRSP contribution adjusted to the maximum limit of $" + MAX_RRSP_LIMIT, Toast.LENGTH_LONG).show();
        }

        // Calculates taxes using the Tax class
        Tax taxCalculator = new Tax(income, totalRRSP);
        double calculatedTax = taxCalculator.TaxCalculator();
        double nextYearLimit = taxCalculator.NextLimit();

        // Displays calculated tax and next year's RRSP limit
        tvTax.setText("Taxes Owed: $" + String.format("%.2f", calculatedTax));
        tvRRSP.setText("Next Year RRSP Limit: $" + String.format("%.2f", nextYearLimit));

        // Saves current income and base RRSP to preferences
        savePreferences(income, baseRRSP);
    }

    private void refreshData() {
        // Gets current income and RRSP from EditText and Slider
        String incomeText = etIncome.getText().toString();
        String rrspText = etRRSP.getText().toString();
        double income = 0;
        double baseRRSP = 0;

        // Parses current income and RRSP values
        try {
            income = Double.parseDouble(incomeText);
            baseRRSP = Double.parseDouble(rrspText);
        }
        //And if it fails, such as invalid input
        catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid input values. Resetting to defaults.", Toast.LENGTH_LONG).show();
        }

        // Adds the current RRSP slider value to the base RRSP
        double rrspAdjustment = rrspSlider.getProgress();
        double totalRRSP = baseRRSP + rrspAdjustment;

        // Updates the RRSP EditText with the new total RRSP
        etRRSP.setText(String.valueOf(totalRRSP));

        // Saves the updated income and total RRSP to SharedPreferences
        savePreferences(income, totalRRSP);

        // Recalculates taxes with the updated data
        calculateTaxes();
    }

    private void loadPreferences() {
        double income = sharedPreferences.getFloat("income", 0);
        double baseRRSP = sharedPreferences.getFloat("rrsp", 0);

        // Sets the inputs with the provided data
        etIncome.setText(String.valueOf(income));
        etRRSP.setText(String.valueOf(baseRRSP));
        rrspSlider.setProgress(0);
        tvSlider.setText("RRSP Adjustment: $0");

        // Recalculate taxes with the new information given
        calculateTaxes();
    }

    private void savePreferences(double income, double baseRRSP) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putFloat("income", (float) income);
        editor.putFloat("rrsp", (float) baseRRSP);
        editor.apply();
    }

    private void resetData() {
        // Clears the input fields as well as the slider
        etIncome.setText("");
        etRRSP.setText("");
        rrspSlider.setProgress(0);
        tvSlider.setText("RRSP Adjustment: $0");

        // Clears all of the sharedprefrences data
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.clear();
        editor.apply();

        // Resets the displayed TV's to the 0 values
        tvTax.setText("Taxes Owed: $0.00");
        tvRRSP.setText("Next Year RRSP Limit: $0.00");
    }
}
