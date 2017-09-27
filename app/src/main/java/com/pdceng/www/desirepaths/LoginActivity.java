package com.pdceng.www.desirepaths;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Arrays;


public class LoginActivity extends FragmentActivity implements AfterGetAll, GoogleApiClient.ConnectionCallbacks {
    private static final int RC_SIGN_IN = 12;
    private final DatabaseHelper dh = new DatabaseHelper(this);
    private final Context mContext = this;
    private String name;
    private String social_media_id;
    private String photo_url;
    private Bundle parameters;
    private LoginButton login_button;
    private Button bAnon;
    private GoogleApiClient mGoogleApiClient;
    private CallbackManager callbackManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);
        dh.getAllFromSQL(this);
        bAnon = (Button) findViewById(R.id.bAnon);
        bAnon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Universals.isAnon = true;
                dh.setAnonymousUser();
                dh.getAllFromSQL((AfterGetAll) mContext);
                Intent intent = new Intent(mContext, MapActivity.class);
                startActivity(intent);
            }
        });
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    "com.pdceng.www.desirepaths",
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException ignored) {

        }

//        FacebookSdk.sdkInitialize(this.getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        login_button = (LoginButton) findViewById(R.id.facebook_button);

        if (AccessToken.getCurrentAccessToken()!=null) {
            GraphRequest graphRequest = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                @Override
                public void onCompleted(JSONObject object, GraphResponse response) {
                    try {
                        name = object.getString("name");
                        social_media_id = object.getString("id");
                        photo_url = "https://graph.facebook.com/" + social_media_id + "/picture?type=large";
                        checkUser();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            parameters = new Bundle();
            parameters.putString("fields", "id,name,first_name,last_name,email");
            graphRequest.setParameters(parameters);
            graphRequest.executeAsync();
        } else {
            login_button.setReadPermissions(Arrays.asList("public_profile", "email"));
            login_button.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    GraphRequest graphRequest = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response) {
                            try {
                                name = object.getString("name");
                                social_media_id = object.getString("id");
                                photo_url = "https://graph.facebook.com/" + social_media_id + "/picture?type=large";
                                checkUser();
                            } catch (JSONException e) {
                                e.printStackTrace();
                            }
                        }
                    });

                    parameters = new Bundle();
                    parameters.putString("fields", "id,name,first_name,last_name,email");
                    graphRequest.setParameters(parameters);
                    graphRequest.executeAsync();
                }

                @Override
                public void onCancel() {

                }

                @Override
                public void onError(FacebookException exception) {

                }
            });
        }

        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail().requestIdToken(this.getString(R.string.default_web_client_id))
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        System.out.println(connectionResult.getErrorMessage());
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        SignInButton signInButton = (SignInButton) findViewById(R.id.google_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleSignIn();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        } else {
            callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d("handleSignInResult", String.valueOf(result.isSuccess()));
        if (result.isSuccess()) {
            Toast.makeText(this, "Sign in success", Toast.LENGTH_SHORT).show();
            GoogleSignInAccount acct = result.getSignInAccount();
            name = (acct != null ? acct.getGivenName() : null) + " " + acct.getFamilyName();
            social_media_id = acct.getId();
            photo_url = acct.getPhotoUrl().toString();
            checkUser();
        } else {
            Toast.makeText(this, "Could not sign-in! Try again, or enter anonymously", Toast.LENGTH_SHORT).show();
            System.out.println("Error: " + result.getStatus().toString());
//            Intent intent = new Intent(this,MapActivity.class);
//            startActivity(intent);
        }
    }

    private void checkUser() {
        if (dh.isUser(social_media_id)) {
            System.out.println("User is found!");
            Universals.SOCIAL_MEDIA_ID = social_media_id;
            Universals.USER_NAME = name;
        } else {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            Bundle bundle = new Bundle();
            bundle.putString(UserTable.SOCIAL_MEDIA_ID, social_media_id);
            bundle.putString(UserTable.NAME, name);
            bundle.putString(UserTable.PHOTO_URL, photo_url);
            bundle.putString(UserTable.REGISTERED_TIMESTAMP, timestamp.toString());
            dh.insert(bundle, new UserTable());
            Universals.SOCIAL_MEDIA_ID = social_media_id;
            Universals.USER_NAME = name;
        }

        dh.getAllFromSQL(this);
        Universals.isAnon = false;
        Intent intent = new Intent(this,MapActivity.class);
        startActivity(intent);
    }

    private void googleSignIn() {
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(intent, RC_SIGN_IN);
    }

    @Override
    public void afterGetAll() {
//        Toast.makeText(this, "Data loaded successfully", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
