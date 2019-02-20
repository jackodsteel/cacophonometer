package nz.org.cacophony.cacophonometer;

import android.content.Context;
import android.support.test.rule.ActivityTestRule;
import android.util.Log;

import junit.framework.AssertionFailedError;

import java.io.File;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.closeSoftKeyboard;
import static android.support.test.espresso.action.ViewActions.replaceText;
import static android.support.test.espresso.action.ViewActions.scrollTo;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static org.hamcrest.Matchers.allOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Tim Hunt on 16-Mar-18.
 */

@SuppressWarnings("unused")
class SignInUser {

    private static Context targetContext;
    private static Prefs prefs;




    public static void signInUser(ActivityTestRule<MainActivity> mActivityTestRule) {


        setUpForSignInUser(mActivityTestRule);

        signIn();

        tearDownForSignInUser(mActivityTestRule);
    }

    private static void setUpForSignInUser(ActivityTestRule<MainActivity> mActivityTestRule){

        mActivityTestRule.getActivity().registerEspressoIdlingResources();
        targetContext = getInstrumentation().getTargetContext();
        prefs = new Prefs(targetContext);



        if (prefs.getDeviceName() == null){
            // Welcome Dialog WILL be displayed - and SetupWizard will be running
            HelperCode.dismissWelcomeDialog();
        }else{
            // Main menu will be showing

            onView(withId(R.id.btnSetup)).perform(click());
        }

       // HelperCode.signOutUser(prefs, targetContext);

        HelperCode.useTestServerAndShortRecordings(prefs, targetContext);
        nowSwipeLeft();
        nowSwipeLeft(); // takes you to Sign In screen

    }

    private static void tearDownForSignInUser(ActivityTestRule<MainActivity> mActivityTestRule) {

         mActivityTestRule.getActivity().unRegisterEspressoIdlingResources();

    }


    private static void signIn(){



            try {
                onView(withId(R.id.btnSignOutUser)).perform(click());
            } catch (Exception e) {
                // View not displayed
            }


        try {


            onView(withId(R.id.etUserNameOrEmailInput)).perform(replaceText("timhot"), closeSoftKeyboard());

            onView(withId(R.id.etPasswordInput)).perform(replaceText("Pppother1"), closeSoftKeyboard());
            onView(withId(R.id.btnSignIn)).perform(click());

        }catch (Exception ex){
            Log.e("SignInUser", ex.getLocalizedMessage());
        }


        Log.e("SignInUser", "Finished");

        boolean userSignedIn = prefs.getUserSignedIn();

        assertEquals(userSignedIn, true);
        onView(withId(R.id.tvTitleMessageSignIn)).check(matches(withText("Signed In")));

    }



    private static void nowSwipeLeft(){

        onView(withId(R.id.SetUpWizard)).perform(swipeLeft());


    }
}
