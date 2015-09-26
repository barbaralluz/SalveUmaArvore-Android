package com.salveumaarvore.barbara.salveumaarvore;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ScaleBarOverlay;

import java.io.File;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CadastroArvoreActivity extends Activity {

    EditText editTextReferencia,editTextEspecie, editTextDescricao, editTextLocalizacao;
    Spinner cond_geral, cond_luz, cond_raiz, manutencao, altura;
    Button btnCreateTree;

    TreeDataBaseAdapter treeDataBaseAdapter;

    MyItemizedOverlay myItemizedOverlay = null;
    GPSTracker gps;
    private MapView mapView;
    double latitude, longitude;
    String path;

    // Activity request codes
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 400;
    public static final int MEDIA_TYPE_IMAGE = 1;

    // directory name to store captured images and videos
    private static final String IMAGE_DIRECTORY_NAME = "SalveUmaArvore_IMGS";

    private Uri fileUri; // file url to store image/video

    private ImageView imgPreview;
    private Button btnCapturePicture;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_arvore);

        imgPreview = (ImageView) findViewById(R.id.imgPreview);
        btnCapturePicture = (Button) findViewById(R.id.btnCapturePicture);

        /**
         * Capture image button click event
         * */
        btnCapturePicture.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // capture picture
                captureImage();
            }
        });

        mapView = (MapView)findViewById(R.id.mapview);

        //BoundingBoxE6 bBox = new BoundingBoxE6(-73.817, -33.733, -28.850, 16.800);
        //map.setScrollableAreaLimit(bBox);

        mapView.getController().setZoom(3);
        mapView.setBuiltInZoomControls(true);
        //mapView.getController().setCenter(new GeoPoint(-12.21119546574942, -50.44919374999993));

        ScaleBarOverlay myScaleBarOverlay = new ScaleBarOverlay(this);
        mapView.getOverlays().add(myScaleBarOverlay);

        Drawable marker=this.getResources().getDrawable(android.R.drawable.star_big_on);
        int markerWidth = marker.getIntrinsicWidth();
        int markerHeight = marker.getIntrinsicHeight();
        marker.setBounds(0, markerHeight, markerWidth, 0);

        ResourceProxy resourceProxy = new DefaultResourceProxyImpl(getApplicationContext());
        myItemizedOverlay = new MyItemizedOverlay(marker, resourceProxy);
        mapView.getOverlays().add(myItemizedOverlay);

        gps = new GPSTracker(CadastroArvoreActivity.this);

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

        // get Instance  of Database Adapter
        treeDataBaseAdapter=new TreeDataBaseAdapter(this);
        try {
            treeDataBaseAdapter=treeDataBaseAdapter.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        // Get Refferences of Views
        editTextReferencia=(EditText)findViewById(R.id.editTextPReferencia);
        editTextEspecie=(EditText)findViewById(R.id.editTextEspecie);
        editTextDescricao=(EditText)findViewById(R.id.editTextDescricao);


        cond_geral=(Spinner)findViewById(R.id.spinnerCondicao);
        cond_luz=(Spinner)findViewById(R.id.spinnerLuz);
        cond_raiz=(Spinner)findViewById(R.id.spinnerRaiz);
        manutencao=(Spinner)findViewById(R.id.spinnerMan);
        altura=(Spinner)findViewById(R.id.spinnerAltura);

        btnCreateTree=(Button)findViewById(R.id.buttonSaveTree);
        btnCreateTree.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub

                String lat = String.valueOf(latitude);
                String lon = String.valueOf(longitude);

                String referencia = editTextReferencia.getText().toString();
                String especie = editTextEspecie.getText().toString();


                String geral = cond_geral.getSelectedItem().toString();
                String alt = altura.getSelectedItem().toString();
                String raiz = cond_raiz.getSelectedItem().toString();
                String luz = cond_luz.getSelectedItem().toString();
                String man = manutencao.getSelectedItem().toString();
                String descricao = editTextDescricao.getText().toString();

                if (fileUri != null){
                    path = fileUri.getPath();
                } else {
                    path = " ";
                }


                treeDataBaseAdapter.insertEntry(lat, lon, referencia, especie, geral, alt, raiz, luz, man, descricao, path);
                Toast.makeText(getApplicationContext(), "Árvore cadastrada com sucesso! ", Toast.LENGTH_LONG).show();

                Intent i = new Intent(CadastroArvoreActivity.this, HomeActivity.class);
                startActivity(i);

            }
        });
    }

    @Override
    protected void onDestroy() {
        // TODO Auto-generated method stub
        super.onDestroy();

        treeDataBaseAdapter.close();
    }



    /*
 * Capturing Camera Image will lauch camera app requrest image capture
 */
    private void captureImage() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        fileUri = getOutputMediaFileUri(MEDIA_TYPE_IMAGE);

        intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri);

        // start the image capture Intent
        startActivityForResult(intent, CAMERA_CAPTURE_IMAGE_REQUEST_CODE);
    }


    /**
     * Receiving activity result method will be called after closing the camera
     * */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // if the result is capturing Image
        if (requestCode == CAMERA_CAPTURE_IMAGE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // successfully captured the image
                // display it in image view
                previewCapturedImage();
            } else if (resultCode == RESULT_CANCELED) {
                // user cancelled Image capture
                Toast.makeText(getApplicationContext(),
                        "User cancelled image capture", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(),
                        "Sorry! Failed to capture image", Toast.LENGTH_SHORT)
                        .show();
            }
        }

    }

    /*
     * Display image from a path to ImageView
     */
    private void previewCapturedImage() {
        try {
            // hide video preview
            imgPreview.setVisibility(View.VISIBLE);

            // bimatp factory
            BitmapFactory.Options options = new BitmapFactory.Options();

            // downsizing image as it throws OutOfMemory Exception for larger
            // images
            options.inSampleSize = 8;

            final Bitmap bitmap = BitmapFactory.decodeFile(fileUri.getPath(),
                    options);

            imgPreview.setImageBitmap(bitmap);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /**
     * Here we store the file url as it will be null after returning from camera
     * app
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        // save file url in bundle as it will be null on scren orientation
        // changes
        outState.putParcelable("file_uri", fileUri);
    }

    /*
     * Here we restore the fileUri again
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        // get the file url
        fileUri = savedInstanceState.getParcelable("file_uri");
    }

    /**
     * Creating file uri to store image/video
     */
    public Uri getOutputMediaFileUri(int type) {
        return Uri.fromFile(getOutputMediaFile(type));
    }

    /*
     * returning image / video
     */
    private static File getOutputMediaFile(int type) {

        // External sdcard location
        File mediaStorageDir = new File(
                Environment
                        .getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES),
                IMAGE_DIRECTORY_NAME);

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.d(IMAGE_DIRECTORY_NAME, "Oops! Failed create "
                        + IMAGE_DIRECTORY_NAME + " directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss",
                Locale.getDefault()).format(new Date());
        File mediaFile;
        if (type == MEDIA_TYPE_IMAGE) {
            mediaFile = new File(mediaStorageDir.getPath() + File.separator
                    + "IMG_" + timeStamp + ".jpg");
        } else {
            return null;
        }

        return mediaFile;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu_home; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_cadastro, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();


        if (id == R.id.logout) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
