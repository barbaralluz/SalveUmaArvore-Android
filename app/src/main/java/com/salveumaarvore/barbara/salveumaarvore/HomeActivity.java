package com.salveumaarvore.barbara.salveumaarvore;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ScaleBarOverlay;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

public class HomeActivity extends Activity {

    GPSTracker gps;
    private MapView mapView;
    double latitude, longitude;

    // tmp hashmap for single tree
    HashMap<String, String> tree = new HashMap<String, String>();

    String jsonStr = null;

    /// URL to get trees JSON
    private static String url = "http://159.203.142.217/trees/?format=json";

    // JSON Node names
    private static final String TAG_TREES = "Trees List";
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

    //define estado e cidade
    String administrative_area_level_1 = "São Paulo";
    String locality = "São José dos Campos";

    String id, point, especie, condicao, endereco;

    // Hashmap for ListView
    ArrayList<HashMap<String, String>> treeList;

    // Session Manager Class
    SessionManager session;

    Marker startMarker;

    AsyncTrees getTrees = new AsyncTrees();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        session = new SessionManager(getApplicationContext());

        mapView = (MapView)findViewById(R.id.mapview);

        treeList = new ArrayList<HashMap<String, String>>();

        Drawable marker=this.getResources().getDrawable(R.drawable.tree_good);
        int markerWidth = marker.getIntrinsicWidth();
        int markerHeight = marker.getIntrinsicHeight();
        marker.setBounds(0, markerHeight, markerWidth, 0);


        //user location
        gps = new GPSTracker(HomeActivity.this);

        if (gps.canGetLocation()) {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();

            /*Toast.makeText(
                    getApplicationContext(),
                    "Minha Localização\nLat: " + latitude + "\nLong: "
                            + longitude, Toast.LENGTH_LONG).show();*/

        } else {
            gps.showSettingsAlert();
        }



        mapView.getController().setZoom(10);
        mapView.getController().setCenter(new GeoPoint(latitude, longitude));



        // Calling async task to get json
        getTrees.execute();


        mapView.getController().setZoom(14);

        mapView.setBuiltInZoomControls(true);
        ScaleBarOverlay myScaleBarOverlay = new ScaleBarOverlay(this);
        mapView.getOverlays().add(myScaleBarOverlay);

    }

    //method to open tree detail
    public void visualizarArvore(View view){

        // Starting single tree activity
        Intent in = new Intent(getApplicationContext(), SingleTree_Activity.class);
        String id  = ((TextView)findViewById(R.id.bubble_description)).getText().toString();
        in.putExtra(TAG_ID, id);
        startActivity(in);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.cadastro) {
            Intent i = new Intent(this, CadastroArvoreActivity.class);
            startActivity(i);

            return true;
        }

        if (id == R.id.logout){
            session.logoutUser();
            Toast.makeText(getApplicationContext(), "Logout realizado!", Toast.LENGTH_LONG).show();

            Intent i = new Intent(HomeActivity.this, LoginActivity.class);
            startActivity(i);
            finish();

            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    /**
     * Async task class to get json by making HTTP call
     * */
    public class AsyncTrees extends AsyncTask<Void, Void, Void> {

        // Creating service handler class instance
        ServiceHandler sh = new ServiceHandler();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
        }

        @Override
        protected Void doInBackground(Void... arg0) {


            // Making a request to url and getting response
            try {
                jsonStr = sh.makeServiceCall(url, ServiceHandler.GET);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

            Log.d("Response: ", "> " + jsonStr);

            if (jsonStr != null) {
                try {
                    JSONArray json = new JSONArray(jsonStr);

                    for(int i=0;i<json.length();i++){

                        JSONObject c = json.getJSONObject(i);

                        id = c.getString(TAG_ID);
                        point = c.getString(TAG_POINT);
                        especie = c.getString(TAG_ESPECIE);
                        if (especie.equals("") || especie.equals(" ")){
                            especie = "Espécie não informada";
                        }
                        condicao = c.getString(TAG_CONDICAO);

                        String neighborhood = ", " + c.getString(TAG_NEIGHBOORHOOD);

                        if (c.getString(TAG_NEIGHBOORHOOD).equals(" ") || c.getString(TAG_NEIGHBOORHOOD).equals("")){
                            neighborhood = "";
                        }

                        String route = ", " + c.getString(TAG_ROUTE);
                        if (c.getString(TAG_ROUTE).equals(" ") || c.getString(TAG_ROUTE).equals("")){
                            route = "";
                        }

                        String numero = ", " + c.getString(TAG_NUMERO);
                        if (c.getString(TAG_NUMERO).equals(" ") || c.getString(TAG_NUMERO).equals("")){
                            numero = "";
                        }

                        endereco = administrative_area_level_1 + ", " + locality + neighborhood +
                                route + numero;

                        String lat = point.substring(7, 17);
                        String lon = point.substring(28, 38);

                        // adding each child node to HashMap key => value
                        tree.put(TAG_ID, id);
                        tree.put(TAG_LAT, lat);
                        tree.put(TAG_LON, lon);
                        tree.put(TAG_CONDICAO, condicao);
                        tree.put(TAG_ADDRESS, endereco);
                        tree.put(TAG_ESPECIE, especie);


                        // adding contact to tree list
                        treeList.add(tree);

                        if (tree.get(TAG_CONDICAO).contains("Boa")) {

                            GeoPoint startPoint = new GeoPoint(Float.parseFloat(tree.get(TAG_LAT)), Float.parseFloat(tree.get(TAG_LON)));

                            startMarker = new Marker(mapView);
                            startMarker.setPosition(startPoint);
                            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                            adiciona(startMarker);

                            startMarker.setIcon(getResources().getDrawable(R.drawable.tree_good));
                            startMarker.setTitle("Espécie: " + tree.get(TAG_ESPECIE));
                            startMarker.setSubDescription("Endereço: " + tree.get(TAG_ADDRESS));
                            startMarker.setSnippet(tree.get(TAG_ID));

                        }

                        if (tree.get(TAG_CONDICAO).contains("Regular")) {

                            GeoPoint startPoint = new GeoPoint(Float.parseFloat(tree.get(TAG_LAT)), Float.parseFloat(tree.get(TAG_LON)));

                            startMarker = new Marker(mapView);
                            startMarker.setPosition(startPoint);
                            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                            adiciona(startMarker);

                            startMarker.setIcon(getResources().getDrawable(R.drawable.tree_regular));
                            startMarker.setTitle("Espécie: " + tree.get(TAG_ESPECIE));
                            startMarker.setSubDescription("Endereço: " + tree.get(TAG_ADDRESS));
                            startMarker.setSnippet(tree.get(TAG_ID));


                        }

                        if (tree.get(TAG_CONDICAO).contains("Ruim")) {

                            GeoPoint startPoint = new GeoPoint(Float.parseFloat(tree.get(TAG_LAT)), Float.parseFloat(tree.get(TAG_LON)));

                            startMarker = new Marker(mapView);
                            startMarker.setPosition(startPoint);
                            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                            adiciona(startMarker);

                            startMarker.setIcon(getResources().getDrawable(R.drawable.tree_bad));
                            startMarker.setTitle("Espécie: " + tree.get(TAG_ESPECIE));
                            startMarker.setSubDescription("Endereço: " + tree.get(TAG_ADDRESS));
                            startMarker.setSnippet(tree.get(TAG_ID));
                        }

                        if (tree.get(TAG_CONDICAO).contains("Caída")) {

                            GeoPoint startPoint = new GeoPoint(Float.parseFloat(tree.get(TAG_LAT)), Float.parseFloat(tree.get(TAG_LON)));

                            startMarker = new Marker(mapView);
                            startMarker.setPosition(startPoint);
                            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                            adiciona(startMarker);

                            startMarker.setIcon(getResources().getDrawable(R.drawable.down));
                            startMarker.setTitle("Espécie: " + tree.get(TAG_ESPECIE));
                            startMarker.setSubDescription("Endereço: " + tree.get(TAG_ADDRESS));
                            startMarker.setSnippet(tree.get(TAG_ID));

                        }
                    }

                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("ServiceHandler", "Couldn't get any data from the url");
            }

            return null;
        }

        protected void adiciona(Marker startmarker){
            mapView.getOverlays().add(startmarker);

        }
        @Override
        protected void onPostExecute(Void result){
            super.onPostExecute(result);


        }

    }
}
