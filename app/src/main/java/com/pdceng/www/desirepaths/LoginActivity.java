package com.pdceng.www.desirepaths;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Arrays;

/**
 * Created by alondon on 8/8/2017.
 */

public class LoginActivity extends FragmentActivity {
    public static final int GOOGLE_LOGIN = 3;
    public static final int FACEBOOK_LOGIN = 5;
    private static final int RC_SIGN_IN = 12;
    String email, name, first_name, last_name, social_media_id;
    Bundle parameters;
    LoginButton login_button;
    DatabaseHelper dh = new DatabaseHelper(this);
    GoogleApiClient mGoogleApiClient;
    private CallbackManager callbackManager;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

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

        FacebookSdk.sdkInitialize(this.getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.splash);

        if (AccessToken.getCurrentAccessToken()!=null) {
            System.out.println("Starting graph request");
            GraphRequest graphRequest = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                @Override
                public void onCompleted(JSONObject object, GraphResponse response) {
//                    Log.d("JSON", "" + response.getJSONObject().toString());

                    try {
                        name = object.getString("name");
                        social_media_id = object.getString("id");
                        checkUser(name, social_media_id);
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

            login_button = (LoginButton) findViewById(R.id.facebook_button);

            login_button.setReadPermissions(Arrays.asList("public_profile", "email"));
            login_button.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    login_button.setVisibility(View.GONE);

                    GraphRequest graphRequest = GraphRequest.newMeRequest(loginResult.getAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                        @Override
                        public void onCompleted(JSONObject object, GraphResponse response) {
                            Log.d("JSON", "" + response.getJSONObject().toString());

                            try {
                                name = object.getString("name");
                                social_media_id = object.getString("id");
                                checkUser(name, social_media_id);
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
                .requestEmail()
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, null)
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            handleSignInResult(result);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        Log.d("handleSignInResult", String.valueOf(result.isSuccess()));
        if (result.isSuccess()) {
            GoogleSignInAccount acct = result.getSignInAccount();
            name = acct.getGivenName() + " " + acct.getFamilyName();
            social_media_id = acct.getId();
            Universals.PHOTO_URL = acct.getPhotoUrl();
            checkUser(name, social_media_id);
        }
    }

    boolean checkUser(String name, String userId) {

        if (dh.isUser(userId)) {
            Universals.SOCIAL_MEDIA_ID = userId;
            Universals.NAME = name;
            System.out.println("user exists!");
            System.out.println("social_media_id: " + userId);
            System.out.println("name: "+ name);
        } else {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            System.out.println("user is being created!");
            System.out.println("social_media_id: " + userId);
            System.out.println("name: "+ name);
            Bundle bundle = new Bundle();
            bundle.putString(UserTable.SOCIAL_MEDIA_ID, userId);
            bundle.putString(UserTable.NAME, name);
            bundle.putString(UserTable.REGISTERED_TIMESTAMP, timestamp.toString());
            dh.insert(bundle, new UserTable());
            Universals.SOCIAL_MEDIA_ID = userId;
            Universals.NAME = name;
        }
        Bundle[] users = dh.getAllInTable(new UserTable());
        System.out.println("no. of users: " + users.length);

        Intent intent = new Intent(this,MapsActivity.class);
        startActivity(intent);
        return true;
    }

    private void googleSignIn() {
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(intent, RC_SIGN_IN);
    }

}
