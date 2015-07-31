package com.test;

import android.app.ProgressDialog;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.LocationManagerProxy;
import com.amap.api.location.LocationProviderProxy;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch.OnGeocodeSearchListener;
import com.dada.mobile.library.pojo.PhoneInfo;
import com.dada.mobile.library.utils.DebugUtil;
import com.dada.mobile.library.utils.Extras;
import com.dada.mobile.library.utils.LocationUpdator;
import com.dada.mobile.library.utils.LocationUpdator.LocationListener;
import com.tomkey.commons.tools.Container;
import com.tomkey.commons.tools.DevUtil;

public class LocationUpdator {
	public static final int LOCATE_INTERVAL = 30 * 1000;
	private static int locateInterval=LOCATE_INTERVAL;// 低于这个时间间隔，将不会发起一个新的定位
	private int locationTimeOut = 60 * 1000;
	private LocationListener locationListener;
	private ProgressDialog progressDialog;
	private LocationManagerProxy aMapLocManager = LocationManagerProxy.getInstance(Container.getContext());
	private Handler handler = new Handler(Looper.getMainLooper());
	private Runnable timeout = new Runnable() {
		@Override
		public void run() {
			stopLocation();
			dismissProgressDialog();
			if (locationListener != null)
				locationListener.onLocationTimeOut();
		}
	};

	public LocationUpdator(int locationTimeOut, LocationListener locationListener) {
		this(locationTimeOut, locationListener, null);
	}

	public LocationUpdator(int locationTimeOut, LocationListener locationListener, ProgressDialog progressDialog) {
		this.locationTimeOut = locationTimeOut;
		this.locationListener = locationListener;
		this.progressDialog = progressDialog;
	}

	public static void setLocateInterval(int locateInterval) {
		locateInterval = locateInterval;
	}

	private AMapLocationListener listener = new AMapLocationListener() {

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {

		}

		@Override
		public void onProviderEnabled(String provider) {

		}

		@Override
		public void onProviderDisabled(String provider) {

		}

		@Override
		public void onLocationChanged(Location location) {

		}

		@Override
		public void onLocationChanged(AMapLocation mapLocation) {
			DevUtil.d("zqt", "onLocationChanged");
			DevUtil.d("zqt", "PhoneInfo.lat:" + PhoneInfo.lat + " PhoneInfo.lng:" + PhoneInfo.lng);
			DevUtil.d("zqt", "mapLocation.getProvider()=" + mapLocation.getProvider());

			if (mapLocation != null && locationListener != null) {
				setLocation(mapLocation);
				handler.removeCallbacks(timeout);
				stopLocation();
				locationListener.onLocationChanged();
				// Toasts.shortToast(Container.getContext(),
				// ""+mapLocation.getCityCode()+"-"+mapLocation.getCity()+"-"+mapLocation.getProvider());
				// DevUtil.d("zqt",
				// ""+mapLocation.getCityCode()+"-"+mapLocation.getCity()+"-"+mapLocation.getProvider());

			} else {
				dismissProgressDialog();
			}
		}
	};

	public ProgressDialog progressDialog() {
		return progressDialog;
	}

	protected void dismissProgressDialog() {
		try {
			if (progressDialog != null && progressDialog.isShowing())
				progressDialog.dismiss();
		} catch (Exception e) {
		}
	}

	public void startLocation() {
		if (locationTimeOut <= 0 || System.currentTimeMillis() - PhoneInfo.locateTime < locateInterval) {
			locationListener.onLocationChanged();
			return;
		}
		if (progressDialog != null && !progressDialog.isShowing())
			progressDialog.show();
		/*
		 * API定位采用GPS和网络混合定位方式
		 * ，第一个参数是定位provider，第二个参数时间最短是100秒，第三个参数距离间隔单位是米，第四个参数是定位监听者
		 */
		// aMapLocManager.requestLocationUpdates(LocationProviderProxy.AMapNetwork,
		// 60000, 10, listener);
		aMapLocManager.requestLocationData(LocationProviderProxy.AMapNetwork, -1, 5, listener);
		handler.postDelayed(timeout, locationTimeOut);
		// aMapLocManager.requestLocationUpdates(
		// LocationProviderProxy.AMapNetwork, 3000, 0, listener);
		// aMapLocManager.setGpsEnable(true);
	}

	/**
	 * 关闭定位
	 */
	private void stopLocation() {
		aMapLocManager.removeUpdates(listener);
		aMapLocManager.destroy();
	}

	public static void setLocation(AMapLocation mapLocation) {
		DevUtil.d("zqt", "setLocation 1=" + mapLocation);
		if (DevUtil.isDebug()) {
			if (mapLocation == null)
				mapLocation = new AMapLocation("");
			mapLocation.setLatitude(DebugUtil.locationPreferences.getFloat(DebugUtil.DEV_LAT,
					(float) mapLocation.getLatitude()));
			mapLocation.setLongitude(DebugUtil.locationPreferences.getFloat(DebugUtil.DEV_LNG,
					(float) mapLocation.getLongitude()));
			mapLocation.setCityCode(DebugUtil.locationPreferences.getString(DebugUtil.DEV_CITY_CODE,
					mapLocation.getCityCode()));
		}
		DevUtil.d("zqt", "setLocation 2=" + mapLocation);
		PhoneInfo.lat = mapLocation.getLatitude();
		PhoneInfo.lng = mapLocation.getLongitude();
		PhoneInfo.accuracy = mapLocation.getAccuracy() + "";
		LocationUpdator.updateCityCode(mapLocation.getCityCode(), mapLocation.getCity());
		PhoneInfo.locateTime = System.currentTimeMillis();
		PhoneInfo.locationProvider = mapLocation.getProvider();
		Container.getPreference().edit().putFloat(Extras.EXTRA_LAT, (float) PhoneInfo.lat)
				.putFloat(Extras.EXTRA_LNG, (float) PhoneInfo.lng)
				.putString(Extras.EXTRA_CITY_CODE, PhoneInfo.cityCode)
				.putLong(Extras.EXTRA_LOCATE_TIME, PhoneInfo.locateTime)
				.putString(Extras.EXTRA_LOCATION_PROVIDER, PhoneInfo.locationProvider).commit();
	}

	public interface LocationListener {
		void onLocationChanged();

		void onLocationTimeOut();
	}

	public static void updateCityCode(String cityCode, String cityName) {
		if (!TextUtils.isEmpty(cityCode) && !TextUtils.isEmpty(cityName)) {
			PhoneInfo.cityCode = cityCode;
			PhoneInfo.cityName = cityName;
		} else {
			GeocodeSearch geocoderSearch = new GeocodeSearch(Container.getContext());
			geocoderSearch.setOnGeocodeSearchListener(new OnGeocodeSearchListener() {

				@Override
				public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
					if (rCode == 0) {
						if (result != null && result.getRegeocodeAddress() != null
								&& result.getRegeocodeAddress().getFormatAddress() != null) {
							PhoneInfo.cityCode = result.getRegeocodeAddress().getCityCode();
							PhoneInfo.cityName = result.getRegeocodeAddress().getCity();
							// Toasts.shortToast(Container.getContext(),
							// PhoneInfo.lat+"-"+PhoneInfo.lng+"-"+PhoneInfo.cityCode);
						}
					}
				}

				@Override
				public void onGeocodeSearched(GeocodeResult result, int rCode) {

				}
			});
			// 第一个参数表示一个Latlng，第二参数表示范围多少米，第三个参数表示是火系坐标系还是GPS原生坐标系
			RegeocodeQuery query = new RegeocodeQuery(new LatLonPoint(PhoneInfo.lat, PhoneInfo.lng), 200,
					GeocodeSearch.AMAP);
			geocoderSearch.getFromLocationAsyn(query);
		}

	}
}

