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
	private OutPort<TimedDouble>		outPortDoubleError;
	private OutPort<TimedDouble>		outPortDoubleBearing;
	private OutPort<TimedDouble>		outPortDoubleSpeed;
//	private OutPort<TimedShort>			outPortNumSatellites;
	
	TimedDouble dataLatitude = new TimedDouble();
	TimedDouble dataLongitude = new TimedDouble();
	TimedDouble dataAltitude = new TimedDouble();
	TimedDouble dataError = new TimedDouble();
	TimedDouble dataBearing = new TimedDouble();
	TimedDouble dataSpeed = new TimedDouble();
//	TimedShort dataNum = new TimedShort();
	RTCTime RTCtm = new RTCTime(0,0);
	RTCTime GPStm = new RTCTime(0,0);
	

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
		GPStm = myRTC.getTimeFromGPS();
		dataLatitude.setData(myRTC.getLatitude());
		dataLatitude.setTm(RTCtm);
		dataLongitude.setData(myRTC.getLongitude());
		dataLongitude.setTm(GPStm);
		dataAltitude.setData(myRTC.getAltitude());
		dataAltitude.setTm(RTCtm);
		dataError.setData(myRTC.getHorizontalError());
		dataError.setTm(RTCtm);
		dataBearing.setData(myRTC.getHeading());
		dataBearing.setTm(RTCtm);
		dataSpeed.setData(myRTC.getHorizontalSpeed());
		dataSpeed.setTm(RTCtm);
/*		dataNum.setData(myRTC.getNumSatellites());
		dataNum.setTm(RTCtm);*/
		
		String str = "";
		str = "GPS\n"
				+ "緯度 : " + dataLatitude.getData() + "\n"
				+ "経度 : " + dataLongitude.getData() + "\n"
				+ "標高 : " + dataAltitude.getData() + "\n"
				+ "精度 : " + dataError.getData() + "\n"
				+ "傾き : " + dataBearing.getData() + "\n"
				+ "速度 : " + dataSpeed.getData() + "\n"
//				+ "衛生数 : " + dataNum.getData() + "\n"
				+ "GPS : " + GPStm.toString() + "\n"
				+ "RTC : " + RTCtm.toString();
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
		String SpeName = RTMonAndroidProfile.OutPort6;
//		String NumName = RTMonAndroidProfile.OutPort7;
		TimedDouble Lat = new TimedDouble();
		TimedDouble Lon = new TimedDouble();
		TimedDouble Alt = new TimedDouble();
		TimedDouble Acc = new TimedDouble();
		TimedDouble Bea = new TimedDouble();
		TimedDouble Spe = new TimedDouble();
//		TimedShort Num = new TimedShort();
		outPortDoubleLatitude = new OutPort<TimedDouble>(LatName, Lat);
		outPortDoubleLongitude = new OutPort<TimedDouble>(LonName, Lon);
		outPortDoubleAltitude = new OutPort<TimedDouble>(AltName, Alt);
		outPortDoubleError = new OutPort<TimedDouble>(AccName,Acc);
		outPortDoubleBearing = new OutPort<TimedDouble>(BeaName,Bea);
		outPortDoubleSpeed = new OutPort<TimedDouble>(SpeName,Spe);
//		outPortNumSatellites = new OutPort<TimedShort>(NumName,Num);
		rtcService.addOutPort(outPortDoubleLatitude);
		rtcService.addOutPort(outPortDoubleLongitude);
		rtcService.addOutPort(outPortDoubleAltitude);
		rtcService.addOutPort(outPortDoubleError);
		rtcService.addOutPort(outPortDoubleBearing);
		rtcService.addOutPort(outPortDoubleSpeed);
//		rtcService.addOutPort(outPortNumSatellites);
	}

	/**
	 * 入出力コントロール
	 */
	private void ioControl(){
		Logger4RTC.spec("SPEC_LOG", "RTM_11j: onExecute in Application Started.");
		outPortDoubleLatitude.write(dataLatitude);						// データをoutPortに書き出す。
		outPortDoubleLongitude.write(dataLongitude);
		outPortDoubleAltitude.write(dataAltitude);
		outPortDoubleError.write(dataError);
		outPortDoubleBearing.write(dataBearing);
		outPortDoubleSpeed.write(dataSpeed);
//		outPortNumSatellites.write(dataNum);
		Logger4RTC.debug(TAG, "onExecute");
		Logger4RTC.spec("SPEC_LOG", "RTM_18j: onExecute in Application Ended.");
	}
}
