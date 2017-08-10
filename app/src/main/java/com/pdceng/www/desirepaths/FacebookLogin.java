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
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.FacebookSdk;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Arrays;

/**
 * Created by alondon on 8/8/2017.
 */

public class FacebookLogin extends FragmentActivity {
    String email,name,first_name,last_name,facebook_id;
    Bundle parameters;
    private CallbackManager callbackManager;
    LoginButton login_button;

    DatabaseHelper dh = new DatabaseHelper(this);

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
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }

        FacebookSdk.sdkInitialize(this.getApplicationContext());
        callbackManager = CallbackManager.Factory.create();

        setContentView(R.layout.splash);

        if (AccessToken.getCurrentAccessToken()!=null) {
            System.out.println("Starting graph request");
            GraphRequest graphRequest = GraphRequest.newMeRequest(AccessToken.getCurrentAccessToken(), new GraphRequest.GraphJSONObjectCallback() {
                @Override
                public void onCompleted(JSONObject object, GraphResponse response) {
                    Log.d("JSON", "" + response.getJSONObject().toString());

                    try {
                        name = object.getString("name");
                        facebook_id = object.getString("id");
                        checkUser(name,facebook_id);
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

            login_button = (LoginButton) findViewById(R.id.login_button);

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
                                facebook_id = object.getString("id");
                                checkUser(name,facebook_id);
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
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        callbackManager.onActivityResult(requestCode, resultCode, data);
    }

    boolean checkUser(String name, String...facebook_id){

        if(dh.isUser(facebook_id)){
            Universals.FACEBOOK_ID = facebook_id[0];
            Universals.NAME = name;
            System.out.println("user exists!");
            System.out.println("facebook_id: " + facebook_id[0]);
            System.out.println("name: "+ name);
        } else {
            Timestamp timestamp = new Timestamp(System.currentTimeMillis());
            System.out.println("user is being created!");
            System.out.println("facebook_id: " + facebook_id[0]);
            System.out.println("name: "+ name);
            Bundle bundle = new Bundle();
            bundle.putString(UserTable.FACEBOOK_ID, facebook_id[0]);
            bundle.putString(UserTable.NAME, name);
            bundle.putString(UserTable.REGISTERED_TIMESTAMP, timestamp.toString());
            dh.add(bundle, new UserTable());
            Universals.FACEBOOK_ID = facebook_id[0];
            Universals.NAME = name;
        }
        Bundle[] users = dh.getAllInTable(new UserTable());
        System.out.println("no. of users: " + users.length);

        Intent intent = new Intent(this,MapsActivity.class);
        startActivity(intent);
        return true;
    }

}
