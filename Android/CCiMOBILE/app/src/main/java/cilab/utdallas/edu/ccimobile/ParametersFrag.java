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
    private EditText editTextSRL, editTextPWL, editTextSL, editTextGL, editTextQFL, editTextBLL, editTextSLL,
            editTextSRR, editTextPWR, editTextSR, editTextGR, editTextQFR, editTextBLR, editTextSLR;
    private TextView textViewITL, textViewSFL, textViewNCL, textViewFTL, textViewITR, textViewSFR, textViewNCR, textViewFTR;

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

    public void doStuff() {
        textViewITL.setText("Sup");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        //return inflater.inflate(R.layout.settings, container, false);

        View settingsView = inflater.inflate(R.layout.settings, container, false);

        TextView textViewITL = settingsView.findViewById(R.id.textViewITL);
        TextView textViewSFL = settingsView.findViewById(R.id.textViewSFL);
        TextView textViewNCL = settingsView.findViewById(R.id.textViewNCL);
        TextView textViewFTL = settingsView.findViewById(R.id.textViewFTL);
        TextView textViewITR = settingsView.findViewById(R.id.textViewITR);
        TextView textViewSFR = settingsView.findViewById(R.id.textViewSFR);
        TextView textViewNCR = settingsView.findViewById(R.id.textViewNCR);
        TextView textViewFTR = settingsView.findViewById(R.id.textViewFTR);

        EditText editTextSRL = settingsView.findViewById(R.id.editTextSRL);
        EditText editTextPWL = settingsView.findViewById(R.id.editTextPWL);
        EditText editTextSL = settingsView.findViewById(R.id.editTextSL);
        EditText editTextGL = settingsView.findViewById(R.id.editTextGL);
        EditText editTextQFL = settingsView.findViewById(R.id.editTextQFL);
        EditText editTextBLL = settingsView.findViewById(R.id.editTextBLL);
        EditText editTextSLL = settingsView.findViewById(R.id.editTextSLL);
        EditText editTextSRR = settingsView.findViewById(R.id.editTextSRR);
        EditText editTextPWR = settingsView.findViewById(R.id.editTextPWR);
        EditText editTextSR = settingsView.findViewById(R.id.editTextSR);
        EditText editTextGR = settingsView.findViewById(R.id.editTextGR);
        EditText editTextQFR = settingsView.findViewById(R.id.editTextQFR);
        EditText editTextBLR = settingsView.findViewById(R.id.editTextBLR);
        EditText editTextSLR = settingsView.findViewById(R.id.editTextSLR);

        Spinner spinnerVL = settingsView.findViewById(R.id.spinnerVL);
        Spinner spinnerVR = settingsView.findViewById(R.id.spinnerVR);
        Spinner spinnerSPSL = settingsView.findViewById(R.id.spinnerSPSL);
        Spinner spinnerSPSR = settingsView.findViewById(R.id.spinnerSPSR);
        Spinner spinnerSOL = settingsView.findViewById(R.id.spinnerSOL);
        Spinner spinnerSOR = settingsView.findViewById(R.id.spinnerSOR);
        Spinner spinnerSML = settingsView.findViewById(R.id.spinnerSML);
        Spinner spinnerSMR = settingsView.findViewById(R.id.spinnerSMR);
        Spinner spinnerWL = settingsView.findViewById(R.id.spinnerWL);
        Spinner spinnerWR = settingsView.findViewById(R.id.spinnerWR);
        Spinner spinnerNML = settingsView.findViewById(R.id.spinnerNML);
        Spinner spinnerNMR = settingsView.findViewById(R.id.spinnerNMR);

        spinnerNML.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.v("item", (String) parent.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        spinnerNMR.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                Log.v("item", (String) parent.getItemAtPosition(position));
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        return inflater.inflate(R.layout.settings, container, false);
        //return settingsView;
    }


}
