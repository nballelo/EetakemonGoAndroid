package edu.upc.eetac.dsa.eetakemongoandroid.Activities;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import edu.upc.eetac.dsa.eetakemongoandroid.GameClient.ClientRequest;
import edu.upc.eetac.dsa.eetakemongoandroid.GameClient.StateFlowGame;
import edu.upc.eetac.dsa.eetakemongoandroid.JSONservice;
import edu.upc.eetac.dsa.eetakemongoandroid.Model.Eetakemon;
import edu.upc.eetac.dsa.eetakemongoandroid.Model.Markers;
import edu.upc.eetac.dsa.eetakemongoandroid.Model.User;
import edu.upc.eetac.dsa.eetakemongoandroid.R;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

//
//

public class Principal extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, OnMapReadyCallback, GoogleMap.OnMarkerClickListener {
    private GoogleMap mMap;
    ClientRequest client;
    private Marker marker;
    private String token;
    private List<Markers> markers;
    private User user;
    private Thread threadListenigGame;
    double lat = 41.275603;
    double lon = 1.986584;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_principal);
        user = (User) getIntent().getSerializableExtra("User");
        token = getIntent().getStringExtra("Token");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        createConnectionRequest();

    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {

        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.principal, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == R.id.capturados) {
            Intent intent = new Intent(Principal.this, EetakemonsCatched.class);
            intent.putExtra("User", (Serializable) user);
            startActivity(intent);
        } else if (id == R.id.eetakedex) {
            Intent intent = new Intent(Principal.this, Eetakedex.class);
            intent.putExtra("User", (Serializable) user);
            intent.putExtra("Token", token);
            startActivity(intent);
        } else if (id == R.id.pelea) {
            startGame();
        } else if (id == R.id.settings) {
            Toast.makeText(this, "Esta opcion no esta disponible aun", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.exit) {
            Toast.makeText(this, "Hasta otra", Toast.LENGTH_SHORT).show();
            finish();
        }
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        miUbicacion();
        mMap.setOnMarkerClickListener(this);
    }

    private void datos() {

        //View v=View.inflate()
        //Picasso.with(this).load(JSONservice.URL+user.getImage()).into(mifoto);
        setContentView(R.layout.nav_header_principal);
        ImageView mifoto = (ImageView) findViewById(R.id.mifoto);
        TextView nombre = (TextView) findViewById(R.id.nombre);
        nombre.setText(user.getName());
    }

    private void miUbicacion() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            Toast.makeText(this, "No tienes permisos para ubicarte", Toast.LENGTH_SHORT).show();
            /*ActivityCompat.requestPermissions(this,
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_LOCATION);*/
            return;
        }
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        actualizarUbicacion(location);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 10000, 0, locationListener);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
    }

    ;

    private void agregarMarcador(double lat, double lon) {
        LatLng coordenadas = new LatLng(lat, lon);
        CameraUpdate miUbicacion = CameraUpdateFactory.newLatLngZoom(coordenadas, 18);
        if (marker != null) marker.remove();
        marker = mMap.addMarker(new MarkerOptions().position(coordenadas).title("mi posicion").icon(BitmapDescriptorFactory.fromResource(R.drawable.personaje)));
        mMap.animateCamera(miUbicacion);
    }

    private void actualizarUbicacion(Location location) {
        if (location != null) {
            lat = location.getLatitude();
            lon = location.getLongitude();
            agregarMarcadores();
        } else {
            Toast.makeText(this, "No se ha encontrado la ubicación", Toast.LENGTH_SHORT).show();
        }

    }

    private void agregarMarcadores() {
        Retrofit retrofit = new Retrofit.Builder().baseUrl(JSONservice.URL).addConverterFactory(GsonConverterFactory.create()).build();
        JSONservice service = retrofit.create(JSONservice.class);
        mMap.clear();
        agregarMarcador(lat, lon);
        Markers mar = new Markers();
        mar.setLat(lat);
        mar.setLng(lon);
        Call<List<Markers>> callMarkers = service.miPos(mar, token);
        callMarkers.enqueue(new Callback<List<Markers>>() {
            @Override
            public void onResponse(Call<List<Markers>> call, Response<List<Markers>> response) {
                markers = response.body();
                for (int i = 0; i < markers.size(); i++) {
                    /*ImageView imageview=new ImageView(Principal.this);
                    Picasso.with(Principal.this).load(JSONservice.URL+markers.get(i).getEetakemon().getImage()).into(imageview);
                    BitmapDrawable drawable = (BitmapDrawable) imageview.getDrawable();
                    Bitmap bitmap = drawable.getBitmap();*/
                    Marker marker1 = mMap.addMarker(new MarkerOptions().position(new LatLng(markers.get(i).getLat(), markers.get(i).getLng())).title(markers.get(i).getEetakemon().getName()));
                }
            }

            @Override
            public void onFailure(Call<List<Markers>> call, Throwable t) {

            }
        });
    }

    LocationListener locationListener = new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            actualizarUbicacion(location);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    @Override
    protected void onActivityResult(int requestCode, final int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == 50) {
            Intent intent1 = new Intent(getApplicationContext(), CatchEetakemon.class);
            Eetakemon suEetakemon = (Eetakemon) intent.getSerializableExtra("Eetakemon");
            Eetakemon miEetakemon = (Eetakemon) intent.getSerializableExtra("miEetakemon");
            intent1.putExtra("Eetakemon", (Serializable) suEetakemon);
            intent1.putExtra("miEetakemon", (Serializable) miEetakemon);
            intent1.putExtra("User", (Serializable) user);
            startActivityForResult(intent1, 51);

        } else if (requestCode == 51) {
            if (resultCode == RESULT_OK) {
                Retrofit retrofit = new Retrofit.Builder().baseUrl(JSONservice.URL).addConverterFactory(GsonConverterFactory.create()).build();
                JSONservice service = retrofit.create(JSONservice.class);
                Eetakemon eetakemon = (Eetakemon) intent.getSerializableExtra("Eetakemon");
                List<Eetakemon> list = new ArrayList<>();
                list.add(eetakemon);
                User user1 = new User();
                user1.setEetakemons(list);
                user1.setUsername(user.getUsername());
                Call<User> addEetakemon = service.addAEetakemonsToUser(user);
                addEetakemon.enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Call<User> call, Response<User> response) {
                        if (response.code() == 200)
                            user.setEetakemons(response.body().getEetakemons());
                        else
                            Toast.makeText(Principal.this, "No se pudo añadir", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(Call<User> call, Throwable t) {
                        Toast.makeText(Principal.this, "No conectar al servidor", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        LatLng mi = this.marker.getPosition();
        LatLng click = marker.getPosition();
        if ((mi.longitude != click.longitude) && (mi.latitude != click.latitude)) {
            Eetakemon eetakemon = new Eetakemon();
            for (int i = 0; i < markers.size(); i++) {
                if (markers.get(i).getEetakemon().getName().equals(marker.getTitle()))
                    eetakemon = markers.get(i).getEetakemon();
            }
            Intent intent = new Intent(getApplicationContext(), SelectEetackemon.class);
            intent.putExtra("Eetakemon", (Serializable) eetakemon);
            intent.putExtra("User", (Serializable) user);
            startActivityForResult(intent, 50);
            marker.remove();
        }
        return false;
    }

    private void createConnectionRequest() {
        client = client == null ? new ClientRequest(user.getUsername()) : client;
        threadListenigGame = new Thread(new Runnable() {
            public void run() {
                try {
                    String message = client.createConnectionRequest();
                    Intent startGame = new Intent(Principal.this, ClientRequest.class);
                    startGame.putExtra("Value", StateFlowGame.AcceptInvitation.getValue());
                    startActivityForResult(startGame, StateFlowGame.AcceptInvitation.getValue());
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                }
            }
        });
        threadListenigGame.start();
    }

    private void startGame() {
        threadListenigGame.interrupt();
        Intent intent = new Intent(Principal.this, ClientRequest.class);
        intent.putExtra("Value", StateFlowGame.SelectUser.getValue());
        startActivityForResult(intent, StateFlowGame.SelectUser.getValue());
    }
}
