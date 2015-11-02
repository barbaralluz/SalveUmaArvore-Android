package com.salveumaarvore.barbara.salveumaarvore;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;

public class SingleTree_Activity extends Activity {

    // JSON node keys
    private static final String TAG_ID = "id";
    private static final String TAG_POINT = "geometry";
    private static final String TAG_LAT = "latitude";
    private static final String TAG_LON = "longitude";
    private static final String TAG_ESPECIE = "especie";
    private static final String TAG_CONDICAO = "condicao_arvore";
    private static final String TAG_ADMINISTRATIVE_AREA_LEVEL_1 = "administrative_area_level_1";
    private static final String TAG_LOCALITY = "locality";
    private static final String TAG_NEIGHBOORHOOD = "neighborhood";
    private static final String TAG_ROUTE = "route";
    private static final String TAG_NUMERO = "numero";
    private static final String TAG_ADDRESS = "endereco";
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_tree);

        // getting intent data
        Intent in = getIntent();

        // Get JSON values from previous intent
        String especie = in.getStringExtra(TAG_ESPECIE);
        String endereco = in.getStringExtra(TAG_ADDRESS);
        String condicao_geral = in.getStringExtra(TAG_CONDICAO);

        // Displaying all values on the screen
        TextView lblEspecie = (TextView) findViewById(R.id.especie_label);
        TextView lblEndereco = (TextView) findViewById(R.id.endereco_label);
        TextView lblCondicaoGeral = (TextView) findViewById(R.id.condicaoGeral_label);

        lblEspecie.setText(especie);
        lblEndereco.setText(endereco);
        lblCondicaoGeral.setText(condicao_geral);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu_home; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_single_tree_, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.voltar_list) {
            Intent i = new Intent(SingleTree_Activity.this, TreesActivity.class);
            startActivity(i);
            finish();
            return true;
        }

        if (id == R.id.voltar) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
