package cilab.utdallas.edu.ccimobile;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public class TestingActivity extends AppCompatActivity implements ElectrodeAdapter.ItemClickListener {

    ElectrodeAdapter myAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.testing);

        ArrayList<Electrode> eList = new ArrayList<>();
        eList.add(new Electrode(1,true,100,100,0,100,100,0));
        eList.add(new Electrode(2,true,100,100,0,100,100,0));
        eList.add(new Electrode(3,true,100,100,0,100,100,0));
        eList.add(new Electrode(4,true,100,100,0,100,100,0));
        eList.add(new Electrode(5,true,100,100,0,100,100,0));
        eList.add(new Electrode(6,true,100,100,0,100,100,0));
        eList.add(new Electrode(7,true,100,100,0,100,100,0));
        eList.add(new Electrode(8,true,100,100,0,100,100,0));
        eList.add(new Electrode(9,true,100,100,0,100,100,0));
        eList.add(new Electrode(10,true,100,100,0,100,100,0));
        eList.add(new Electrode(11,true,100,100,0,100,100,0));
        eList.add(new Electrode(12,true,100,100,0,100,100,0));
        eList.add(new Electrode(13,true,100,100,0,100,100,0));
        eList.add(new Electrode(14,true,100,100,0,100,100,0));
        eList.add(new Electrode(15,true,100,100,0,100,100,0));
        eList.add(new Electrode(16,true,100,100,0,100,100,0));
        eList.add(new Electrode(17,true,100,100,0,100,100,0));
        eList.add(new Electrode(18,true,100,100,0,100,100,0));
        eList.add(new Electrode(19,true,100,100,0,100,100,0));
        eList.add(new Electrode(20,true,100,100,0,100,100,0));
        eList.add(new Electrode(21,true,100,100,0,100,100,0));
        eList.add(new Electrode(22,true,100,100,0,100,100,0));


        RecyclerView recyclerView = findViewById(R.id.myRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        myAdapter = new ElectrodeAdapter(this, eList);
        myAdapter.setClickListener(this);
        recyclerView.setAdapter(myAdapter);
    }

    @Override
    public void onItemClick(View view, int position) {
        Toast.makeText(this,"Sup", Toast.LENGTH_SHORT).show();
        //Toast.makeText(this, "You clicked " + adapter.getItem(position) + " on row number " + position, Toast.LENGTH_SHORT).show();
    }

}
