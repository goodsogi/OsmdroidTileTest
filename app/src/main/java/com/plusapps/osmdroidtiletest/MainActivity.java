package com.plusapps.osmdroidtiletest;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.RelativeLayout;
import android.widget.Toast;

import org.osmdroid.api.IMapController;
import org.osmdroid.config.Configuration;
import org.osmdroid.tileprovider.MapTileProviderBasic;
import org.osmdroid.tileprovider.tilesource.FileBasedTileSource;
import org.osmdroid.tileprovider.tilesource.XYTileSource;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.TilesOverlay;


//***************************************************************************************
// 1. 오프라인 지도 생성
// 2. 오프라인 지도 rendering
// 3. location 처리
// 4. 권한 처리
// 5. 라이프사이클
//***************************************************************************************
public class MainActivity extends AppCompatActivity implements PlusLocationListener {

    //***************************************************************************************
    //
    // 오프라인 지도 생성
    //
    //***************************************************************************************


    private static final int MINZOOM = 1;
    private static final int MAXZOOM = 30;
    private MapView offlineMap = null;


    private void createOfflineMap() {


        Configuration.getInstance().load(getApplicationContext(),
                PreferenceManager.getDefaultSharedPreferences(getApplicationContext()));

        final RelativeLayout mapContainer = (RelativeLayout) findViewById(R.id.offline_map_container);


        XYTileSource tileSource = new XYTileSource(
                "4uMaps",
                MINZOOM,
                MAXZOOM,
                256,
                ".png",
                new String[]{}
        );
        offlineMap = new MapView(this);
        offlineMap.setUseDataConnection(false);



        offlineMap.setTileSource(tileSource);






        mapContainer.addView(offlineMap, 0);



    }



    //***************************************************************************************
    //
    // 오프라인 지도 rendering
    //
    //***************************************************************************************
    private void showCurrentLocation(Location location) {

        if (offlineMap == null) {
            return;
        }

        GeoPoint startPoint = new GeoPoint(location.getLatitude(), location.getLongitude());

        //TODO mOfflineMap이 null인 경우가 가끔 발생. 처리하세요.
//        if(mOfflineMap == null) {
//            return;
//        }

        IMapController mapController = offlineMap.getController();
        mapController.setZoom(10);
        mapController.setCenter(startPoint);
    }


    //***************************************************************************************
    //
    // location 처리
    //
    //***************************************************************************************

    void initLocationFinder() {
        //Fused location provider를 사용하는 경우
        PlusFusedLocationFinder fusedLocationFinder = new PlusFusedLocationFinder(this, this);
        fusedLocationFinder.getIndoorLocation();
    }

    @Override
    public void onLocationCatched(Location location) {

    }

    @Override
    public void onFirstLocationCatched(Location location) {
        showCurrentLocation(location);
    }




    //***************************************************************************************
    //
    // 권한 처리
    //
    //***************************************************************************************


    private static final int REQUEST_CODE_REQUEST_APP_PERMISSIONS = 213;

    private void checkAppPermissions() {

        if ( ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
        ) != PackageManager.PERMISSION_GRANTED


        ) {
            //사용자가 권한 설정을 거부했는지 체크
            //거부한 경우 shouldShowRequestPermissionRationale는 true 반환
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            ) && ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION

            ) && ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            )
            ) {

                Toast.makeText(
                        this, "앱 실행을 위해서는 모든 권한을 설정해야 합니다",
                        Toast.LENGTH_LONG
                ).show();
                finish();

            } else {
                ActivityCompat.requestPermissions(
                        this,
                        new String[] {
                                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION
                        },
                        REQUEST_CODE_REQUEST_APP_PERMISSIONS
                );

            }
        } else {

            initLocationFinder();
            createOfflineMap();
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_CODE_REQUEST_APP_PERMISSIONS && grantResults.length > 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED
                && grantResults[1] == PackageManager.PERMISSION_GRANTED && grantResults[2] == PackageManager.PERMISSION_GRANTED
               ) {
            initLocationFinder();
            createOfflineMap();
        }

    }







    //***************************************************************************************
    //
    // 라이프사이클
    //
    //***************************************************************************************

    @Override
    public void onCreate(Bundle savedInstance){
        super.onCreate(savedInstance);

        //TODO check permissions

        setContentView(R.layout.activity_main);
        checkAppPermissions();


    }




    @Override
    public void onResume(){
        super.onResume();

        if (offlineMap !=null) {
            offlineMap.onResume();
        }
    }

    @Override
    public void onPause(){
        super.onPause();

        if (offlineMap !=null) {
            offlineMap.onPause();
        }
    }




}
