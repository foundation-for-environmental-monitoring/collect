package org.odk.collect.android.utilities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;
import android.support.annotation.Nullable;

/**
 * Utility functions for api related actions.
 */
@SuppressWarnings("deprecation")
public final class ApiUtil {

    private ApiUtil() {
    }

    public static void startInstalledAppDetailsActivity(@Nullable final Activity context) {
        if (context == null) {
            return;
        }
        final Intent i = new Intent();
        i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        i.addCategory(Intent.CATEGORY_DEFAULT);
        i.setData(Uri.parse("package:" + context.getPackageName()));
        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
        context.startActivity(i);
    }
}
