package com.pdceng.www.desirepaths;

import android.Manifest;
import android.animation.Animator;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.InputType;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.webkit.URLUtil;
import android.widget.AbsListView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.facebook.CallbackManager;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.algo.Algorithm;
import com.google.maps.android.clustering.algo.NonHierarchicalDistanceBasedAlgorithm;
import com.google.maps.android.heatmaps.Gradient;
import com.google.maps.android.heatmaps.HeatmapTileProvider;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.jibble.simpleftp.SimpleFTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ScheduledExecutorService;

import static android.graphics.Color.LTGRAY;
import static android.widget.LinearLayout.VERTICAL;

public class MapsActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener, AfterGetAll, PublicInputListFragment.OnListFragmentInteractionListener
{
    static final int delay_getAll = 30;
    private static final String TAG = "tag";
    private static final int REQUEST_IMAGE_CAPTURE = 1;
    private static final int COLOR_BLACK_ARGB = 0xff000000;
    private static final int COLOR_WHITE_ARGB = 0xffffffff;
    private static final int COLOR_GREEN_ARGB = 0xff388E3C;
    private static final int COLOR_PURPLE_ARGB = 0xff81C784;
    private static final int COLOR_ORANGE_ARGB = 0xffF57F17;
    private static final int COLOR_BLUE_ARGB = 0xffF9A825;
    private static final int POLYLINE_STROKE_WIDTH_PX = 12;
    private static final int POLYGON_STROKE_WIDTH_PX = 8;
    private static final int PATTERN_DASH_LENGTH_PX = 20;
    private static final int PATTERN_GAP_LENGTH_PX = 20;
    private static final PatternItem DOT = new Dot();
    private static final PatternItem DASH = new Dash(PATTERN_DASH_LENGTH_PX);
    private static final PatternItem GAP = new Gap(PATTERN_GAP_LENGTH_PX);
    // Create a stroke pattern of a gap followed by a dot.
    private static final List<PatternItem> PATTERN_POLYLINE_DOTTED = Arrays.asList(GAP, DOT);
    // Create a stroke pattern of a gap followed by a dash.
    private static final List<PatternItem> PATTERN_POLYGON_ALPHA = Arrays.asList(GAP, DASH);
    // Create a stroke pattern of a dot followed by a gap, a dash, and another gap.
    private static final List<PatternItem> PATTERN_POLYGON_BETA =
            Arrays.asList(DOT, GAP, DASH, GAP);
    private static final int PICK_IMAGE = 4234;
    private static final int PERMISSIONS_MULTIPLE_REQUEST = 123;
    private static int DEFAULT_ZOOM = 10;
    private final LatLng startingPoint = new LatLng(61.2185, -149.8996);
    List<LatLng> lls = new ArrayList<>();
    Context mContext = this;
    CommentsAdapter adapter;
    ListView listView;
    DatabaseHelper dh = new DatabaseHelper(this);
    ScheduledExecutorService executorService;
    Runnable getAllFromSQL;
    float translationY = 0;
    boolean noPicture = false;
    private GoogleMap mMap;
    private ClusterManager<MyItem> mClusterManager;
    private HeatmapTileProvider mProvider;
    private TileOverlay mOverlay;
    private boolean mLocationPermissionGranted;
    private Location mDefaultLocation;
    private Location mLastKnownLocation;
    private CameraPosition mCameraPosition;
    private GoogleApiClient mGoogleApiClient;
    private boolean mCameraPermissionGranted;
    private boolean mStoragePermissionGranted;
    private ImageView mImageView;
    private FusedLocationProviderClient mFusedLocationClient;
    private LatLng mCurrLatLng;
    private Algorithm<MyItem> clusterManagerAlgorithm;
    private List<CameraPosition> previousCameraPositions = new ArrayList<>();
    private CameraPosition tempCameraPosition;
    private FloatingActionButton prevMapFab;
    private boolean updateCameraMemory = true;
    private boolean permissionRequested = false;
    private CallbackManager callbackManager;
    private Uri imageUri;
    private Universals universals;
    private int animDur = 200;
    private int ivHeightSetting = 700;

    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new PublicInputContent(this);
        setContentView(R.layout.activity_maps);
        universals = new Universals(this);
        setClickListeners();
        bringUpMap();
        checkPermission();
    }

    private void setClickListeners() {
        findViewById(R.id.myLocation).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getLocation(v);
            }
        });
        findViewById(R.id.addInputOnly).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                addPublicInputEntry(null, true);
            }
        });
        findViewById(R.id.fromLibrary).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                pickImageFromLibrary(v);
            }
        });
        findViewById(R.id.takePhoto).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent(v);
            }
        });
        findViewById(R.id.filterToggle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleOptions(v);
            }
        });
        findViewById(R.id.addInputToggle).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toggleOptions(v);
            }
        });
        findViewById(R.id.bCards).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startCardsActivity(v);
            }
        });
        findViewById(R.id.zoomIn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomIn(v);
            }
        });
        findViewById(R.id.zoomOut).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zoomOut(v);
            }
        });
        findViewById(R.id.prevMap).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToPreviousCameraPosition(v);
            }
        });
        findViewById(R.id.cbPositive).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterMarkers(v);
            }
        });
        findViewById(R.id.cbNeutral).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterMarkers(v);
            }
        });
        findViewById(R.id.cbNegative).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterMarkers(v);
            }
        });
        findViewById(R.id.list).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                loadListFragment(view);
            }
        });
    }

    private void bringUpMap() {
        if(Universals.NAME!=null){
            Toast.makeText(mContext, "Welcome, " + Universals.NAME.split(" ")[0], Toast.LENGTH_SHORT).show();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        prevMapFab = (FloatingActionButton) findViewById(R.id.prevMap);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        findViewById(R.id.llFilter).setVisibility(View.GONE);

        checkPermission(Manifest.permission.INTERNET, mStoragePermissionGranted);

//        if (dh.getAllInTable(new PIEntryTable())==null) {
//            addPIEntriesToDatabase();
//        }


//        new ConnectMySQL(null,null,this,1).execute("id2380250_alexlondon","Anchorage_0616");
//        System.out.println(dh.getRow(new UserTable(),UserTable.SOCIAL_MEDIA_ID, Universals.SOCIAL_MEDIA_ID).toString());
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startingPoint, 12));
        mMap.setOnInfoWindowClickListener(this);

        //Add geometry to Fairbanks
       /* LatLng[] linePoints = new LatLng[]{
                new LatLng(64.834832, -147.707030),
                new LatLng(64.835818, -147.712352),
                new LatLng(64.836438, -147.721879),
                new LatLng(64.836913, -147.726685),
                new LatLng(64.836840, -147.830369),
                new LatLng(64.836475, -147.834918)
        };

        LatLng[] polygonPoints = new LatLng[]{
                new LatLng(64.839832, -147.707030),
                new LatLng(64.839818, -147.712352),
                new LatLng(64.839438, -147.721879),
                new LatLng(64.839913, -147.726685),
                new LatLng(64.839840, -147.830369),
                new LatLng(64.839475, -147.834918),
                new LatLng(64.830475, -147.834918),
                new LatLng(64.830840, -147.830369),
                new LatLng(64.830913, -147.726685),
                new LatLng(64.830438, -147.721879),
                new LatLng(64.830818, -147.712352),
                new LatLng(64.830832, -147.707030)
        };

        mMap.addPolyline((new PolylineOptions())
                .add(linePoints)
                .width(5)
                .color(Color.BLUE)
                .geodesic(true));

        mMap.addCircle((new CircleOptions())
                .radius(10f)
                .center(new LatLng(64.834832, -147.707030))
                .fillColor(Color.RED)
                .strokeColor(Color.RED));

        mMap.addCircle((new CircleOptions())
                .radius(10f)
                .center(new LatLng(64.836475, -147.834918))
                .fillColor(Color.RED)
                .strokeColor(Color.RED));

        mMap.addPolygon((new PolygonOptions())
                .add(polygonPoints).strokeColor(Color.DKGRAY).strokeWidth(3f).geodesic(true));*/

        checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, mLocationPermissionGranted);

        mMap.setOnCameraMoveStartedListener(new GoogleMap.OnCameraMoveStartedListener() {
            @Override
            public void onCameraMoveStarted(int i) {
                tempCameraPosition = mMap.getCameraPosition();
                if (previousCameraPositions.size() < 1)
                    previousCameraPositions.add(tempCameraPosition);
                mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
                    @Override
                    public void onCameraIdle() {
                        if (tempCameraPosition != mMap.getCameraPosition() && updateCameraMemory) {
                            previousCameraPositions.add(mMap.getCameraPosition());
                            toggleReturnFab();
                        }
                        mClusterManager.onCameraIdle();
                        updateCameraMemory = true;
                    }
                });
            }
        });

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {
                unCheckToggleButtons();
            }
        });

        mClusterManager = new ClusterManager<>(this, mMap);

        setUpCluster();

        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MyItem>() {
            @Override
            public boolean onClusterItemClick(final MyItem myItem) {

                return openPublicInputView(myItem);
            }

        });

    }

    void unCheckToggleButtons() {
        ToggleButton tb = (ToggleButton) findViewById(R.id.filterToggle);
        ToggleButton tb2 = (ToggleButton) findViewById(R.id.addInputToggle);
        ToggleButton[] tbs = new ToggleButton[]{tb, tb2};
        for (ToggleButton toggleButton : tbs) {
            if (toggleButton.isChecked()) {
                toggleButton.setChecked(false);
                toggleOptions(toggleButton);
            }
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        Toast.makeText(MapsActivity.this, marker.getPosition().toString(), Toast.LENGTH_SHORT).show();
    }

    boolean openPublicInputView(final MyItem myItem) {
        translationY = 0;

        unCheckToggleButtons();

        //Creates CardView
        final RelativeLayout topView = (RelativeLayout) findViewById(R.id.topView);
        final CardView cardView = new CardView(mContext);

        final int margin = (int) getResources().getDimension(R.dimen.card_margin);
        CardView.LayoutParams params = new CardView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        cardView.setPadding(0, 200, 0, 200);
        cardView.setLayoutParams(params);

        cardView.setCardBackgroundColor(COLOR_WHITE_ARGB);

        cardView.bringToFront();
        cardView.setRadius(0);
        cardView.setClickable(true);
        cardView.setElevation(20f);

        topView.addView(cardView);

        //"Creates" grey background
        RelativeLayout relativeLayout = new RelativeLayout(mContext);
        relativeLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        LinearLayout.LayoutParams llParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        final LinearLayout linearLayout = new LinearLayout(mContext);
        linearLayout.setOrientation(VERTICAL);
        linearLayout.setLayoutParams(llParams);
//        linearLayout.setBackgroundColor(getColor(R.color.lightGrey));

        LinearLayout entryLayout = new LinearLayout(mContext);
        entryLayout.setLayoutParams(llParams);
        entryLayout.setOrientation(VERTICAL);
//        entryLayout.setBackgroundColor(getColor(R.color.white));

        //Creates main ImageView
        mImageView = new ImageView(mContext);
        ViewGroup.LayoutParams ivParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ivHeightSetting);
        mImageView.setLayoutParams(ivParams);
        mImageView.setScaleType(ImageView.ScaleType.CENTER_CROP);

        //Creates circular progress bar
        RelativeLayout.LayoutParams pbParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        pbParams.addRule(RelativeLayout.CENTER_IN_PARENT);
        ProgressBar progressBar = new ProgressBar(mContext);
        progressBar.setLayoutParams(pbParams);

        //Creates TextViews
        LinearLayout.LayoutParams snippetParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        snippetParams.setMargins(margin * 2, 0, 0, 0);

        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        titleParams.setMargins(margin * 2, 0, 0, 0);

        TextView tvUserDate = new TextView(mContext);
        TextView tvTitle = new TextView(mContext);
        TextView tvSnippet = new TextView(mContext);

        LinearLayout.LayoutParams userDateParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        userDateParams.gravity = Gravity.END;
        userDateParams.setMargins(0, margin, margin, 0);

        tvUserDate.setLayoutParams(userDateParams);
        tvTitle.setLayoutParams(titleParams);
        tvSnippet.setLayoutParams(snippetParams);

        final String addedBy = "Added by: ";
        SpannableString italicUserDate = new SpannableString(addedBy + myItem.getUser() + " on " + formatDateForTimestamp(myItem.getTimestamp()));
        italicUserDate.setSpan(new StyleSpan(Typeface.BOLD_ITALIC), addedBy.length(), addedBy.length() + myItem.getUser().length(), 0);

        SpannableString boldTitle = new SpannableString(myItem.getTitle());
        boldTitle.setSpan(new StyleSpan(Typeface.BOLD), 0, myItem.getTitle().length(), 0);

        tvUserDate.setText(italicUserDate);
        tvTitle.setText(boldTitle);
        tvSnippet.setText(myItem.getSnippet());

        //Close window 'button'
        ImageView closeWindow = createCloseWindowButton(cardView, topView);

        relativeLayout.addView(mImageView);
        relativeLayout.addView(closeWindow);
        relativeLayout.addView(progressBar);

        linearLayout.addView(relativeLayout);

        linearLayout.addView(entryLayout);

        entryLayout.addView(tvUserDate);
        entryLayout.addView(tvTitle);
        entryLayout.addView(tvSnippet);

        //comment button
        LinearLayout.LayoutParams commentButtonParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        commentButtonParams.gravity = Gravity.END;
        ImageButton ibComment = new ImageButton(mContext);
        ibComment.setImageResource(R.drawable.ic_event_note_black_24dp);
//        ibComment.setImageDrawable(getDrawable(R.drawable.ic_action_comment));
        ibComment.setBackground(null);
        ibComment.setLayoutParams(commentButtonParams);
        ibComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (noPicture) {
                    cardView.animate()
                            .translationY(0)
                            .setInterpolator(new AccelerateDecelerateInterpolator())
                            .setDuration(animDur);
                }
                final LinearLayout linearLayout1 = new LinearLayout(mContext);
                linearLayout1.setOrientation(VERTICAL);
                cardView.addView(linearLayout1);
                linearLayout1.setLayoutParams(new FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT, FrameLayout.LayoutParams.MATCH_PARENT));
                linearLayout1.setBackgroundColor(getColor(R.color.lightGreyTransparent));

                LinearLayout.LayoutParams etParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 300);
                etParams.setMargins(margin, margin, margin, margin);
                etParams.gravity = Gravity.TOP;

                final EditText etComment = new EditText(mContext);
                etComment.setHint("Add a comment...");
                etComment.setBackgroundColor(getColor(R.color.white));
                etComment.setPadding(margin, 0, margin, 0);
                etComment.setLayoutParams(etParams);
                etComment.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);

                Button postButton = new Button(mContext);
                postButton.setText("POST");
                postButton.getBackground().setColorFilter(getColor(R.color.darkBlue), PorterDuff.Mode.MULTIPLY);
                Button cancelButton = new Button(mContext);
                cancelButton.setText("CANCEL");

                linearLayout1.addView(etComment);
                linearLayout1.addView(postButton);
                linearLayout1.addView(cancelButton);

                etComment.requestFocus();
                final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                inputMethodManager.toggleSoftInputFromWindow(linearLayout1.getApplicationWindowToken(), InputMethodManager.SHOW_IMPLICIT, 0);

                postButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (etComment.getText().toString().isEmpty()) {
                            inputMethodManager.toggleSoftInputFromWindow(linearLayout1.getApplicationWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                            Snackbar.make(findViewById(android.R.id.content),
                                    "A comment must be entered to post",
                                    Snackbar.LENGTH_INDEFINITE).setAction("OKAY",
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            inputMethodManager.toggleSoftInputFromWindow(linearLayout1.getApplicationWindowToken(), InputMethodManager.SHOW_IMPLICIT, 0);
                                        }
                                    }).show();
//                            Toast.makeText(MapsActivity.this, "A comment must be entered to post", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        Bundle bundle = new Bundle();
                        bundle.putString(CommentsTable.PIEntry_ID, String.valueOf(myItem.getId()));
                        bundle.putString(CommentsTable.RATING, "0");
                        bundle.putString(CommentsTable.COMMENT, etComment.getText().toString());
                        bundle.putString(CommentsTable.TIMESTAMP, String.valueOf(System.currentTimeMillis()));
                        bundle.putString(CommentsTable.SOCIAL_MEDIA_ID, Universals.SOCIAL_MEDIA_ID);
                        dh.insert(bundle, new CommentsTable());
                        inputMethodManager.toggleSoftInputFromWindow(linearLayout1.getApplicationWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                        cardView.removeView(linearLayout1);
                        setCommentsAdapter(String.valueOf(myItem.getId()));

//                        if (noPicture) {
//                            cardView.animate()
//                                    .translationY(translationY)
//                                    .setInterpolator(new AccelerateDecelerateInterpolator())
//                                    .setDuration(animDur);
//                        }
                    }
                });

                cancelButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        cardView.removeView(linearLayout1);
                        inputMethodManager.toggleSoftInputFromWindow(linearLayout1.getApplicationWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY, 0);

//                        if (noPicture) {
//                            cardView.animate()
//                                    .translationY(translationY)
//                                    .setInterpolator(new AccelerateDecelerateInterpolator())
//                                    .setDuration(animDur);
//                        }
                    }
                });
            }
        });
        entryLayout.addView(ibComment);

        //ListView
        listView = new ListView(mContext);

        ListView.LayoutParams lvParams = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        listView.setLayoutParams(lvParams);

        setCommentsAdapter(String.valueOf(myItem.getId()));

        linearLayout.addView(listView);

        cardView.addView(linearLayout);

        //load image
        String bitmapURL = myItem.getBitmapUrlString();
        if (URLUtil.isValidUrl(bitmapURL) || !bitmapURL.isEmpty()) {
            Log.d("Bitmap URL", bitmapURL);
            new DownloadImageTask(mImageView, progressBar).execute(bitmapURL);
        } else {
            relativeLayout.removeView(mImageView);
            relativeLayout.removeView(progressBar);
//            translationY = ivHeightSetting + 100;
//            noPicture = true;
        }

        //open animation
        int height = cardView.getMeasuredHeight();
        Log.d("cardView height", String.valueOf(height));
        cardView.setY(topView.getHeight());
        cardView.animate()
                .translationY(height)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setDuration(animDur);

        return true;
    }

    private void loadListFragment(View v) {
//        new PublicInputContent(this);
//        PublicInputListFragment publicInputListFragment = new PublicInputListFragment();
//        RelativeLayout topView = (RelativeLayout) findViewById(R.id.topView);
//        RecyclerView rv = new RecyclerView(this);
//        rv.setId(R.id.listView);
//        getFragmentManager().beginTransaction().add(R.id.piList, PublicInputListFragment.newInstance(null)).commit();
//        topView.addView(rv);
    }

    private ImageView createCloseWindowButton(final View cardView, final RelativeLayout topView) {
        final ImageView closeWindow = new ImageView(mContext);
        closeWindow.setImageDrawable(getDrawable(R.drawable.ic_dialog_close_dark));
        closeWindow.setColorFilter(LTGRAY);
        RelativeLayout.LayoutParams fabParams = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        fabParams.setMargins(10, 10, 0, 0);
        closeWindow.setLayoutParams(fabParams);
        closeWindow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dh.getAllFromSQL((AfterGetAll) mContext);
                cardView.animate()
                        .translationY(topView.getHeight())
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .setDuration(animDur)
                        .setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
//                                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
//                                imm.toggleSoftInputFromWindow(closeWindow.getApplicationWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                                unCheckToggleButtons();
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                topView.removeView(cardView);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {
                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {
                            }
                        });
            }
        });
        return closeWindow;
    }

    void setCommentsAdapter(String id) {
        listView.animate()
                .alpha(0f)
                .setDuration(500)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {

                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
        List<String> commentIds = dh.getComments(id);
        adapter = new CommentsAdapter(mContext, commentIds);
        listView.setAdapter(adapter);
        adapter.notifyDataSetChanged();
        listView.setAlpha(0f);
        listView.animate()
                .alpha(1f)
                .setDuration(500)
                .setListener(new Animator.AnimatorListener() {
                    @Override
                    public void onAnimationStart(Animator animation) {

                    }

                    @Override
                    public void onAnimationEnd(Animator animation) {

                    }

                    @Override
                    public void onAnimationCancel(Animator animation) {

                    }

                    @Override
                    public void onAnimationRepeat(Animator animation) {

                    }
                });
    }

    public boolean setUpCluster() {
        clusterManagerAlgorithm = new NonHierarchicalDistanceBasedAlgorithm<>();
        mClusterManager.setAlgorithm(clusterManagerAlgorithm);
        mClusterManager.setRenderer(new OwnIconRendered(this, mMap, mClusterManager));
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
        addItems(new String[]{null});

        Collection<MyItem> markers = clusterManagerAlgorithm.getItems();

        return true;
    }

    private void addItems(String... strings) {
        Bundle[] bundles = dh.getAllInTable(new PIEntryTable());
        mClusterManager.clearItems();

        for (Bundle bundle : bundles) {
            MyItem myItem = new MyItem(bundle);
            if (strings == null) {
                //Do nothing
            } else if (strings[0] == null) {
                mClusterManager.addItem(myItem);
            } else {
                for (String string : strings) {
                    if (Objects.equals(myItem.getSentiment(), string)) {
                        mClusterManager.addItem(myItem);
                    }
                }
            }
        }

        mClusterManager.cluster();
    }

    void zoomOut(View view) {
        mMap.moveCamera(CameraUpdateFactory.zoomOut());
    }

    void zoomIn(View view) {
        mMap.moveCamera(CameraUpdateFactory.zoomIn());
    }

    private void addHeatMap() {
        int[] colors = {
                Color.rgb(102, 225, 0), //Green
                Color.rgb(255, 0, 0), //Red
        };

        float[] startPoints = {
                0.2f, 1f
        };

        Gradient gradient = new Gradient(colors, startPoints);

        List<LatLng> list = null;
        list = lls;

        mProvider = new HeatmapTileProvider.Builder()
                .data(list)
                .gradient(gradient)
                .build();
        mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
    }

    private ArrayList<LatLng> readItems(int resource) throws JSONException {
        ArrayList<LatLng> list = new ArrayList<>();
        InputStream inputStream = getResources().openRawResource(resource);
        String json = new Scanner(inputStream).useDelimiter("\\A").next();
        JSONArray array = new JSONArray(json);
        for (int i = 0; i < array.length(); i++) {
            JSONObject object = array.getJSONObject(i);
            double lat = object.getDouble("lat");
            double lng = object.getDouble("lng");
            list.add(new LatLng(lat, lng));
        }
        return list;
    }

    void dispatchTakePictureIntent(View v) {
        if (checkPermission(Manifest.permission.CAMERA, mCameraPermissionGranted) > 0
                && checkPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE, mStoragePermissionGranted) > 0) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.TITLE, "New Picture");
            values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
            imageUri = getContentResolver()
                    .insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivityForResult(intent, 1);
            }
        }
    }

    void pickImageFromLibrary(View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);
    }

    private int checkPermission(String permission, boolean bool) {
        int result = 0;
        if (ContextCompat.checkSelfPermission(this.getApplicationContext(),
                permission)
                == PackageManager.PERMISSION_GRANTED) {
            System.out.println("...permission is granted");
            result = 1;
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{permission},
                    1);
            System.out.println("...asking for permission");
        }
        return result;
    }

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) + ContextCompat
                .checkSelfPermission(this,
                        Manifest.permission.CAMERA) + ContextCompat
                .checkSelfPermission(this,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE) + ContextCompat
                .checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION) + ContextCompat
                .checkSelfPermission(this,
                        Manifest.permission.ACCESS_NETWORK_STATE) + ContextCompat
                .checkSelfPermission(this,
                        Manifest.permission.INTERNET)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale
                    (this, Manifest.permission.READ_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale
                            (this, Manifest.permission.CAMERA) ||
                    ActivityCompat.shouldShowRequestPermissionRationale
                            (this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale
                            (this, Manifest.permission.ACCESS_FINE_LOCATION) ||
                    ActivityCompat.shouldShowRequestPermissionRationale
                            (this, Manifest.permission.ACCESS_NETWORK_STATE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale
                            (this, Manifest.permission.INTERNET)) {

                Snackbar.make(this.findViewById(android.R.id.content),
                        "Please Grant Permissions to share photos as public input",
                        Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                requestPermissions(
                                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                                Manifest.permission.CAMERA,
                                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                Manifest.permission.ACCESS_FINE_LOCATION,
                                                Manifest.permission.ACCESS_NETWORK_STATE,
                                                Manifest.permission.INTERNET},
                                        PERMISSIONS_MULTIPLE_REQUEST);
                            }
                        }).show();
            } else {
                requestPermissions(
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                Manifest.permission.CAMERA,
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_NETWORK_STATE,
                                Manifest.permission.INTERNET},
                        PERMISSIONS_MULTIPLE_REQUEST);
            }
        } else {
            // write your logic code if permission already granted
        }
    }

    void getLocation(View v) {
        System.out.println("getting current location!");
        if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, mLocationPermissionGranted) > 0) {
            System.out.println("permission asked");
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                System.out.println("moving to location!");
                mFusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        if (location != null) {
                            mCurrLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                            Toast.makeText(MapsActivity.this, "Current location: " + mCurrLatLng.latitude + "," + mCurrLatLng.longitude, Toast.LENGTH_SHORT).show();
//                            mMap.addMarker(new MarkerOptions().position(mCurrLatLng).title("Your \'current\' location"));
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurrLatLng, 18));

                        }
                    }
                });
            } else {
                System.out.println("access not granted!");
            }
        }
    }

    void moveToPreviousCameraPosition(View v) {
        updateCameraMemory = false;
        previousCameraPositions.remove(previousCameraPositions.size() - 1);
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(previousCameraPositions.get(previousCameraPositions.size() - 1)));
        toggleReturnFab();
    }

    private void toggleReturnFab() {
        if (hasPreviousPosition()) prevMapFab.setVisibility(View.VISIBLE);
        else prevMapFab.setVisibility(View.INVISIBLE);
    }

    private boolean hasPreviousPosition() {
        return previousCameraPositions.size() > 1;
    }

    void toggleOptions(View v) {
        LinearLayout ll = null;
        if (v instanceof ToggleButton) {
            switch (v.getId()) {
                case R.id.filterToggle:
                    ll = (LinearLayout) findViewById(R.id.llFilter);
                    break;
                case R.id.addInputToggle:
                    ll = (LinearLayout) findViewById(R.id.llAddType);
            }
            if (((ToggleButton) v).isChecked()) {

                ll.setVisibility(View.VISIBLE);
                ll.setAlpha(0);
                ll.setPivotY(ll.getMeasuredHeight());
                final LinearLayout finalLl = ll;
                ll.animate()
                        .setInterpolator(new DecelerateInterpolator())
                        .alpha(1)
                        .setDuration(animDur)
                        .setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                finalLl.setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {
                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {
                            }
                        });
            } else {
                ll.setPivotY(ll.getMeasuredHeight());
                final LinearLayout finalLl1 = ll;
                ll.animate()
                        .alpha(0)
                        .setInterpolator(new AccelerateInterpolator())
                        .setDuration(animDur)
                        .setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {

                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                finalLl1.setVisibility(View.GONE);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        });
            }
        }
    }

    void filterMarkers(View v) {
        if (v instanceof CheckBox) {
            CheckBox cbPositive = (CheckBox) findViewById(R.id.cbPositive);
            CheckBox cbNeutral = (CheckBox) findViewById(R.id.cbNeutral);
            CheckBox cbNegative = (CheckBox) findViewById(R.id.cbNegative);

            CheckBox[] cbs = new CheckBox[]{cbPositive, cbNeutral, cbNegative};
            List<String> strings = new ArrayList<>();
            for (CheckBox cb : cbs) {
                if (cb.isChecked()) {
                    strings.add(cb.getText().toString().toLowerCase());
                }
            }
            if (strings.size() == 0) {
                addItems(null);
            } else {
                String[] strs = new String[strings.size()];
                strs = strings.toArray(strs);
                addItems(strs);
            }
        }
    }

    void startCardsActivity(View v) {
        Intent intent = new Intent(this, CardsActivity.class);
        startActivity(intent);
    }

    private void addPIEntriesToDatabase() {
        List<PublicInput> publicInputList = CardsUtils.loadPublicInputs(mContext);

        for (PublicInput publicInput : publicInputList) {
            Bundle bundle = new Bundle();
            bundle.putString(PIEntryTable.URL, publicInput.getUrl());
            bundle.putString(PIEntryTable.LATITUDE, String.valueOf(publicInput.getLatitude()));
            bundle.putString(PIEntryTable.LONGITUDE, String.valueOf(publicInput.getLongitude()));
            bundle.putString(PIEntryTable.SENTIMENT, publicInput.getSentiment());
            bundle.putString(PIEntryTable.TITLE, publicInput.getTitle());
            bundle.putString(PIEntryTable.SNIPPET, publicInput.getSnippet());
            bundle.putString(PIEntryTable.USER, publicInput.getUser());
            bundle.putString(PIEntryTable.TIMESTAMP, publicInput.getTimestamp());
            dh.insert(bundle, new PIEntryTable());
        }

        //test user
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        Bundle bundle = new Bundle();
        bundle.putString(UserTable.SOCIAL_MEDIA_ID, "test_user");
        bundle.putString(UserTable.REGISTERED_TIMESTAMP, timestamp.toString());
        dh.insert(bundle, new UserTable());
    }

    void togglePreviewSize(View v) {
        int min = (int) getResources().getDimension(R.dimen.preview_height_min);
        int max = (int) getResources().getDimension(R.dimen.preview_height_max);

        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) v.getLayoutParams();

        if (params.height < max) {
            params.height = max;
        } else {
            params.height = min;
        }

        v.setLayoutParams(params);
    }

    String formatDateForTimestamp(Timestamp timestamp) {
        String timeString = timestamp.toString();
        timeString = timeString.substring(0, 10);
        String year = timeString.substring(0, 4);
        timeString = timeString.replace(year, "");
        timeString = timeString.substring(1);
        timeString += "-" + year;
        return timeString;
    }

    private void addPublicInputEntry(Bitmap imageBitmap, final boolean chooseLocation) {
        RelativeLayout relativeLayout = new RelativeLayout(this);

        if (imageBitmap != null) {
            imageBitmap = Universals.sampleBitmap(imageBitmap);

            final ImageView imageView = new ImageView(this);
            ViewGroup.LayoutParams ivParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 700);
            imageView.setLayoutParams(ivParams);
            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

            if (imageView.getDrawable() == null) imageView.setImageBitmap(imageBitmap);

            relativeLayout.addView(imageView);
        }

        final RelativeLayout topView = (RelativeLayout) findViewById(R.id.topView);
        final CardView cardView = new CardView(mContext);

        CardView.LayoutParams params = new CardView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        params.setMargins(0, 0, 0, 0);
        cardView.setLayoutParams(params);

        cardView.setCardBackgroundColor(COLOR_WHITE_ARGB);
        cardView.setCardElevation(20f);
        cardView.setClickable(true);

        topView.addView(cardView);

        relativeLayout.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        final LinearLayout linearLayout = new LinearLayout(this);
        linearLayout.setOrientation(VERTICAL);
        linearLayout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        final EditText titleEdit = new EditText(this);
        final EditText snippetEdit = new EditText(this);
        final Spinner spinner = new Spinner(this);
        Button button = new Button(this);
        button.setText(R.string.addButton);
        button.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));

        titleEdit.setHint("Add a title");
        titleEdit.setInputType(InputType.TYPE_TEXT_FLAG_IME_MULTI_LINE);
        snippetEdit.setHint("Your message");
        snippetEdit.setInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE);

        spinner.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, new String[]{"(Type)", "Positive", "Neutral", "Negative"}));

        //Close window 'button'
        ImageView closeWindow = createCloseWindowButton(cardView, topView);

        relativeLayout.addView(closeWindow);

        linearLayout.addView(relativeLayout);

        linearLayout.addView(titleEdit);
        linearLayout.addView(snippetEdit);
        linearLayout.addView(spinner);
        linearLayout.addView(button);

        cardView.addView(linearLayout);

        titleEdit.requestFocus();
        final InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputMethodManager.toggleSoftInputFromWindow(linearLayout.getApplicationWindowToken(), InputMethodManager.SHOW_IMPLICIT, 0);

        SendImageFTP sendImageFTP = null;
        if (imageBitmap != null) {
            sendImageFTP = new SendImageFTP(imageBitmap, this);
            sendImageFTP.execute();
        }

        final Bitmap finalImageBitmap = imageBitmap;
        final SendImageFTP finalSendImageFTP = sendImageFTP;

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Check if all fields are filled
                for (int i = 0; i < linearLayout.getChildCount(); i++) {
                    if (linearLayout.getChildAt(i) instanceof EditText) {
                        EditText et = (EditText) linearLayout.getChildAt(i);
                        if (et.getText() == null || et.getText().toString().length() < 2) {
                            inputMethodManager.toggleSoftInputFromWindow(cardView.getApplicationWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                            Snackbar.make(findViewById(android.R.id.content),
                                    "Please complete all the fields",
                                    Snackbar.LENGTH_INDEFINITE).setAction("OKAY",
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            inputMethodManager.toggleSoftInputFromWindow(cardView.getApplicationWindowToken(), InputMethodManager.SHOW_IMPLICIT, 0);
                                        }
                                    }).show();
//                            Toast.makeText(MapsActivity.this, "Please complete all the fields", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    } else if (linearLayout.getChildAt(i) instanceof Spinner) {
                        Spinner spinner = (Spinner) linearLayout.getChildAt(i);
                        if (spinner.getSelectedItemPosition() == 0) {
                            inputMethodManager.toggleSoftInputFromWindow(cardView.getApplicationWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                            Snackbar.make(findViewById(android.R.id.content),
                                    "Please choose an option from the spinner",
                                    Snackbar.LENGTH_INDEFINITE).setAction("OKAY",
                                    new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                        }
                                    }).show();
//                            Toast.makeText(MapsActivity.this, "Please choose an option from the spinner", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                }

                //Remember field values
                final String sentiment = spinner.getSelectedItem().toString().toLowerCase();
                final String title = titleEdit.getText().toString();
                final String snippet = snippetEdit.getText().toString();

                //slide out CardView
                cardView.animate()
                        .translationY(topView.getHeight())
                        .setInterpolator(new AccelerateDecelerateInterpolator())
                        .setDuration(animDur)
                        .setListener(new Animator.AnimatorListener() {
                            @Override
                            public void onAnimationStart(Animator animation) {
//                                inputMethodManager.toggleSoftInputFromWindow(cardView.getApplicationWindowToken(), InputMethodManager.HIDE_IMPLICIT_ONLY, 0);
                                unCheckToggleButtons();
                            }

                            @Override
                            public void onAnimationEnd(Animator animation) {
                                topView.removeView(cardView);
                            }

                            @Override
                            public void onAnimationCancel(Animator animation) {

                            }

                            @Override
                            public void onAnimationRepeat(Animator animation) {

                            }
                        });

                System.out.println("Checking location...");
                //Check & requests location; handles outcome
                if (checkPermission(Manifest.permission.ACCESS_FINE_LOCATION, mLocationPermissionGranted) > 0) {
                    if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        Toast.makeText(mContext, "Cannot access location!", Toast.LENGTH_SHORT).show();
                        System.out.println("Permission not granted");
                        chooseLocationExec(false, title, snippet, sentiment);
                    } else {
                        System.out.println("Permission granted");
                        mFusedLocationClient.getLastLocation().addOnSuccessListener((MapsActivity) mContext, new OnSuccessListener<Location>() {
                            @Override
                            public void onSuccess(Location location) {
                                if (location == null) {
                                    Toast.makeText(mContext, "Location permission was granted, but service can't find location. Please turn location on from device settings.", Toast.LENGTH_SHORT).show();
                                    chooseLocationExec(false, title, snippet, sentiment);
                                } else {
                                    mCurrLatLng = new LatLng(location.getLatitude(), location.getLongitude());
                                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurrLatLng, 25));

                                    if (chooseLocation) {
                                        chooseLocationExec(location != null, title, snippet, sentiment);
                                    } else {
                                        Bundle bundle = new Bundle();
                                        bundle.putString(PIEntryTable.URL, finalImageBitmap != null ? finalSendImageFTP.getFilename() : "");
                                        bundle.putString(PIEntryTable.LATITUDE, String.valueOf(location.getLatitude()));
                                        bundle.putString(PIEntryTable.LONGITUDE, String.valueOf(location.getLongitude()));
                                        bundle.putString(PIEntryTable.SENTIMENT, sentiment);
                                        bundle.putString(PIEntryTable.TITLE, title);
                                        bundle.putString(PIEntryTable.SNIPPET, snippet);
                                        bundle.putString(PIEntryTable.USER, Universals.NAME);
                                        bundle.putString(PIEntryTable.TIMESTAMP, new Timestamp(System.currentTimeMillis()).toString());
                                        dh.insert(bundle, new PIEntryTable());
                                        Toast.makeText(MapsActivity.this, "Content added!", Toast.LENGTH_SHORT).show();
                                        addItems(new String[]{null});
                                    }
                                }
                            }
                        });
                    }
                }
            }

            private void chooseLocationExec(boolean knownLoc, final String title, final String snippet, final String sentiment) {
                Snackbar.make(findViewById(android.R.id.content),
                        "Click map or drag marker to position, then click \'OK\'",
                        Snackbar.LENGTH_INDEFINITE).setAction("THANKS, GOT IT",
                        new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                            }
                        }).show();
//                Toast.makeText(mContext, "Drag marker to desired position; click \'OK\'", Toast.LENGTH_LONG).show();
                final Button okButton = new Button(mContext);
                okButton.setText(R.string.OK);
                RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                layoutParams.addRule(RelativeLayout.ALIGN_PARENT_END);
                layoutParams.addRule(RelativeLayout.ABOVE, findViewById(R.id.menu).getId());

                okButton.setLayoutParams(layoutParams);
                topView.addView(okButton);

                if (!knownLoc) {
                    mCurrLatLng = mMap.getCameraPosition().target;
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurrLatLng, 25));
                }

                BitmapDescriptor mIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
                switch (sentiment) {
                    case "positive":
                        mIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
                        break;
                    case "neutral":
                        mIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_YELLOW);
                        break;
                    case "negative":
                        mIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
                }

                final Marker tempMarker = mMap.addMarker(new MarkerOptions()
                        .position(mCurrLatLng)
                        .title("Click map or drag to position; click OK to finish")
                        .icon(mIcon));
                tempMarker.setDraggable(true);

                mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        tempMarker.setPosition(latLng);
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(latLng));
                    }
                });
                mMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                    @Override
                    public void onMarkerDragStart(Marker marker) {
                        marker.setAlpha(.75f);
                    }

                    @Override
                    public void onMarkerDrag(Marker marker) {

                    }

                    @Override
                    public void onMarkerDragEnd(Marker marker) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
                        marker.setAlpha(1f);
                    }
                });

                okButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mCurrLatLng = tempMarker.getPosition();
                        tempMarker.remove();
                        topView.removeView(okButton);

                        Bundle bundle = new Bundle();
                        bundle.putString(PIEntryTable.URL, finalImageBitmap != null ? finalSendImageFTP.getFilename() : "");
                        bundle.putString(PIEntryTable.LATITUDE, String.valueOf(mCurrLatLng.latitude));
                        bundle.putString(PIEntryTable.LONGITUDE, String.valueOf(mCurrLatLng.longitude));
                        bundle.putString(PIEntryTable.SENTIMENT, sentiment);
                        bundle.putString(PIEntryTable.TITLE, title);
                        bundle.putString(PIEntryTable.SNIPPET, snippet);
                        bundle.putString(PIEntryTable.USER, Universals.NAME);
                        bundle.putString(PIEntryTable.TIMESTAMP, new Timestamp(System.currentTimeMillis()).toString());
                        dh.insert(bundle, new PIEntryTable());
                        Toast.makeText(MapsActivity.this, "Content added!", Toast.LENGTH_SHORT).show();
                        addItems(new String[]{null});
                    }
                });
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean chooseLocation = false;
        if (resultCode == RESULT_OK) {
            Bitmap imageBitmap = null;
            if (requestCode == REQUEST_IMAGE_CAPTURE) {

                try {
                    imageBitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), imageUri);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else if (requestCode == PICK_IMAGE) {
                chooseLocation = true;
                InputStream inputStream;
                try {
                    inputStream = getContentResolver().openInputStream(data.getData());
                    imageBitmap = BitmapFactory.decodeStream(inputStream);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("imageBitmap: " + imageBitmap);
            addPublicInputEntry(imageBitmap, chooseLocation);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PERMISSIONS_MULTIPLE_REQUEST:
                if (grantResults.length > 0) {
                    boolean readExternalFile = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                    boolean cameraPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean writeExternalFile = grantResults[2] == PackageManager.PERMISSION_GRANTED;
                    boolean accessFineLocation = grantResults[3] == PackageManager.PERMISSION_GRANTED;
                    boolean accessNetworkState = grantResults[4] == PackageManager.PERMISSION_GRANTED;
                    boolean internet = grantResults[5] == PackageManager.PERMISSION_GRANTED;

                    if (readExternalFile && cameraPermission && writeExternalFile && accessFineLocation && accessNetworkState && internet) {
                        // write your logic here
                    } else {
                        Snackbar.make(this.findViewById(android.R.id.content),
                                "Please Grant Permissions to upload profile photo",
                                Snackbar.LENGTH_INDEFINITE).setAction("ENABLE",
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        requestPermissions(
                                                new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                                        Manifest.permission.CAMERA,
                                                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                                        Manifest.permission.ACCESS_NETWORK_STATE,
                                                        Manifest.permission.INTERNET},
                                                PERMISSIONS_MULTIPLE_REQUEST);
                                    }
                                }).show();
                    }
                }
                break;
        }
    }

    @Override
    public void afterGetAll() {
        setUpCluster();
    }

    @Override
    public void onListFragmentInteraction(PublicInput item) {

    }

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        ImageView bmImage;
        ProgressBar progressBar;

        DownloadImageTask(ImageView bmImage, ProgressBar progressBar) {
            this.bmImage = bmImage;
            this.progressBar = progressBar;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(View.VISIBLE);
            mImageView.setImageBitmap(null);
        }

        protected Bitmap doInBackground(String... urls) {
            String urlDisplay = urls[0];
            System.out.println(urlDisplay);
            Bitmap bitmap = null;
            if (!URLUtil.isValidUrl(urlDisplay)) {
                if (!universals.isBitmapInMemoryCache(urlDisplay)) {
                    FTPClient ftpClient = new FTPClient();
                    System.out.println("Starting connection to FTP site!");
                    try {
                        ftpClient.connect("153.92.6.4");
                        ftpClient.login(getString(R.string.ftp_username), getString(R.string.ftp_password));
                        ftpClient.enterLocalPassiveMode();
                        ftpClient.setFileType(FTP.BINARY_FILE_TYPE);

                        File file = new File(Environment.getExternalStorageDirectory() + File.separator + urlDisplay);
                        Log.d("filepath:", file.getAbsolutePath());
                        FileOutputStream fos = new FileOutputStream(file);
                        ftpClient.retrieveFile(urlDisplay, fos);
                        fos.flush();
                        fos.close();
                        bitmap = BitmapFactory.decodeFile(file.getAbsolutePath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    universals.addBitmapToMemoryCache(urlDisplay, bitmap);
                }
                bitmap = universals.getBitmapFromMemoryCache(urlDisplay);
            } else {
                if (!universals.isBitmapInMemoryCache(urlDisplay)) {
                    universals.addBitmapToMemoryCache(urlDisplay, universals.getBitmapFromURL(urlDisplay, 200, 200));
                }
                bitmap = universals.getBitmapFromMemoryCache(urlDisplay);
            }
            return bitmap;
        }

        protected void onPostExecute(Bitmap result) {
            bmImage.setAlpha(0f);
            bmImage.setImageBitmap(result);
            bmImage.animate()
                    .setDuration(500)
                    .alphaBy(1f);
            progressBar.setVisibility(View.GONE);
        }
    }

    private class SendImageFTP extends AsyncTask<Void, Integer, String> {
        Bitmap bitmap;
        Context context;
        String filename;
        ProgressDialog progressDialog;

        SendImageFTP(Bitmap bitmap, Context context) {
            this.bitmap = bitmap;
            this.context = context;
            this.filename = Universals.SOCIAL_MEDIA_ID + "_" + String.valueOf(System.currentTimeMillis() + ".jpg");
        }

        @Override
        protected String doInBackground(Void... params) {
            //converts bitmap to image file
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, bos);
            File file = new File(Environment.getExternalStorageDirectory() + File.separator + filename);

            try {
                FileOutputStream fos = new FileOutputStream(file);
                fos.write(bos.toByteArray());
                fos.flush();
                fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            SimpleFTP ftp = new SimpleFTP();

            try {
                ftp.connect("153.92.6.4", 21, getString(R.string.ftp_username), getString(R.string.ftp_password));
                ftp.bin();
                if (ftp.stor(file)) {
                    // TODO: 8/16/2017 Close progress bar on main thread
                    progressDialog.dismiss();
                }
                ftp.disconnect();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialog = ProgressDialog.show(mContext, "Uploading image", null, true);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
        }

        String getFilename() {
            return filename;
        }
    }

}