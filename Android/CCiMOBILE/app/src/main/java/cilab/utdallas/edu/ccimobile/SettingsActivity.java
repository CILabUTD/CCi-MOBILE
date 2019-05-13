package cilab.utdallas.edu.ccimobile;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.ScaleAnimation;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager.widget.ViewPager;

public class SettingsActivity extends AppCompatActivity implements ParametersFrag.OnParametersSelectedListener {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    String leftImplantGeneration, rightImplantGeneration;
    int leftStimulationModeCode, rightStimulationModeCode, leftPulsesPerFramePerChannel, rightPulsesPerFramePerChannel, leftPulsesPerFrame, rightPulsesPerFrame, leftnRFcycles, rightnRFcycles;
    double leftMAPsensitivity, rightMAPsensitivity, leftMAPgain, rightMAPgain, leftInterpulseDuration, rightInterpulseDuration;
    boolean leftExists, rightExists;

    FloatingActionButton fab;
    int[] colorIntArray = {R.color.colorAccent, R.color.colorAccent};
    int[] iconIntArray = {R.drawable.ic_autorenew_black_24dp, R.drawable.ic_autorenew_black_24dp};

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof ParametersFrag) {
            ParametersFrag paramFragment = (ParametersFrag) fragment;
            paramFragment.setOnParametersSelectedListener(this);
        }
    }

    public void onArticleSelected(int position) {
        // The user selected the headline of an article from the HeadlinesFragment
        // Do something here to display that article
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings_tabbed);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Create the adapter that will return a fragment for each of the two
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = findViewById(R.id.tabs);

        mViewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.ViewPagerOnTabSelectedListener(mViewPager));

        fab = findViewById(R.id.fab);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ParametersFrag fragmentObj = (ParametersFrag) mSectionsPagerAdapter.instantiateItem(mViewPager, 0);
                boolean problems = fragmentObj.updateMAPButton();
                if (!problems)
                    Snackbar.make(findViewById(R.id.rootSettings), "MAP updated successfully.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
                sendIntentBack(fragmentObj);

            }
        });


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mViewPager.setCurrentItem(tab.getPosition());
                animateFab(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });


    }

    /**
     * Animation for the fab
     * @param position p
     */
    protected void animateFab(final int position) {
        fab.clearAnimation();
        // Scale down animation
        ScaleAnimation shrink =  new ScaleAnimation(1f, 0.2f, 1f, 0.2f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
        shrink.setDuration(150);     // animation duration in milliseconds
        shrink.setInterpolator(new DecelerateInterpolator());
        shrink.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                // Change FAB color and icon
                // fab.setBackgroundTintList(getResources().getColorStateList(colorIntArray[position]));
                // fab.setImageDrawable(getResources().getDrawable(iconIntArray[position], null));

                // Scale up animation
                ScaleAnimation expand =  new ScaleAnimation(0.2f, 1f, 0.2f, 1f, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
                expand.setDuration(100);     // animation duration in milliseconds
                expand.setInterpolator(new AccelerateInterpolator());
                fab.startAnimation(expand);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });
        fab.startAnimation(shrink);
    }



    /**
     * Sends back an intent to MainActivity
     * @param fo ParametersFrag fragment object
     */
    public void sendIntentBack(ParametersFrag fo) {
        // Send info back to first activity
        Intent intent = new Intent();

        // Get info from Parameters fragment
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
        leftExists = preferences.getBoolean("leftMapExists", false);
        rightExists = preferences.getBoolean("rightMapExists", false);

        if (leftExists) {
            leftMAPsensitivity = getDouble(preferences, "Left.sensitivity");
            leftMAPgain = getDouble(preferences, "Left.gain");
            leftImplantGeneration = fo.getLeftImplantGeneration();
            leftStimulationModeCode = fo.getLeftStimulationModeCode();
            leftPulsesPerFramePerChannel = fo.getLeftPulsesPerFramePerChannel();
            leftPulsesPerFrame = fo.getLeftPulsesPerFrame();
            leftInterpulseDuration = fo.getLeftInterpulseDuration();
            leftnRFcycles = fo.getLeftnRFcycles();

            intent.putExtra("leftSensitivity", leftMAPsensitivity);
            intent.putExtra("leftGain", leftMAPgain);
            intent.putExtra("leftMAPimplantGeneration", leftImplantGeneration);
            intent.putExtra("leftMAPstimulationModeCode", leftStimulationModeCode);
            intent.putExtra("leftMAPpulsesPerFramePerChannel", leftPulsesPerFramePerChannel);
            intent.putExtra("leftMAPpulsesPerFrame", leftPulsesPerFrame);
            intent.putExtra("leftMAPinterpulseDuration", leftInterpulseDuration);
            intent.putExtra("leftMAPnRFcycles", leftnRFcycles);
        }

        if (rightExists) {
            rightMAPsensitivity = getDouble(preferences, "Right.sensitivity");
            rightMAPgain = getDouble(preferences, "Right.gain");
            rightImplantGeneration = fo.getRightImplantGeneration();
            rightStimulationModeCode = fo.getRightStimulationModeCode();
            rightPulsesPerFramePerChannel = fo.getRightPulsesPerFramePerChannel();
            rightPulsesPerFrame = fo.getRightPulsesPerFrame();
            rightInterpulseDuration = fo.getRightInterpulseDuration();
            rightnRFcycles = fo.getRightnRFcycles();

            intent.putExtra("rightSensitivity", rightMAPsensitivity);
            intent.putExtra("rightGain", rightMAPgain);
            intent.putExtra("rightMAPimplantGeneration", rightImplantGeneration);
            intent.putExtra("rightMAPstimulationModeCode", rightStimulationModeCode);
            intent.putExtra("rightMAPpulsesPerFramePerChannel", rightPulsesPerFramePerChannel);
            intent.putExtra("rightMAPpulsesPerFrame", rightPulsesPerFrame);
            intent.putExtra("rightMAPinterpulseDuration", rightInterpulseDuration);
            intent.putExtra("rightMAPnRFcycles", rightnRFcycles);
        }

        setResult(RESULT_OK, intent);
    }

    public static class ElectrodesFragment extends Fragment implements ElectrodeAdapter.ItemClickListener {
        private int[] leftMAPTHR, rightMAPTHR, leftMAPMCL, rightMAPMCL, leftMAPelectrodes, rightMAPelectrodes;
        private double[] leftMAPgains, rightMAPgains;
        private boolean leftExists, rightExists;
        private int leftMAPnbands, rightMAPnbands;
        ElectrodeAdapter myAdapter;
        RecyclerView recyclerView;

        public ElectrodesFragment() {
            // required empty constructor
        }

        public static ElectrodesFragment newInstance(int sectionNumber) {
            ElectrodesFragment fragment = new ElectrodesFragment();
//            Bundle args = new Bundle();
//            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
//            fragment.setArguments(args);
            return fragment;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            View electrode_view = inflater.inflate(R.layout.activity_electrodes, container, false);

            recyclerView = electrode_view.findViewById(R.id.myRecyclerView);
            recyclerView.setFocusable(false);

            return electrode_view;
        }

        @Override
        public void onStart() {
            super.onStart();
            getElectrodeFromMAP();
        }

        /**
         * Gets data from the MAP using SharedPreferences
         */
        private void getElectrodeFromMAP() {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());

            leftExists = preferences.getBoolean("leftMapExists", false);
            rightExists = preferences.getBoolean("rightMapExists", false);
            int maxNum = 22;

            if (leftExists) {
                leftMAPnbands = preferences.getInt("leftMAPnbands", 0);
                leftMAPTHR = new int[leftMAPnbands];
                leftMAPMCL = new int[leftMAPnbands];
                leftMAPgains = new double[leftMAPnbands];
                leftMAPelectrodes = new int[leftMAPnbands];

                for (int i = 0; i < leftMAPnbands; i++) {
                    leftMAPTHR[i] = preferences.getInt("leftTHR" + i, 0);
                    leftMAPMCL[i] = preferences.getInt("leftMCL" + i, 0);
                    leftMAPgains[i] = getDouble(preferences, "leftgain" + i);
                    leftMAPelectrodes[i] = preferences.getInt("leftelectrodes" + i, 0);
                }

                //UpdateMAPText("left");

            } else {
                //disableMAPtext("left");

            }

            if (rightExists) {
                rightMAPnbands = preferences.getInt("rightMAPnbands", 0);
                rightMAPTHR = new int[rightMAPnbands];
                rightMAPMCL = new int[rightMAPnbands];
                rightMAPgains = new double[rightMAPnbands];
                rightMAPelectrodes = new int[rightMAPnbands];

                for (int i = 0; i < rightMAPnbands; i++) {
                    rightMAPTHR[i] = preferences.getInt("rightTHR" + i, 0);
                    rightMAPMCL[i] = preferences.getInt("rightMCL" + i, 0);
                    rightMAPgains[i] = getDouble(preferences, "rightgain" + i);
                    rightMAPelectrodes[i] = preferences.getInt("rightelectrodes" + i, 0);
                }

                //UpdateMAPText("right");
            } else {
                //disableMAPtext("right");

            }

            if (leftExists || rightExists) {
                updateElectrodeLayout();
            }
        }

        private void updateElectrodeLayout() {
            ArrayList<Electrode> eList = new ArrayList<>();
            int numBands = 22;

            if (leftExists && rightExists) {
                for (int i = 0; i < numBands; i++) {
                    eList.add(new Electrode(leftMAPelectrodes[i],true,leftMAPTHR[i],leftMAPMCL[i],leftMAPgains[i],rightMAPTHR[i],rightMAPMCL[i],rightMAPgains[i]));
                }
            }
            else if (leftExists) {
                for (int i = 0; i < numBands; i++) {
                    eList.add(new Electrode(leftMAPelectrodes[i],true,leftMAPTHR[i],leftMAPMCL[i],leftMAPgains[i],-1,-1,-1));
                }
            }
            else if (rightExists) {
                for (int i = 0; i < numBands; i++) {
                    eList.add(new Electrode(rightMAPelectrodes[i],true,-1,-1,-1,rightMAPTHR[i],rightMAPMCL[i],rightMAPgains[i]));
                }
            }

            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            myAdapter = new ElectrodeAdapter(getContext(), eList);
            myAdapter.setClickListener(this);
            recyclerView.setAdapter(myAdapter);
        }

        @Override
        public void onItemClick(View view, int position) {
            Toast.makeText(getContext(),"Sup", Toast.LENGTH_SHORT).show();
            //Toast.makeText(this, "You clicked " + adapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
        }

        private double getDouble(final SharedPreferences prefs, final String key) {
            return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits((double) 0)));
        }
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        public PlaceholderFragment() {
        }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_settings_activity_tabbed, container, false);
            TextView textView = rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));
            return rootView;
        }
    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return ParametersFrag.newInstance(position + 1);
                case 1:
                    return ElectrodesFragment.newInstance(position + 1);
                default:
                    return new ElectrodesFragment();
            }

            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            // return PlaceholderFragment.newInstance(position + 1);
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 2;
        }
    }

//    void getMAPFromPreferences() {
//        if (leftMAPelectrodes.length == 22) { // left electrode array
////            textView38.setText(String.valueOf(leftMAPTHR[0]));
////            textView46.setText(String.valueOf(leftMAPTHR[1]));
////            textView54.setText(String.valueOf(leftMAPTHR[2]));
////            textView62.setText(String.valueOf(leftMAPTHR[3]));
////            textView70.setText(String.valueOf(leftMAPTHR[4]));
////            textView78.setText(String.valueOf(leftMAPTHR[5]));
////            textView86.setText(String.valueOf(leftMAPTHR[6]));
////            textView94.setText(String.valueOf(leftMAPTHR[7]));
////            textView102.setText(String.valueOf(leftMAPTHR[8]));
////            textView110.setText(String.valueOf(leftMAPTHR[9]));
////            textView118.setText(String.valueOf(leftMAPTHR[10]));
////            textView126.setText(String.valueOf(leftMAPTHR[11]));
////            textView134.setText(String.valueOf(leftMAPTHR[12]));
////            textView142.setText(String.valueOf(leftMAPTHR[13]));
////            textView150.setText(String.valueOf(leftMAPTHR[14]));
////            textView158.setText(String.valueOf(leftMAPTHR[15]));
////            textView166.setText(String.valueOf(leftMAPTHR[16]));
////            textView174.setText(String.valueOf(leftMAPTHR[17]));
////            textView182.setText(String.valueOf(leftMAPTHR[18]));
////            textView190.setText(String.valueOf(leftMAPTHR[19]));
////            textView198.setText(String.valueOf(leftMAPTHR[20]));
////            textView206.setText(String.valueOf(leftMAPTHR[21]));
////
////            textView39.setText(String.valueOf(leftMAPMCL[0]));
////            textView47.setText(String.valueOf(leftMAPMCL[1]));
////            textView55.setText(String.valueOf(leftMAPMCL[2]));
////            textView63.setText(String.valueOf(leftMAPMCL[3]));
////            textView71.setText(String.valueOf(leftMAPMCL[4]));
////            textView79.setText(String.valueOf(leftMAPMCL[5]));
////            textView87.setText(String.valueOf(leftMAPMCL[6]));
////            textView95.setText(String.valueOf(leftMAPMCL[7]));
////            textView103.setText(String.valueOf(leftMAPMCL[8]));
////            textView111.setText(String.valueOf(leftMAPMCL[9]));
////            textView119.setText(String.valueOf(leftMAPMCL[10]));
////            textView127.setText(String.valueOf(leftMAPMCL[11]));
////            textView135.setText(String.valueOf(leftMAPMCL[12]));
////            textView143.setText(String.valueOf(leftMAPMCL[13]));
////            textView151.setText(String.valueOf(leftMAPMCL[14]));
////            textView159.setText(String.valueOf(leftMAPMCL[15]));
////            textView167.setText(String.valueOf(leftMAPMCL[16]));
////            textView175.setText(String.valueOf(leftMAPMCL[17]));
////            textView183.setText(String.valueOf(leftMAPMCL[18]));
////            textView191.setText(String.valueOf(leftMAPMCL[19]));
////            textView199.setText(String.valueOf(leftMAPMCL[20]));
////            textView207.setText(String.valueOf(leftMAPMCL[21]));
////
////            textView40.setText(String.valueOf(leftMAPgains[0]));
////            textView48.setText(String.valueOf(leftMAPgains[1]));
////            textView56.setText(String.valueOf(leftMAPgains[2]));
////            textView64.setText(String.valueOf(leftMAPgains[3]));
////            textView72.setText(String.valueOf(leftMAPgains[4]));
////            textView80.setText(String.valueOf(leftMAPgains[5]));
////            textView88.setText(String.valueOf(leftMAPgains[6]));
////            textView96.setText(String.valueOf(leftMAPgains[7]));
////            textView104.setText(String.valueOf(leftMAPgains[8]));
////            textView112.setText(String.valueOf(leftMAPgains[9]));
////            textView120.setText(String.valueOf(leftMAPgains[10]));
////            textView128.setText(String.valueOf(leftMAPgains[11]));
////            textView136.setText(String.valueOf(leftMAPgains[12]));
////            textView144.setText(String.valueOf(leftMAPgains[13]));
////            textView152.setText(String.valueOf(leftMAPgains[14]));
////            textView160.setText(String.valueOf(leftMAPgains[15]));
////            textView168.setText(String.valueOf(leftMAPgains[16]));
////            textView176.setText(String.valueOf(leftMAPgains[17]));
////            textView184.setText(String.valueOf(leftMAPgains[18]));
////            textView192.setText(String.valueOf(leftMAPgains[19]));
////            textView200.setText(String.valueOf(leftMAPgains[20]));
////            textView208.setText(String.valueOf(leftMAPgains[21]));
//        } else {
//
//            int electrodeNum;
////            for (int i = 0; i < leftMAPelectrodes.length; i++) {
////                electrodeNum = leftMAPelectrodes[i];
////                switch (electrodeNum) {
////                    case 1:
////                        textView38.setText(String.valueOf(leftMAPTHR[i]));
////                        textView39.setText(String.valueOf(leftMAPMCL[i]));
////                        textView40.setText(String.valueOf(leftMAPgains[i]));
////                        break;
////                    case 2:
////                        textView46.setText(String.valueOf(leftMAPTHR[i]));
////                        textView47.setText(String.valueOf(leftMAPMCL[i]));
////                        textView48.setText(String.valueOf(leftMAPgains[i]));
////                        break;
////                    case 3:
////                        textView54.setText(String.valueOf(leftMAPTHR[i]));
////                        textView55.setText(String.valueOf(leftMAPMCL[i]));
////                        textView56.setText(String.valueOf(leftMAPgains[i]));
////                        break;
////                    case 4:
////                        textView62.setText(String.valueOf(leftMAPTHR[i]));
////                        textView63.setText(String.valueOf(leftMAPMCL[i]));
////                        textView64.setText(String.valueOf(leftMAPgains[i]));
////                        break;
////                    case 5:
////                        textView70.setText(String.valueOf(leftMAPTHR[i]));
////                        textView71.setText(String.valueOf(leftMAPMCL[i]));
////                        textView72.setText(String.valueOf(leftMAPgains[i]));
////                        break;
////                    case 6:
////                        textView78.setText(String.valueOf(leftMAPTHR[i]));
////                        textView79.setText(String.valueOf(leftMAPMCL[i]));
////                        textView80.setText(String.valueOf(leftMAPgains[i]));
////                        break;
////                    case 7:
////                        textView86.setText(String.valueOf(leftMAPTHR[i]));
////                        textView87.setText(String.valueOf(leftMAPMCL[i]));
////                        textView88.setText(String.valueOf(leftMAPgains[i]));
////                        break;
////                    case 8:
////                        textView94.setText(String.valueOf(leftMAPTHR[i]));
////                        textView95.setText(String.valueOf(leftMAPMCL[i]));
////                        textView96.setText(String.valueOf(leftMAPgains[i]));
////                        break;
////                    case 9:
////                        textView102.setText(String.valueOf(leftMAPTHR[i]));
////                        textView103.setText(String.valueOf(leftMAPMCL[i]));
////                        textView104.setText(String.valueOf(leftMAPgains[i]));
////                        break;
////                    case 10:
////                        textView110.setText(String.valueOf(leftMAPTHR[i]));
////                        textView111.setText(String.valueOf(leftMAPMCL[i]));
////                        textView112.setText(String.valueOf(leftMAPgains[i]));
////                    case 11:
////                        textView118.setText(String.valueOf(leftMAPTHR[i]));
////                        textView119.setText(String.valueOf(leftMAPMCL[i]));
////                        textView120.setText(String.valueOf(leftMAPgains[i]));
////                        break;
////                    case 12:
////                        textView126.setText(String.valueOf(leftMAPTHR[i]));
////                        textView127.setText(String.valueOf(leftMAPMCL[i]));
////                        textView128.setText(String.valueOf(leftMAPgains[i]));
////                        break;
////                    case 13:
////                        textView134.setText(String.valueOf(leftMAPTHR[i]));
////                        textView135.setText(String.valueOf(leftMAPMCL[i]));
////                        textView136.setText(String.valueOf(leftMAPgains[i]));
////                        break;
////                    case 14:
////                        textView142.setText(String.valueOf(leftMAPTHR[i]));
////                        textView143.setText(String.valueOf(leftMAPMCL[i]));
////                        textView144.setText(String.valueOf(leftMAPgains[i]));
////                        break;
////                    case 15:
////                        textView150.setText(String.valueOf(leftMAPTHR[i]));
////                        textView151.setText(String.valueOf(leftMAPMCL[i]));
////                        textView152.setText(String.valueOf(leftMAPgains[i]));
////                        break;
////                    case 16:
////                        textView158.setText(String.valueOf(leftMAPTHR[i]));
////                        textView159.setText(String.valueOf(leftMAPMCL[i]));
////                        textView160.setText(String.valueOf(leftMAPgains[i]));
////                        break;
////                    case 17:
////                        textView166.setText(String.valueOf(leftMAPTHR[i]));
////                        textView167.setText(String.valueOf(leftMAPMCL[i]));
////                        textView168.setText(String.valueOf(leftMAPgains[i]));
////                        break;
////                    case 18:
////                        textView174.setText(String.valueOf(leftMAPTHR[i]));
////                        textView175.setText(String.valueOf(leftMAPMCL[i]));
////                        textView176.setText(String.valueOf(leftMAPgains[i]));
////                        break;
////                    case 19:
////                        textView182.setText(String.valueOf(leftMAPTHR[i]));
////                        textView183.setText(String.valueOf(leftMAPMCL[i]));
////                        textView184.setText(String.valueOf(leftMAPgains[i]));
////                        break;
////                    case 20:
////                        textView190.setText(String.valueOf(leftMAPTHR[i]));
////                        textView191.setText(String.valueOf(leftMAPMCL[i]));
////                        textView192.setText(String.valueOf(leftMAPgains[i]));
////                        break;
////                    case 21:
////                        textView198.setText(String.valueOf(leftMAPTHR[i]));
////                        textView199.setText(String.valueOf(leftMAPMCL[i]));
////                        textView200.setText(String.valueOf(leftMAPgains[i]));
////                        break;
////                    case 22:
////                        textView206.setText(String.valueOf(leftMAPTHR[i]));
////                        textView207.setText(String.valueOf(leftMAPMCL[i]));
////                        textView208.setText(String.valueOf(leftMAPgains[i]));
////                        break;
////                }
////            }
//        }
//
//    }


//    void setRightMAPText() {
//        if (rightMAPelectrodes.length == 22) { // right electrode array
////            textView41.setText(String.valueOf(rightMAPTHR[0]));
////            textView49.setText(String.valueOf(rightMAPTHR[1]));
////            textView57.setText(String.valueOf(rightMAPTHR[2]));
////            textView65.setText(String.valueOf(rightMAPTHR[3]));
////            textView73.setText(String.valueOf(rightMAPTHR[4]));
////            textView81.setText(String.valueOf(rightMAPTHR[5]));
////            textView89.setText(String.valueOf(rightMAPTHR[6]));
////            textView97.setText(String.valueOf(rightMAPTHR[7]));
////            textView105.setText(String.valueOf(rightMAPTHR[8]));
////            textView113.setText(String.valueOf(rightMAPTHR[9]));
////            textView121.setText(String.valueOf(rightMAPTHR[10]));
////            textView129.setText(String.valueOf(rightMAPTHR[11]));
////            textView137.setText(String.valueOf(rightMAPTHR[12]));
////            textView145.setText(String.valueOf(rightMAPTHR[13]));
////            textView153.setText(String.valueOf(rightMAPTHR[14]));
////            textView161.setText(String.valueOf(rightMAPTHR[15]));
////            textView169.setText(String.valueOf(rightMAPTHR[16]));
////            textView177.setText(String.valueOf(rightMAPTHR[17]));
////            textView185.setText(String.valueOf(rightMAPTHR[18]));
////            textView193.setText(String.valueOf(rightMAPTHR[19]));
////            textView201.setText(String.valueOf(rightMAPTHR[20]));
////            textView209.setText(String.valueOf(rightMAPTHR[21]));
////
////            textView42.setText(String.valueOf(rightMAPMCL[0]));
////            textView50.setText(String.valueOf(rightMAPMCL[1]));
////            textView58.setText(String.valueOf(rightMAPMCL[2]));
////            textView66.setText(String.valueOf(rightMAPMCL[3]));
////            textView74.setText(String.valueOf(rightMAPMCL[4]));
////            textView82.setText(String.valueOf(rightMAPMCL[5]));
////            textView90.setText(String.valueOf(rightMAPMCL[6]));
////            textView98.setText(String.valueOf(rightMAPMCL[7]));
////            textView106.setText(String.valueOf(rightMAPMCL[8]));
////            textView114.setText(String.valueOf(rightMAPMCL[9]));
////            textView122.setText(String.valueOf(rightMAPMCL[10]));
////            textView130.setText(String.valueOf(rightMAPMCL[11]));
////            textView138.setText(String.valueOf(rightMAPMCL[12]));
////            textView146.setText(String.valueOf(rightMAPMCL[13]));
////            textView154.setText(String.valueOf(rightMAPMCL[14]));
////            textView162.setText(String.valueOf(rightMAPMCL[15]));
////            textView170.setText(String.valueOf(rightMAPMCL[16]));
////            textView178.setText(String.valueOf(rightMAPMCL[17]));
////            textView186.setText(String.valueOf(rightMAPMCL[18]));
////            textView194.setText(String.valueOf(rightMAPMCL[19]));
////            textView202.setText(String.valueOf(rightMAPMCL[20]));
////            textView210.setText(String.valueOf(rightMAPMCL[21]));
////
////            textView43.setText(String.valueOf(rightMAPgains[0]));
////            textView51.setText(String.valueOf(rightMAPgains[1]));
////            textView59.setText(String.valueOf(rightMAPgains[2]));
////            textView67.setText(String.valueOf(rightMAPgains[3]));
////            textView75.setText(String.valueOf(rightMAPgains[4]));
////            textView83.setText(String.valueOf(rightMAPgains[5]));
////            textView91.setText(String.valueOf(rightMAPgains[6]));
////            textView99.setText(String.valueOf(rightMAPgains[7]));
////            textView107.setText(String.valueOf(rightMAPgains[8]));
////            textView115.setText(String.valueOf(rightMAPgains[9]));
////            textView123.setText(String.valueOf(rightMAPgains[10]));
////            textView131.setText(String.valueOf(rightMAPgains[11]));
////            textView139.setText(String.valueOf(rightMAPgains[12]));
////            textView147.setText(String.valueOf(rightMAPgains[13]));
////            textView155.setText(String.valueOf(rightMAPgains[14]));
////            textView163.setText(String.valueOf(rightMAPgains[15]));
////            textView171.setText(String.valueOf(rightMAPgains[16]));
////            textView179.setText(String.valueOf(rightMAPgains[17]));
////            textView187.setText(String.valueOf(rightMAPgains[18]));
////            textView195.setText(String.valueOf(rightMAPgains[19]));
////            textView203.setText(String.valueOf(rightMAPgains[20]));
////            textView211.setText(String.valueOf(rightMAPgains[21]));
//        } else {
//
//            int electrodeNum;
////            for (int i = 0; i < rightMAPelectrodes.length; i++) {
////                electrodeNum = rightMAPelectrodes[i];
////                switch (electrodeNum) {
////                    case 1:
////                        textView41.setText(String.valueOf(rightMAPTHR[i]));
////                        textView42.setText(String.valueOf(rightMAPMCL[i]));
////                        textView43.setText(String.valueOf(rightMAPgains[i]));
////                        break;
////                    case 2:
////                        textView49.setText(String.valueOf(rightMAPTHR[i]));
////                        textView50.setText(String.valueOf(rightMAPMCL[i]));
////                        textView51.setText(String.valueOf(rightMAPgains[i]));
////                        break;
////                    case 3:
////                        textView57.setText(String.valueOf(rightMAPTHR[i]));
////                        textView58.setText(String.valueOf(rightMAPMCL[i]));
////                        textView59.setText(String.valueOf(rightMAPgains[i]));
////                        break;
////                    case 4:
////                        textView65.setText(String.valueOf(rightMAPTHR[i]));
////                        textView66.setText(String.valueOf(rightMAPMCL[i]));
////                        textView67.setText(String.valueOf(rightMAPgains[i]));
////                        break;
////                    case 5:
////                        textView73.setText(String.valueOf(rightMAPTHR[i]));
////                        textView74.setText(String.valueOf(rightMAPMCL[i]));
////                        textView75.setText(String.valueOf(rightMAPgains[i]));
////                        break;
////                    case 6:
////                        textView81.setText(String.valueOf(rightMAPTHR[i]));
////                        textView82.setText(String.valueOf(rightMAPMCL[i]));
////                        textView83.setText(String.valueOf(rightMAPgains[i]));
////                        break;
////                    case 7:
////                        textView89.setText(String.valueOf(rightMAPTHR[i]));
////                        textView90.setText(String.valueOf(rightMAPMCL[i]));
////                        textView91.setText(String.valueOf(rightMAPgains[i]));
////                        break;
////                    case 8:
////                        textView97.setText(String.valueOf(rightMAPTHR[i]));
////                        textView98.setText(String.valueOf(rightMAPMCL[i]));
////                        textView99.setText(String.valueOf(rightMAPgains[i]));
////                        break;
////                    case 9:
////                        textView105.setText(String.valueOf(rightMAPTHR[i]));
////                        textView106.setText(String.valueOf(rightMAPMCL[i]));
////                        textView107.setText(String.valueOf(rightMAPgains[i]));
////                        break;
////                    case 10:
////                        textView113.setText(String.valueOf(rightMAPTHR[i]));
////                        textView114.setText(String.valueOf(rightMAPMCL[i]));
////                        textView115.setText(String.valueOf(rightMAPgains[i]));
////                    case 11:
////                        textView121.setText(String.valueOf(rightMAPTHR[i]));
////                        textView122.setText(String.valueOf(rightMAPMCL[i]));
////                        textView123.setText(String.valueOf(rightMAPgains[i]));
////                        break;
////                    case 12:
////                        textView129.setText(String.valueOf(rightMAPTHR[i]));
////                        textView130.setText(String.valueOf(rightMAPMCL[i]));
////                        textView131.setText(String.valueOf(rightMAPgains[i]));
////                        break;
////                    case 13:
////                        textView137.setText(String.valueOf(rightMAPTHR[i]));
////                        textView138.setText(String.valueOf(rightMAPMCL[i]));
////                        textView139.setText(String.valueOf(rightMAPgains[i]));
////                        break;
////                    case 14:
////                        textView145.setText(String.valueOf(rightMAPTHR[i]));
////                        textView146.setText(String.valueOf(rightMAPMCL[i]));
////                        textView147.setText(String.valueOf(rightMAPgains[i]));
////                        break;
////                    case 15:
////                        textView153.setText(String.valueOf(rightMAPTHR[i]));
////                        textView154.setText(String.valueOf(rightMAPMCL[i]));
////                        textView155.setText(String.valueOf(rightMAPgains[i]));
////                        break;
////                    case 16:
////                        textView161.setText(String.valueOf(rightMAPTHR[i]));
////                        textView162.setText(String.valueOf(rightMAPMCL[i]));
////                        textView163.setText(String.valueOf(rightMAPgains[i]));
////                        break;
////                    case 17:
////                        textView169.setText(String.valueOf(rightMAPTHR[i]));
////                        textView170.setText(String.valueOf(rightMAPMCL[i]));
////                        textView171.setText(String.valueOf(rightMAPgains[i]));
////                        break;
////                    case 18:
////                        textView177.setText(String.valueOf(rightMAPTHR[i]));
////                        textView178.setText(String.valueOf(rightMAPMCL[i]));
////                        textView179.setText(String.valueOf(rightMAPgains[i]));
////                        break;
////                    case 19:
////                        textView185.setText(String.valueOf(rightMAPTHR[i]));
////                        textView186.setText(String.valueOf(rightMAPMCL[i]));
////                        textView187.setText(String.valueOf(rightMAPgains[i]));
////                        break;
////                    case 20:
////                        textView193.setText(String.valueOf(rightMAPTHR[i]));
////                        textView194.setText(String.valueOf(rightMAPMCL[i]));
////                        textView195.setText(String.valueOf(rightMAPgains[i]));
////                        break;
////                    case 21:
////                        textView201.setText(String.valueOf(rightMAPTHR[i]));
////                        textView202.setText(String.valueOf(rightMAPMCL[i]));
////                        textView203.setText(String.valueOf(rightMAPgains[i]));
////                        break;
////                    case 22:
////                        textView209.setText(String.valueOf(rightMAPTHR[i]));
////                        textView210.setText(String.valueOf(rightMAPMCL[i]));
////                        textView211.setText(String.valueOf(rightMAPgains[i]));
////                        break;
////                }
////            }
//
//        }
//    }

    double getDouble(final SharedPreferences prefs, final String key) {
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits((double) 0)));
    }

}