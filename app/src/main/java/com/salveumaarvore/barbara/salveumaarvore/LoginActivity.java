package com.salveumaarvore.barbara.salveumaarvore;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends Activity implements View.OnClickListener {

    String responseServer;
    String userName, password;

    Button btnSignIn;

    // Session Manager Class
    SessionManager session;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        session = new SessionManager(getApplicationContext());
        Toast.makeText(getApplicationContext(), "User Login Status: " + session.isLoggedIn(), Toast.LENGTH_LONG).show();


        boolean logado = session.isLoggedIn(); //Pega do prefs se já está criado
        if(logado){
            Intent i = new Intent(LoginActivity.this, HomeActivity.class);
            startActivity(i);
            finish();
        }


        btnSignIn=(Button)findViewById(R.id.buttonSignIn);


    }

    public void acessar(View V) throws IOException {
        final EditText editTextUserName=(EditText)findViewById(R.id.editTextUserNameToLogin);
        final  EditText editTextPassword=(EditText)findViewById(R.id.editTextPasswordToLogin);

        userName = editTextUserName.getText().toString();
        password = editTextPassword.getText().toString();

        AsyncT asyncT = new AsyncT();
        asyncT.execute();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu_home; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);

        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {

    }

    /* Inner class to get response */
    class AsyncT extends AsyncTask<Void, Void, Void> {
        @Override
        protected Void doInBackground(Void... voids) {

            ServiceHandler sh = new ServiceHandler();

            try {

                List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
                postParameters.add(new BasicNameValuePair("username", userName));
                postParameters.add(new BasicNameValuePair("password", password));

                responseServer = sh.makeServiceCall("http://10.0.2.2:8000/rest-auth/loggin/", ServiceHandler.POST, postParameters);

                //Log.e("response", "response -----" + responseServer);

                if (responseServer.contains("id")){

                    JSONObject jsonObj  = new JSONObject(responseServer);

                    //Cria sessão para o usuário
                    User user_sistema = new User(jsonObj.getInt("id"), jsonObj.getString("username"), jsonObj.getString("email"));

                    Log.e("id", jsonObj.getString("id"));
                    Log.e("username", jsonObj.getString("username"));
                    Log.e("email", jsonObj.getString("email"));

                    session.createLoginSession(String.valueOf(user_sistema.getId()), user_sistema.getUsername());

                    Intent i = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(i);
                    finish();

                }


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
