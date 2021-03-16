package io.ffem.collect.android.activities;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.widget.Toolbar;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.CollectAbstractActivity;
import org.odk.collect.android.fragments.dialogs.ResetSettingsResultDialog;
import org.odk.collect.android.listeners.OnBackPressedListener;
import org.odk.collect.android.preferences.ServerPreferences;
import org.odk.collect.android.preferences.screens.AdminPreferencesFragment;
import org.odk.collect.android.preferences.screens.FormManagementPreferencesFragment;

import io.ffem.collect.android.preferences.AppPreferences;
import io.ffem.collect.android.preferences.OtherPreferenceFragment;
import io.ffem.collect.android.preferences.TestingPreferenceFragment;

public class SettingsActivity extends CollectAbstractActivity
        implements ResetSettingsResultDialog.ResetSettingsResultDialogListener {

    private OnBackPressedListener onBackPressedListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActivity();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        setupActivity();
    }

    private void setupActivity() {

        setTitle(R.string.settings);

        setContentView(R.layout.activity_settings);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.layoutFormManagement, new FormManagementPreferencesFragment())
                .commitAllowingStateLoss();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.layoutInfo, new OtherPreferenceFragment())
                .commitAllowingStateLoss();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.layoutServer, new ServerPreferences())
                .commitAllowingStateLoss();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.layoutAdmin, new AdminPreferencesFragment())
                .commitAllowingStateLoss();

        if (AppPreferences.isDiagnosticMode(this)) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.layoutTesting, new TestingPreferenceFragment())
                    .commitAllowingStateLoss();

            findViewById(R.id.layoutTesting).setVisibility(View.VISIBLE);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        try {
            setSupportActionBar(toolbar);
        } catch (Exception ignored) {
        }

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        setTitle(R.string.settings);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (AppPreferences.isDiagnosticMode(this)) {
            getMenuInflater().inflate(R.menu.menu_settings, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDialogClosed() {
        finish();
    }

    public void onDisableDiagnostics(MenuItem item) {
        Toast.makeText(getBaseContext(), getString(R.string.diagnosticModeDisabled),
                Toast.LENGTH_SHORT).show();

        AppPreferences.disableDiagnosticMode(this);

//        changeActionBarStyleBasedOnCurrentMode();

        invalidateOptionsMenu();

        removeAllFragments();
    }

    private void removeAllFragments() {
        findViewById(R.id.layoutTesting).setVisibility(View.GONE);
    }

    public void setOnBackPressedListener(OnBackPressedListener onBackPressedListener) {
        this.onBackPressedListener = onBackPressedListener;
    }
}
