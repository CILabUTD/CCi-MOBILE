package cilab.utdallas.edu.ccimobile;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ParametersFrag extends Fragment {
    TextView textViewITL;

    OnParametersSelectedListener mCallback;

    public void setOnParametersSelectedListener(Activity activity) {
        mCallback = (OnParametersSelectedListener) activity;
    }

    // Container Activity must implement this interface
    public interface OnParametersSelectedListener {
        public void onArticleSelected(int position);
    }


    public ParametersFrag() {
        // required empty constructor
    }

    /**
     * Returns a new instance of this fragment.
     */
    public static ParametersFrag newInstance(int sectionNumber) {
        ParametersFrag fragment = new ParametersFrag();
//        Bundle args = new Bundle();
//        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
//        fragment.setArguments(args);
        return fragment;
    }

    public void doStuff() {
        textViewITL.setText("Sup");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View settingsView = inflater.inflate(R.layout.settings, container, false);
        textViewITL = settingsView.findViewById(R.id.textViewITL);
        //textViewITL.setText("Sup");

        return settingsView;
        //return settingsView;
    }


}
