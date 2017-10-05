package com.pdceng.www.desirepaths;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Movie;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.util.Base64;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
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
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Timestamp;
import java.util.Arrays;
import java.util.Timer;
import java.util.TimerTask;


public class LoginActivity extends FragmentActivity implements AfterGetAll, GoogleApiClient.ConnectionCallbacks {
    private static final int RC_SIGN_IN = 9001;
    private static final String TAG = "GoogleActivity";
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
    private FirebaseAuth mAuth;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash);

        dh.getAllFromSQL(this);

        //Anonymous login setup
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
        callbackManager = CallbackManager.Factory.create();

        //Facebook login setup
        login_button = (LoginButton) findViewById(R.id.facebook_button);

        if (AccessToken.getCurrentAccessToken() != null) {
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

        //Google login setup
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken(this.getString(R.string.default_web_client_id))
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
//                .addConnectionCallbacks(this)
                .enableAutoManage(this, new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
                        System.out.println(connectionResult.getErrorMessage());
                    }
                })
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();

        mAuth = FirebaseAuth.getInstance();

        final SignInButton signInButton = (SignInButton) findViewById(R.id.google_button);
        signInButton.setSize(SignInButton.SIZE_STANDARD);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleSignIn();
            }
        });

        signInButton.setVisibility(View.GONE);
        login_button.setVisibility(View.GONE);
        bAnon.setVisibility(View.GONE);
        final View tvName = findViewById(R.id.name);
        final View tvDescr = findViewById(R.id.description);
        tvName.setVisibility(View.GONE);
        tvDescr.setVisibility(View.GONE);

        final WebView webView = new WebView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.CENTER;

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int ivHeightSetting = size.y / 2;
        int ivWidthSetting = size.x / 2;
        int topMargin = ivHeightSetting - 450;
        int leftMargin = ivWidthSetting - 490;

        params.setMargins(leftMargin, topMargin, 0, 0);
        webView.setLayoutParams(params);
        webView.loadUrl("file:///android_asset/pdc_logo_anim.html");
        final LinearLayout topView = (LinearLayout) findViewById(R.id.topView);
        topView.addView(webView);

        new Timer().schedule(new TimerTask() {
            @Override
            public void run() {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        webView.setVisibility(View.GONE);
                        signInButton.setVisibility(View.VISIBLE);
                        login_button.setVisibility(View.VISIBLE);
                        bAnon.setVisibility(View.VISIBLE);
                        tvName.setVisibility(View.VISIBLE);
                        tvDescr.setVisibility(View.VISIBLE);

                        for (int i = 0; i < topView.getChildCount(); i++) {
                            View view = topView.getChildAt(i);
                            view.setAlpha(0);
                            view.animate()
                                    .alphaBy(1)
                                    .setDuration(500)
                                    .start();
                        }
                    }
                });
            }
        }, 3500);

    }



    private void handleSignInResult(GoogleSignInResult result) {
        Log.d("handleSignInResult", String.valueOf(result.isSuccess()));
        if (result.isSuccess()) {
            Toast.makeText(this, "Sign in success", Toast.LENGTH_SHORT).show();
            GoogleSignInAccount acct = result.getSignInAccount();
            firebaseAuthWithGoogle(acct);
            if (acct != null) {
                name = acct.getGivenName() + " " + acct.getFamilyName();
                social_media_id = acct.getId();
                photo_url = acct.getPhotoUrl().toString();
                checkUser();
            }
        } else {
            Toast.makeText(this, "Could not sign-in! Try again, or enter anonymously", Toast.LENGTH_SHORT).show();
            System.out.println("Error: " + result.getStatus().toString());
//            Intent intent = new Intent(this,MapActivity.class);
//            startActivity(intent);
        }
    }

    private void firebaseAuthWithGoogle(GoogleSignInAccount acct) {
        Log.d(TAG, "firebaseAuthWithGoogleL" + acct.getId());

        AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d(TAG, "signInWithCredential:success");
                            FirebaseUser user = mAuth.getCurrentUser();
                            checkUser();
                        } else {
                            Log.w(TAG, "signInWithCredential:failure", task.getException());
                            Toast.makeText(LoginActivity.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
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
        Intent intent = new Intent(this, MapActivity.class);
        startActivity(intent);
    }

    private void googleSignIn() {
        Intent intent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(intent, RC_SIGN_IN);
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

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();
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


    public class GIFView extends View {
        public Movie mMovie;
        public long movieStart;
        private int gifId;

        public GIFView(Context context) {
            super(context);
            initializeView();
        }

        public GIFView(Context context, AttributeSet attrs) {
            super(context, attrs);
            initializeView();
        }

        public GIFView(Context context, AttributeSet attrs, int defStyle) {
            super(context, attrs, defStyle);
            initializeView();
        }

        private void initializeView() {
//R.drawable.loader - our animated GIF
            InputStream is = getContext().getResources().openRawResource(R.raw.pdc_logo_anim);
            mMovie = Movie.decodeStream(is);
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawColor(Color.TRANSPARENT);
            super.onDraw(canvas);
            long now = android.os.SystemClock.uptimeMillis();
            if (movieStart == 0) {
                movieStart = now;
            }
            if (mMovie != null) {
                int relTime = (int) ((now - movieStart) % mMovie.duration());
                mMovie.setTime(relTime);
                mMovie.draw(canvas, getWidth() - mMovie.width(), getHeight() - mMovie.height());
                this.invalidate();
            }
        }

        public int getGIFResource() {
            return this.gifId;
        }

        public void setGIFResource(int resId) {
            this.gifId = resId;
            initializeView();
        }
    }
}
