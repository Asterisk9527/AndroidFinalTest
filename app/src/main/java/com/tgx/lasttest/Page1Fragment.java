package com.tgx.lasttest;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import android.widget.Toast;
import com.baidu.location.*;
import com.baidu.mapapi.SDKInitializer;
import com.baidu.mapapi.map.*;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.*;
import com.baidu.mapapi.search.sug.OnGetSuggestionResultListener;
import com.baidu.mapapi.search.sug.SuggestionResult;
import com.baidu.mapapi.search.sug.SuggestionSearch;
import com.baidu.mapapi.search.sug.SuggestionSearchOption;

import java.io.File;
import java.util.List;
import java.util.Objects;

/***
 *
 * GPS传感器应用
 * 百度地图SDK绘制轨迹
 */

public class Page1Fragment extends Fragment {
    private MapView mMapView;
    private BaiduMap mBaiduMap;
    private LocationClient mLocationClient;
    private LatLng latLng;
    private boolean isFirstLoc = true;//是否是首次定位
    public BDLocationListener myListener = new MyLocationListener();
    private MyLocationData locData;
    private double mLatitude;
    private double mLongitude;
    private AutoCompleteTextView mEditAddress;
    private List<String> stringlist = new ArrayList<>();
    private List<String> stringlist2 = new ArrayList<>();

    private Button bt;
    private Button button;
    private Button buttons;


    List<LatLng> points;


    private View view;
    private Context context;

    private BitmapDescriptor mIconLocation;
    private MyOrientationListener mMyOrientationListener;
    private float mCurrentX;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        SDKInitializer.initialize(getActivity().getApplicationContext());
        view = inflater.inflate(R.layout.page1 , container, false);
        return view;
    }

    @Override
    public void onStart() {
        Log.d("homePage", "第一个view");
        super.onStart();

        //类成员变量的实例化
        mMapView = getView().findViewById(R.id.bmapView);

        bt = getView().findViewById(R.id.bt);
        bt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MapStatusUpdate mapStatusUpdate = MapStatusUpdateFactory.newLatLng(latLng);
                mBaiduMap.animateMapStatus(mapStatusUpdate);
            }
        });
        button = getView().findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_SATELLITE);
            }
        });
        buttons = getView().findViewById(R.id.buttons);
        buttons.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
            }
        });
        initMap();

        points = new ArrayList<>();
        mMapView.showZoomControls(false);

    }

    Page1Fragment(Context context) {
        this.context = context;
    }
    /**
     * 定位
     */
    private void initMap() {

        //获取地图控件引用
        mBaiduMap = mMapView.getMap();
        //普通地图
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        mBaiduMap.setMyLocationEnabled(true);

        //默认显示普通地图
        mBaiduMap.setMapType(BaiduMap.MAP_TYPE_NORMAL);
        //开启交通图
        //mBaiduMap.setTrafficEnabled(true);
        //开启热力图
        //mBaiduMap.setBaiduHeatMapEnabled(true);
        // 开启定位图层
        mBaiduMap.setMyLocationEnabled(true);
        LocationClient.setAgreePrivacy(true);
        try {
            mLocationClient = new LocationClient(getContext());     //声明LocationClient类
        } catch (Exception e) {
            e.printStackTrace();
        }
        if(mLocationClient != null) {
            //配置定位SDK参数
            initLocation();
            mLocationClient.registerLocationListener(myListener);    //注册监听函数
            //开启定位
            mLocationClient.start();
            mMyOrientationListener.start();
            //图片点击事件，回到定位点
            mLocationClient.requestLocation();
            initPermission();

        }



    }

    private void initPermission() {
        String permissions[] = {
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_WIFI_STATE,
                Manifest.permission.CHANGE_WIFI_STATE,
                Manifest.permission.READ_PHONE_STATE,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.INTERNET,
        };

        ArrayList<String> toApplyList = new ArrayList<String>();

        for (String perm : permissions) {
            if (PackageManager.PERMISSION_GRANTED != ContextCompat.checkSelfPermission(Objects.requireNonNull(getContext()), perm)) {
                toApplyList.add(perm);
                //进入到这里代表没有权限.
            }
        }
        String tmpList[] = new String[toApplyList.size()];
        if (!toApplyList.isEmpty()) {
            ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), toApplyList.toArray(tmpList), 123);
        }
    }
    /**
     * 配置定位SDK参数
     */
    private void initLocation() {
        LocationClientOption option = new LocationClientOption();
        option.setCoorType("bd09ll");//可选，coorType - 取值有3个： 返回国测局经纬度坐标系：gcj02 返回百度墨卡托坐标系 ：bd09 返回百度经纬度坐标系 ：bd09ll
        Log.e("获取地址信息设置", option.getAddrType());//获取地址信息设置
        option.setIsNeedAddress(true);//可选，设置是否需要地址信息，默认不需要
        option.setOpenGps(true); // 是否打开gps进行定位
        option.setLocationNotify(true);//可选，默认false，设置是否当GPS有效时按照1S/1次频率输出GPS结果
        option.setScanSpan(100);//可选，设置的扫描间隔，单位是毫秒，设置发起定位请求的间隔需要大于等于1000ms才是有效的
        Log.e("获取设置的Prod字段值", option.getProdName());
        option.setOpenAutoNotifyMode(3000,1, LocationClientOption.LOC_SENSITIVITY_HIGHT);
        option.setLocationMode(LocationClientOption.LocationMode.Hight_Accuracy);//可选，默认高精度，设置定位模式，高精度，低功耗，仅设备
        option.setNeedDeviceDirect(true);//在网络定位时，是否需要设备方向- true:需要 ; false:不需要。默认为false
        option.SetIgnoreCacheException(false);//可选，默认false，设置是否收集CRASH信息，默认收集
        option.setIgnoreKillProcess(false);//可选，默认true，定位SDK内部是一个SERVICE，并放到了独立进程，设置是否在stop的时候杀死这个进程，默认不杀死
        option.setIsNeedLocationDescribe(true);//可选，默认false，设置是否需要位置语义化结果，可以在BDLocation
        // .getLocationDescribe里得到，结果类似于“在北京天安门附近”
        option.setIsNeedLocationPoiList(true);//可选，默认false，设置是否需要POI结果，可以在BDLocation.getPoiList里得到
        option.setEnableSimulateGps(false);//可选，默认false，设置是否需要过滤GPS仿真结果，默认需要
        mLocationClient.setLocOption(option);


        MyLocationConfiguration config = new
                MyLocationConfiguration(
                MyLocationConfiguration.LocationMode.NORMAL, true, mIconLocation);
//
        mBaiduMap.setMyLocationConfiguration(config);

        initOrientation();
    }


    //实现BDLocationListener接口,BDLocationListener为结果监听接口，异步获取定位结果
    public class MyLocationListener implements BDLocationListener {
        @Override
        public void onReceiveLocation(final BDLocation location) {
            latLng = new LatLng(location.getLatitude(), location.getLongitude());
            // 构造定位数据
            mLatitude = location.getLatitude();
            mLongitude = location.getLongitude();
            locData = new MyLocationData.Builder()
                    .accuracy(location.getRadius())
                    .direction(mCurrentX)// 此处设置开发者获取到的方向信息，顺时针0-360
                    .latitude(mLatitude)
                    .longitude(mLongitude).build();
            mBaiduMap.setMyLocationData(locData);// 设置定位数据

            setMarker();
            setUserMapCenter();
            Log.v("baiduMap", "onReceiveLocation: lat : " + mLatitude + " lon : " + mLongitude);
            Toast.makeText(getContext(), "定位已经发生了变化，当前维度:" + mLatitude + "当前经度：" + mLongitude, Toast.LENGTH_SHORT).show();

            if (isFirstLoc) {
                isFirstLoc = false;
                LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
                MapStatus.Builder builder = new MapStatus.Builder();
                builder.target(latLng).zoom(18.0f);
                mBaiduMap.animateMapStatus(MapStatusUpdateFactory.newMapStatus(builder.build()));

                if (location.getLocType() == BDLocation.TypeGpsLocation) {// GPS定位结果
                    Toast.makeText(getContext(), location.getAddrStr(), Toast.LENGTH_SHORT).show();
                } else if (location.getLocType() == BDLocation.TypeNetWorkLocation) {// 网络定位结果
                    Toast.makeText(getContext(), location.getAddrStr(), Toast.LENGTH_SHORT).show();
                } else if (location.getLocType() == BDLocation.TypeOffLineLocation) {// 离线定位结果
                    Toast.makeText(getContext(), location.getAddrStr(), Toast.LENGTH_SHORT).show();
                } else if (location.getLocType() == BDLocation.TypeServerError) {//服务器错误
                    Toast.makeText(getContext(), "服务器错误，请检查", Toast.LENGTH_SHORT).show();
                } else if (location.getLocType() == BDLocation.TypeNetWorkException) {//网络错误
                    Toast.makeText(getContext(), "网络错误，请检查", Toast.LENGTH_SHORT).show();
                } else if (location.getLocType() == BDLocation.TypeCriteriaException) {//手机模式错误
                    Toast.makeText(getContext(), "手机模式错误，请检查是否飞行", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    private void setMarker() {
        Log.v("map", "setMarker : lat : " + mLatitude + " lon : " + mLongitude);
        //定义Maker坐标点
        LatLng point = new LatLng(mLatitude, mLongitude);
        //fileTransfer.exportFile(fileName, System.currentTimeMillis() + "," + mLatitude + "," + mLongitude + "\n");
        //构建Marker图标
        /*
        BitmapDescriptor bitmap = BitmapDescriptorFactory.fromResource(R.drawable.flag);
        //构建MarkerOption，用于在地图上添加Marker
        OverlayOptions option = new MarkerOptions().position(point).icon(bitmap);
        //在地图上添加Marker，并显示
        mBaiduMap.addOverlay(option);
        */
        //
        //构建折线点坐标
        points.add(point);
        if (points.size() < 2){
            return;
        }

        //设置折线的属性
        OverlayOptions mOverlayOptions = new PolylineOptions()
                .width(10)
                .color(0xAAFF0000)
                .points(points);
        Overlay mPolyline = mBaiduMap.addOverlay(mOverlayOptions);
        mBaiduMap.setMyLocationEnabled(true);
        //清除之前绘制的点
        LatLng last_latLng = points.get(points.size() - 1);
        points.clear();
        points.add(last_latLng);

    }

    //设置整张地图的显示
    private void setUserMapCenter() {
        Log.v("baiduMap", "set UserMapCenter lat : " + mLatitude + " lon : " + mLongitude);
        LatLng latLng = new LatLng(mLatitude, mLongitude);
        //定义地图状态
        MapStatus mMapStatus = new MapStatus.Builder().target(latLng).zoom(18).build();
        //定义MapStatusUpdate对象，以便描述地图状态将要发生的变化
        MapStatusUpdate mMapStatusUpdate = MapStatusUpdateFactory.newMapStatus(mMapStatus);
        //改变地图状态
        mBaiduMap.setMapStatus(mMapStatusUpdate);
    }

    //传感器
    private void initOrientation() {
        //传感器
        mMyOrientationListener = new MyOrientationListener(context);
        mMyOrientationListener.setOnOrientationListener(x -> mCurrentX = x);

    }

    @Override
    public void onDestroy() {
        //在activity执行onDestroy时执行mMapView.onDestroy()，实现地图生命周期管理
        mLocationClient.stop();
        mBaiduMap.setMyLocationEnabled(false);
        mMapView.onDestroy();
        mMapView = null;
        super.onDestroy();
    }
}