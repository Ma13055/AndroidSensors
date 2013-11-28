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
package jp.ac.it.shibaura.android.sensors;

import jp.co.sec.rtc.RTMonAndroidProfile;
import jp.co.sec.rtc.RTMonAndroidImpl;
import jp.co.sec.rtm.Logger4RTC;
import jp.co.sec.rtm.NameServerConnectTask;
import jp.co.sec.rtm.NameServerConnectTask.NameServerConnectListener;
import jp.co.sec.rtm.RTCService;
import jp.co.sec.rtm.RTCTime;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.text.SpannableStringBuilder;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.preference.PreferenceManager;
import android.os.Handler;
import android.os.Message;

/**
 * RTMonAndroid
 */
public class RTMonAndroidOfSensors extends Activity {
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

	private RTMonAndroidImpl rtcImpl = null;

	private boolean			drawText = false;
	
	int Height = 0;
	
	//センサ情報取得用変数
	private SensorManager sm;
	private SetSensorEventListener sse;
	
	//出力データ用変数
	private RTCTime RTCtm = new RTCTime();
	private double[] Accele = new double[3];		//加速度センサ情報
	private double[] LinearAccele = new double[3];	//線形加速度センサ情報
	private double[] Gyro = new double[3];			//ジャイロセンサ情報
	private double[] Gravity = new double[3];		//重力加速度センサ情報
	private double[] Magnetic = new double[3];		//磁気センサ情報
	private double[] Orientation = new double[3];	//傾きセンサ情報
	private double[] RotationV = new double[5];		//回転ベクトル情報
	private double LightData;						//照度センサ情報
	private double PressureData;					//圧力センサ情報
	private double ProximityData;					//近接センサ情報
	private double AmbientData;						//周辺温度センサ情報
	
	boolean[] sensors = new boolean[11];
	boolean[] getValueFlag = new boolean[11];
	
	String str = "";
	
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
		if(rtcImpl == null)setContentView(R.layout.sub);
		initToast();

		myEditText = (EditText)findViewById(R.id.editText);
		myEditText.setText(getNameServerAddress(RTMonAndroidProfile.DefaultNameServer));

		myStartButton = (Button)findViewById(R.id.startButton);
		myStopButton  = (Button)findViewById(R.id.stopButton);

		myInDataView = (TextView)findViewById(R.id.inDataView);
		myInDataView.setMovementMethod(ScrollingMovementMethod.getInstance());
		
		Intent intent = getIntent();
		sensors = intent.getBooleanArrayExtra("Sensors");

		
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
		
		sse = new SetSensorEventListener();
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
		sm = (SensorManager)getSystemService(Context.SENSOR_SERVICE);
		
		for(int i=0;i<11;i++){
			if(sensors[i]==true){
				switch(i){
				case 0:
					sm.registerListener(sse,
							sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
							SensorManager.SENSOR_DELAY_GAME);
					str += "getAcceleration\n";
					break;
				case 1:
					sm.registerListener(sse,
							sm.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
							SensorManager.SENSOR_DELAY_GAME);
					str += "getLinearAcceleration\n";
					break;
				case 2:
					sm.registerListener(sse,
							sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
							SensorManager.SENSOR_DELAY_GAME);
					str += "getGyroscope\n";
					break;
				case 3:
					sm.registerListener(sse,
							sm.getDefaultSensor(Sensor.TYPE_GRAVITY),
							SensorManager.SENSOR_DELAY_GAME);
					str += "getGravity\n";
					break;
				case 4:
					sm.registerListener(sse,
							sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),
							SensorManager.SENSOR_DELAY_GAME);
					str += "getMagneticField\n";
					break;
				case 5:
					sm.registerListener(sse,
							sm.getDefaultSensor(Sensor.TYPE_ORIENTATION),
							SensorManager.SENSOR_DELAY_GAME);
					str += "getOrientation\n";
					break;
				case 6:
					sm.registerListener(sse,
							sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR),
							SensorManager.SENSOR_DELAY_GAME);
					str += "getRotationVector\n";
					break;
				case 7:
					sm.registerListener(sse,
							sm.getDefaultSensor(Sensor.TYPE_LIGHT),
							SensorManager.SENSOR_DELAY_GAME);
					str += "getLight\n";
					break;
				case 8:
					sm.registerListener(sse,
							sm.getDefaultSensor(Sensor.TYPE_PRESSURE),
							SensorManager.SENSOR_DELAY_GAME);
					str += "getPressure\n";
					break;
				case 9:
					sm.registerListener(sse,
							sm.getDefaultSensor(Sensor.TYPE_PROXIMITY),
							SensorManager.SENSOR_DELAY_GAME);
					str += "getProximity\n";
					break;
				case 10:
					sm.registerListener(sse,
							sm.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE),
							SensorManager.SENSOR_DELAY_GAME);
					str += "getAmbientTemperature\n";
					break;
				default:
					break;
				}
			}
		}
		if(rtcImpl == null)textDraw(str);

		
	}

	/**
	 * アプリ停止
	 */
	@Override
	public void onPause() {
		drawText = false;
		super.onPause();
		Logger4RTC.debug(TAG, "onPause");
		sm.unregisterListener(sse);
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
			Intent intent = new Intent(RTMonAndroidOfSensors.this, RTCService.class);
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

			rtcImpl = new RTMonAndroidImpl(RTMonAndroidOfSensors.this, rtcService, sensors);

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
			Intent intent = new Intent(RTMonAndroidOfSensors.this, RTCService.class);
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
	
	public boolean[] getSensorChose(){
		return sensors;
	}
	
	/**
	 * 加速度センサの情報を返す
	 * @return 三軸のセンサ値配列(double[3])
	 */
	public double[] getAccele(){
		return Accele;
	}
	
	/**
	 * 線形加速度センサの情報を返す
	 * @return 三軸のセンサ値配列(double[3])
	 */
	public double[] getLinerAccele(){
		return LinearAccele;
	}

	/**
	 * ジャイロセンサの情報を返す
	 * @return 三軸のセンサ値配列(double[3])
	 */
	public double[] getGyro(){
		return Gyro;
	}

	/**
	 * 重力加速度センサの情報を返す
	 * @return 三軸のセンサ値配列(double[3])
	 */
	public double[] getGravity(){
		return Gravity;
	}

	/**
	 * 磁気センサの情報を返す
	 * @return 三軸のセンサ値配列(double[3])
	 */
	public double[] getMagneticField(){
		return Magnetic;
	}

	/**
	 * 傾きセンサの情報を返す
	 * @return 三軸のセンサ値配列(double[3])
	 */
	public double[] getOrientation(){
		return Orientation;
	}

	/**
	 * 回転ベクトルセンサの情報を返す
	 * @return センサ値配列(double[5])
	 */
	public double[] getRotationVector(){
		return RotationV;
	}

	/**
	 * 照度センサの情報を返す
	 * @return 照度センサ値(double)
	 */
	public double getLight(){
		return LightData;
	}
	
	/**
	 * 気圧センサの情報を返す
	 * @return 気圧センサ値(double)
	 */
	public double getPressure(){
		return PressureData;
	}

	/**
	 * 近接センサの情報を返す
	 * @return 近接センサ値(double)
	 */
	public double getProximity(){
		return ProximityData;
	}

	/**
	 * 温度センサの情報を返す
	 * @return 温度センサ値(double)
	 */
	public double getAmbientTemperature(){
		return AmbientData;
	}
	
	/**
	 * RTCTimeの各ステータスを更新して返す
	 * @return RTCTime
	 */
	public RTCTime getRTCTime(){
		long nanoTm = System.nanoTime()%1000000;
		long millisTm = System.currentTimeMillis();
		int sec = (int)(millisTm/1000);
		int nsec = (int)((millisTm%1000)*1000000+nanoTm);
		RTCtm.set(sec, nsec);
		return RTCtm;
	}	
	
	/**
	 * センサが一度でも実行されたかどうかのフラグを返す
	 * @return 実行フラグ
	 */
	public boolean[] getValueFlag(){
		return getValueFlag;
	}
		
	/**
	 * センサの情報を監視して、加速度センサのデータが変化するたびに、
	 * プログラム内の加速度センサ用変数の値を更新する
	 * TYPE_ACCELEROMETER == 加速度センサ
	 */
	class SetSensorEventListener implements SensorEventListener{
		public void onSensorChanged(SensorEvent e){
			switch(e.sensor.getType()){
			case Sensor.TYPE_ACCELEROMETER:
				Accele[0] = e.values[0];	//加速度センサx軸情報
				Accele[1] = e.values[1];	//加速度センサy軸情報
				Accele[2] = e.values[2];	//加速度センサz軸情報
				getValueFlag[0] = true;
				break;
			case Sensor.TYPE_LINEAR_ACCELERATION:
				LinearAccele[0] = e.values[0];	//線形加速度センサx軸情報
				LinearAccele[1] = e.values[1];	//線形加速度センサy軸情報
				LinearAccele[2] = e.values[2];	//線形加速度センサz軸情報
				getValueFlag[1] = true;
				break;
			case Sensor.TYPE_GYROSCOPE:
				Gyro[0] = e.values[0];	//ジャイロセンサx軸情報
				Gyro[1] = e.values[1];	//ジャイロセンサy軸情報
				Gyro[2] = e.values[2];	//ジャイロセンサz軸情報
				getValueFlag[2] = true;
				break;
			case Sensor.TYPE_GRAVITY:
				Gravity[0] = e.values[0];	//重力加速度センサx軸情報
				Gravity[1] = e.values[1];	//重力加速度センサy軸情報
				Gravity[2] = e.values[2];	//重力加速度センサz軸情報
				getValueFlag[3] = true;
				break;
			case Sensor.TYPE_MAGNETIC_FIELD:
				Magnetic[0] = e.values[0];	//磁気センサx軸情報
				Magnetic[1] = e.values[1];	//磁気センサy軸情報
				Magnetic[2] = e.values[2];	//磁気センサz軸情報
				getValueFlag[4] = true;
				break;
			case Sensor.TYPE_ORIENTATION:
				Orientation[0] = e.values[0];	//傾きセンサ方位角情報(Yaw)
				Orientation[1] = e.values[1];	//傾きセンサ傾斜角情報(Pitch)
				Orientation[2] = e.values[2];	//傾きセンサ回転角情報(Roll)
				getValueFlag[5] = true;
				break;
			case Sensor.TYPE_ROTATION_VECTOR:
				RotationV[0] = e.values[0];	//回転ベクトルセンサ情報 x*sin(θ/2)
				RotationV[1] = e.values[1];	//回転ベクトルセンサ情報 y*sin(θ/2)
				RotationV[2] = e.values[2];	//回転ベクトルセンサ情報 z*sin(θ/2)
				RotationV[3] = e.values[3];	//回転ベクトルセンサ情報 cos(θ/2)
				RotationV[4] = e.values[4];	//回転ベクトルセンサ情報 estimated heading Accuracy (in radians) (-1 if unavailable)
				getValueFlag[6] = true;
				break;
			case Sensor.TYPE_LIGHT:
				LightData = e.values[0];	//照度センサの距離情報
				getValueFlag[7] = true;
				break;
			case Sensor.TYPE_PRESSURE:
				PressureData = e.values[0];		//気圧センサの圧力情報
				getValueFlag[8] = true;
				break;
			case Sensor.TYPE_PROXIMITY:
				ProximityData = e.values[0];	//近接センサの距離情報
				getValueFlag[9] = true;
				break;
			case Sensor.TYPE_AMBIENT_TEMPERATURE:
				AmbientData = e.values[0];	//周辺温度センサ情報
				getValueFlag[10] = true;
				break;
			default:
				break;
			}

		}
		
		public void onAccuracyChanged(Sensor s,int accuracy){}
	}
}
