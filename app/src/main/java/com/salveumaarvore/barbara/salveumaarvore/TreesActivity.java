package com.salveumaarvore.barbara.salveumaarvore;

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;

public class TreesActivity extends ListActivity {

    private ProgressDialog pDialog;

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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trees);

        treeList = new ArrayList<HashMap<String, String>>();

        ListView lv = getListView();

        // Listview on item click listener
        lv.setOnItemClickListener(new OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                // getting values from selected ListItem
                String name = ((TextView) view.findViewById(R.id.name))
                        .getText().toString();
                String cost = ((TextView) view.findViewById(R.id.email))
                        .getText().toString();
                String description = ((TextView) view.findViewById(R.id.mobile))
                        .getText().toString();

                // Starting single contact activity
                Intent in = new Intent(getApplicationContext(),
                        SingleTree_Activity.class);
                in.putExtra(TAG_ESPECIE, name);
                in.putExtra(TAG_ADDRESS, cost);
                in.putExtra(TAG_CONDICAO, description);
                startActivity(in);
                finish();

            }
        });

        // Calling async task to get json
        new GetContacts().execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu_home; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_trees, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        if (id == R.id.voltar) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Async task class to get json by making HTTP call
     * */
    private class GetContacts extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(TreesActivity.this);
            pDialog.setMessage("Please wait...");
            pDialog.setCancelable(false);
            pDialog.show();

        }

        @Override
        protected Void doInBackground(Void... arg0) {
            // Creating service handler class instance
            ServiceHandler sh = new ServiceHandler();

            // Making a request to url and getting response
            String jsonStr = null;
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

                        // Phone node is JSON Object

                        // tmp hashmap for single contact
                        HashMap<String, String> tree = new HashMap<String, String>();

                        // adding each child node to HashMap key => value
                        tree.put(TAG_CONDICAO, condicao);
                        tree.put(TAG_ADDRESS, address);
                        tree.put(TAG_ESPECIE, especie);


                        // adding contact to contact list
                        treeList.add(tree);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            } else {
                Log.e("ServiceHandler", "Couldn't get any data from the url");
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            super.onPostExecute(result);
            // Dismiss the progress dialog
            if (pDialog.isShowing())
                pDialog.dismiss();
            /**
             * Updating parsed JSON data into ListView
             * */
            ListAdapter adapter = new SimpleAdapter(
                    TreesActivity.this, treeList,
                    R.layout.list_item, new String[] { TAG_ESPECIE, TAG_ADDRESS,
                    TAG_CONDICAO }, new int[] { R.id.name,
                    R.id.email, R.id.mobile });

            setListAdapter(adapter);
        }

    }

}
