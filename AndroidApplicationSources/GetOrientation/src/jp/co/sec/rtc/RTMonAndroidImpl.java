package jp.co.sec.rtc;

import jp.ac.it.shibaura.android.orientaion.RTMonAndroidOfOrientation;
import jp.co.sec.rtm.*;

/**
 *
 */
public class RTMonAndroidImpl extends RTCBase {
	private static final String TAG = "MyRTC";

	private static RTMonAndroidOfOrientation	myRTC = null;
	private RTCService			rtcService;

	// InPort
	//	Non

	// OutPort
	private OutPort<TimedDouble>		outPortDoubleAzimuth;
	private OutPort<TimedDouble>		outPortDoublePitch;
	private OutPort<TimedDouble>		outPortDoubleRoll;
	
	TimedDouble dataAzimuth = new TimedDouble();
	TimedDouble dataPitch = new TimedDouble();
	TimedDouble dataRoll = new TimedDouble();
	RTCTime RTCtm = new RTCTime(0,0);
	

	/**
	 * コンストラクタ
	 *
	 * @param myRTC
	 * @param rtcService
	 * @param mode データ型
	 */
	public RTMonAndroidImpl(RTMonAndroidOfOrientation myRTC, RTCService rtcService) {
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
		if(myRTC.getSensorBoolean() == true){
			RTCtm = myRTC.getRTCTime();
			dataAzimuth.setData(myRTC.getAzimuth());
			dataAzimuth.setTm(RTCtm);
			dataPitch.setData(myRTC.getPitch());
			dataPitch.setTm(RTCtm);
			dataRoll.setData(myRTC.getRoll());
			dataRoll.setTm(RTCtm);
			String str = "";
			str = "傾きセンサ[rad:degree]\n"
					+ "方位角 : " + "( " + String.format("%3.1f",Math.toRadians(dataAzimuth.getData())) + " ) : " + dataAzimuth.getData() + "\n"
					+ "傾斜角 : " + "( " + String.format("%3.1f",Math.toRadians(dataPitch.getData())) + " ) : " + dataPitch.getData() + "\n"
					+ "回転角 : " + "( " + String.format("%3.1f",Math.toRadians(dataRoll.getData())) + " ) : " + dataRoll.getData() + "\n"
					+ "TIME : " + RTCtm.toString();
			myRTC.textDraw(str);		// 画面表示
			ioControl();
		}
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
		String AName = RTMonAndroidProfile.OutPort1;
		String PName = RTMonAndroidProfile.OutPort2;
		String RName = RTMonAndroidProfile.OutPort3;
		TimedDouble Az = new TimedDouble();
		TimedDouble Pi = new TimedDouble();
		TimedDouble Ro = new TimedDouble();
		outPortDoubleAzimuth = new OutPort<TimedDouble>(AName, Az);
		outPortDoublePitch = new OutPort<TimedDouble>(PName, Pi);
		outPortDoubleRoll = new OutPort<TimedDouble>(RName, Ro);
		rtcService.addOutPort(outPortDoubleAzimuth);
		rtcService.addOutPort(outPortDoublePitch);
		rtcService.addOutPort(outPortDoubleRoll);
	}

	/**
	 * 入出力コントロール
	 */
	private void ioControl(){
		Logger4RTC.spec("SPEC_LOG", "RTM_11j: onExecute in Application Started.");
		outPortDoubleAzimuth.write(dataAzimuth);						// データをoutPortに書き出す。
		outPortDoublePitch.write(dataPitch);
		outPortDoubleRoll.write(dataRoll);
		Logger4RTC.debug(TAG, "onExecute");
		Logger4RTC.spec("SPEC_LOG", "RTM_18j: onExecute in Application Ended.");
	}
}
