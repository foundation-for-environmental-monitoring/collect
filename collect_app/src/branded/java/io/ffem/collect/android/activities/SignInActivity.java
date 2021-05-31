package io.ffem.collect.android.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputLayout;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.CollectAbstractActivity;
import org.odk.collect.android.activities.MainMenuActivity;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.logic.PropertyManager;
import org.odk.collect.android.preferences.source.SettingsProvider;
import org.odk.collect.android.projects.CurrentProjectProvider;
import org.odk.collect.android.projects.ProjectImporter;
import org.odk.collect.android.utilities.ThemeUtils;
import org.odk.collect.android.utilities.WebCredentialsUtils;
import org.odk.collect.projects.Project;

import javax.inject.Inject;

import io.ffem.collect.android.util.AdjustingViewGlobalLayoutListener;

import static android.view.View.GONE;

/**
 * Sign in screen shown on app first launch
 */
public class SignInActivity extends CollectAbstractActivity {
    private static final String MASK = "**********";

    WebCredentialsUtils webCredentialsUtils;
    @Inject
    PropertyManager propertyManager;
    @Inject
    ProjectImporter projectImporter;
    @Inject
    CurrentProjectProvider currentProjectProvider;

    private EditText editText;
    private EditText editPassword;
    private TextInputLayout layoutUserName;
    private TextInputLayout layoutPassword;
    private boolean isSettings;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        DaggerUtils.getComponent(this).inject(this);

        isSettings = getIntent().getBooleanExtra("isSettings", false);

        try {
            currentProjectProvider.getCurrentProject();
        } catch (Exception e) {
            String projectName = "";
            if (webCredentialsUtils != null) {
                projectName = webCredentialsUtils.getUserNameFromPreferences();
            }
            currentProjectProvider.setCurrentProject(projectImporter
                    .importExistingProject(projectName).getUuid());
        }

        webCredentialsUtils = new WebCredentialsUtils(new SettingsProvider(this).getGeneralSettings());
        if (!webCredentialsUtils.getPasswordFromPreferences().isEmpty() && !isSettings) {
            startActivity(new Intent(getBaseContext(), MainMenuActivity.class));
            finish();
        }

        super.onCreate(savedInstanceState);

        ThemeUtils themeUtils = new ThemeUtils(this);
        setTheme(themeUtils.getAppTheme());

        setContentView(R.layout.activity_signin);

        Toolbar toolbar = findViewById(R.id.toolbar);
        try {
            setSupportActionBar(toolbar);
        } catch (Exception ignored) {
        }

        if (isSettings) {

            findViewById(R.id.imageLogo).setVisibility(GONE);

            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }

            setTitle(R.string.server_credentials);
        } else {
            if (toolbar != null) {
                toolbar.setVisibility(GONE);
            }
        }

        initialize();

    }

    private String maskPassword(String password) {
        return password != null && password.length() > 0
                ? MASK
                : "";
    }

    private void initialize() {
        editText = findViewById(R.id.editUsername);
        editPassword = findViewById(R.id.editPassword);

        findViewById(R.id.textCreateAccount).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(getResources().getString(R.string.create_account_link)));
            startActivity(intent);
        });

        findViewById(R.id.textForgot).setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(getResources().getString(R.string.reset_password_link)));
            startActivity(intent);
        });

        findViewById(R.id.buttonSignIn).setOnClickListener(view -> {
            if (isInputValid()) {
                if (!editPassword.getText().toString().equals(MASK)) {
                    webCredentialsUtils.saveCredentialsPreferences(editText.getText().toString().trim(),
                            editPassword.getText().toString().trim(), propertyManager);
                }

                String projectName = "Existing";
                if (webCredentialsUtils != null) {
                    projectName = webCredentialsUtils.getUserNameFromPreferences();
                }
                Project.Saved project = new Project.Saved(currentProjectProvider.getCurrentProjectId(),
                        projectName, projectName.substring(0, 1).toUpperCase(), "#3e9fcc");
                projectImporter.updateProject(project);

                startActivity(new Intent(getBaseContext(), MainMenuActivity.class));
                finish();
            }
        });

        layoutUserName = findViewById(R.id.layoutUsername);
        layoutPassword = findViewById(R.id.passwordLayout);

        if (!webCredentialsUtils.getPasswordFromPreferences().isEmpty() || isSettings) {
            editText.setText(webCredentialsUtils.getUserNameFromPreferences());
            editPassword.setText(maskPassword(webCredentialsUtils.getPasswordFromPreferences()));
        }

        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                layoutUserName.setError(null);
                layoutUserName.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        editPassword.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                layoutPassword.setError(null);
                layoutPassword.setErrorEnabled(false);
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

        AdjustingViewGlobalLayoutListener listen =
                new AdjustingViewGlobalLayoutListener(findViewById(R.id.authLayout));
        getWindow().getDecorView().getRootView().getViewTreeObserver().addOnGlobalLayoutListener(listen);
    }

    private boolean isInputValid() {
        String username = editText.getText().toString().trim();
        String password = editPassword.getText().toString().trim();

        if (username.isEmpty()) {
            layoutUserName.setErrorEnabled(true);
            layoutUserName.setError(getResources().getString(R.string.enter_username));
            return false;
        } else {
            layoutUserName.setErrorEnabled(false);
            layoutUserName.setError(null);
        }

        if (password.isEmpty()) {
            layoutPassword.setErrorEnabled(true);
            layoutPassword.setError(getResources().getString(R.string.enter_password));
            return false;
        } else {
            layoutPassword.setErrorEnabled(false);
            layoutPassword.setError(null);
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
}