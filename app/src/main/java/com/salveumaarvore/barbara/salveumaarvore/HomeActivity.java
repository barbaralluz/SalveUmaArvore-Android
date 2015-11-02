package com.salveumaarvore.barbara.salveumaarvore;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.OverlayItem;
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
    private static String url = "http://10.0.3.2:8000/trees/?format=json";

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

    // Hashmap for ListView
    ArrayList<HashMap<String, String>> treeList;

    // Session Manager Class
    SessionManager session;


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


        //verifica localização do usuário
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
        new GetTrees().execute();

        mapView.getController().setZoom(14);

        mapView.setBuiltInZoomControls(true);
        ScaleBarOverlay myScaleBarOverlay = new ScaleBarOverlay(this);
        mapView.getOverlays().add(myScaleBarOverlay);

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

        if (id == R.id.list_tree) {

            Intent i = new Intent(this, TreesActivity.class);
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
    /**
     * Async task class to get json by making HTTP call
     * */
    public class GetTrees extends AsyncTask<Void, Void, Void> {

        ArrayList<OverlayItem> anotherOverlayItemArray;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();

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

                        String id = c.getString(TAG_ID);
                        String point = c.getString(TAG_POINT);
                        String especie = c.getString(TAG_ESPECIE);
                        if (especie.equals("") || especie.equals(" ")){
                            especie = "Espécie não informada";
                        }
                        String condicao = c.getString(TAG_CONDICAO);

                        String administrative_area_level_1 = c.getString(TAG_ADMINISTRATIVE_AREA_LEVEL_1);
                        String locality = c.getString(TAG_LOCALITY);

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

                        String address = administrative_area_level_1 + ", " + locality + neighborhood +
                                route + numero;

                        String lat = point.substring(7, 17);
                        String lon = point.substring(28, 38);

                        // adding each child node to HashMap key => value
                        tree.put(TAG_LAT, lat);
                        tree.put(TAG_LON, lon);
                        tree.put(TAG_CONDICAO, condicao);
                        tree.put(TAG_ADDRESS, address);
                        tree.put(TAG_ESPECIE, especie);


                        // adding contact to contact list
                        treeList.add(tree);

                        if (tree.get(TAG_CONDICAO).contains("Boa")) {

                            GeoPoint startPoint = new GeoPoint(Float.parseFloat(tree.get(TAG_LAT)), Float.parseFloat(tree.get(TAG_LON)));

                            Marker startMarker = new Marker(mapView);
                            startMarker.setPosition(startPoint);
                            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                            adiciona(startMarker);

                            startMarker.setIcon(getResources().getDrawable(R.drawable.tree_good));
                            startMarker.setTitle("Espécie: " + tree.get(TAG_ESPECIE));
                            startMarker.setSubDescription("Endereço: " + tree.get(TAG_ADDRESS));

                        }

                        if (tree.get(TAG_CONDICAO).contains("Regular")) {

                            GeoPoint startPoint = new GeoPoint(Float.parseFloat(tree.get(TAG_LAT)), Float.parseFloat(tree.get(TAG_LON)));

                            Marker startMarker = new Marker(mapView);
                            startMarker.setPosition(startPoint);
                            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                            adiciona(startMarker);

                            startMarker.setIcon(getResources().getDrawable(R.drawable.tree_regular));
                            startMarker.setTitle("Espécie: " + tree.get(TAG_ESPECIE));
                            startMarker.setSubDescription("Endereço: " + tree.get(TAG_ADDRESS));

                        }

                        if (tree.get(TAG_CONDICAO).contains("Ruim")) {

                            GeoPoint startPoint = new GeoPoint(Float.parseFloat(tree.get(TAG_LAT)), Float.parseFloat(tree.get(TAG_LON)));

                            Marker startMarker = new Marker(mapView);
                            startMarker.setPosition(startPoint);
                            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                            adiciona(startMarker);

                            startMarker.setIcon(getResources().getDrawable(R.drawable.tree_bad));
                            startMarker.setTitle("Espécie: " + tree.get(TAG_ESPECIE));
                            startMarker.setSubDescription("Endereço: " + tree.get(TAG_ADDRESS));

                        }

                        if (tree.get(TAG_CONDICAO).contains("Caída")) {

                            GeoPoint startPoint = new GeoPoint(Float.parseFloat(tree.get(TAG_LAT)), Float.parseFloat(tree.get(TAG_LON)));

                            Marker startMarker = new Marker(mapView);
                            startMarker.setPosition(startPoint);
                            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                            adiciona(startMarker);

                            startMarker.setIcon(getResources().getDrawable(R.drawable.down));
                            startMarker.setTitle("Espécie: " + tree.get(TAG_ESPECIE));
                            startMarker.setSubDescription("Endereço: " + tree.get(TAG_ADDRESS));


                        }

                        Log.i("Condição", tree.get(TAG_ADDRESS));
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
