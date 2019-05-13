package cilab.utdallas.edu.ccimobile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import java.util.ArrayList;

import androidx.recyclerview.widget.RecyclerView;

public class ElectrodeAdapter extends RecyclerView.Adapter<ElectrodeAdapter.ViewHolder> {

    private ArrayList<Electrode> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        //TextView myTextView;

        TextView elecNum, T1, M1, G1, T2, M2, G2;
        CheckBox onOffBox;

        ViewHolder(View itemView) {
            super(itemView);

            elecNum = itemView.findViewById(R.id.stuff1);
            onOffBox = itemView.findViewById(R.id.stuff2);
            T1 = itemView.findViewById(R.id.stuff3);
            M1 = itemView.findViewById(R.id.stuff4);
            G1 = itemView.findViewById(R.id.stuff5);
            T2 = itemView.findViewById(R.id.stuff6);
            M2 = itemView.findViewById(R.id.stuff7);
            G2 = itemView.findViewById(R.id.stuff8);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null)
                mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // data is passed into the constructor
    public ElectrodeAdapter(Context context, ArrayList<Electrode> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent,
                                         int viewType) {
        // create a new view
        View view = mInflater.inflate(R.layout.recyclerview_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        // - get element from your dataset at this position
        // - replace the contents of the view with that element

        Electrode currentElectrode = mData.get(position);
        int electrode_num = currentElectrode.getElectrodeNum();
        boolean on_off = currentElectrode.getOnOrOff();
        int THR1 = currentElectrode.getTHR1();
        int THR2 = currentElectrode.getTHR2();
        int MCL1 = currentElectrode.getMCL1();
        int MCL2 = currentElectrode.getMCL2();
        double gain1 = currentElectrode.getGain1();
        double gain2 = currentElectrode.getGain2();

        holder.elecNum.setText(String.valueOf(electrode_num));
        holder.onOffBox.setChecked(on_off);
        holder.T1.setText(String.valueOf(THR1));
        holder.T2.setText(String.valueOf(THR2));
        holder.M1.setText(String.valueOf(MCL1));
        holder.M2.setText(String.valueOf(MCL2));
        holder.G1.setText(String.valueOf(gain1));
        holder.G2.setText(String.valueOf(gain2));
    }

    // total number of rows
    @Override
    public int getItemCount() {
        //return mDataset.length;
        return mData.size();
    }

    // convenience method for getting data at click position
//    String getItem(int id) {
//        return mData.get(id);
//    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }


}



