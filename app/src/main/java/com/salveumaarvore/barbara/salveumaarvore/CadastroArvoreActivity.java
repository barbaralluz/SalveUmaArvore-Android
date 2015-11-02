package com.salveumaarvore.barbara.salveumaarvore;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.osmdroid.bonuspack.overlays.Marker;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ScaleBarOverlay;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CadastroArvoreActivity extends Activity {

    EditText editTextReferencia,editTextEspecie, editTextDescricao, editTextNeighborhood, editTextRoute, editTextNumero, editTextPostalCode;
    Spinner cond_geral, cond_luz, cond_raiz, manutencao, altura;
    Button btnCreateTree;

    String country,neighborhood, route, numero, postal_code, point_of_interest, lat, lon, geometry, condicao_arvore;
    String especie, altur, condicao_raiz, condicao_luz, condicao_man, descricao, foto1, foto2, foto3;
    String administrative_area_level_1_id,  locality_id, usuario_id;
    String path;
    String ba1;

    String url = "http://10.0.3.2:8000/trees/";
    private Uri fileUri; // file url to store image

    private ImageView imgPreview;
    private Button btnCapturePicture;

    // Response
    String responseServer;

    GPSTracker gps;
    private MapView mapView;
    double latitude, longitude;

    // Activity request codes
    private static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 400;
    public static final int MEDIA_TYPE_IMAGE = 1;

    // directory name to store captured images and videos
    private static final String IMAGE_DIRECTORY_NAME = "SalveUmaArvore_IMGS";

    // Session Manager Class
    SessionManager session;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cadastro_arvore);

        session = new SessionManager(getApplicationContext());

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

        mapView.getController().setZoom(3);
        mapView.setBuiltInZoomControls(true);
        ScaleBarOverlay myScaleBarOverlay = new ScaleBarOverlay(this);
        mapView.getOverlays().add(myScaleBarOverlay);

        gps = new GPSTracker(CadastroArvoreActivity.this);

        if (gps.canGetLocation()) {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();

            Toast.makeText(
                    getApplicationContext(),
                    "Sua localização\nLat: " + latitude + "\nLong: "
                            + longitude, Toast.LENGTH_LONG).show();

            GeoPoint startPoint = new GeoPoint(latitude, longitude);

            Marker startMarker = new Marker(mapView);
            startMarker.setPosition(startPoint);
            startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
            mapView.getOverlays().add(startMarker);
            startMarker.setIcon(getResources().getDrawable(R.drawable.male_21));

            mapView.getController().setZoom(10);
            mapView.getController().setCenter(new GeoPoint(latitude, longitude));
        } else {
            gps.showSettingsAlert();
        }

        // Get Refferences of Views
        editTextNeighborhood=(EditText)findViewById(R.id.editTextNeighborhood);
        editTextRoute=(EditText)findViewById(R.id.editTextRoute);
        editTextNumero=(EditText)findViewById(R.id.editTextNumero);
        editTextPostalCode=(EditText)findViewById(R.id.editTextPostalCode);
        editTextReferencia=(EditText)findViewById(R.id.editTextPReferencia);
        cond_geral=(Spinner)findViewById(R.id.spinnerCondicao);
        editTextEspecie=(EditText)findViewById(R.id.editTextEspecie);
        editTextDescricao=(EditText)findViewById(R.id.editTextDescricao);

        cond_luz=(Spinner)findViewById(R.id.spinnerLuz);
        cond_raiz=(Spinner)findViewById(R.id.spinnerRaiz);
        manutencao=(Spinner)findViewById(R.id.spinnerMan);
        altura=(Spinner)findViewById(R.id.spinnerAltura);

        btnCreateTree=(Button)findViewById(R.id.buttonSaveTree);
        btnCreateTree.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                // TODO Auto-generated method stub

                country = "Brazil";
                administrative_area_level_1_id = "35";
                locality_id = "3549904";
                neighborhood = editTextNeighborhood.getText().toString();
                route = editTextRoute.getText().toString();
                numero = editTextNumero.getText().toString();
                postal_code = editTextPostalCode.getText().toString();;
                point_of_interest = editTextReferencia.getText().toString();

                lat = String.valueOf(latitude);
                lon = String.valueOf(longitude);
                geometry = "POINT(" +  lat + " " + lon + ")";

                condicao_arvore = cond_geral.getSelectedItem().toString();
                especie = editTextEspecie.getText().toString();
                altur = altura.getSelectedItem().toString();
                condicao_raiz = cond_raiz.getSelectedItem().toString();
                condicao_luz = cond_luz.getSelectedItem().toString();
                condicao_man = manutencao.getSelectedItem().toString();
                descricao = editTextDescricao.getText().toString();
                String data_cadastro = getDateTime();

                //para a foto
                if (fileUri != null){
                    path = fileUri.getPath();
                } else {
                    path = null;
                }

                Log.e("path", "----------------" + path);

                // Image
                Bitmap bm = BitmapFactory.decodeFile(path);
                ByteArrayOutputStream bao = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG, 90, bao);
                byte[] ba = bao.toByteArray();
                ba1 = Base64.encodeToString(ba, 1);

                Log.e("base64", "-----" + ba1);

                foto1 = ba1;
                foto2 = " ";
                foto3 = " ";

                usuario_id = session.getUserDetails().get("id");

                Log.e("id ", usuario_id);

                AsyncT asyncT = new AsyncT();
                asyncT.execute();


                Toast.makeText(getApplicationContext(), "Árvore cadastrada com sucesso! ", Toast.LENGTH_LONG).show();

                Intent i = new Intent(CadastroArvoreActivity.this, LoginActivity.class);
                startActivity(i);
                finish();

            }
        });
    }

    private String getDateTime() {
        DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        Date date = new Date();
        return dateFormat.format(date);
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


        if (id == R.id.voltar) {
            finish();
            return true;
        }




        return super.onOptionsItemSelected(item);
    }

    /* Inner class to get response */
    class AsyncT extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... params) {
            ServiceHandler sh = new ServiceHandler();

            try {

                List<NameValuePair> postParameters = new ArrayList<NameValuePair>();

                postParameters.add(new BasicNameValuePair("geometry", geometry));
                postParameters.add(new BasicNameValuePair("country", country));
                postParameters.add(new BasicNameValuePair("administrative_area_level_1", administrative_area_level_1_id));
                postParameters.add(new BasicNameValuePair("locality", locality_id));
                postParameters.add(new BasicNameValuePair("neighborhood", neighborhood));
                postParameters.add(new BasicNameValuePair("route", route));
                postParameters.add(new BasicNameValuePair("numero",numero));
                postParameters.add(new BasicNameValuePair("postal_code", postal_code));
                postParameters.add(new BasicNameValuePair("condicao_arvore", condicao_arvore));
                postParameters.add(new BasicNameValuePair("especie", especie));
                postParameters.add(new BasicNameValuePair("altura", altur));
                postParameters.add(new BasicNameValuePair("condicao_raiz", condicao_raiz));
                postParameters.add(new BasicNameValuePair("condicao_luz", condicao_luz));
                postParameters.add(new BasicNameValuePair("condicao_man", condicao_man));
                postParameters.add(new BasicNameValuePair("descricao", descricao));
                postParameters.add(new BasicNameValuePair("foto1", foto1));
                postParameters.add(new BasicNameValuePair("usuario", usuario_id));

                Log.i("Parameters", postParameters.toString());

                // Execute HTTP Post Request
                responseServer = sh.makeServiceCall(url, ServiceHandler.POST, postParameters);

                Log.e("response", "response -----" + responseServer);

            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
        }
    }

}



