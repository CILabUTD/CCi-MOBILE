package cilab.utdallas.edu.ccimobile;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {

    private List<String> mData;
    private LayoutInflater mInflater;
    private ItemClickListener mClickListener;

    // data is passed into the constructor
    MyAdapter(Context context, List<String> data) {
        this.mInflater = LayoutInflater.from(context);
        this.mData = data;
    }

    // inflates the row layout from xml when needed
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.recyclerview_row, parent, false);
        return new ViewHolder(view);
    }

    // binds the data to the TextView in each row
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        String animal = mData.get(position);
        holder.myTextView.setText(animal);
    }

    // total number of rows
    @Override
    public int getItemCount() {
        return mData.size();
    }


    // stores and recycles views as they are scrolled off screen
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView myTextView;

        ViewHolder(View itemView) {
            super(itemView);
            //myTextView = itemView.findViewById(R.id.tvAnimalName); !!!!!!!!!!!!!
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view) {
            if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
        }
    }

    // convenience method for getting data at click position
    String getItem(int id) {
        return mData.get(id);
    }

    // allows clicks events to be caught
    void setClickListener(ItemClickListener itemClickListener) {
        this.mClickListener = itemClickListener;
    }

    // parent activity will implement this method to respond to click events
    public interface ItemClickListener {
        void onItemClick(View view, int position);
    }
}

//public class MyAdapter extends RecyclerView.Adapter<MyAdapter.ViewHolder> {
//    // private String[] mDataset;
//
//    private List<String> mData;
//    private LayoutInflater mInflater;
//    private ItemClickListener mClickListener;
//
//    // Provide a reference to the views for each data item
//    // Complex data items may need more than one view per item, and
//    // you provide access to all the views for a data item in a view holder
////    public static class MyViewHolder extends RecyclerView.ViewHolder {
////        // each data item is just a string in this case
////        public TextView textView;
////        public MyViewHolder(TextView v) {
////            super(v);
////            textView = v;
////        }
////    }
//
//    // stores and recycles views as they are scrolled off screen
//    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
//        TextView myTextView;
//
//        ViewHolder(View itemView) {
//            super(itemView);
//            myTextView = itemView.findViewById(R.id.tvAnimalName);
//            itemView.setOnClickListener(this);
//        }
//
//        @Override
//        public void onClick(View view) {
//            if (mClickListener != null)
//                mClickListener.onItemClick(view, getAdapterPosition());
//        }
//    }
//
//    // Provide a suitable constructor (depends on the kind of dataset)
////    public MyAdapter(String[] myDataset) {
////        mDataset = myDataset;
////    }
//
//    // data is passed into the constructor
//    public MyAdapter(Context context, List<String> data) {
//        this.mInflater = LayoutInflater.from(context);
//        this.mData = data;
//    }
//
//    // Create new views (invoked by the layout manager)
//
//    // inflates the row layout from xml when needed
//    @Override
//    public ViewHolder onCreateViewHolder(ViewGroup parent,
//                                                     int viewType) {
//        // create a new view
//        View view = mInflater.inflate(R.layout.recyclerview_row, parent, false);
//        return new ViewHolder(view);
//    }
////    @Override
////    public MyAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent,
////                                                     int viewType) {
////        // create a new view
////        TextView v = (TextView) LayoutInflater.from(parent.getContext())
////                .inflate(R.layout.my_text_view, parent, false);
////
////        MyViewHolder vh = new MyViewHolder(v);
////        return vh;
////    }
//
//    // Replace the contents of a view (invoked by the layout manager)
//    // binds the data to the TextView in each row
//    @Override
//    public void onBindViewHolder(ViewHolder holder, int position) {
//        // - get element from your dataset at this position
//        // - replace the contents of the view with that element
//        //holder.textView.setText(mDataset[position]);
//
//        String animal = mData.get(position);
//        holder.myTextView.setText(animal);
//    }
//
//    // Return the size of your dataset (invoked by the layout manager)
//    // total number of rows
//    @Override
//    public int getItemCount() {
//        //return mDataset.length;
//        return mData.size();
//    }
//
//    // convenience method for getting data at click position
//    String getItem(int id) {
//        return mData.get(id);
//    }
//
//    // allows clicks events to be caught
//    void setClickListener(ItemClickListener itemClickListener) {
//        this.mClickListener = itemClickListener;
//    }
//
//    // parent activity will implement this method to respond to click events
//    public interface ItemClickListener {
//        void onItemClick(View view, int position);
//    }
//}