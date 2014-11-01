package jp.co.sec.rtc;

import jp.ac.it.shibaura.android.gravity.RTMonAndroidOfGravity;
import jp.co.sec.rtm.*;

/**
 *
 */
public class RTMonAndroidImpl extends RTCBase {
	private static final String TAG = "MyRTC";

	private static RTMonAndroidOfGravity	myRTC = null;
	private RTCService			rtcService;

	// InPort
	//	Non

	// OutPort
	private OutPort<TimedDouble>		outPortDoubleX;
	private OutPort<TimedDouble>		outPortDoubleY;
	private OutPort<TimedDouble>		outPortDoubleZ;
	
	TimedDouble dataX = new TimedDouble();
	TimedDouble dataY = new TimedDouble();
	TimedDouble dataZ = new TimedDouble();
	RTCTime RTCtm = new RTCTime(0,0);
	

	/**
	 * コンストラクタ
	 *
	 * @param myRTC
	 * @param rtcService
	 * @param mode データ型
	 */
	public RTMonAndroidImpl(RTMonAndroidOfGravity myRTC, RTCService rtcService) {
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
			dataX.setData(myRTC.getX());
			dataX.setTm(RTCtm);
			dataY.setData(myRTC.getY());
			dataY.setTm(RTCtm);
			dataZ.setData(myRTC.getZ());
			dataZ.setTm(RTCtm);
			String str = "";
			str = "重力センサ[m/s^2]\n"
					+ "X軸 : " + dataX.getData() + "\n"
					+ "Y軸 : " + dataY.getData() + "\n"
					+ "Z軸 : " + dataZ.getData() + "\n"
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
		String XName = RTMonAndroidProfile.OutPort1;
		String YName = RTMonAndroidProfile.OutPort2;
		String ZName = RTMonAndroidProfile.OutPort3;
		TimedDouble XX = new TimedDouble();
		TimedDouble YY = new TimedDouble();
		TimedDouble ZZ = new TimedDouble();
		outPortDoubleX = new OutPort<TimedDouble>(XName, XX);
		outPortDoubleY = new OutPort<TimedDouble>(YName, YY);
		outPortDoubleZ = new OutPort<TimedDouble>(ZName, ZZ);
		rtcService.addOutPort(outPortDoubleX);
		rtcService.addOutPort(outPortDoubleY);
		rtcService.addOutPort(outPortDoubleZ);
	}

	/**
	 * 入出力コントロール
	 */
	private void ioControl(){
		Logger4RTC.spec("SPEC_LOG", "RTM_11j: onExecute in Application Started.");
		outPortDoubleX.write(dataX);						// データをoutPortに書き出す。
		outPortDoubleY.write(dataY);
		outPortDoubleZ.write(dataZ);
		Logger4RTC.debug(TAG, "onExecute");
		Logger4RTC.spec("SPEC_LOG", "RTM_18j: onExecute in Application Ended.");
	}
}
