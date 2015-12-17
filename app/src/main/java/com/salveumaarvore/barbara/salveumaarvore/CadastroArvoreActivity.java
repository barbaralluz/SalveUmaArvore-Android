package com.salveumaarvore.barbara.salveumaarvore;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
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
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CadastroArvoreActivity extends Activity {

    //Define ids and name for Estado e Cidade (IBGE)
    String administrative_area_level_1_id = "35";
    String administrative_area_level_1 = "São Paulo";
    String locality_id = "3549904";
    String locality = "São José dos Campos";

    String url = "http://159.203.142.217/trees/";

    EditText editTextReferencia,editTextEspecie, editTextDescricao, editTextNumero;
    Spinner cond_geral, cond_luz, cond_raiz, manutencao, altura;
    Button btnCreateTree, btnCancel;

    String country,neighborhood, route, numero, postal_code, point_of_interest;
    String lat, lon, geometry, especie;
    String condicao_arvore, altur, condicao_raiz, condicao_luz, condicao_man, descricao, foto1, foto2, foto3;
    String usuario_id;
    String path;
    String ba1;

    String bairro, rua, cep;


    Uri fileUri; // file url to store image

    ImageView imgPreview;
    Button btnCapturePicture;

    // Response
    String responseServer;

    GPSTracker gps;
    double latitude, longitude;

    // Activity request codes
    static final int CAMERA_CAPTURE_IMAGE_REQUEST_CODE = 400;
    static final int MEDIA_TYPE_IMAGE = 1;

    // directory name to store captured images and videos
    static final String IMAGE_DIRECTORY_NAME = "SalveUmaArvore_IMGS";

    // Session Manager Class
    SessionManager session;

    //AsyncTCadastro asyncTcadastro;

    ProgressDialog pDialog;

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


        TextView myAddress = (TextView)findViewById(R.id.myaddress);

        gps = new GPSTracker(CadastroArvoreActivity.this);

        if (gps.canGetLocation()) {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();

            /*Toast.makeText(
                    getApplicationContext(),
                    "Sua localização\nLat: " + latitude + "\nLong: "
                            + longitude, Toast.LENGTH_LONG).show();
*/
            Geocoder geocoder = new Geocoder(this, Locale.ENGLISH);

            try {
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

                if(addresses != null) {

                    Address returnedAddress = addresses.get(0);
                    rua = addresses.get(0).getThoroughfare();
                    cep = addresses.get(0).getPostalCode();
                    bairro = addresses.get(0).getSubLocality();

                    StringBuilder strReturnedAddress = new StringBuilder("Localização da Árvore:\n");
                    for(int i=0; i<returnedAddress.getMaxAddressLineIndex(); i++) {
                        strReturnedAddress.append(returnedAddress.getAddressLine(i)).append("\n");
                    }
                    myAddress.setText("Localização da Árvore:\n" + administrative_area_level_1 + ", " + locality + "\n" +
                            bairro + ", " + rua + "\n" +
                            cep);
                }
                else{
                    //myAddress.setText("No Address returned!");
                }
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        } else {
            gps.showSettingsAlert();
        }

        // Get Refferences of Views
        editTextNumero=(EditText)findViewById(R.id.editTextNumero);
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

                neighborhood = bairro;
                route = rua;
                postal_code = cep;

                numero = editTextNumero.getText().toString();
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

                //for photo
                if (fileUri != null){
                    path = fileUri.getPath();
                } else {
                    path = null;
                }

                Log.e("path", "----------------" + path);

                // Image
                Bitmap bm = BitmapFactory.decodeFile(path);
                ByteArrayOutputStream bao = new ByteArrayOutputStream();
                bm.compress(Bitmap.CompressFormat.JPEG, 25, bao);
                byte[] bytes1 = bao.toByteArray();
                ba1 = Base64.encodeToString(bytes1, Base64.DEFAULT);


                Log.e("base64", "-----" + ba1);

                foto1 = ba1;
                foto2 = " ";
                foto3 = " ";

                usuario_id = session.getUserDetails().get("id");

                Log.i("id ", usuario_id);

                AsyncTCadastro asyncTcadastro = new AsyncTCadastro();
                asyncTcadastro.execute();

            }
        });

        btnCancel = (Button) findViewById(R.id.buttonCancelTree);
        btnCancel.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // cancelar
                Toast.makeText(getApplicationContext(), "Cadastro de árvore cancelado!", Toast.LENGTH_SHORT).show();
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
                        "Captura de imagem cancelada!", Toast.LENGTH_SHORT)
                        .show();
            } else {
                // failed to capture image
                Toast.makeText(getApplicationContext(),
                        "Falha ao tentar capturar imagem!", Toast.LENGTH_SHORT)
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
                Log.d(IMAGE_DIRECTORY_NAME, "Falha ao criar diretório "
                        + IMAGE_DIRECTORY_NAME);
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
    public class AsyncTCadastro extends AsyncTask<Void, Void, Void> {
        ServiceHandler sh = new ServiceHandler();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(CadastroArvoreActivity.this);
            pDialog.setMessage("Salvando ...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {


            try {

                //usuario_id = session.getUserDetails().get("id");

                Log.i("id ", usuario_id);

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

            if (pDialog.isShowing())
                pDialog.dismiss();

            if (responseServer.contains("id")){
                Toast.makeText(getApplicationContext(), "Árvore cadastrada com sucesso! ", Toast.LENGTH_LONG).show();
                finish();

            } else {
                Toast.makeText(getApplicationContext(), "Dados inválidos!", Toast.LENGTH_LONG).show();
            }


        }
    }

}


