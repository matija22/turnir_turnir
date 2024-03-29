package com.example.turnirmk;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.airbnb.lottie.LottieAnimationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
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
    Button pogledaj;
    DatabaseReference databaseEkipe;
    DatabaseReference databaseFlag = FirebaseDatabase.getInstance().getReference("profil");
    DatabaseReference databaseStrijelac;
    ListView listViewEkipe;
    List<Ekipe> ekipe;
    List<Profil> profili;
    public static int flag;
    public static String test;
    public static String vlasnik;
    private Button napraviGrupe;
    public static int brojekipa = 0;
    List<String> idevi;
    private FirebaseAuth mAuth;
    FirebaseUser currentUser = mAuth.getInstance().getCurrentUser();
    String email = currentUser.getEmail();
    String[] username = email.split("@");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dodaj_ekipu);

        napraviGrupe = (Button) findViewById(R.id.napraviGrupe);
        imeDog = (TextView) findViewById(R.id.imeDog);
        imeEkipe = (EditText) findViewById(R.id.imeEkipe);
        Prijavi = (Button) findViewById(R.id.Prijavi);
        pogledaj = (Button) findViewById(R.id.pogledaj);
        listViewEkipe = (ListView) findViewById(R.id.listViewEkipe);
        ekipe = new ArrayList<>();
        profili = new ArrayList<>();
        idevi = new ArrayList<>();

        Intent intent = getIntent();
        final String id = intent.getStringExtra(DohvatiPodatke.ID_DOGADAJA);
        final String name = intent.getStringExtra(DohvatiPodatke.IME_DOGADAJA);
        final String dat = intent.getStringExtra(DohvatiPodatke.DATUM);
        vlasnik = intent.getStringExtra("VLASNIK");

        imeDog.setText(name);
        napraviGrupe.setVisibility(View.GONE);
        pogledaj.setVisibility(View.GONE);

        databaseEkipe = FirebaseDatabase.getInstance().getReference("ekipe").child(id);
        databaseStrijelac = FirebaseDatabase.getInstance().getReference("strijelci").child(id);
        DatabaseReference databaseFlagUtakmice = FirebaseDatabase.getInstance().getReference("utakmice").child(id);



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
        pogledaj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(DodajEkipu.this, tabMain.class);
                intent.putExtra("ID", id);
                intent.putExtra("IME",name);
                intent.putExtra("VLASNIK", vlasnik);
                startActivity(intent);
            }
        });
        databaseFlagUtakmice.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if(dataSnapshot.exists()){
                    flag = 1;
                    pogledaj.setVisibility(View.VISIBLE);
                    napraviGrupe.setVisibility(View.GONE);
                    imeEkipe.setEnabled(false);
                    Prijavi.setEnabled(false);
                }
                else{
                    flag = 0;
                    pogledaj.setVisibility(View.GONE);
                    napraviGrupe.setVisibility(View.VISIBLE);
                    imeEkipe.setEnabled(true);
                    Prijavi.setEnabled(true);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    @Override
    protected void onStart() {
        super.onStart();
        setTitle("Prijava ekipa");
        databaseEkipe.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                ekipe.clear();
                for(DataSnapshot ekipeSnapshot : dataSnapshot.getChildren()){
                    Ekipe ekipa = ekipeSnapshot.getValue(Ekipe.class);
                    ekipe.add(ekipa);
                }
                brojekipa = ekipe.size();
                if(vlasnik.equals(username[0]) && (brojekipa==8 || brojekipa==16) && flag==0){
                    napraviGrupe.setVisibility(View.VISIBLE);
                }
                else{
                    napraviGrupe.setVisibility(View.GONE);
                }
                ListaEkipa listaEkipaAdapter = new ListaEkipa(DodajEkipu.this, ekipe);
                listViewEkipe.setAdapter(listaEkipaAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


        databaseFlag.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                profili.clear();
                for (DataSnapshot data:dataSnapshot.getChildren()){
                    Profil imeProfila = data.getValue(Profil.class);
                    profili.add(imeProfila);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });


    }


    private void saveEkipe(){
        final String name = imeEkipe.getText().toString().trim();
        final String id = getIntent().getStringExtra(DohvatiPodatke.ID_DOGADAJA);

        if(!TextUtils.isEmpty(name)){
            String id_ekipa = databaseEkipe.push().getKey();
            Ekipe ekipe = new Ekipe(id_ekipa, name, username[0], id);
            databaseEkipe.child(id_ekipa).setValue(ekipe);
            for (int i=0;i<profili.size();i++) {
                String tim = profili.get(i).getEkipa();
                if(name.equals(tim)) {
                    String id_strijelca = databaseStrijelac.push().getKey();
                    Strijelac strijelac = new Strijelac(profili.get(i).getUsername(), profili.get(i).getEkipa(), id_strijelca,0);
                    databaseStrijelac.child(id_strijelca).setValue(strijelac);
                }
            }

            Toast.makeText(this, "Uspjesno ste prijavili ekipu!",Toast.LENGTH_SHORT).show();
            imeEkipe.setText("");
        }
        else{
            Toast.makeText(this, "Morate upisati ime ekipe!",Toast.LENGTH_LONG).show();
        }
    }

}
