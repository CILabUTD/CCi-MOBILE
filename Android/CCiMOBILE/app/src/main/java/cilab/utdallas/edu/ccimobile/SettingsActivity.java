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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Objects;

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

import static cilab.utdallas.edu.ccimobile.SharedHelper.getDouble;

public class SettingsActivity extends AppCompatActivity implements ParametersFragment.OnParametersSelectedListener {

    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    FloatingActionButton fab;
    int[] colorIntArray = {R.color.colorAccent, R.color.colorAccent};
    int[] iconIntArray = {R.drawable.ic_autorenew_black_24dp, R.drawable.ic_autorenew_black_24dp};

    @Override
    public void onAttachFragment(Fragment fragment) {
        if (fragment instanceof ParametersFragment) {
            ParametersFragment paramFragment = (ParametersFragment) fragment;
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
            ParametersFragment fragmentObj = (ParametersFragment) mSectionsPagerAdapter.instantiateItem(mViewPager, 0);
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
     * @param fo ParametersFragment fragment object
     */
    public void sendIntentBack(ParametersFragment fo) {
        // Send info back to first activity
        Intent intent = new Intent();

        // here
        PatientMAP patientMAPleft = new PatientMAP();
        PatientMAP patientMAPright = new PatientMAP();

        // Get info from Parameters fragment
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(SettingsActivity.this);
        patientMAPleft.setExists(preferences.getBoolean("leftMapExists", false));
        patientMAPright.setExists(preferences.getBoolean("rightMapExists", false));

        if (patientMAPleft.isExists()) {
            // insert sound processing strategy
            patientMAPleft.setnMaxima(preferences.getInt("Left.nMaxima", 0));
            // insert stimulation mode
            patientMAPleft.setStimulationModeCode(fo.getLeftStimulationModeCode());
            patientMAPleft.setStimulationRate(preferences.getInt("Left.stimulationRate", 0));
            patientMAPleft.setPulseWidth(preferences.getInt("Left.pulseWidth", 0));
            patientMAPleft.setSensitivity(getDouble(preferences, "Left.sensitivity"));
            patientMAPleft.setGain(getDouble(preferences, "Left.gain"));
            patientMAPleft.setVolume(preferences.getInt("Left.volume", 0));
            patientMAPleft.setQfactor(getDouble(preferences, "Left.Qfactor"));
            patientMAPleft.setBaseLevel(getDouble(preferences, "Left.baseLevel"));
            patientMAPleft.setSaturationLevel(getDouble(preferences, "Left.saturationLevel"));
            patientMAPleft.setStimulationOrder(preferences.getString("Left.stimulationOrder", ""));
            patientMAPleft.setWindow(preferences.getString("Left.window", ""));
            // put in full check?

            // other
            patientMAPleft.setImplantGeneration(fo.getLeftImplantGeneration());
            patientMAPleft.setPulsesPerFramePerChannel(fo.getLeftPulsesPerFramePerChannel());
            patientMAPleft.setPulsesPerFrame(fo.getLeftPulsesPerFrame());
            patientMAPleft.setInterpulseDuration(fo.getLeftInterpulseDuration());
            patientMAPleft.setnRFcycles(fo.getLeftnRFcycles());
        }

        if (patientMAPright.isExists()) {
            // insert sound processing strategy
            patientMAPright.setnMaxima(preferences.getInt("Right.nMaxima", 0));
            // insert stimulation mode
            patientMAPright.setStimulationModeCode(fo.getRightStimulationModeCode());
            patientMAPright.setStimulationRate(preferences.getInt("Right.stimulationRate", 0));
            patientMAPright.setPulseWidth(preferences.getInt("Right.pulseWidth", 0));
            patientMAPright.setSensitivity(getDouble(preferences, "Right.sensitivity"));
            patientMAPright.setGain(getDouble(preferences, "Right.gain"));
            patientMAPright.setVolume(preferences.getInt("Right.volume", 0));
            patientMAPright.setQfactor(getDouble(preferences, "Right.Qfactor"));
            patientMAPright.setBaseLevel(getDouble(preferences, "Right.baseLevel"));
            patientMAPright.setSaturationLevel(getDouble(preferences, "Right.saturationLevel"));
            patientMAPright.setStimulationOrder(preferences.getString("Right.stimulationOrder", ""));
            patientMAPright.setWindow(preferences.getString("Right.window", ""));
            // put in full check?

            // other
            patientMAPright.setImplantGeneration(fo.getRightImplantGeneration());
            patientMAPright.setPulsesPerFramePerChannel(fo.getRightPulsesPerFramePerChannel());
            patientMAPright.setPulsesPerFrame(fo.getRightPulsesPerFrame());
            patientMAPright.setInterpulseDuration(fo.getRightInterpulseDuration());
            patientMAPright.setnRFcycles(fo.getRightnRFcycles());
        }

        intent.putExtra("PatientMAPleft", patientMAPleft);
        intent.putExtra("PatientMAPright", patientMAPright);

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
         * Gets data from the MAP
         */
        private void getElectrodeFromMAP() {
            Intent intent = Objects.requireNonNull(getActivity()).getIntent();
            PatientMAP pmapleft = intent.getParcelableExtra("PatientMAPleft");
            PatientMAP pmapright = intent.getParcelableExtra("PatientMAPright");

            leftExists = pmapleft.isExists();
            rightExists = pmapright.isExists();

            if (leftExists) {
                leftMAPnbands = pmapleft.getNbands();
                leftMAPTHR = pmapleft.getTHR();
                leftMAPMCL = pmapleft.getMCL();
                leftMAPgains = pmapleft.getGains();
                leftMAPelectrodes = pmapleft.getElectrodes();
            }

            if (rightExists) {
                rightMAPnbands = pmapright.getNbands();
                rightMAPTHR = pmapright.getTHR();
                rightMAPMCL = pmapright.getMCL();
                rightMAPgains = pmapright.getGains();
                rightMAPelectrodes = pmapright.getElectrodes();
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
            // Toast.makeText(getContext(),"Example", Toast.LENGTH_SHORT).show();
            // Toast.makeText(this, "You clicked " + adapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
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
                    return ParametersFragment.newInstance(position + 1);
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

}