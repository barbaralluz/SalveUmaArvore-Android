package com.salveumaarvore.barbara.salveumaarvore;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
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

public class LoginActivity extends Activity{

    String responseServer;
    String userName, password;
    String url = "http://159.203.142.217/rest-auth/loggin/";
    Button btnSignIn;

    // Session Manager Class
    SessionManager session;

    AsyncTLogin asyncT = new AsyncTLogin();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);


        session = new SessionManager(getApplicationContext());

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

        asyncT.execute();
    }


    /* Inner class to get response */
    class AsyncTLogin extends AsyncTask<Void, Void, Void> {

        ServiceHandler sh = new ServiceHandler();

        User user_sistema;

        @Override
        protected Void doInBackground(Void... voids) {



            try {

                List<NameValuePair> postParameters = new ArrayList<NameValuePair>();
                postParameters.add(new BasicNameValuePair("username", userName));
                postParameters.add(new BasicNameValuePair("password", password));

                responseServer = sh.makeServiceCall(url, ServiceHandler.POST, postParameters);

                Log.e("response", "response -----" + responseServer);

                if (responseServer.contains("id")){

                    JSONObject jsonObj  = new JSONObject(responseServer);

                    //Cria sessão para o usuário
                    user_sistema = new User(jsonObj.getInt("id"), jsonObj.getString("username"), jsonObj.getString("email"));

                    /*Log.e("id", jsonObj.getString("id"));
                    Log.e("username", jsonObj.getString("username"));
                    Log.e("email", jsonObj.getString("email"));*/

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

            if (responseServer.contains("id")){

                Intent i = new Intent(LoginActivity.this, HomeActivity.class);
                startActivity(i);
                finish();

            } else {

                Toast.makeText(getApplicationContext(), "Usuário inválido !", Toast.LENGTH_LONG).show();
            }

        }
    }


}
