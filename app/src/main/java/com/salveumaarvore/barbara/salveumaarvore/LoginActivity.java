package com.salveumaarvore.barbara.salveumaarvore;

import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;

import org.json.JSONException;
import org.osmdroid.DefaultResourceProxyImpl;
import org.osmdroid.ResourceProxy;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.ScaleBarOverlay;

import java.io.FileNotFoundException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.SQLException;

public class LoginActivity extends Activity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "LoginActivity" ;
    //Login bd
    Button btnSignIn;
    TreeDataBaseAdapter loginDataBaseAdapter;

    //Login Facebook
    public CallbackManager callbackManager;
    private LoginButton fbLoginButton;
    boolean facebook = false;

    //Login Google
    private static final int SIGNED_IN = 0;
    private static final int STATE_SIGNING_IN = 1;
    private static final int STATE_IN_PROGRESS = 2;
    private final int RC_SIGN_IN = 0;

    // Google client to communicate with Google
    GoogleApiClient mGoogleApiClient;

    private boolean mIntentInProgress;
    private boolean signedInUser;
    private ConnectionResult mConnectionResult;
    private SignInButton signinGoogle;
    boolean google = false;

    private TextView username, emailLabel;
    private LinearLayout signinFrame;
    private RelativeLayout mapFrame;

    // Session Manager Class
    SessionManager session;


    Button btnShowLocation;
    MyItemizedOverlay myItemizedOverlay = null;
    GPSTracker gps;
    private MapView mapView;
    double latitude, longitude;

    TreeDataBaseAdapter treeDataBaseAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        FacebookSdk.sdkInitialize(getApplicationContext());
        callbackManager = CallbackManager.Factory.create();
        getFbKeyHash("com.salveumaarvore.barbara.salveumaarvore");

        setContentView(R.layout.activity_login);


        session = new SessionManager(getApplicationContext());
        Toast.makeText(getApplicationContext(), "User Login Status: " + session.isLoggedIn(), Toast.LENGTH_LONG).show();

        mapFrame = (RelativeLayout) findViewById(R.id.profileFrame);
        signinFrame = (LinearLayout) findViewById(R.id.signinFrame);

        boolean logado = session.isLoggedIn(); //Pega do prefs se já está criado
        if(logado){
            updateProfile(true);
        }

        //Facebook
        fbLoginButton = (LoginButton)findViewById(R.id.fb_login_button);
        fbLoginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {

                System.out.println("Facebook Login Successful!");
                System.out.println("Logged in user Details : ");
                System.out.println("--------------------------");
                System.out.println("User ID  : " + loginResult.getAccessToken().getUserId());
                System.out.println("Authentication Token : " + loginResult.getAccessToken().getToken());


                session.createLoginSession(loginResult.getAccessToken().getUserId(), loginResult.getAccessToken().getToken());
                Toast.makeText(getApplicationContext(), "Login realizado com sucesso!", Toast.LENGTH_LONG).show();
                updateProfile(true);

            }

            @Override
            public void onCancel() {
                Toast.makeText(getApplicationContext(), "Login cancelado por usuário!", Toast.LENGTH_LONG).show();
                System.out.println("Facebook Login failed!!");

            }

            @Override
            public void onError(FacebookException e) {
                Toast.makeText(getApplicationContext(), "Erro ao tentar realizar acesso!", Toast.LENGTH_LONG).show();
                System.out.println("Facebook Login failed!!");
            }
        });

        //Google

        signinGoogle = (SignInButton) findViewById(R.id.google_login_button);
        signinGoogle.setOnClickListener(this);
        mGoogleApiClient = new GoogleApiClient.Builder(this).addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this).addApi(Plus.API, Plus.PlusOptions.builder().build())
                .addScope(Plus.SCOPE_PLUS_LOGIN).build();

        //Conexão com Database
        loginDataBaseAdapter=new TreeDataBaseAdapter(this);
        try {
            loginDataBaseAdapter=loginDataBaseAdapter.open();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        btnSignIn=(Button)findViewById(R.id.buttonSignIn);

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

        gps = new GPSTracker(LoginActivity.this);

        if (gps.canGetLocation()) {
            latitude = gps.getLatitude();
            longitude = gps.getLongitude();

            /*Toast.makeText(
                    getApplicationContext(),
                    "Minha Localização\nLat: " + latitude + "\nLong: "
                            + longitude, Toast.LENGTH_LONG).show();*/

            myItemizedOverlay.addItem(new GeoPoint(latitude, longitude), "Eu", "Eu");
            mapView.getController().setZoom(10);
            mapView.getController().setCenter(new GeoPoint(latitude, longitude));
        } else {
            gps.showSettingsAlert();
        }


    }

    //Para Acesso com Facebook
    public void getFbKeyHash(String packageName) {
        facebook = true;

        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    packageName,
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("YourKeyHash :", Base64.encodeToString(md.digest(), Base64.DEFAULT));
                System.out.println("YourKeyHash: "+ Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }

        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {
        }
    }

    //Para acesso com Google
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    protected void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    private void resolveSignInError() {
        if (mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                mConnectionResult.startResolutionForResult(this, RC_SIGN_IN);
            } catch (IntentSender.SendIntentException e) {
                mIntentInProgress = false;
                mGoogleApiClient.connect();
            }
        }
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        if (!result.hasResolution()) {
            GooglePlayServicesUtil.getErrorDialog(result.getErrorCode(), this, 0).show();
            return;
        }

        if (!mIntentInProgress) {
            // store mConnectionResult
            mConnectionResult = result;

            if (signedInUser) {
                resolveSignInError();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        switch (requestCode) {
            case RC_SIGN_IN:
                if (responseCode == RESULT_OK) {
                    signedInUser = false;

                }
                mIntentInProgress = false;
                if (!mGoogleApiClient.isConnecting()) {
                    mGoogleApiClient.connect();
                }
                break;
        }

        if (facebook){
            callbackManager.onActivityResult(requestCode, responseCode, intent);
        }
    }

    @Override
    public void onConnected(Bundle arg0) {
        signedInUser = false;
        //Toast.makeText(this, "Connected", Toast.LENGTH_LONG).show();
        getProfileInformation();
        updateProfile(true);

    }

    private void updateProfile(boolean isSignedIn) {
        if (isSignedIn) {
            signinFrame.setVisibility(View.GONE);
            mapFrame.setVisibility(View.VISIBLE);

        } else {
            signinFrame.setVisibility(View.VISIBLE);
            mapFrame.setVisibility(View.GONE);
        }
    }

    private void getProfileInformation() {
        try {
            if (Plus.PeopleApi.getCurrentPerson(mGoogleApiClient) != null) {
                Person currentPerson = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);
                String personName = currentPerson.getDisplayName();
                String email = Plus.AccountApi.getAccountName(mGoogleApiClient);

                // update profile frame with new info about Google Account
                // profile
                session.createLoginSession(personName, email);
                Toast.makeText(getApplicationContext(), "Login realizado com sucesso!", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int cause) {
        mGoogleApiClient.connect();
        updateProfile(false);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.google_login_button:
                googlePlusLogin();
                break;
            case R.id.logout:
                googlePlusLogout();
                break;
        }
    }

    public void signIn(View v) {
        googlePlusLogin();
    }

    public void logout(View v) {
        googlePlusLogout();
    }

    private void googlePlusLogin() {
        if (!mGoogleApiClient.isConnecting()) {
            signedInUser = true;
            resolveSignInError();
            getProfileInformation();
            google=true;
        }
    }

    private void googlePlusLogout() {
        if (mGoogleApiClient.isConnected()) {
            Plus.AccountApi.clearDefaultAccount(mGoogleApiClient);
            mGoogleApiClient.disconnect();
            mGoogleApiClient.connect();

        }
    }

    //Acesso com dados do bd
    public void acessar(View V) {
        final EditText editTextUserName=(EditText)findViewById(R.id.editTextUserNameToLogin);
        final  EditText editTextPassword=(EditText)findViewById(R.id.editTextPasswordToLogin);

        String userName = editTextUserName.getText().toString();
        String password = editTextPassword.getText().toString();

        //String storedPassword = loginDataBaseAdapter.getSinlgeEntry(userName);

        if (password.equals("123")) {
            Toast.makeText(getApplicationContext(), "Login realizado com sucesso!", Toast.LENGTH_LONG).show();
            session.createLoginSession("teste", "teste");
            updateProfile(true);

        } else {
            Toast.makeText(LoginActivity.this, "Usuário ou senha incorretos!", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        loginDataBaseAdapter.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu_home; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_home, menu);

        if(facebook || google){
            return true;
        } else {
            return false;
        }

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
            session.logoutUser();

            if (!google){
                google = false;
                googlePlusLogout();
                Toast.makeText(getApplicationContext(), "Logout do google realizado!", Toast.LENGTH_LONG).show();


            }

            if (facebook){
                    facebook = false;
                    LoginManager.getInstance().logOut();
                    Toast.makeText(getApplicationContext(), "Logout do facebook realizado!", Toast.LENGTH_LONG).show();
                    //updateProfile(false);

            } else {

                Toast.makeText(getApplicationContext(), "Logout normal realizado!", Toast.LENGTH_LONG).show();
                //updateProfile(false);
            }
            //updateProfile(false);

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
