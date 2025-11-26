package ca.gbc.comp3074.uiprototype.ui.checkin;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

import ca.gbc.comp3074.uiprototype.R;

public class CheckInActivity extends AppCompatActivity {

    private TextView textPlaceName;
    private EditText inputNote;
    private MaterialButton btnConfirmCheckIn;

    // Selection states
    private String quietLevel = "Very Quiet";
    private String busyLevel = "Steady";
    private String wifiQuality = "Good Wifi";
    private String outletAvailability = "Plenty";

    // Buttons
    private MaterialButton btnNoisy, btnModerate, btnQuiet;
    private MaterialButton btnEmpty, btnSteady, btnFull;
    private MaterialButton btnGoodWifi, btnPoorWifi;
    private MaterialButton btnPlentyOutlets, btnScarceOutlets;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);

        // Initialize Views
        textPlaceName = findViewById(R.id.textPlaceName);
        inputNote = findViewById(R.id.inputNote);
        btnConfirmCheckIn = findViewById(R.id.btnConfirmCheckIn);

        // Initialize Buttons
        btnNoisy = findViewById(R.id.btnNoisy);
        btnModerate = findViewById(R.id.btnModerate);
        btnQuiet = findViewById(R.id.btnQuiet);

        btnEmpty = findViewById(R.id.btnEmpty);
        btnSteady = findViewById(R.id.btnSteady);
        btnFull = findViewById(R.id.btnFull);

        btnGoodWifi = findViewById(R.id.btnGoodWifi);
        btnPoorWifi = findViewById(R.id.btnPoorWifi);

        btnPlentyOutlets = findViewById(R.id.btnPlentyOutlets);
        btnScarceOutlets = findViewById(R.id.btnScarceOutlets);

        // Get data from Intent
        String placeName = getIntent().getStringExtra("PLACE_NAME");
        if (placeName != null) {
            textPlaceName.setText(placeName);
        }

        // Set up listeners
        setupQuietGroup();
        setupBusyGroup();
        setupWifiGroup();
        setupOutletGroup();

        // Back button
        findViewById(R.id.toolbar).setOnClickListener(v -> finish());

        // Confirm button
        btnConfirmCheckIn.setOnClickListener(v -> {
            // Here you would typically save the data to a database (e.g., Supabase)
            String note = inputNote.getText().toString();
            String message = "Checked in at " + placeName + "\n" +
                    "Quiet: " + quietLevel + "\n" +
                    "Busy: " + busyLevel + "\n" +
                    "Wifi: " + wifiQuality + "\n" +
                    "Outlets: " + outletAvailability;

            Toast.makeText(this, "Check-in Confirmed!", Toast.LENGTH_SHORT).show();
            finish();
        });
    }

    private void setupQuietGroup() {
        View.OnClickListener listener = v -> {
            resetButtonStyle(btnNoisy);
            resetButtonStyle(btnModerate);
            resetButtonStyle(btnQuiet);

            MaterialButton clicked = (MaterialButton) v;
            selectButtonStyle(clicked);

            if (v == btnNoisy)
                quietLevel = "Noisy";
            else if (v == btnModerate)
                quietLevel = "Moderate";
            else if (v == btnQuiet)
                quietLevel = "Very Quiet";
        };

        btnNoisy.setOnClickListener(listener);
        btnModerate.setOnClickListener(listener);
        btnQuiet.setOnClickListener(listener);

        // Default selection
        selectButtonStyle(btnQuiet);
    }

    private void setupBusyGroup() {
        View.OnClickListener listener = v -> {
            resetButtonStyle(btnEmpty);
            resetButtonStyle(btnSteady);
            resetButtonStyle(btnFull);

            MaterialButton clicked = (MaterialButton) v;
            selectButtonStyle(clicked);

            if (v == btnEmpty)
                busyLevel = "Empty";
            else if (v == btnSteady)
                busyLevel = "Steady";
            else if (v == btnFull)
                busyLevel = "Full";
        };

        btnEmpty.setOnClickListener(listener);
        btnSteady.setOnClickListener(listener);
        btnFull.setOnClickListener(listener);

        // Default selection
        selectButtonStyle(btnSteady);
    }

    private void setupWifiGroup() {
        View.OnClickListener listener = v -> {
            resetButtonStyle(btnGoodWifi);
            resetButtonStyle(btnPoorWifi);

            MaterialButton clicked = (MaterialButton) v;
            selectButtonStyle(clicked);

            if (v == btnGoodWifi)
                wifiQuality = "Good Wifi";
            else if (v == btnPoorWifi)
                wifiQuality = "No/Poor Wifi";
        };

        btnGoodWifi.setOnClickListener(listener);
        btnPoorWifi.setOnClickListener(listener);

        // Default selection
        selectButtonStyle(btnGoodWifi);
    }

    private void setupOutletGroup() {
        View.OnClickListener listener = v -> {
            resetButtonStyle(btnPlentyOutlets);
            resetButtonStyle(btnScarceOutlets);

            MaterialButton clicked = (MaterialButton) v;
            selectButtonStyle(clicked);

            if (v == btnPlentyOutlets)
                outletAvailability = "Plenty";
            else if (v == btnScarceOutlets)
                outletAvailability = "Scarce";
        };

        btnPlentyOutlets.setOnClickListener(listener);
        btnScarceOutlets.setOnClickListener(listener);

        // Default selection
        selectButtonStyle(btnPlentyOutlets);
    }

    private void selectButtonStyle(MaterialButton button) {
        button.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.quiet_space_primary));
        button.setTextColor(ContextCompat.getColor(this, R.color.white));
        button.setStrokeWidth(0);
    }

    private void resetButtonStyle(MaterialButton button) {
        button.setBackgroundTintList(ContextCompat.getColorStateList(this, android.R.color.white));
        button.setTextColor(ContextCompat.getColor(this, R.color.black));
        button.setStrokeColor(ContextCompat.getColorStateList(this, R.color.quiet_space_border));
        // Convert 1dp to px
        int strokeWidth = (int) (1 * getResources().getDisplayMetrics().density);
        button.setStrokeWidth(strokeWidth);
    }
}
