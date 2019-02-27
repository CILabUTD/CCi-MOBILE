package cilab.utdallas.edu.ccimobile;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class TestingActivity extends AppCompatActivity implements ElectrodeAdapter.ItemClickListener{
    //public class TestingActivity extends AppCompatActivity implements MyAdapter.ItemClickListener{

//    private RecyclerView recyclerView;
//    private RecyclerView.Adapter mAdapter;
//    private RecyclerView.LayoutManager layoutManager;

    //MyAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testing);

        ArrayList<Electrode> eList = new ArrayList<>();
        eList.add(new Electrode(1,true,50,100,150,200,250,500));
        eList.add(new Electrode(2,true,60,100,150,200,250,500));
        eList.add(new Electrode(3,true,70,100,150,200,250,500));
        eList.add(new Electrode(4,true,80,100,150,200,250,500));
        eList.add(new Electrode(5,true,90,100,150,200,250,500));

        //ElectrodeAdapter myAd = new ElectrodeAdapter(this, eList);
        ElectrodeAdapter myAd;

        RecyclerView recyclerView = findViewById(R.id.rvAnimals);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        myAd = new ElectrodeAdapter(this, eList);
        myAd.setClickListener(this);
        recyclerView.setAdapter(myAd);

        // data to populate the RecyclerView with
//        ArrayList<String> animalNames = new ArrayList<>();
//        animalNames.add("Horse");
//        animalNames.add("Cow");
//        animalNames.add("Camel");
//        animalNames.add("Sheep");
//        animalNames.add("Goat");

        // set up the RecyclerView
//        RecyclerView recyclerView = findViewById(R.id.rvAnimals);
//        recyclerView.setLayoutManager(new LinearLayoutManager(this));
//        adapter = new MyAdapter(this, animalNames);
//        adapter.setClickListener(this);
//        recyclerView.setAdapter(adapter);

//        recyclerView = findViewById(R.id.my_recycler_view);
//
//        // use this setting to improve performance if you know that changes
//        // in content do not change the layout size of the RecyclerView
//        recyclerView.setHasFixedSize(true);
//
//        // use a linear layout manager
//        layoutManager = new LinearLayoutManager(this);
//        recyclerView.setLayoutManager(layoutManager);
//
//        // specify an adapter (see also next example)
//        mAdapter = new MyAdapter(myDataset);
//        recyclerView.setAdapter(mAdapter);
    }

    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(this,"Sup", Toast.LENGTH_SHORT).show();
        //Toast.makeText(this, "You clicked " + adapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
    }

    public void sendIntentBack() {
        // Send info back to first activity
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
    }



}
