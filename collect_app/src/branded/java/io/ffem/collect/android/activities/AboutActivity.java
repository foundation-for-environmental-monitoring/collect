package io.ffem.collect.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import org.odk.collect.android.R;
import org.odk.collect.android.activities.CollectAbstractActivity;
import org.odk.collect.android.activities.WebViewActivity;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.utilities.CustomTabHelper;

/**
 * Activity to display info about the app.
 */
public class AboutActivity extends CollectAbstractActivity {

    private static final String LICENSES_HTML_PATH = "file:///android_asset/open_source_licenses.html";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_layout);
        initToolbar();

        ((TextView) findViewById(R.id.textVersion))
                .setText(Collect.getInstance().getVersionedAppName());
    }

    private void initToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setTitle(getString(R.string.about));
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    /**
     * Displays legal information.
     */
    public void onSoftwareNoticesClick(View view) {
        Intent intent = new Intent(this, WebViewActivity.class);
        intent.putExtra(CustomTabHelper.OPEN_URL, LICENSES_HTML_PATH);
        startActivity(intent);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
