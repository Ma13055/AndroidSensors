package jp.co.sec.rtc;

import jp.ac.it.shibaura.android.GPS.RTMonAndroidOfGPS;
import jp.co.sec.rtm.*;

/**
 *
 */
public class RTMonAndroidImpl extends RTCBase {
	private static final String TAG = "MyRTC";

	private static RTMonAndroidOfGPS	myRTC = null;
	private RTCService			rtcService;

	// InPort
//	private InPort<TimedLong>		inPortLong;

	// OutPort
	private OutPort<TimedDouble>		outPortDoubleLatitude;
	private OutPort<TimedDouble>		outPortDoubleLongitude;
	private OutPort<TimedDouble>		outPortDoubleAltitude;
	private OutPort<TimedDouble>			outPortDoubleAccuracy;
	private OutPort<TimedDouble>			outPortDoubleBearing;
	
	TimedDouble dataLatitude = new TimedDouble();
	TimedDouble dataLongitude = new TimedDouble();
	TimedDouble dataAltitude = new TimedDouble();
	TimedDouble dataAccuracy = new TimedDouble();
	TimedDouble dataBearing = new TimedDouble();
	RTCTime RTCtm = new RTCTime(0,0);
	

	/**
	 * コンストラクタ
	 *
	 * @param myRTC
	 * @param rtcService
	 * @param mode データ型
	 */
	public RTMonAndroidImpl(RTMonAndroidOfGPS myRTC, RTCService rtcService) {
		Logger4RTC.debug(TAG, "RTMonAndroidImpl");
		RTMonAndroidImpl.myRTC = myRTC;
		this.rtcService = rtcService;
		rtcService.setRTC(this);

		initInPort();
		initOutPort();
	}

	/**
	 * 初期化処理.コンポーネントライフサイクルの開始時に一度だけ呼ばれる.
	 */
	@Override
	public int onInitialize() {
		Logger4RTC.debug(TAG, "onInitialize");
		myRTC.showToast("onInitialize");
		return ReturnCode.RTC_RTC_OK;
	}

	/**
	 * 非アクティブ状態からアクティブ化されるとき1度だけ呼ばれる.
	 */
	@Override
	public int onActivated() {
		Logger4RTC.debug(TAG, "onActivated");
		myRTC.showToast("onActivate");
		return ReturnCode.RTC_RTC_OK;
	}

	/**
	 * アクティブ状態時に周期的に呼ばれる.
	 */
	@Override
	public int onExecute() {
		Logger4RTC.debug(TAG, "onExecute");
		RTCtm = myRTC.getRTCTime();
		dataLatitude.setData(myRTC.getLatitude());
		dataLatitude.setTm(RTCtm);
		dataLongitude.setData(myRTC.getLongitude());
		dataLongitude.setTm(RTCtm);
		dataAltitude.setData(myRTC.getAltitude());
		dataAltitude.setTm(RTCtm);
		dataAccuracy.setData(myRTC.getAccuracy());
		dataAccuracy.setTm(RTCtm);
		dataBearing.setData(myRTC.getBearing());
		dataBearing.setTm(RTCtm);
		String str = "";
		str = "GPS\n"
				+ "緯度 : " + dataLatitude.getData() + "\n"
				+ "経度 : " + dataLongitude.getData() + "\n"
				+ "標高 : " + dataAltitude.getData() + "\n"
				+ "精度 : " + dataAccuracy.getData() + "\n"
				+ "傾き : " + dataBearing.getData() + "\n"
				+ "TIME : " + RTCtm.toString();
		myRTC.textDraw(str);		// 画面表示
		ioControl();
		return ReturnCode.RTC_RTC_OK;
	}

	/**
	 * アクティブ状態から非アクティブ化されるとき1度だけ呼ばれる.
	 */
	@Override
	public int onDeactivated() {
		Logger4RTC.debug(TAG, "onDeactivated");
		myRTC.showToast("onDectivate");
		return ReturnCode.RTC_RTC_OK;
	}

	/**
	 * エラー状態に入る前に1度だけ呼ばれる.
	 */
	@Override
	public int onAborting() {
		Logger4RTC.debug(TAG, "onAborting");
		myRTC.showToast("onAborting");
		return ReturnCode.RTC_RTC_OK;
	}

	/**
	 * エラー状態からリセットされ非アクティブ状態に移行するときに1度だけ呼ばれる.
	 */
	@Override
	public int onReset() {
		Logger4RTC.debug(TAG, "onReset");
		myRTC.showToast("onReset");
		return ReturnCode.RTC_RTC_OK;
	}

	/**
	 * エラー状態にいる間周期的に呼ばれる.
	 */
	@Override
	public int onError() {
		Logger4RTC.debug(TAG, "onError");
		myRTC.showToast("onError");
		return ReturnCode.RTC_RTC_OK;
	}

	/**
	 * コンポーネントライフサイクルの終了時に1度だけ呼ばれる.
	 */
	@Override
	public int onFinalize() {
		Logger4RTC.debug(TAG, "onFinalize");
		myRTC.showToast("onFinalize");
		return ReturnCode.RTC_RTC_OK;
	}

	/**
	 * InPort初期化
	 */
	private void initInPort(){
	}

	/**
	 * OutPort初期化
	 */
	private void initOutPort(){
		String LatName = RTMonAndroidProfile.OutPort1;
		String LonName = RTMonAndroidProfile.OutPort2;
		String AltName = RTMonAndroidProfile.OutPort3;
		String AccName = RTMonAndroidProfile.OutPort4;
		String BeaName = RTMonAndroidProfile.OutPort5;
		TimedDouble Lat = new TimedDouble();
		TimedDouble Lon = new TimedDouble();
		TimedDouble Alt = new TimedDouble();
		TimedDouble Acc = new TimedDouble();
		TimedDouble Bea = new TimedDouble();
		outPortDoubleLatitude = new OutPort<TimedDouble>(LatName, Lat);
		outPortDoubleLongitude = new OutPort<TimedDouble>(LonName, Lon);
		outPortDoubleAltitude = new OutPort<TimedDouble>(AltName, Alt);
		outPortDoubleAccuracy = new OutPort<TimedDouble>(AccName,Acc);
		outPortDoubleBearing = new OutPort<TimedDouble>(BeaName,Bea);
		rtcService.addOutPort(outPortDoubleLatitude);
		rtcService.addOutPort(outPortDoubleLongitude);
		rtcService.addOutPort(outPortDoubleAltitude);
		rtcService.addOutPort(outPortDoubleAccuracy);
		rtcService.addOutPort(outPortDoubleBearing);
	}

	/**
	 * 入出力コントロール
	 */
	private void ioControl(){
		Logger4RTC.spec("SPEC_LOG", "RTM_11j: onExecute in Application Started.");
		outPortDoubleLatitude.write(dataLatitude);						// データをoutPortに書き出す。
		outPortDoubleLongitude.write(dataLongitude);
		outPortDoubleAltitude.write(dataAltitude);
		outPortDoubleAccuracy.write(dataAccuracy);
		outPortDoubleBearing.write(dataBearing);
		Logger4RTC.debug(TAG, "onExecute");
		Logger4RTC.spec("SPEC_LOG", "RTM_18j: onExecute in Application Ended.");
	}
}
