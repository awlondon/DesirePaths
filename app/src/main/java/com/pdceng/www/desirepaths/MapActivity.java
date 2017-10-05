package com.pdceng.www.desirepaths;

import android.Manifest;
import android.animation.Animator;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.TranslateAnimation;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
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
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.Dash;
import com.google.android.gms.maps.model.Dot;
import com.google.android.gms.maps.model.Gap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PatternItem;
import com.google.android.gms.maps.model.PolylineOptions;
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
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ScheduledExecutorService;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener, AfterGetAll, CommentsAdapterInterface
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
    //    private final LatLng startingPoint = new LatLng(61.2185, -149.8996);
    private final LatLng startingPoint = new LatLng(64.836888, -147.773023);
    private final List<LatLng> lls = new ArrayList<>();
    private final Context mContext = this;
    private final DatabaseHelper dh = new DatabaseHelper(this);
    private final List<CameraPosition> previousCameraPositions = new ArrayList<>();
    public GoogleMap mMap;
    public FusedLocationProviderClient mFusedLocationClient;
    ScheduledExecutorService executorService;
    Runnable getAllFromSQL;
    float translationY = 0;
    boolean noPicture = false;
    FragmentManager fragmentManager;
    FragmentTransaction fragmentTransaction;
    LatLng mCurrLatLng;
    Universals universals;
    private CommentsAdapter adapter;
    private ListView listView;
    private RelativeLayout topView;
    private SupportMapFragment mapFragment;
    private ClusterManager<MyItem> mClusterManager;
    private boolean mLocationPermissionGranted;
    private Location mDefaultLocation;
    private Location mLastKnownLocation;
    private CameraPosition mCameraPosition;
    private GoogleApiClient mGoogleApiClient;
    private boolean mCameraPermissionGranted;
    private boolean mStoragePermissionGranted;
    private ImageView mImageView;
    private CameraPosition tempCameraPosition;
    private FloatingActionButton prevMapFab;
    private boolean updateCameraMemory = true;
    private boolean permissionRequested = false;
    private CallbackManager callbackManager;
    private Uri imageUri;
    private int ivHeightSetting = 700;

    public MapActivity() {
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        topView = (RelativeLayout) findViewById(R.id.topView);
        universals = new Universals(this);
        setClickListeners();
        bringUpMap();
        checkPermission();
    }

    private void chooseProject(final GoogleMap googleMap) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Choose a project");
        final String[] projects = dh.getAllProjectNames();
        builder.setItems(projects, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Universals.PROJECT = dh.getProjectObject(projects[which]);
                dialog.dismiss();
                establishMap(googleMap);
            }
        });
        builder.setCancelable(false);
        builder.create();
        builder.show();
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
                Universals.setChooseLocation(true);
                addPublicInputEntry();
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
                if (Universals.isAnon) {
                    Toast.makeText(mContext, "You cannot rate public input anonymously", Toast.LENGTH_SHORT).show();
                } else {
                    startCardsActivity(v);
                }
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
        findViewById(R.id.cbIdea).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterMarkers(v);
            }
        });
        findViewById(R.id.cbComment).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterMarkers(v);
            }
        });
        findViewById(R.id.cbWarning).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterMarkers(v);
            }
        });
        findViewById(R.id.help).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                runTutorial();
            }
        });
        findViewById(R.id.info).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProjectInfo();
            }
        });
    }

    private void showProjectInfo() {
        final ViewGroup viewGroup = null;
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View view = LayoutInflater.from(getApplicationContext()).inflate(R.layout.project_info, viewGroup);
        builder.setView(view);
        TextView tvName = (TextView) view.findViewById(R.id.name);
        TextView tvLocation = (TextView) view.findViewById(R.id.location);
        TextView tvDescription = (TextView) view.findViewById(R.id.description);
        Button bWebsite = (Button) view.findViewById(R.id.website);

        final Project project = Universals.PROJECT;
        tvName.setText(project.getName());
        tvLocation.setText(project.getLocation());
        tvDescription.setText(project.getDescription());

        builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        bWebsite.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = Uri.parse(project.getWebsite());
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);
            }
        });

        builder.show();
    }

    private void bringUpMap() {
        if (Universals.USER_NAME != null) {
            Toast.makeText(mContext, "Welcome, " + Universals.USER_NAME.split(" ")[0], Toast.LENGTH_SHORT).show();
        }

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        mapFragment = (SupportMapFragment) getSupportFragmentManager()
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
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(63.976, -151.4777), 4));
        chooseProject(googleMap);
    }

    private void runTutorial() {
        final String title = "Instructions";
        if (Universals.isAnon) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(title);
            builder.setMessage("As an anonymous user, you can view public input, but you cannot rate or comment. Enjoy!");
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        } else {
            final TranslateAnimation mAnimation = new TranslateAnimation(0, 0, 0, 30);
            mAnimation.setDuration(500);
            mAnimation.setRepeatCount(-1);
            mAnimation.setRepeatMode(Animation.REVERSE);
            mAnimation.setInterpolator(new LinearInterpolator());

            final View addArrow = findViewById(R.id.addArrow);
            final View thumbsArrow = findViewById(R.id.thumbsArrow);
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(title);
            builder.setMessage("Add public input using a photo and/or an idea, comment or warning." + " (1/3)");
            addArrow.setVisibility(View.VISIBLE);
            addArrow.setAnimation(mAnimation);
            builder.setCancelable(false);
            builder.setPositiveButton("Next", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    addArrow.setAnimation(null);
                    addArrow.setVisibility(View.GONE);
                    AlertDialog.Builder builder1 = new AlertDialog.Builder(mContext);
                    builder1.setCancelable(false);
                    builder1.setTitle(title);
                    builder1.setMessage("Click on the map markers to view others' public input, agree or disagree, and add comments." + " (2/3)");
                    builder1.setPositiveButton("Next", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            AlertDialog.Builder builder2 = new AlertDialog.Builder(mContext);
                            builder2.setCancelable(false);
                            builder2.setTitle(title);
                            builder2.setMessage("Click the 'thumbs' menu button to quickly flip through public input, and agree or disagree with them. Press OK to get started!" + " (3/3)");
                            thumbsArrow.setVisibility(View.VISIBLE);
                            thumbsArrow.setAnimation(mAnimation);
                            builder2.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
//                                    ToggleButton tb = (ToggleButton) findViewById(R.id.addInputToggle);
//                                    tb.setChecked(true);
//                                    toggleOptions(tb);
                                    thumbsArrow.setAnimation(null);
                                    thumbsArrow.setVisibility(View.INVISIBLE);
                                    dialog.dismiss();
                                }
                            });
                            builder2.show();
                        }
                    });
                    builder1.show();
                }
            });
            builder.show();
        }
    }

    private void establishMap(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Universals.PROJECT.getLatLng(), Universals.PROJECT.getZoom()));
        mMap.setOnInfoWindowClickListener(this);

        //Add geometry to Fairbanks
        LatLng[] linePoints = new LatLng[]{
                new LatLng(64.834832, -147.707030),
                new LatLng(64.835818, -147.712352),
                new LatLng(64.836438, -147.721879),
                new LatLng(64.836913, -147.726685),
                new LatLng(64.836840, -147.830369),
                new LatLng(64.836475, -147.834918)
        };

//        LatLng[] polygonPoints = new LatLng[]{
//                new LatLng(64.839832, -147.707030),
//                new LatLng(64.839818, -147.712352),
//                new LatLng(64.839438, -147.721879),
//                new LatLng(64.839913, -147.726685),
//                new LatLng(64.839840, -147.830369),
//                new LatLng(64.839475, -147.834918),
//                new LatLng(64.830475, -147.834918),
//                new LatLng(64.830840, -147.830369),
//                new LatLng(64.830913, -147.726685),
//                new LatLng(64.830438, -147.721879),
//                new LatLng(64.830818, -147.712352),
//                new LatLng(64.830832, -147.707030)
//        };

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

//        mMap.addPolygon((new PolygonOptions())
//                .add(polygonPoints).strokeColor(Color.DKGRAY).strokeWidth(3f).geodesic(true));

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

        mClusterManager.setOnClusterItemClickListener(new ClusterManager.OnClusterItemClickListener<MyItem>() {
            @Override
            public boolean onClusterItemClick(final MyItem myItem) {
                openPublicInputView(myItem);
                return true;
            }

        });

        Universals.mapActivity = this;

        setUpCluster();

//        runTutorial();
    }

    private void unCheckToggleButtons() {
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
        Toast.makeText(MapActivity.this, marker.getPosition().toString(), Toast.LENGTH_SHORT).show();
    }

    private void openPublicInputView(final MyItem myItem) {
        Intent intent = new Intent(this, PublicInputViewActivity.class);
        intent.putExtras(myItem.getBundle());
        startActivity(intent);
    }

    @Override
    public void setCommentsAdapter(String id) {
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

    public void setUpCluster() {
        Algorithm<MyItem> clusterManagerAlgorithm = new NonHierarchicalDistanceBasedAlgorithm<>();
        mClusterManager.setAlgorithm(clusterManagerAlgorithm);
        mClusterManager.setRenderer(new OwnIconRendered(this, mMap, mClusterManager));
        mMap.setOnCameraIdleListener(mClusterManager);
        mMap.setOnMarkerClickListener(mClusterManager);
        addItems(new String[]{null});

        Collection<MyItem> markers = clusterManagerAlgorithm.getItems();

    }

    void addItems(String... strings) {
        Bundle[] bundles = dh.getAllInTable(new PIEntryTable());
        List<Bundle> bundleList = new ArrayList<>();
        Collections.addAll(bundleList, bundles);
        System.out.println("Universals.PROJECT.getId(): " + Universals.PROJECT.getId());
        List<Bundle> removeList = new ArrayList<>();
        for (Bundle bundle : bundleList) {
            if (Integer.valueOf(bundle.getString(PIEntryTable.PROJECT_ID)) != Universals.PROJECT.getId()) {
                removeList.add(bundle);
            }
        }
        bundleList.removeAll(removeList);
        bundles = bundleList.toArray(new Bundle[bundleList.size()]);

        if (bundles.length < 1 && strings != null && strings[0] == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Welcome!");
            builder.setMessage("There is no public input yet.\nBe the first!");
            builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    runTutorial();
                }
            }).show();
        }

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

    private void zoomOut(View view) {
        mMap.moveCamera(CameraUpdateFactory.zoomOut());
    }

    private void zoomIn(View view) {
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

        List<LatLng> list;
        list = lls;

        HeatmapTileProvider mProvider = new HeatmapTileProvider.Builder()
                .data(list)
                .gradient(gradient)
                .build();
        TileOverlay mOverlay = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(mProvider));
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

    private void dispatchTakePictureIntent(View v) {
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

    private void pickImageFromLibrary(View v) {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);
    }

    int checkPermission(String permission, boolean bool) {
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
        }
    }

    private void getLocation(View v) {
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

    private void moveToPreviousCameraPosition(View v) {
        updateCameraMemory = false;
        previousCameraPositions.remove(previousCameraPositions.size() - 1);
        mMap.moveCamera(CameraUpdateFactory.newCameraPosition(previousCameraPositions.get(previousCameraPositions.size() - 1)));
        toggleReturnFab();
    }

    private void toggleReturnFab() {
        if (hasPreviousPosition()) {
            prevMapFab.setVisibility(View.VISIBLE);
            /*if (!Universals.prevMapTutorialWasShown) {
                final View arrow = findViewById(R.id.arrow);
                arrow.setVisibility(View.VISIBLE);
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage("Click the blue button to go back to the last map extent.");
                builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        arrow.setVisibility(View.GONE);
                        Universals.prevMapTutorialWasShown = true;
                        dialog.dismiss();
                    }
                });
                builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        arrow.setVisibility(View.GONE);
                        Universals.prevMapTutorialWasShown = true;
                        dialog.dismiss();
                    }
                });
                builder.show();
            }*/
        } else {
            prevMapFab.setVisibility(View.INVISIBLE);
        }
    }

    private boolean hasPreviousPosition() {
        return previousCameraPositions.size() > 1;
    }

    private void toggleOptions(View v) {
        LinearLayout ll = null;
        System.out.println("toggle options was called...");
        if (v instanceof ToggleButton) {
            System.out.println("view is instance of toggle button...");
            switch (v.getId()) {
                case R.id.filterToggle:
                    ll = (LinearLayout) findViewById(R.id.llFilter);
                    break;
                case R.id.addInputToggle:
                    if (Universals.isAnon) {
                        Toast.makeText(mContext, "You cannot add public input anonymously", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    ll = (LinearLayout) findViewById(R.id.llAddType);
            }
            int animDur = 200;
            if (((ToggleButton) v).isChecked()) {

                assert ll != null;
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
                assert ll != null;
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

    private void filterMarkers(View v) {
        if (v instanceof CheckBox) {
            CheckBox cbIdea = (CheckBox) findViewById(R.id.cbIdea);
            CheckBox cbComment = (CheckBox) findViewById(R.id.cbComment);
            CheckBox cbWarning = (CheckBox) findViewById(R.id.cbWarning);

            CheckBox[] cbs = new CheckBox[]{cbIdea, cbComment, cbWarning};
            List<String> strings = new ArrayList<>();
            for (CheckBox cb : cbs) {
                if (cb.isChecked()) {
                    strings.add(cb.getText().toString().toLowerCase().substring(0, cb.getText().toString().length() - 1));
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

    private void startCardsActivity(View v) {
        Intent intent = new Intent(this, CardsActivity.class);
        startActivity(intent);
    }

    private void addPIEntriesToDatabase() {
        List<PublicInput> publicInputList = CardsUtils.loadPublicInputs(mContext);

        assert publicInputList != null;
        for (PublicInput publicInput : publicInputList) {
            Bundle bundle = new Bundle();
            bundle.putString(PIEntryTable.URL, publicInput.getUrl());
            bundle.putString(PIEntryTable.LATITUDE, String.valueOf(publicInput.getLatitude()));
            bundle.putString(PIEntryTable.LONGITUDE, String.valueOf(publicInput.getLongitude()));
            bundle.putString(PIEntryTable.SENTIMENT, publicInput.getSentiment());
            bundle.putString(PIEntryTable.TITLE, publicInput.getTitle());
            bundle.putString(PIEntryTable.SNIPPET, publicInput.getSnippet());
            bundle.putString(PIEntryTable.SOCIAL_MEDIA_ID, publicInput.getSocialMediaId());
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

    private void addPublicInputEntry() {
        ((ToggleButton) findViewById(R.id.addInputToggle)).setChecked(false);
        toggleOptions(findViewById(R.id.addInputToggle));
        Intent intent = new Intent(this, PublicInputAddActivity.class);
        startActivity(intent);
    }

    void chooseLocationExec(boolean knownLoc, final String title, final String snippet, final String sentiment, final Bitmap finalImageBitmap, final PublicInputAddActivity.SendImageFTP finalSendImageFTP, Activity activityRef) {
        activityRef.finish();
        String message = "Touch map to move marker or press and hold-down on marker to drag into position, then click \'OK\'";
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        final Button okButton = new Button(mContext);
        okButton.setText(R.string.OK);
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.addRule(RelativeLayout.ABOVE, findViewById(R.id.menu).getId());

        okButton.setLayoutParams(layoutParams);
        topView.addView(okButton);

        if (!knownLoc ||
                mCurrLatLng == null ||
                Math.abs(mCurrLatLng.latitude - Universals.PROJECT.getLatLng().latitude) > 0.3f ||
                Math.abs(mCurrLatLng.longitude - Universals.PROJECT.getLatLng().longitude) > 0.3f) {

            mCurrLatLng = mMap.getCameraPosition().target;

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(mCurrLatLng, 15));
            Toast.makeText(this, "Your current location is unknown or too far from the project area.", Toast.LENGTH_SHORT).show();
        }

        BitmapDescriptor mIcon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
        switch (sentiment) {
            case Universals.IDEA:
                mIcon = BitmapDescriptorFactory.fromResource(R.drawable.idea_icon_shadow);
                break;
            case Universals.COMMENT:
                mIcon = BitmapDescriptorFactory.fromResource(R.drawable.comment_icon_shadow);
                break;
            case Universals.WARNING:
                mIcon = BitmapDescriptorFactory.fromResource(R.drawable.attention_icon_shadow);
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
                Universals.bitmapBeingProcessed = null;

                Bundle bundle = new Bundle();
                bundle.putString(PIEntryTable.URL, finalImageBitmap != null ? finalSendImageFTP.getFilename() : "");
                bundle.putString(PIEntryTable.LATITUDE, String.valueOf(mCurrLatLng.latitude));
                bundle.putString(PIEntryTable.LONGITUDE, String.valueOf(mCurrLatLng.longitude));
                bundle.putString(PIEntryTable.SENTIMENT, sentiment);
                bundle.putString(PIEntryTable.TITLE, title);
                bundle.putString(PIEntryTable.SNIPPET, snippet);
                bundle.putString(PIEntryTable.SOCIAL_MEDIA_ID, Universals.SOCIAL_MEDIA_ID);
                bundle.putString(PIEntryTable.PROJECT_ID, String.valueOf(Universals.PROJECT.getId()));
                bundle.putString(PIEntryTable.TIMESTAMP, new Timestamp(System.currentTimeMillis()).toString());
                dh.insert(bundle, new PIEntryTable());
                Toast.makeText(mContext, "Content added!", Toast.LENGTH_SHORT).show();
                addItems(new String[]{null});
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
            Universals.sendBitmapForProcessing(imageBitmap);
            Universals.setChooseLocation(chooseLocation);

            addPublicInputEntry();
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

    private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
        final ImageView bmImage;
        final ProgressBar progressBar;

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
                if (!Universals.isBitmapInMemoryCache(urlDisplay)) {
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
                    Universals.addBitmapToMemoryCache(urlDisplay, bitmap);
                }
                bitmap = Universals.getBitmapFromMemoryCache(urlDisplay);
            } else {
                if (!Universals.isBitmapInMemoryCache(urlDisplay)) {
                    Universals.addBitmapToMemoryCache(urlDisplay, Universals.getBitmapFromURL(urlDisplay, 200, 200));
                }
                bitmap = Universals.getBitmapFromMemoryCache(urlDisplay);
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
        final Bitmap bitmap;
        final Context context;
        final String filename;
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