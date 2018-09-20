package org.odk.collect.android.utilities;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.odk.collect.android.http.HttpCredentials;
import org.odk.collect.android.http.HttpCredentialsInterface;
import org.odk.collect.android.preferences.GeneralSharedPreferences;
import org.odk.collect.android.preferences.PreferenceKeys;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Singleton;

@Singleton
public class WebCredentialsUtils {

    private static final Map<String, HttpCredentialsInterface> HOST_CREDENTIALS = new HashMap<>();

    public void saveCredentials(@NonNull String url, @NonNull String username, @NonNull String password) {
        if (username.isEmpty()) {
            return;
        }

        String host = Uri.parse(url).getHost();
        HOST_CREDENTIALS.put(host, new HttpCredentials(username, password));
    }

    public void saveCredentialsPreferences(String userName, String password) {
        GeneralSharedPreferences.getInstance().save(PreferenceKeys.KEY_USERNAME, userName);
        GeneralSharedPreferences.getInstance().save(PreferenceKeys.KEY_PASSWORD, password);
    }

    public void clearCredentials(@NonNull String url) {
        if (url.isEmpty()) {
            return;
        }

        String host = Uri.parse(url).getHost();
        if (host != null) {
            HOST_CREDENTIALS.remove(host);
        }
    }

    public String getServerUrlFromPreferences() {
        if (GeneralSharedPreferences.getInstance() == null) {
            return "";
        }
        return (String) GeneralSharedPreferences.getInstance().get(PreferenceKeys.KEY_SERVER_URL);
    }

    public String getPasswordFromPreferences() {
        if (GeneralSharedPreferences.getInstance() == null) {
            return "";
        }
        return (String) GeneralSharedPreferences.getInstance().get(PreferenceKeys.KEY_PASSWORD);
    }

    public String getUserNameFromPreferences() {
        if (GeneralSharedPreferences.getInstance() == null) {
            return "";
        }
        return (String) GeneralSharedPreferences.getInstance().get(PreferenceKeys.KEY_USERNAME);
    }

    /**
     * Returns a credentials object from the url
     *
     * @param url to find the credentials object
     * @return either null or an instance of HttpCredentialsInterface
     */
    public @Nullable HttpCredentialsInterface getCredentials(@NonNull URI url) {
        String host = url.getHost();
        String serverPrefsUrl = getServerUrlFromPreferences();
        String prefsServerHost = (serverPrefsUrl == null) ? null : Uri.parse(serverPrefsUrl).getHost();

        // URL host is the same as the host in preferences
        if (prefsServerHost != null && prefsServerHost.equalsIgnoreCase(host)) {
            // Use the temporary credentials if they exist, otherwise use the credentials saved to preferences
            if (HOST_CREDENTIALS.containsKey(host)) {
                return HOST_CREDENTIALS.get(host);
            } else {
                return new HttpCredentials(getUserNameFromPreferences(), getPasswordFromPreferences());
            }
        } else {
            return HOST_CREDENTIALS.get(host);
        }
    }

}
