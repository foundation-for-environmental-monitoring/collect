package org.odk.collect.android.application.initialization;

import android.app.Application;
import android.os.Handler;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.startup.AppInitializer;

import net.danlew.android.joda.JodaTimeInitializer;

import org.javarosa.core.model.CoreModelModule;
import org.javarosa.core.services.PrototypeManager;
import org.javarosa.core.util.JavaRosaCoreModule;
import org.javarosa.model.xform.XFormsModule;
import org.javarosa.xform.parse.XFormParser;
import org.odk.collect.analytics.Analytics;
import org.odk.collect.android.BuildConfig;
import org.odk.collect.android.application.Collect;
import org.odk.collect.android.application.initialization.upgrade.AppUpgrader;
import org.odk.collect.android.geo.MapboxUtils;
import org.odk.collect.android.logic.PropertyManager;
import org.odk.collect.android.logic.actions.setgeopoint.CollectSetGeopointActionHandler;
import org.odk.collect.android.storage.StorageInitializer;
import org.odk.collect.android.utilities.LaunchState;
import org.odk.collect.utilities.UserAgentProvider;

import java.util.Locale;

import javax.inject.Inject;

import timber.log.Timber;

public class ApplicationInitializer {

    private final Application context;
    private final UserAgentProvider userAgentProvider;
    private final PropertyManager propertyManager;
    private final Analytics analytics;
    private final StorageInitializer storageInitializer;
    private final LaunchState launchState;
    private final AppUpgrader appUpgrader;

    @Inject
    public ApplicationInitializer(Application context, UserAgentProvider userAgentProvider,
                                  PropertyManager propertyManager, Analytics analytics,
                                  StorageInitializer storageInitializer, LaunchState launchState,
                                  AppUpgrader appUpgrader) {
        this.context = context;
        this.userAgentProvider = userAgentProvider;
        this.propertyManager = propertyManager;
        this.analytics = analytics;
        this.storageInitializer = storageInitializer;
        this.launchState = launchState;
        this.appUpgrader = appUpgrader;
    }

    public void initialize() {
        initializeStorage();
        performUpgradeIfNeeded();
        initializeFrameworks();
        initializeLocale();

        launchState.appLaunched();
    }

    private void performUpgradeIfNeeded() {
        if (launchState.isUpgradedFirstLaunch()) {
            appUpgrader.upgrade();
        }
    }

    private void initializeStorage() {
        storageInitializer.createOdkDirsOnStorage();
    }

    private void initializeFrameworks() {
        initializeLogging();
        AppInitializer.getInstance(context).initializeComponent(JodaTimeInitializer.class);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        initializeMapFrameworks();
        initializeJavaRosa();
        initializeAnalytics();
    }

    private void initializeAnalytics() {
        analytics.setAnalyticsCollectionEnabled(false);

//        FormUpdateMode formUpdateMode = getFormUpdateMode(context, generalSettings);
//        analytics.setUserProperty("FormUpdateMode", formUpdateMode.getValue(context));
    }

    private void initializeLocale() {
        Collect.defaultSysLanguage = Locale.getDefault().getLanguage();
    }

    private void initializeJavaRosa() {
        propertyManager.reload();
        org.javarosa.core.services.PropertyManager
                .setPropertyManager(propertyManager);

        // Register prototypes for classes that FormDef uses
        PrototypeManager.registerPrototypes(JavaRosaCoreModule.classNames);
        PrototypeManager.registerPrototypes(CoreModelModule.classNames);
        new XFormsModule().registerModule();

        // When registering prototypes from Collect, a proguard exception also needs to be added
        PrototypeManager.registerPrototype("org.odk.collect.android.logic.actions.setgeopoint.CollectSetGeopointAction");
        XFormParser.registerActionHandler(CollectSetGeopointActionHandler.ELEMENT_NAME, new CollectSetGeopointActionHandler());
    }

    private void initializeLogging() {
        if (BuildConfig.BUILD_TYPE.equals("odkCollectRelease")) {
            Timber.plant(new CrashReportingTree(analytics));
        } else {
            Timber.plant(new Timber.DebugTree());
        }
    }

    private void initializeMapFrameworks() {
        try {
            // Brand change ----
//            Handler handler = new Handler(context.getMainLooper());
//            handler.post(() -> {
//                // This has to happen on the main thread but we might call `initialize` from tests
//                new com.google.android.gms.maps.MapView(context).onCreate(null);
//            });
            org.osmdroid.config.Configuration.getInstance().setUserAgentValue(userAgentProvider.getUserAgent());
            // Brand change ----
//            MapboxUtils.initMapbox();
        } catch (Exception | Error ignore) {
            // ignored
        }
    }
}

