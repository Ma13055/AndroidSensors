/*
 * Copyright (C) 2011 SEC (Systems Engineering Consultants) Co.,LTD.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jp.ac.it.shibaura.android.GPS;

import jp.co.sec.rtc.RTMonAndroidProfile;
import jp.co.sec.rtc.RTMonAndroidImpl;
import jp.co.sec.rtm.Logger4RTC;
import jp.co.sec.rtm.NameServerConnectTask;
import jp.co.sec.rtm.NameServerConnectTask.NameServerConnectListener;
import jp.co.sec.rtm.RTCService;
import jp.co.sec.rtm.RTCTime;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.location.GpsSatellite;
import android.location.GpsStatus;
import android.location.GpsStatus.NmeaListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.IBinder;
import android.text.SpannableStringBuilder;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.os.Handler;
import android.os.Message;

/**
 * RTMonAndroid
 */
@SuppressLint("NewApi")
public class RTMonAndroidOfGPS extends Activity {
	private static final String TAG = "MyRTC";

	private static final String Pref_NameServerAddress = "NameServerAddress";	// ネームサーバーアドレスをプリファレンスで扱うときのタグ
	private static final int MSG_TEXT	= 0x000;
	private static final int MSG_TOAST	= 0x100;

	private Context			context;
	private NameServerConnectTask	nameServerConnectTask = null;
	private ServiceConnection	serviceConnection = null;
	private RTCService		rtcService = null;
	private ToastHandler	mToastHandler;

	private EditText		myEditText;			// host:portの入力部分
	private Button			myStartButton;		// START RTC ボタン
	private Button			myStopButton;		// STOP RTC ボタン
	private TextView		myInDataView;		// 受信データ表示部分

	private String			nameServer;			// ネームサーバーアドレス

	private RTMonAndroidImpl rtcImpl;

	private boolean			drawText = false;
	
	//センサ情報取得用変数
	private LocationManager lm = null;
	private LocationEventListener lel;
	private LocationProvider p;
	
	//出力データ用変数
	private double dataAltitude = 0;			//GPS 標高情報
	private double dataLatitude = 0;			//GPS 緯度情報
	private double dataLongitude = 0;			//GPS 経度情報
	private double dataAccuracy = 0;			//GPS 位置精度情報
	private double dataSpeed = 0;				//GPS 速度情報
	private RTCTime tmFromGPS = new RTCTime();	//GPS GPSの時間情報
	private RTCTime tm = new RTCTime();			//GPS システムの時間情報
//	private short numSatellites = 0;			//GPS 衛星数情報
//	private long dataFix = 0;					//GPS FixType情報
	private double dataBearing = 0;				//GPS ベアリング情報	


	/**
	 * アプリ生成
	 */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		context = getApplicationContext();
		Logger4RTC.setDebuggable(context);
		Logger4RTC.debug(TAG, "onCreate Start");

		rtcService = null;
		serviceConnection = null;
		setContentView(R.layout.main);
		initToast();

		myEditText = (EditText)findViewById(R.id.editText);
		myEditText.setText(getNameServerAddress(RTMonAndroidProfile.DefaultNameServer));

		myStartButton = (Button)findViewById(R.id.startButton);
		myStopButton  = (Button)findViewById(R.id.stopButton);

		myInDataView = (TextView)findViewById(R.id.inDataView);

		lm = (LocationManager)getSystemService(LOCATION_SERVICE);
		lel = new LocationEventListener();
		lm.addNmeaListener(lel);
		
		/**
		 *	START RTC ボタン
		 */
		myStartButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Logger4RTC.debug(TAG, "startRTC Button Pushed");
				if (nameServerConnectTask == null) {
					connectNameServer();		// NameServerへの接続
				}
				else {
					showToast("RTC Service already started !!", 1);
				}
			}
		});

		/**
		 *	STOP RTC ボタン
		 */
		myStopButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Logger4RTC.debug(TAG, "stopRTC Button Pushed");
				if (rtcService != null) {
					rtcService.stopRTC();
					rtcService = null;
					showToast("RTCService destroyRTC", 1);
				}
				releaseService();
			}
		});
		
	}

	/**
	 * アプリ起動
	 */
	@Override
	public void onStart() {
		super.onStart();
		Logger4RTC.debug(TAG, "onStart");
	}

	/**
	 * アプリ開始
	 */
	@Override
	public void onResume() {
		super.onResume();
		drawText = true;
		Logger4RTC.debug(TAG, "onResume");
		p = lm.getProvider("gps");
		if(lm != null){
			lm.requestLocationUpdates(p.getName(), 0, 0, lel);
		}
	}

	/**
	 * アプリ停止
	 */
	@Override
	public void onPause() {
		drawText = false;
		super.onPause();
		Logger4RTC.debug(TAG, "onPause");
/*		if(lm != null){
			lm.removeUpdates(lel);
		}*/
	}

	/**
	 * アプリ破棄
	 */
	@Override
	public void onDestroy() {
		super.onDestroy();
		Logger4RTC.debug(TAG, "onDestroy");
		if (rtcService != null) {
			rtcService.stopRTC();
			rtcService = null;
			showToast("RTCService destroyRTC", 1);
		}
		releaseService();
		lm.removeUpdates(lel);
		lm.removeNmeaListener(lel);
	}

	/**
	 * NameServerへの接続
	 */
	private void connectNameServer() {
		Logger4RTC.debug(TAG, "connectNameServer");
		SpannableStringBuilder sb = (SpannableStringBuilder)myEditText.getText();
		nameServer = sb.toString();
		saveNameServerAddress(nameServer);		// この時点でのネームサーバーアドレスを保存しておく

		nameServerConnectTask = new NameServerConnectTask(this);
		nameServerConnectTask.setListener(new NameServerConnectListenerImpl());	// NameServer接続完了リスナーを登録
		nameServerConnectTask.setIpAddress(nameServer);
		nameServerConnectTask.setRetryCount(5);
		nameServerConnectTask.execute();		// ネームサーバーへの接続を開始する
	}

	/**
	 * NameServer接続完了リスナー
	 */
	private class NameServerConnectListenerImpl implements NameServerConnectListener {
		/**
		 * RTCサービスを開始
		 */
		public void onConnected() {
			Intent intent = new Intent(RTMonAndroidOfGPS.this, RTCService.class);
			startService(intent);
			serviceConnection = new RtcServiceConnection();
			bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
		}

		/**
		 * 接続失敗
		 */
		public void onConnectFailed() {
		}

		/**
		 * 接続キャンセル
		 */
		public void onConnectCanceled() {
		}
	}

	/**
	 * RTCService接続処理
	 */
	private class RtcServiceConnection implements ServiceConnection {
		/**
		 * RTCService bind完了
		 */
		public void onServiceConnected(ComponentName className, IBinder service) {
			Logger4RTC.debug(TAG, "RTCService binded");
			rtcService = ((RTCService.RTCServiceBinder) service).getService();

			// プロパティーを設定する
			rtcService.setProfiles(
					RTMonAndroidProfile.DefaultNameServer,	RTMonAndroidProfile.Name,
					RTMonAndroidProfile.ImplementationId,	RTMonAndroidProfile.Type,
					RTMonAndroidProfile.Description,		RTMonAndroidProfile.Version,
					RTMonAndroidProfile.Vendor,				RTMonAndroidProfile.Category,
					String.valueOf(RTMonAndroidProfile.execute_rate));

			rtcImpl = new RTMonAndroidImpl(RTMonAndroidOfGPS.this, rtcService);

			SpannableStringBuilder sb = (SpannableStringBuilder)myEditText.getText();	// EditTextに入力された文字
			rtcService.startRTC(sb.toString(), context.getPackageName());		// START RTC
			showToast("start RTC : " + nameServer, 1);
		}

		/**
		 * RTCService終了処理
		 */
		public void onServiceDisconnected(ComponentName className) {
			Logger4RTC.debug(TAG, "onServiceDisconnected");
		}
	}

	/**
	 * RTCServiceとの結合を解除
	 */
	private void releaseService() {
		if (nameServerConnectTask != null) {
			nameServerConnectTask.cancel(true);
			nameServerConnectTask = null;
		}
		if (serviceConnection != null) {
			unbindService(serviceConnection);
			serviceConnection = null;
			Intent intent = new Intent(RTMonAndroidOfGPS.this, RTCService.class);
			stopService(intent);
		}

		if (rtcImpl != null){
			rtcImpl = null;
		}
	}

	/**
	 * 受信内容を画面書き込む
	 */
	public void textDraw(String str) {
		Logger4RTC.debug(TAG, "receiverDraw : " + str);
		mToastHandler.sendMessage(mToastHandler.obtainMessage(MSG_TEXT, str));
	}

	/**
	 * トースト表示のハンドラで表示の為の初期化
	 */
	private void initToast(){
		mToastHandler = new ToastHandler();
	}

	/**
	 * トースト表示のリクエスト
	 * @param msg 表示する文字
	 */
	public void showToast(String msg){
		mToastHandler.sendMessage(mToastHandler.obtainMessage(MSG_TOAST, msg));
	}

	/**
	 * トースト表示のリクエスト
	 * @param msg 表示する文字
	 * @param mode 0=long, 1=short
	 */
	public void showToast(String msg, int mode){
		int lng = Toast.LENGTH_LONG;
		if (0 == mode) lng = Toast.LENGTH_SHORT;
		mToastHandler.sendMessage(mToastHandler.obtainMessage(MSG_TOAST+lng, msg));
	}

	/**
	 * TEXTとトーストの表示
	 */
	private class ToastHandler extends Handler{
		public void handleMessage(Message msg) {
			int para = msg.what;
			if (MSG_TOAST > para){
				if (drawText){
					myInDataView.setText(msg.obj.toString());	// TEXT描画
				}
			}
			else{
				Toast toast = Toast.makeText(getBaseContext(), msg.obj.toString(), msg.what);
				toast.show();
			}
		}
	}

	/**
	 * ネームサーバーのアドレスを得る
	 * @param default_name_server デフォルトネームサーバー
	 * @return ネームサーバーのアドレス
	 */
	private String getNameServerAddress(String default_name_server){
		String nameServer = default_name_server;
		try{
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
			nameServer = sp.getString(Pref_NameServerAddress, nameServer);
		}
		catch (Exception e) {
		}
		return nameServer;
	}

	/**
	 * ネームサーバーのアドレスをプリファレンスに保存する
	 * @param nameServerAddress ネームサーバーアドレス
	 * @return 成功時 true
	 */
	private boolean saveNameServerAddress(String nameServerAddress){
		try{
			SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
			SharedPreferences.Editor editor = sp.edit();
			editor.putString(Pref_NameServerAddress, nameServerAddress);
			editor.commit();
		}
		catch (Exception e) {
			Logger4RTC.error(TAG, "error Preference Write");
			return false;
		}
		return true;
	}
	
	/**
	 * GPSの標高情報を返す
	 * @return GPSの標高データ(double)
	 */
	public double getAltitude(){
		return dataAltitude;
	}

	/**
	 * GPSの緯度情報を返す
	 * @return GPSの緯度データ(double)
	 */
	public double getLatitude(){
		return dataLatitude;
	}	
	
	/**
	 * GPSの経度情報を返す
	 * @return GPSの経度データ(double)
	 */
	public double getLongitude(){
		return dataLongitude;
	}
	
	/**
	 * GPSの位置精度情報を返す
	 * @return GPSの位置精度データ(double)
	 */
	public double getHorizontalError(){
		return dataAccuracy;
	}
	
	/**
	 * GPSの速度情報を返す
	 * @return GPSの速度データ(double)
	 */
	public double getHorizontalSpeed(){
		return dataSpeed;
	}
	
	/**
	 * GPS上の時間情報を返す
	 * @return GPSの時間データ(RTCTime)
	 */
	public RTCTime getTimeFromGPS(){
		return tmFromGPS;
	}
	
	/**
	 * システム上の時間情報を更新して返す
	 * @return システム上の時間データ(RTCTime)
	 */
	public RTCTime getRTCTime(){
		long nanoTm = System.nanoTime()%1000000;
		long millisTm = System.currentTimeMillis();
		int sec = (int)(millisTm/1000);
		int nsec = (int)((millisTm%1000)*1000000+nanoTm);
		tm.set(sec, nsec);
		return tm;
	}
	
	/**
	 * GPSの衛生の数情報を返す
	 * @return GPSの衛星数データ(short)
	 */
/*	public short getNumSatellites(){
		return numSatellites;
	}*/
	
	/**
	 * GPSのFixType情報を返す
	 * @return GPSのFixTypeデータ(long)
	 */
/*	public long getFixType(){
		return dataFix;
	}		*/
	
	/**
	 * GPSの方向情報を返す
	 * @return GPSの方向データ(double)
	 */
	public double getHeading(){
		return dataBearing;
	}
		
	/**
	 * GPSの情報を監視して、GPSデータが変化するたびに、
	 * プログラム内のGPS用変数の値を更新するクラス
	 */
	@SuppressLint("NewApi")
	class LocationEventListener implements LocationListener, GpsStatus.NmeaListener{
		
		@Override
		public void onLocationChanged(Location location) {
			// TODO 自動生成されたメソッド・スタブ
			dataAltitude = location.getAltitude();
			dataLatitude = location.getLatitude();
			dataLongitude = location.getLongitude();
			dataAccuracy = location.getAccuracy();
			dataBearing = location.getBearing();
/*			long nanoTm = location.getElapsedRealtimeNanos()%1000000;	*/
			long millisTm = location.getTime();
			int sec = (int)(millisTm/1000);
			int nsec = (int)((millisTm%1000)*1000000);
			tmFromGPS.set(sec, nsec);
		}

		@Override
		public void onProviderDisabled(String provider) {
			// TODO 自動生成されたメソッド・スタブ
			
		}

		@Override
		public void onProviderEnabled(String provider) {
			// TODO 自動生成されたメソッド・スタブ
			
		}

		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) {
/*			Iterable<GpsSatellite> gpsStat = lm.getGpsStatus(null).getSatellites();
			numSatellites = 0;
			for(GpsSatellite Gps : gpsStat){
				if(Gps.usedInFix()==true)numSatellites++;
			}*/
		}

		@Override
		public void onNmeaReceived(long timestamp, String nmea) {
			// TODO 自動生成されたメソッド・スタブ
//			showToast(nmea.toString(), 1);
/*			if(nmea != null){
				showToast("A", 1);
				String[] data = nmea.split(",");
				showToast("B", 1);
				//GPGSV,GPGSA,GPRMC,GPVTG,GPGGA
				if(data[0].equals("$GPGSA")){
					showToast("C", 1);
					numSatellites=0;
					for(int i=3;i<data.length-3;i++){
						if(data[i].equals("")==false)numSatellites++;
					}
				}else if(data[0].equals("$GPGSV")){
					showToast("D", 1);
					numSatellites=Short.valueOf(data[3]);
				}else if(data[0].equals("$GPGGA")){
					showToast("E", 1);
					numSatellites=Short.valueOf(data[7]);
				}
			}*/
//			showToast("F", 1);
		}
	}
}
