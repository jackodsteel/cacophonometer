package nz.org.cacophony.cacophonometerlite;

import android.support.test.espresso.Espresso;
import android.support.test.espresso.UiController;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.ViewInteraction;
import android.support.test.espresso.matcher.ViewMatchers;
import android.support.test.rule.ActivityTestRule;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.Checkable;
import android.widget.TextView;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.pressImeActionButton;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withClassName;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;

/**
 * Created by Tim Hunt on 14-Mar-18.
 */

public class RegisterWithServer {

//    @Rule
//    public static ActivityTestRule<MainActivity> mActivityTestRule = new ActivityTestRule<>(MainActivity.class);


    public static void registerWithServer( ActivityTestRule<MainActivity> mActivityTestRule, boolean testServer){
//        this.testServer = testServer;
        // Register with idling couunter
        // https://developer.android.com/training/testing/espresso/idling-resource.html

//stackoverflow.com/questions/25470210/using-espresso-idling-resource-with-multiple-activities // this gave me idea to use an inteface

        Espresso.registerIdlingResources((mActivityTestRule.getActivity().getIdlingResource()));
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        ViewInteraction appCompatTextView = onView(
                allOf(ViewMatchers.withId(R.id.title), withText("Settings"),
                        childAtPosition(
                                childAtPosition(
                                        withClassName(is("android.support.v7.view.menu.ListMenuItemView")),
                                        0),
                                0),
                        isDisplayed()));
        appCompatTextView.perform(click());

        // Unregister device (it may or may not be currently registered)
        onView(withId(R.id.setupUnregister)).perform(scrollTo(), click());


        // check it has un registered
        onView(withId(R.id.setupRegisterStatus)).check(matches(withText("Device not registered.")));




        onView(withId(R.id.cbUseTestServer)).perform(scrollTo(), setChecked(!testServer)); // setChecked does not fire the onCheckboxUserTestServerClicked code, so check it then click it on next line

        onView(withId(R.id.cbUseTestServer)).perform(scrollTo()).perform(click());

//        onView(withId(R.id.cbUseTestServer))
//                .perform(scrollTo())
//                .check(matches(not(testServer)))
//                .perform(click())
//                .check(matches(isChecked()));
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        // now register
        onView(withId(R.id.setupGroupNameInput)).perform(scrollTo(), replaceText("tim1"), closeSoftKeyboard());
        onView(withId(R.id.setupGroupNameInput)).perform(pressImeActionButton());
        onView(withId(R.id.setupRegisterButton)).perform(scrollTo(), click());


        // check it has registerd
        onView(withId(R.id.setupRegisterStatus)).check(matches(withText("Registered in group: tim1")));


        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

//        // now un register and check for toast and text box message
//        unRegisterButton.perform(scrollTo(), click());
//
//        // testing for the following toast failed - I think as the toast was queued behing other toasts
////        onView(withText("Success - Device is no longer registered")).inRoot(new ToastMatcher())
////                .check(matches(isDisplayed()));
//
//        textViewRegisteredMessage.check(matches(withText("Device not registered.")));


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



    private static Matcher<View> isDeviceIdOK() {
        // https://www.programcreek.com/java-api-examples/?code=kevalpatel2106/smart-lens/smart-lens-master/app/src/androidTest/java/com/kevalpatel2106/smartlens/testUtils/CustomMatchers.java
        // http://blog.sqisland.com/2016/06/advanced-espresso-at-io16.html

        return new TypeSafeMatcher<View>() {
            @Override
            public void describeTo(Description description) {
                description.appendText("is Device Id OK ");

            }

            @Override
            public boolean matchesSafely(View view) {
                return view instanceof TextView && ((TextView)view).getText().length() > 11;  // if > 11 characters it means there is a device id displayed

            }
        };
    }

    public static ViewAction setChecked(final boolean checked) {
        // https://stackoverflow.com/questions/37819278/android-espresso-click-checkbox-if-not-checked
        return new ViewAction() {
            @Override
            public BaseMatcher<View> getConstraints() {
                return new BaseMatcher<View>() {
                    @Override
                    public boolean matches(Object item) {
                        return isA(Checkable.class).matches(item);
                    }

                    @Override
                    public void describeMismatch(Object item, Description mismatchDescription) {}

                    @Override
                    public void describeTo(Description description) {}
                };
            }

            @Override
            public String getDescription() {
                return null;
            }

            @Override
            public void perform(UiController uiController, View view) {
                Checkable checkableView = (Checkable) view;
                checkableView.setChecked(checked);
            }
        };
    }

}
