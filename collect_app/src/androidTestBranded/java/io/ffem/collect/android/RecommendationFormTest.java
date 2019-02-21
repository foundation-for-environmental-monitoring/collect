package io.ffem.collect.android;

import android.Manifest;
import android.app.Activity;
import android.app.Instrumentation.ActivityResult;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Environment;
import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.contrib.RecyclerViewActions;
import android.support.test.espresso.intent.rule.IntentsTestRule;
import android.support.test.espresso.matcher.BoundedMatcher;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.runner.lifecycle.ActivityLifecycleMonitorRegistry;
import android.support.test.runner.lifecycle.Stage;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.NumberPicker;
import android.widget.SeekBar;

import net.bytebuddy.utility.RandomString;

import org.apache.commons.io.IOUtils;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;
import org.odk.collect.android.R;
import org.odk.collect.android.activities.FormEntryActivity;
import org.odk.collect.android.utilities.ActivityAvailability;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import androidx.test.uiautomator.By;
import androidx.test.uiautomator.UiDevice;
import androidx.test.uiautomator.UiObject2;
import tools.fastlane.screengrab.Screengrab;
import tools.fastlane.screengrab.UiAutomatorScreenshotStrategy;
import tools.fastlane.screengrab.locale.LocaleTestRule;

import static android.app.Activity.RESULT_OK;
import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.intent.Intents.intending;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasAction;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasData;
import static android.support.test.espresso.intent.matcher.IntentMatchers.hasExtra;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.startsWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.odk.collect.android.activities.FormEntryActivity.EXTRA_TESTING_PATH;
import static org.odk.collect.android.application.Collect.APP_FOLDER;

/**
 * Integration test that runs through a form with all question types.
 * <p>
 * <a href="https://docs.fastlane.tools/actions/screengrab/"> screengrab </a> is used to generate screenshots for
 * documentation and releases. Calls to Screengrab.screenshot("image-name") trigger screenshot
 * creation.
 */

@RunWith(AndroidJUnit4.class)
public class RecommendationFormTest {

    private static UiDevice mDevice;

    @ClassRule
    public static final LocaleTestRule LOCALE_TEST_RULE = new LocaleTestRule();
    private static final String ALL_WIDGETS_FORM = "recommendation-test.xml";
    private static final String FORMS_DIRECTORY = File.separator + APP_FOLDER + "/forms/";
    private final Random random = new Random();
    private final ActivityResult okResult = new ActivityResult(RESULT_OK, new Intent());
    @Rule
    public FormEntryActivityTestRule activityTestRule = new FormEntryActivityTestRule();

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Rule
    public GrantPermissionRule permissionRule =
            GrantPermissionRule.grant(Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE);

    @Mock
    private ActivityAvailability activityAvailability;

    @BeforeClass
    public static void initialize() {
        if (mDevice == null) {
            mDevice = UiDevice.getInstance(getInstrumentation());
        }
    }

    private static Matcher<View> childAtPosition(
            final Matcher<View> parentMatcher, final int position) {

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("Child at position " + position + " in parent ");
                parentMatcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                ViewParent parent = view.getParent();
                return parent instanceof ViewGroup && parentMatcher.matches(parent)
                        && view.equals(((ViewGroup) parent).getChildAt(position));
            }
        };
    }

    //region Test prep.
    @BeforeClass
    public static void copyFormToSdCard() throws IOException {
        String pathname = formPath();
        if (new File(pathname).exists()) {
            return;
        }

        AssetManager assetManager = InstrumentationRegistry.getContext().getAssets();
        InputStream inputStream = assetManager.open(ALL_WIDGETS_FORM);

        File outFile = new File(pathname);
        OutputStream outputStream = new FileOutputStream(outFile);

        IOUtils.copy(inputStream, outputStream);
    }

    @BeforeClass
    public static void beforeAll() {
        Screengrab.setDefaultScreenshotStrategy(new UiAutomatorScreenshotStrategy());
    }

    //region Helper methods.
    private static String formPath() {
        return Environment.getExternalStorageDirectory().getPath()
                + FORMS_DIRECTORY
                + ALL_WIDGETS_FORM;
    }

    //endregion

    public static Matcher<View> withProgress(final int expectedProgress) {
        return new BoundedMatcher<View, SeekBar>(SeekBar.class) {
            @Override
            public void describeTo(Description description) {
                description.appendText("expected: ");
                description.appendText(String.valueOf(expectedProgress));
            }

            @Override
            public boolean matchesSafely(SeekBar seekBar) {
                return seekBar.getProgress() == expectedProgress;
            }
        };
    }
    //endregion

    //region Widget tests.

    public static ViewAction setProgress(final int progress) {
        return new ViewAction() {
            @Override
            public void perform(UiController uiController, View view) {
                SeekBar seekBar = (SeekBar) view;
                seekBar.setProgress(progress);
            }

            @Override
            public String getDescription() {
                return "Set a progress on a SeekBar";
            }

            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(SeekBar.class);
            }
        };
    }

    public static ViewAction setNumberPickerValue(final int value) {
        return new ViewAction() {
            @Override
            public void perform(UiController uiController, View view) {
                NumberPicker numberPickerDialog = (NumberPicker) view;
                numberPickerDialog.setValue(value);
            }

            @Override
            public String getDescription() {
                return "Set a value on a Number Picker";
            }

            @Override
            public Matcher<View> getConstraints() {
                return ViewMatchers.isAssignableFrom(NumberPicker.class);
            }
        };
    }

    public static Matcher<View> withIndex(final Matcher<View> matcher, final int index) {
        return new TypeSafeMatcher<View>() {
            int currentIndex = 0;

            @Override
            public void describeTo(Description description) {
                description.appendText("with index: ");
                description.appendValue(index);
                matcher.describeTo(description);
            }

            @Override
            public boolean matchesSafely(View view) {
                return matcher.matches(view) && currentIndex++ == index;
            }
        };
    }

    @Before
    public void prepareDependencies() {
        FormEntryActivity activity = activityTestRule.getActivity();
        activity.setActivityAvailability(activityAvailability);
        activity.setShouldOverrideAnimations(true);
    }

    //region Main test block.
    @Test
    public void testActivityOpen() {

        onView(withText("Select date")).perform(click());

        ViewInteraction appCompatButton3 = onView(
                allOf(withId(android.R.id.button1), withText("OK"),
                        childAtPosition(
                                allOf(withClassName(is("com.android.internal.widget.ButtonBarLayout")),
                                        childAtPosition(
                                                withClassName(is("android.widget.LinearLayout")),
                                                3)),
                                3),
                        isDisplayed()));
        appCompatButton3.perform(click());

        ViewInteraction editText = onView(
                allOf(childAtPosition(
                        childAtPosition(
                                withClassName(is("android.widget.LinearLayout")),
                                3),
                        2),
                        isDisplayed()));
        editText.perform(replaceText("Test"), closeSoftKeyboard());

        ViewInteraction editText2 = onView(
                allOf(childAtPosition(
                        childAtPosition(
                                withClassName(is("android.widget.LinearLayout")),
                                5),
                        2),
                        isDisplayed()));
        editText2.perform(replaceText("123"), closeSoftKeyboard());

        onView(withText(startsWith("Phone"))).perform(swipeLeft());

        ViewInteraction editText3 = onView(
                allOf(childAtPosition(
                        childAtPosition(
                                withClassName(is("android.widget.LinearLayout")),
                                1),
                        2),
                        isDisplayed()));
        editText3.perform(replaceText("Abc"), closeSoftKeyboard());

        ViewInteraction editText4 = onView(
                allOf(childAtPosition(
                        childAtPosition(
                                withClassName(is("android.widget.LinearLayout")),
                                5),
                        2),
                        isDisplayed()));
        editText4.perform(replaceText("456"), closeSoftKeyboard());

        onView(withText(startsWith("Sample"))).perform(swipeLeft());

        startExternalTest("Available Nitrogen", 6, 0, false);
        startExternalTest("Available Phosphorous", 7, 1, false);
        startExternalTest("Available Potassium", 8, 2, true);

        onView(withText(startsWith("Crop Group"))).perform(swipeLeft());

        ViewInteraction button2 = onView(
                allOf(withText("Launch"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.widget.LinearLayout")),
                                        1),
                                1),
                        isDisplayed()));
        button2.perform(click());

        // Added a sleep statement to match the app's execution delay.
        // The recommended way to handle such scenarios is to use Espresso idling resources:
        // https://google.github.io/android-testing-support-library/docs/espresso/idling-resource/index.html
        try {
            Thread.sleep(12000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        UiObject2 save = mDevice.findObject(By.text("Save"));

        save.click();

        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        onView(withIndex(withText("Crop Recommendation"), 1)).perform(swipeLeft());

        testEnd();

    }

    private void startExternalTest(String name, int questionIndex, int buttonIndex, boolean goNext) {
        ArrayList<String> extras = new ArrayList<>();

        String codeName = name.replace(" ", "_");
        extras.add(codeName);
        extras.add(codeName + "_Unit");
        extras.add(codeName + "_Dilution");
        testExternal("io.ffem.soil", name, extras,
                "", questionIndex, buttonIndex, goNext);
    }

    private void testExternal(String action, String title, ArrayList<String> extras,
                              String suffix, int testIndex, int buttonIndex, boolean goNext) {

//        String exStringWidgetFirstText = randomString();

        when(activityAvailability.isActivityAvailable(any(Intent.class)))
                .thenReturn(false);

//        try {
//            onView(allOf(withIndex(withText(""), buttonIndex) ,hasSibling(withText("Launch"))))
//                    .perform(setTextInTextView(exStringWidgetFirstText));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        openWidgetList();
        onView(withId(R.id.list))
                .perform(RecyclerViewActions
                        .actionOnItemAtPosition(testIndex, click()));

//        Screengrab.screenshot("ex-string");

        // Replace with Intent value:
        String exStringWidgetSecondText = randomString();

        Intent stringIntent = new Intent();
        for (int i = 0; i < extras.size(); i++) {
            String extra = extras.get(i);
            String value = exStringWidgetSecondText;
            if (extra.contains("Dilution")) {
                value = randomIntegerString();
            }
            if (i == 0){
                value = String.valueOf(randomFloat());
            }

            stringIntent.putExtra(extra + suffix, value);
        }

        ActivityResult exStringResult = new ActivityResult(RESULT_OK, stringIntent);

        switch (extras.size()) {
            case 1:
                intending(allOf(
                        hasAction(action),
                        hasExtra(extras.get(0) + suffix, null)
                )).respondWith(exStringResult);
                break;
            case 2:
                intending(allOf(
                        hasAction(action),
                        hasExtra(extras.get(0) + suffix, null),
                        hasExtra(extras.get(1) + suffix, null)
                )).respondWith(exStringResult);
                break;
            case 3:
                intending(allOf(
                        hasAction(action),
                        hasExtra(extras.get(0) + suffix, null),
                        hasExtra(extras.get(1) + suffix, null),
                        hasExtra(extras.get(2) + suffix, null)
                )).respondWith(exStringResult);
                break;
            case 4:
                intending(allOf(
                        hasAction(action),
                        hasExtra(extras.get(0) + suffix, null),
                        hasExtra(extras.get(1) + suffix, null),
                        hasExtra(extras.get(2) + suffix, null),
                        hasExtra(extras.get(3) + suffix, null)
                )).respondWith(exStringResult);
                break;
        }

        when(activityAvailability.isActivityAvailable(any(Intent.class)))
                .thenReturn(true);

        if (extras.size() == 1 && extras.get(0).equals("value")) {
            onView(withIndex(withId(R.id.simple_button), buttonIndex)).perform(click());
            onView(withText(exStringWidgetSecondText)).check(matches(isDisplayed()));
        } else {
            onView(allOf(withText("Launch"), hasSibling(withText(title)))).perform(click());
            onView(withIndex(withText(extras.get(0)
                    .replace("_", " ") + ": "), 0)).check(matches(isDisplayed()));
            onView(withIndex(withText(exStringWidgetSecondText), 0)).check(matches(isDisplayed()));
        }

//        Screengrab.screenshot("ex-string2");

        openWidgetList();
        onView(withId(R.id.list))
                .perform(RecyclerViewActions
                        .actionOnItemAtPosition(testIndex, click()));

        if (extras.size() == 1 && extras.get(0).equals("value")) {
            onView(withText(exStringWidgetSecondText)).check(matches(isDisplayed()));
        } else {
            onView(withIndex(withText(exStringWidgetSecondText), 0)).check(matches(isDisplayed()));
            onView(withIndex(withText(extras.get(0)
                    .replace("_", " ") + ": "), 0)).check(matches(isDisplayed()));
        }

        if (goNext) {
            onView(withIndex(withText(startsWith(title)), 0)).perform(swipeLeft());
        }
    }

    private void testEnd() {
        onView(withText("You are at the end of Fertilizer Recommendation.")).check(matches(isDisplayed()));
        onView(withText("Mark form as finalized")).check(matches(isDisplayed()));
        onView(withText("Send Form")).check(matches(isDisplayed()));
        onView(withText("Mark form as finalized")).perform(click());
        onView(withText("Save Form and Exit")).check(matches(isDisplayed()));
    }

    public void testStringWidget() {
        String stringWidgetText = randomString();

        onVisibleEditText().perform(replaceText(stringWidgetText));

        // captures screenshot of string widget
        Screengrab.screenshot("string-input");

        openWidgetList();
        onView(withText("String widget")).perform(click());

        onVisibleEditText().check(matches(withText(stringWidgetText)));

        onView(withText("String widget")).perform(swipeLeft());
    }

    public void testStringNumberWidget() {
        String stringNumberWidgetText = randomIntegerString();

        onVisibleEditText().perform(replaceText(stringNumberWidgetText));

        Screengrab.screenshot("string-number");

        openWidgetList();

        onView(withText("String number widget")).perform(click());

        onVisibleEditText().check(matches(withText(stringNumberWidgetText)));

        onView(withText("String number widget")).perform(swipeLeft());

    }

    public void testUrlWidget() {
        Uri uri = Uri.parse("http://opendatakit.org/");

        intending(allOf(hasAction(Intent.ACTION_VIEW), hasData(uri)))
                .respondWith(okResult);

        Screengrab.screenshot("url");

        onView(withId(R.id.simple_button)).perform(click());
        onView(withText("URL widget")).perform(swipeLeft());
    }

    public void testLabelWidget() {

        Screengrab.screenshot("label-widget");

        onView(withText("Label widget")).perform(swipeLeft());
    }


    public void testSubmission() {

    }

    private ViewInteraction onVisibleEditText() {
        return onView(withClassName(endsWith("EditText")));
    }

    private void openWidgetList() {
        onView(withId(R.id.menu_goto)).perform(click());
    }

    // private void saveForm() {
    //    onView(withId(R.id.menu_save)).perform(click());
    // }

    private String randomString() {
        return RandomString.make();
    }

    private int randomInt() {
        return Math.abs(random.nextInt());
    }

    private float randomFloat() {
        return Math.abs(random.nextFloat());
    }

    private String randomIntegerString() {
        String s = Integer.toString(randomInt());
        while (s.length() > 9) {
            s = s.substring(1);
        }

        // Make sure the result is a valid Integer String:
        return Integer.toString(Integer.parseInt(s));
    }

    //    private ActivityResult cancelledResult() {
    //        return new ActivityResult(RESULT_CANCELED, null);
    //    }
    //
    //    private ActivityResult okResult(@Nullable Intent data) {
    //        return new ActivityResult(RESULT_OK, data);
    //    }

    //endregion

    public Activity getActivityInstance() {
        final Activity[] currentActivity = new Activity[1];
        getInstrumentation().runOnMainSync(() -> {
            Collection resumedActivities = ActivityLifecycleMonitorRegistry
                    .getInstance().getActivitiesInStage(Stage.RESUMED);
            if (resumedActivities.iterator().hasNext()) {
                currentActivity[0] = (Activity) resumedActivities.iterator().next();
            }
        });

        return currentActivity[0];
    }
    //endregion

    //region Custom TestRule.
    private class FormEntryActivityTestRule extends IntentsTestRule<FormEntryActivity> {

        FormEntryActivityTestRule() {
            super(FormEntryActivity.class);
        }

        @Override
        protected Intent getActivityIntent() {
            Context context = getInstrumentation().getTargetContext();
            Intent intent = new Intent(context, FormEntryActivity.class);

            intent.putExtra(EXTRA_TESTING_PATH, formPath());

            return intent;
        }
    }

}
