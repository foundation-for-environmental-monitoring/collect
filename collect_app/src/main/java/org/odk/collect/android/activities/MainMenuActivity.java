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
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProviders;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.viewmodels.MainMenuViewModel;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.configure.qr.QRCodeTabsActivity;
import org.odk.collect.android.gdrive.GoogleDriveActivity;
import org.odk.collect.android.injection.DaggerUtils;
import org.odk.collect.android.instances.Instance;
import org.odk.collect.android.instances.InstancesRepository;
import org.odk.collect.android.preferences.dialogs.AdminPasswordDialogFragment;
import org.odk.collect.android.preferences.dialogs.AdminPasswordDialogFragment.Action;
import org.odk.collect.android.preferences.keys.AdminKeys;
import org.odk.collect.android.preferences.keys.GeneralKeys;
import org.odk.collect.android.preferences.screens.AdminPreferencesActivity;
import org.odk.collect.android.project.ProjectSettingsDialog;
import org.odk.collect.android.provider.InstanceProviderAPI.InstanceColumns;
import org.odk.collect.android.utilities.AdminPasswordProvider;
import org.odk.collect.android.utilities.ApplicationConstants;
import org.odk.collect.android.utilities.DialogUtils;
import org.odk.collect.android.utilities.MultiClickGuard;
import org.odk.collect.android.utilities.PlayServicesChecker;
import org.odk.collect.android.utilities.ToastUtils;

import java.lang.ref.WeakReference;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static org.odk.collect.android.utilities.DialogUtils.showIfNotShowing;

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
    private MenuItem qrcodeScannerMenuItem;
    private final IncomingHandler handler = new IncomingHandler(this);
    private final MyContentObserver contentObserver = new MyContentObserver();

    @BindView(R.id.version_sha)
    TextView versionSHAView;

    @Inject
    AdminPasswordProvider adminPasswordProvider;

    @Inject
    MainMenuViewModel.Factory viewModelFactory;

    @Inject
    InstancesRepository instancesRepository;

    private MainMenuViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Collect.getInstance().getComponent().inject(this);
        // brand change ----
        setContentView(R.layout.main_menu_branded);
        // end brand change ----
        ButterKnife.bind(this);
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(MainMenuViewModel.class);

        initToolbar();
        DaggerUtils.getComponent(this).inject(this);

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

        String versionSHA = viewModel.getVersionCommitDescription();
        if (versionSHA != null) {
            versionSHAView.setText(versionSHA);
        } else {
            versionSHAView.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Brand change
        getSupportActionBar().setDisplayHomeAsUpEnabled(false);

        updateButtons();
        getContentResolver().registerContentObserver(InstanceColumns.CONTENT_URI, true, contentObserver);
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
    protected void onPause() {
        super.onPause();
        getContentResolver().unregisterContentObserver(contentObserver);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        qrcodeScannerMenuItem = menu.findItem(R.id.menu_configure_qr_code);
        return super.onCreateOptionsMenu(menu);
    }

    // brand change ----
//    @Override
//    public boolean onPrepareOptionsMenu(Menu menu) {
//        qrcodeScannerMenuItem.setVisible(settingsProvider.getAdminSettings().getBoolean(AdminKeys.KEY_QR_CODE_SCANNER));
//        return super.onPrepareOptionsMenu(menu);
//    }
// end brand change ----

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (!MultiClickGuard.allowClick(getClass().getName())) {
            return true;
        }

        switch (item.getItemId()) {
            case R.id.projects:
                DialogUtils.showIfNotShowing(ProjectSettingsDialog.class, getSupportFragmentManager());
                return true;
            case R.id.menu_configure_qr_code:
                if (adminPasswordProvider.isAdminPasswordSet()) {
                    Bundle args = new Bundle();
                    args.putSerializable(AdminPasswordDialogFragment.ARG_ACTION, Action.SCAN_QR_CODE);
                    showIfNotShowing(AdminPasswordDialogFragment.class, args, getSupportFragmentManager());
                } else {
                    startActivity(new Intent(this, QRCodeTabsActivity.class));
                }
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        // Brand change -----------------
        setTitle(getString(R.string.app_name));
        // setTitle(String.format("%s %s", getString(R.string.app_name), viewModel.getVersion()));
        // end brand change -------------
        setSupportActionBar(toolbar);
    }

    private void updateButtons() {
        int finalizedInstances = instancesRepository.getCountByStatus(Instance.STATUS_COMPLETE, Instance.STATUS_SUBMISSION_FAILED);
        int sentInstances = instancesRepository.getCountByStatus(Instance.STATUS_SUBMITTED);
        int unsentInstances = instancesRepository.getCountByStatus(Instance.STATUS_INCOMPLETE, Instance.STATUS_COMPLETE, Instance.STATUS_SUBMISSION_FAILED);

        // Brand change -----------------
//        if (finalizedInstances > 0) {
//            sendDataButton.setText(getString(R.string.send_data_button, String.valueOf(finalizedInstances)));
//        } else {
//            sendDataButton.setText(getString(R.string.send_data));
//        }

        if (unsentInstances > 0) {
            reviewDataButton.setVisibility(VISIBLE);
            reviewDataButton.setText(getString(R.string.review_data_button, String.valueOf(unsentInstances)));
        } else {
            reviewDataButton.setVisibility(GONE);
            reviewDataButton.setText(getString(R.string.review_data));
        }

        if (sentInstances > 0) {
            viewSentFormsButton.setVisibility(VISIBLE);
            viewSentFormsButton.setText(getString(R.string.view_sent_forms_button, String.valueOf(sentInstances)));
        } else {
            viewSentFormsButton.setVisibility(GONE);
            viewSentFormsButton.setText(getString(R.string.view_sent_forms));
        }
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

    /*
     * Used to prevent memory leaks
     */
    static class IncomingHandler extends Handler {
        private final WeakReference<MainMenuActivity> target;

        IncomingHandler(MainMenuActivity target) {
            this.target = new WeakReference<>(target);
        }

        @Override
        public void handleMessage(Message msg) {
            MainMenuActivity target = this.target.get();
            if (target != null) {
                target.updateButtons();
            }
        }
    }

    /**
     * notifies us that something changed
     */
    private class MyContentObserver extends ContentObserver {

        MyContentObserver() {
            super(null);
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            handler.sendEmptyMessage(0);
        }
    }
}
