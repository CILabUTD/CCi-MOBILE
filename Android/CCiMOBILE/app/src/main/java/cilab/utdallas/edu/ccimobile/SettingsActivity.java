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

        fab.setOnClickListener(view -> {
            ParametersFrag fragmentObj = (ParametersFrag) mSectionsPagerAdapter.instantiateItem(mViewPager, 0);
            boolean problems = fragmentObj.updateMAPButton();
            if (!problems)
                Snackbar.make(findViewById(R.id.rootSettings), "MAP updated successfully.", Snackbar.LENGTH_LONG).setAction("Action", null).show();
            sendIntentBack(fragmentObj);

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
            }


            if (leftExists || rightExists) {
                updateElectrodeLayout();
            }
        }

        private void updateElectrodeLayout() {
            ArrayList<Electrode> eList = new ArrayList<>();
            //int numBands = 22;

            if (leftExists && rightExists) {
                for (int i = 0; i < leftMAPnbands; i++) {
                    eList.add(new Electrode(leftMAPelectrodes[i],true,leftMAPTHR[i],leftMAPMCL[i],leftMAPgains[i],rightMAPTHR[i],rightMAPMCL[i],rightMAPgains[i]));
                }
            }
            else if (leftExists) {
                for (int i = 0; i < leftMAPnbands; i++) {
                    eList.add(new Electrode(leftMAPelectrodes[i],true,leftMAPTHR[i],leftMAPMCL[i],leftMAPgains[i],-1,-1,-1));
                }
            }
            else if (rightExists) {
                for (int i = 0; i < rightMAPnbands; i++) {
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

    double getDouble(final SharedPreferences prefs, final String key) {
        return Double.longBitsToDouble(prefs.getLong(key, Double.doubleToLongBits((double) 0)));
    }

}