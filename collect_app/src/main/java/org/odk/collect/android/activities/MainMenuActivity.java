/*
 * Copyright (C) 2009 University of Washington
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.odk.collect.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.viewmodels.MainMenuViewModel;
import org.odk.collect.android.configure.qr.QRCodeTabsActivity;
import org.odk.collect.android.gdrive.GoogleDriveActivity;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.network.NetworkStateProvider;
import org.odk.collect.android.preferences.dialogs.AdminPasswordDialogFragment;
import org.odk.collect.android.preferences.dialogs.AdminPasswordDialogFragment.Action;
import org.odk.collect.android.preferences.keys.GeneralKeys;
import org.odk.collect.android.preferences.screens.AdminPreferencesActivity;
import org.odk.collect.android.projects.ProjectSettingsDialog;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.MultiClickGuard;
import org.odk.collect.android.utilities.PlayServicesChecker;
import org.odk.collect.android.utilities.ToastUtils;

import javax.inject.Inject;

import org.odk.collect.android.instances.Instance;
import org.odk.collect.android.instances.InstancesRepository;
import io.ffem.collect.android.activities.MainMenuActivityBranded;

/**
 * Responsible for displaying buttons to launch the major activities. Launches
 * some activities based on returns of others.
 *
 * @author Carl Hartung (carlhartung@gmail.com)
 * @author Yaw Anokwa (yanokwa@gmail.com)
 */
public class MainMenuActivity extends MainMenuActivityBranded implements AdminPasswordDialogFragment.AdminPasswordDialogCallback {
    // buttons
    private Button manageFilesButton;
    private Button sendDataButton;
    private Button viewSentFormsButton;
    private Button reviewDataButton;
    private Button getFormsButton;

    @Inject
    MainMenuViewModel.Factory viewModelFactory;

    @Inject
    InstancesRepository instancesRepository;

    @Inject
    NetworkStateProvider connectivityProvider;

    private MainMenuViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        DaggerUtils.getComponent(this).inject(this);
        // brand change ----
        setContentView(R.layout.main_menu_branded);
        // end brand change ----
        viewModel = new ViewModelProvider(this, viewModelFactory).get(MainMenuViewModel.class);

        initToolbar();
        // enter data button. expects a result.
        Button enterDataButton = findViewById(R.id.enter_data);
        enterDataButton.setText(getString(R.string.enter_data_button));
        enterDataButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(),
                        FillBlankFormActivity.class);
                startActivity(i);
            }
        });

        // review data button. expects a result.
        reviewDataButton = findViewById(R.id.review_data);
        reviewDataButton.setText(getString(R.string.review_data_button));
        reviewDataButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), InstanceChooserList.class);
                i.putExtra(ApplicationConstants.BundleKeys.FORM_MODE,
                        ApplicationConstants.FormModes.EDIT_SAVED);
                startActivity(i);
            }
        });

        // send data button. expects a result.
// brand change ----
//        sendDataButton = findViewById(R.id.send_data);
//        sendDataButton.setText(getString(R.string.send_data_button));
//        sendDataButton.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent i = new Intent(getApplicationContext(),
//                        InstanceUploaderListActivity.class);
//                startActivity(i);
//            }
//        });

        //View sent forms
        viewSentFormsButton = findViewById(R.id.view_sent_forms);
        viewSentFormsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getApplicationContext(), InstanceChooserList.class);
                i.putExtra(ApplicationConstants.BundleKeys.FORM_MODE,
                        ApplicationConstants.FormModes.VIEW_SENT);
                startActivity(i);
            }
        });

        // manage forms button. no result expected.
        getFormsButton = findViewById(R.id.get_forms);
        getFormsButton.setText(getString(R.string.get_forms));
        getFormsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                // brand change ----
                if (!connectivityProvider.isDeviceOnline()) {
                    ToastUtils.showShortToast(R.string.no_connection);
                    return;
                }
                String protocol = settingsProvider.getGeneralSettings().getString(GeneralKeys.KEY_PROTOCOL);
                Intent i = null;
                if (protocol.equalsIgnoreCase(getString(R.string.protocol_google_sheets))) {
                    if (new PlayServicesChecker().isGooglePlayServicesAvailable(MainMenuActivity.this)) {
                        i = new Intent(getApplicationContext(),
                                GoogleDriveActivity.class);
                    } else {
                        new PlayServicesChecker().showGooglePlayServicesAvailabilityErrorDialog(MainMenuActivity.this);
                        return;
                    }
                } else {
                    i = new Intent(getApplicationContext(),
                            FormDownloadListActivity.class);
                }
                startActivity(i);
            }
        });

// brand change ----
        // manage forms button. no result expected.
//        manageFilesButton = findViewById(R.id.manage_forms);
//        manageFilesButton.setText(getString(R.string.manage_files));
//        manageFilesButton.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent i = new Intent(getApplicationContext(),
//                        DeleteSavedFormActivity.class);
//                startActivity(i);
//            }
//        });

        TextView versionSHAView = findViewById(R.id.version_sha);
        String versionSHA = viewModel.getVersionCommitDescription();
        if (versionSHA != null) {
            versionSHAView.setText(versionSHA);
        } else {
            versionSHAView.setVisibility(View.GONE);
        }

//        viewModel.getFinalizedFormsCount().observe(this, finalized -> {
//            if (finalized > 0) {
//                sendDataButton.setText(getString(R.string.send_data_button, String.valueOf(finalized)));
//            } else {
//                sendDataButton.setText(getString(R.string.send_data));
//            }
//        });


        viewModel.getUnsentFormsCount().observe(this, unsent -> {
            if (unsent > 0) {
                reviewDataButton.setText(getString(R.string.review_data_button, String.valueOf(unsent)));
                // Brand change
                reviewDataButton.setVisibility(View.VISIBLE);
            } else {
                reviewDataButton.setText(getString(R.string.review_data));
                // Brand change
                reviewDataButton.setVisibility(View.GONE);
            }
        });


        viewModel.getSentFormsCount().observe(this, sent -> {
            if (sent > 0) {
                viewSentFormsButton.setText(getString(R.string.view_sent_forms_button, String.valueOf(sent)));
                // Brand change
                viewSentFormsButton.setVisibility(View.VISIBLE);
            } else {
                viewSentFormsButton.setText(getString(R.string.view_sent_forms));
                // Brand change
                viewSentFormsButton.setVisibility(View.GONE);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        viewModel.resume();

        // Brand change
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        setButtonsVisibility();
        invalidateOptionsMenu();
    }

    private void setButtonsVisibility() {
// brand change ----
//        reviewDataButton.setVisibility(viewModel.shouldEditSavedFormButtonBeVisible() ? VISIBLE : GONE);
//        if (sendDataButton.getVisibility() == View.VISIBLE) {
//            sendDataButton.setVisibility(viewModel.shouldSendFinalizedFormButtonBeVisible() ? View.VISIBLE : View.GONE);
//        }
//        viewSentFormsButton.setVisibility(viewModel.shouldViewSentFormButtonBeVisible() ? VISIBLE : GONE);
//        getFormsButton.setVisibility(viewModel.shouldGetBlankFormButtonBeVisible() ? View.VISIBLE : View.GONE);
//        manageFilesButton.setVisibility(viewModel.shouldDeleteSavedFormButtonBeVisible() ? View.VISIBLE : View.GONE);
// end brand change ----
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

// Brand change -----------------
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        if (!MultiClickGuard.allowClick(getClass().getName())) {
//            return true;
//        }
//
//        if (item.getItemId() == R.id.projects) {
//            showIfNotShowing(ProjectSettingsDialog.class, getSupportFragmentManager());
//            return true;
//        }
//        return super.onOptionsItemSelected(item);
//    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        // Brand change -----------------
        setTitle(getString(R.string.app_name));
        // setTitle(String.format("%s %s", getString(R.string.app_name), viewModel.getVersion()));
        // end brand change -------------
        setSupportActionBar(toolbar);
    }

    @Override
    public void onCorrectAdminPassword(Action action) {
        switch (action) {
            case ADMIN_SETTINGS:
                startActivity(new Intent(this, AdminPreferencesActivity.class));
                break;
            case SCAN_QR_CODE:
                startActivity(new Intent(this, QRCodeTabsActivity.class));
                break;
        }
    }

    @Override
    public void onIncorrectAdminPassword() {
        ToastUtils.showShortToast(R.string.admin_password_incorrect);
    }
}
