package com.salveumaarvore.barbara.salveumaarvore;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.InputStream;

public class SingleTree_Activity extends Activity {

    //Define estado e cidade
    String administrative_area_level_1 = "São Paulo";
    String locality = "São José dos Campos";

    String url = "http://159.203.142.217/trees";

    // JSON node keys
    private static final String TAG_ID = "id";
    private static final String TAG_POINT = "geometry";
    private static final String TAG_LAT = "latitude";
    private static final String TAG_LON = "longitude";
    private static final String TAG_ESPECIE = "especie";
    private static final String TAG_ALTURA = "altura";

    private static final String TAG_ADMINISTRATIVE_AREA_LEVEL_1 = "administrative_area_level_1";
    private static final String TAG_LOCALITY = "locality";
    private static final String TAG_NEIGHBOORHOOD = "neighborhood";
    private static final String TAG_ROUTE = "route";
    private static final String TAG_NUMERO = "numero";
    private static final String TAG_ADDRESS = "endereco";
    private static final String TAG_CEP = "postal_code";
    private static final String TAG_REFERENCE = "point_of_interest";

    private static final String TAG_CONDICAO = "condicao_arvore";
    private static final String TAG_CONDICAORAIZ = "condicao_raiz";
    private static final String TAG_CONDICAOLUZ = "condicao_luz";
    private static final String TAG_CONDICAOMAN = "condicao_man";

    private static final String TAG_INFORMACOES = "descricao";

    private static final String TAG_USUARIO = "usuario";

    private static final String TAG_FOTO1 = "foto1";

    AsyncTree getTree = new AsyncTree();

    //for textviews in activity
    TextView lblId, lblEspecie;
    TextView lblEndereco, lblReferencia ;
    TextView lblAltura;
    TextView lblCondicaoGeral, lblCondicaoRaiz, lblCondicaoLuz, lblCondicaoManutencao;
    TextView lblInformacoes;
    TextView lblUsuario;
    ImageView imageView_Foto1;


    String id, especie, address, referencia, altura;
    String condicao, condicao_raiz, condicao_luz, condicao_man;
    String informacoes;
    String usuario;
    String foto1;

    String jsonStr = null;

    ProgressDialog pDialog;

    Bitmap bitmap, scaled;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_tree);

        // getting intent data
        Intent in = getIntent();

        // Get JSON values from previous intent
        id = in.getStringExtra(TAG_ID);

        // Calling async task to get json
        getTree.execute();

        // To display values in screen
        lblId = (TextView) findViewById(R.id.id_label);
        lblEspecie = (TextView) findViewById(R.id.especie_label);
        lblEndereco = (TextView) findViewById(R.id.address_label);
        lblReferencia = (TextView) findViewById(R.id.reference_label);
        lblAltura = (TextView) findViewById(R.id.altura_label);
        lblCondicaoGeral = (TextView) findViewById(R.id.condicaoGeral_label);
        lblCondicaoRaiz = (TextView) findViewById(R.id.condicaoRaiz_label);
        lblCondicaoLuz = (TextView) findViewById(R.id.condicaoLuz_label);
        lblCondicaoManutencao = (TextView) findViewById(R.id.condicaoManutencao_label);
        lblInformacoes = (TextView) findViewById(R.id.informacoes_label);
        lblUsuario = (TextView) findViewById(R.id.usuario_label);

        imageView_Foto1 = (ImageView) findViewById(R.id.imageView_foto1);

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

        if (id == R.id.voltar) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Async task class to get json by making HTTP call
     * */
    public class AsyncTree extends AsyncTask<Void, Void, Void> {

        // Creating service handler class instance
        ServiceHandler sh = new ServiceHandler();

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // Showing progress dialog
            pDialog = new ProgressDialog(SingleTree_Activity.this);
            pDialog.setMessage("Carregando...");
            pDialog.setCancelable(false);
            pDialog.show();
        }

        @Override
        protected Void doInBackground(Void... arg0) {

            /// URL to get tree JSON
            String url_tree = url + "/" + id;

            // Making a request to url and getting response
            try {
                jsonStr = sh.makeServiceCall(url_tree, ServiceHandler.GET);
                Log.d("Response: ", "> " + jsonStr);

                if (jsonStr.contains("id")) {

                    JSONObject c = new JSONObject(jsonStr);

                    ////Define espécie
                    especie = c.getString(TAG_ESPECIE);
                    if (especie.equals("") || especie.equals(" ")){
                        especie = "Espécie não informada";
                    }

                    //Define endereço
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

                    String postal_code = ", " + c.getString(TAG_CEP);
                    if (c.getString(TAG_CEP).equals(" ") || c.getString(TAG_CEP).equals("")){
                        postal_code = "";
                    }

                    address = administrative_area_level_1 + ", " + locality + neighborhood +
                            route + numero + postal_code;

                    //Define ponto de referencia
                    referencia = c.getString(TAG_REFERENCE);
                    if (referencia.equals("") || referencia.equals(" ") || referencia.equals("null") || referencia.equals(null) ){
                        referencia = "Sem ponto de referência";
                    }

                    //Define altura
                    altura = c.getString(TAG_ALTURA);

                    //Define condição da árvore
                    condicao = c.getString(TAG_CONDICAO);

                    //Define condição das raízes
                    condicao_raiz = c.getString(TAG_CONDICAORAIZ);

                    //Define condição_luz
                    condicao_luz = c.getString(TAG_CONDICAOLUZ);

                    //Define necessidade de manutenção
                    condicao_man = c.getString(TAG_CONDICAOMAN);

                    //Define descrição da árvore
                    informacoes = c.getString(TAG_INFORMACOES);
                    if (informacoes.equals("") || informacoes.equals(" ")){
                        informacoes = "Não foram adicionados detalhes sobre a árvore ";
                    }

                    //Define usuário que cadastrou árvore
                    usuario = c.getString(TAG_USUARIO);

                    //Define foto da árvore
                    foto1 = "http://159.203.142.217" + c.getString(TAG_FOTO1);



                    InputStream in = new java.net.URL(foto1).openStream();
                    bitmap = BitmapFactory.decodeStream(in);

                    int h = 320; // height in pixels
                    int w = 260; // width in pixels
                    scaled = Bitmap.createScaledBitmap(bitmap, h, w, true);

                } else {
                    Log.e("ServiceHandler", "Couldn't get any data from the url");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            return null;
        }


        @Override
        protected void onPostExecute(Void result){
            super.onPostExecute(result);

            if (pDialog.isShowing())
                pDialog.dismiss();

            lblId.setText("ÁRVORE " + id);
            lblEspecie.setText("Espécie: " + especie);
            lblEndereco.setText("Endereço: " + address);
            lblReferencia.setText("Ponto de Referência: " + referencia);
            lblAltura.setText("Altura: " + altura);
            lblCondicaoGeral.setText("Condição da Árvore: " + condicao);
            lblCondicaoRaiz.setText("Condição das Raízes: " + condicao_raiz);
            lblCondicaoLuz.setText("Proximidade com Rede Elétrica: " + condicao_luz);
            lblCondicaoManutencao.setText("Necessidade de Manutenção: " + condicao_man);
            lblInformacoes.setText("Informações: " + informacoes);
            imageView_Foto1.setImageBitmap(scaled);


        }

    }
}
