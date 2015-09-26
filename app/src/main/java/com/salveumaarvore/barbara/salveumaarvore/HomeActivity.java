package com.salveumaarvore.barbara.salveumaarvore;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONException;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ScaleBarOverlay;

import java.io.FileNotFoundException;
import java.sql.SQLException;

public class HomeActivity extends Activity {

    Button btnShowLocation;
    MyItemizedOverlay myItemizedOverlay = null;
    GPSTracker gps;
    private MapView mapView;
    double latitude, longitude;

    TreeDataBaseAdapter treeDataBaseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        mapView = (MapView)findViewById(R.id.mapview);

        //BoundingBoxE6 bBox = new BoundingBoxE6(-73.817, -33.733, -28.850, 16.800);
        //map.setScrollableAreaLimit(bBox);

        mapView.getController().setZoom(3);
        mapView.setBuiltInZoomControls(true);
        mapView.getController().setCenter(new GeoPoint(-12.21119546574942, -50.44919374999993));

        ScaleBarOverlay myScaleBarOverlay = new ScaleBarOverlay(this);
        mapView.getOverlays().add(myScaleBarOverlay);

        Drawable marker=this.getResources().getDrawable(android.R.drawable.star_big_on);
        int markerWidth = marker.getIntrinsicWidth();
        int markerHeight = marker.getIntrinsicHeight();
        marker.setBounds(0, markerHeight, markerWidth, 0);

        ResourceProxy resourceProxy = new DefaultResourceProxyImpl(getApplicationContext());
        myItemizedOverlay = new MyItemizedOverlay(marker, resourceProxy);
        mapView.getOverlays().add(myItemizedOverlay);

        gps = new GPSTracker(HomeActivity.this);

        if (gps.canGetLocation()) {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();

            Toast.makeText(
                    getApplicationContext(),
                    "Minha Localização\nLat: " + latitude + "\nLong: "
                            + longitude, Toast.LENGTH_LONG).show();

            myItemizedOverlay.addItem(new GeoPoint(latitude, longitude), "Eu", "Eu");
            mapView.getController().setZoom(10);
            mapView.getController().setCenter(new GeoPoint(latitude, longitude));
        } else {
            gps.showSettingsAlert();
        }
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

        //noinspection SimplifiableIfStatement
        if (id == R.id.cadastro) {
            Intent i = new Intent(this, CadastroArvoreActivity.class);
            startActivity(i);
            return true;
        }

        if (id == R.id.sincronizar) {

            // get Instance  of Database Adapter
            treeDataBaseAdapter=new TreeDataBaseAdapter(this);
            try {
                treeDataBaseAdapter=treeDataBaseAdapter.open();

                treeDataBaseAdapter.getAllDataAndGenerateJSON();

                treeDataBaseAdapter.close();

                Toast.makeText(getApplicationContext(), "Foi ...", Toast.LENGTH_LONG).show();

            } catch (SQLException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return true;
        }

        if (id == R.id.logout){





            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
