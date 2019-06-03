package com.example.turnirmk;

import android.content.Intent;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class DodajEkipu extends AppCompatActivity {

    TextView imeDog;
    EditText imeEkipe;

    Button Prijavi;
    DatabaseReference databaseEkipe;

    ListView listViewEkipe;
    List<Ekipe> ekipe;
    private FloatingActionButton napraviGrupe;
    public static int brojekipa = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_ekipu);

        napraviGrupe = (FloatingActionButton) findViewById(R.id.napraviGrupe);
        imeDog = (TextView) findViewById(R.id.imeDog);
        imeEkipe = (EditText) findViewById(R.id.imeEkipe);
        Prijavi = (Button) findViewById(R.id.Prijavi);

        listViewEkipe = (ListView) findViewById(R.id.listViewEkipe);

        ekipe = new ArrayList<>();

        Intent intent = getIntent();
        final String id = intent.getStringExtra(DohvatiPodatke.ID_DOGADAJA);
        String name = intent.getStringExtra(DohvatiPodatke.IME_DOGADAJA);
        final String dat = intent.getStringExtra(DohvatiPodatke.DATUM);

        imeDog.setText(name);

        databaseEkipe = FirebaseDatabase.getInstance().getReference("ekipe").child(id);

        Prijavi.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveEkipe();
            }
        });
        napraviGrupe.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(DodajEkipu.this, grupe.class);
                    intent.putExtra("GRUPE", id);
                    intent.putExtra("DATE", dat);
                    startActivity(intent);
                }
            });
    }


    @Override
    protected void onStart() {
        super.onStart();

        databaseEkipe.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ekipe.clear();
                for(DataSnapshot ekipeSnapshot : dataSnapshot.getChildren()){
                    Ekipe ekipa = ekipeSnapshot.getValue(Ekipe.class);
                    ekipe.add(ekipa);
                }
                ListaEkipa listaEkipaAdapter = new ListaEkipa(DodajEkipu.this, ekipe);
                listViewEkipe.setAdapter(listaEkipaAdapter);
                //tu ima broj ekipa
                brojekipa = ekipe.size();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private void saveEkipe(){
        String name = imeEkipe.getText().toString().trim();

        if(!TextUtils.isEmpty(name)){
            String id = databaseEkipe.push().getKey();
            Ekipe ekipe = new Ekipe(id, name);
            databaseEkipe.child(id).setValue(ekipe);
            Toast.makeText(this, "Uspjesno ste prijavili ekipu!",Toast.LENGTH_LONG).show();

        }
        else{
            Toast.makeText(this, "Morate upisati ime ekipe!",Toast.LENGTH_LONG).show();
        }
    }

}