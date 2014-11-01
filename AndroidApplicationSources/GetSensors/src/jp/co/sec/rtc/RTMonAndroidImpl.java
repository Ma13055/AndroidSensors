package jp.co.sec.rtc;

import java.util.Arrays;

import jp.ac.it.shibaura.android.sensors.RTMonAndroidOfSensors;
import jp.co.sec.rtm.*;

/**
 *
 */
public class RTMonAndroidImpl extends RTCBase {
	private static final String TAG = "MyRTC";

	private static RTMonAndroidOfSensors	myRTC = null;
	private RTCService			rtcService;

	// InPort
	// Non

	// OutPort
	private OutPort<TimedDouble>	outPortAcceleX;
	private OutPort<TimedDouble>	outPortAcceleY;
	private OutPort<TimedDouble>	outPortAcceleZ;
	private OutPort<TimedDouble>	outPortLinearAcceleX;
	private OutPort<TimedDouble>	outPortLinearAcceleY;
	private OutPort<TimedDouble>	outPortLinearAcceleZ;
	private OutPort<TimedDouble>	outPortGyroX;
	private OutPort<TimedDouble>	outPortGyroY;
	private OutPort<TimedDouble>	outPortGyroZ;
	private OutPort<TimedDouble>	outPortGravityX;
	private OutPort<TimedDouble>	outPortGravityY;
	private OutPort<TimedDouble>	outPortGravityZ;
	private OutPort<TimedDouble>	outPortMagneticX;
	private OutPort<TimedDouble>	outPortMagneticY;
	private OutPort<TimedDouble>	outPortMagneticZ;
	private OutPort<TimedDouble>	outPortOrientationYaw;
	private OutPort<TimedDouble>	outPortOrientationPitch;
	private OutPort<TimedDouble>	outPortOrientationRoll;
	private OutPort<TimedDouble>	outPortRotationVX;
	private OutPort<TimedDouble>	outPortRotationVY;
	private OutPort<TimedDouble>	outPortRotationVZ;
	private OutPort<TimedDouble>	outPortRotationVCos;
	private OutPort<TimedDouble>	outPortRotationVAccuracy;
	private OutPort<TimedDouble>	outPortLight;
	private OutPort<TimedDouble>	outPortPressure;
	private OutPort<TimedDouble>	outPortProximity;
	private OutPort<TimedDouble>	outPortAmbient;
	
	private TimedDouble[] Accele = new TimedDouble[3];
	private TimedDouble[] LinearAccele = new TimedDouble[3];
	private TimedDouble[] Gyro = new TimedDouble[3];
	private TimedDouble[] Gravity = new TimedDouble[3];
	private TimedDouble[] Magnetic = new TimedDouble[3];
	private TimedDouble[] Orientation = new TimedDouble[3];
	private TimedDouble[] RotationV = new TimedDouble[5];
	private TimedDouble LightData = new TimedDouble();
	private TimedDouble PressureData = new TimedDouble();
	private TimedDouble ProximityData = new TimedDouble();
	private TimedDouble AmbientData = new TimedDouble();
	
	private RTCTime RTCtm = new RTCTime(0,0);
	
	private boolean[] sensors = new boolean[11];
	
	String str = "";
	
	/**
	 * コンストラクタ
	 *
	 * @param myRTC
	 * @param rtcService
	 * @param mode データ型
	 */
	public RTMonAndroidImpl(RTMonAndroidOfSensors myRTC, RTCService rtcService, boolean[] chose) {
		Logger4RTC.debug(TAG, "RTMonAndroidImpl");
		RTMonAndroidImpl.myRTC = myRTC;
		this.rtcService = rtcService;
		rtcService.setRTC(this);
		sensors = chose;
		
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
		str = "";
		RTCtm = myRTC.getRTCTime();
		str += RTCtm.toString();
		for(int i=0;i<11;i++){
			if(sensors[i]==true){
				switch(i){
				case 0:
					double[] AcceleData = myRTC.getAccele().clone();
					Accele[0].setData(AcceleData[0]);
					Accele[1].setData(AcceleData[1]);
					Accele[2].setData(AcceleData[2]);
					Accele[0].setTm(RTCtm);
					Accele[1].setTm(RTCtm);
					Accele[2].setTm(RTCtm);
					str += "\n加速度センサ[m/s^2]\n"
							+ "X : " + Accele[0].getData() + "\n"
							+ "Y : " + Accele[1].getData() + "\n"
							+ "Z : " + Accele[2].getData();
					break;
				case 1:
					double[] LAcceleData = myRTC.getLinerAccele();
					LinearAccele[0].setData(LAcceleData[0]);
					LinearAccele[1].setData(LAcceleData[1]);
					LinearAccele[2].setData(LAcceleData[2]);
					LinearAccele[0].setTm(RTCtm);
					LinearAccele[1].setTm(RTCtm);
					LinearAccele[2].setTm(RTCtm);
					str += "\n線形加速度センサ[m/s^2]\n"
							+ "X : " + LinearAccele[0].getData() + "\n"
							+ "Y : " + LinearAccele[1].getData() + "\n"
							+ "Z : " + LinearAccele[2].getData();
					break;
				case 2:
					double[] GyroData = myRTC.getGyro();
					Gyro[0].setData(GyroData[0]);
					Gyro[1].setData(GyroData[1]);
					Gyro[2].setData(GyroData[2]);
					Gyro[0].setTm(RTCtm);
					Gyro[1].setTm(RTCtm);
					Gyro[2].setTm(RTCtm);
					str += "\nジャイロセンサ[m/s]\n"
							+ "R : " + Gyro[0].getData() + "\n"
							+ "P : " + Gyro[1].getData() + "\n"
							+ "Y : " + Gyro[2].getData();
					break;
				case 3:
					double[] GravityData = myRTC.getGravity();
					Gravity[0].setData(GravityData[0]);
					Gravity[1].setData(GravityData[1]);
					Gravity[2].setData(GravityData[2]);
					Gravity[0].setTm(RTCtm);
					Gravity[1].setTm(RTCtm);
					Gravity[2].setTm(RTCtm);
					str += "\n重力加速度センサ[m/s^2]\n"
							+ "X : " + Gravity[0].getData() + "\n"
							+ "Y : " + Gravity[1].getData() + "\n"
							+ "Z : " + Gravity[2].getData();
					break;
				case 4:
					double[] MagneticData = myRTC.getMagneticField();
					Magnetic[0].setData(MagneticData[0]);
					Magnetic[1].setData(MagneticData[1]);
					Magnetic[2].setData(MagneticData[2]);
					Magnetic[0].setTm(RTCtm);
					Magnetic[1].setTm(RTCtm);
					Magnetic[2].setTm(RTCtm);
					str += "\n磁気センサ[uT]\n"
							+ "X : " + Magnetic[0].getData() + "\n"
							+ "Y : " + Magnetic[1].getData() + "\n"
							+ "Z : " + Magnetic[2].getData();
					break;
				case 5:
					double[] OrientationData = myRTC.getOrientation();
					Orientation[0].setData(OrientationData[0]);
					Orientation[1].setData(OrientationData[1]);
					Orientation[2].setData(OrientationData[2]);
					Orientation[0].setTm(RTCtm);
					Orientation[1].setTm(RTCtm);
					Orientation[2].setTm(RTCtm);
					str += "\n傾きセンサ[deg]\n"
							+ "Y : " + Orientation[0].getData() + "\n"
							+ "P : " + Orientation[1].getData() + "\n"
							+ "R : " + Orientation[2].getData();
					break;
				case 6:
					double[] RotationVData = myRTC.getRotationVector();
					RotationV[0].setData(RotationVData[0]);
					RotationV[1].setData(RotationVData[1]);
					RotationV[2].setData(RotationVData[2]);
					RotationV[3].setData(RotationVData[3]);
					RotationV[4].setData(RotationVData[4]);
					RotationV[0].setTm(RTCtm);
					RotationV[1].setTm(RTCtm);
					RotationV[2].setTm(RTCtm);
					RotationV[3].setTm(RTCtm);
					RotationV[4].setTm(RTCtm);
					str += "\n回転ベクトルセンサ\n"
							+ "X : " + RotationV[0].getData() + "\n"
							+ "Y : " + RotationV[1].getData() + "\n"
							+ "Z : " + RotationV[2].getData() + "\n"
							+ "Cos : " + RotationV[3].getData() + "\n"
							+ "Acc : " + RotationV[4].getData();
					break;
				case 7:
					LightData.setData(myRTC.getLight());
					LightData.setTm(RTCtm);
					str += "\n照度センサ[Lux]\n"
							+ "Val : " + LightData.getData();
					break;
				case 8:
					PressureData.setData(myRTC.getPressure());
					PressureData.setTm(RTCtm);
					str += "\n圧力センサ[hPa]\n"
							+ "Val : " + PressureData.getData();
					break;
				case 9:
					ProximityData.setData(myRTC.getProximity());
					ProximityData.setTm(RTCtm);
					str += "\n近接センサ[cm]\n"
							+ "Val : " + ProximityData.getData();
					break;
				case 10:
					AmbientData.setData(myRTC.getAmbientTemperature());
					AmbientData.setTm(RTCtm);
					str += "\n温度センサ[Ta]\n"
							+ "Val : " + LightData.getData();
					break;
				default:
					break;
				}
			}
			myRTC.textDraw(str);		// 画面表示
			if((Arrays.equals(sensors, myRTC.getValueFlag())))ioControl();
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
		for(int i=0;i<11;i++){
			if(sensors[i]==true){
				switch(i){
				case 0:
					String AcceleXName = RTMonAndroidProfile.OutAcceleX;
					String AcceleYName = RTMonAndroidProfile.OutAcceleY;
					String AcceleZName = RTMonAndroidProfile.OutAcceleZ;
					TimedDouble AX = new TimedDouble();
					TimedDouble AY = new TimedDouble();
					TimedDouble AZ = new TimedDouble();
					outPortAcceleX = new OutPort<TimedDouble>(AcceleXName, AX);
					outPortAcceleY = new OutPort<TimedDouble>(AcceleYName, AY);
					outPortAcceleZ = new OutPort<TimedDouble>(AcceleZName, AZ);
					rtcService.addOutPort(outPortAcceleX);
					rtcService.addOutPort(outPortAcceleY);
					rtcService.addOutPort(outPortAcceleZ);
					Accele[0] = new TimedDouble();
					Accele[1] = new TimedDouble();
					Accele[2] = new TimedDouble();
					break;
				case 1:
					String LAcceleXName = RTMonAndroidProfile.OutLinearAcceleX;
					String LAcceleYName = RTMonAndroidProfile.OutLinearAcceleY;
					String LAcceleZName = RTMonAndroidProfile.OutLinearAcceleZ;
					TimedDouble LAX = new TimedDouble();
					TimedDouble LAY = new TimedDouble();
					TimedDouble LAZ = new TimedDouble();
					outPortLinearAcceleX = new OutPort<TimedDouble>(LAcceleXName, LAX);
					outPortLinearAcceleY = new OutPort<TimedDouble>(LAcceleYName, LAY);
					outPortLinearAcceleZ = new OutPort<TimedDouble>(LAcceleZName, LAZ);
					rtcService.addOutPort(outPortLinearAcceleX);
					rtcService.addOutPort(outPortLinearAcceleY);
					rtcService.addOutPort(outPortLinearAcceleZ);
					LinearAccele[0] = new TimedDouble();
					LinearAccele[1] = new TimedDouble();
					LinearAccele[2] = new TimedDouble();
					break;
				case 2:
					String GyroXName = RTMonAndroidProfile.OutGyroX;
					String GyroYName = RTMonAndroidProfile.OutGyroY;
					String GyroZName = RTMonAndroidProfile.OutGyroZ;
					TimedDouble GyX = new TimedDouble();
					TimedDouble GyY = new TimedDouble();
					TimedDouble GyZ = new TimedDouble();
					outPortGyroX = new OutPort<TimedDouble>(GyroXName, GyX);
					outPortGyroY = new OutPort<TimedDouble>(GyroYName, GyY);
					outPortGyroZ = new OutPort<TimedDouble>(GyroZName, GyZ);
					rtcService.addOutPort(outPortGyroX);
					rtcService.addOutPort(outPortGyroY);
					rtcService.addOutPort(outPortGyroZ);
					Gyro[0] = new TimedDouble();
					Gyro[1] = new TimedDouble();
					Gyro[2] = new TimedDouble();
					break;
				case 3:
					String GravityXName = RTMonAndroidProfile.OutGravityX;
					String GravityYName = RTMonAndroidProfile.OutGravityY;
					String GravityZName = RTMonAndroidProfile.OutGravityZ;
					TimedDouble GrX = new TimedDouble();
					TimedDouble GrY = new TimedDouble();
					TimedDouble GrZ = new TimedDouble();
					outPortGravityX = new OutPort<TimedDouble>(GravityXName, GrX);
					outPortGravityY = new OutPort<TimedDouble>(GravityYName, GrY);
					outPortGravityZ = new OutPort<TimedDouble>(GravityZName, GrZ);
					rtcService.addOutPort(outPortGravityX);
					rtcService.addOutPort(outPortGravityY);
					rtcService.addOutPort(outPortGravityZ);
					Gravity[0] = new TimedDouble();
					Gravity[1] = new TimedDouble();
					Gravity[2] = new TimedDouble();
					break;
				case 4:
					String MagneticXName = RTMonAndroidProfile.OutMagneticX;
					String MagneticYName = RTMonAndroidProfile.OutMagneticY;
					String MagneticZName = RTMonAndroidProfile.OutMagneticZ;
					TimedDouble MX = new TimedDouble();
					TimedDouble MY = new TimedDouble();
					TimedDouble MZ = new TimedDouble();
					outPortMagneticX = new OutPort<TimedDouble>(MagneticXName, MX);
					outPortMagneticY = new OutPort<TimedDouble>(MagneticYName, MY);
					outPortMagneticZ = new OutPort<TimedDouble>(MagneticZName, MZ);
					rtcService.addOutPort(outPortMagneticX);
					rtcService.addOutPort(outPortMagneticY);
					rtcService.addOutPort(outPortMagneticZ);
					Magnetic[0] = new TimedDouble();
					Magnetic[1] = new TimedDouble();
					Magnetic[2] = new TimedDouble();
					break;
				case 5:
					String OrientXName = RTMonAndroidProfile.OutOrientationYaw;
					String OrientYName = RTMonAndroidProfile.OutOrientationPitch;
					String OrientZName = RTMonAndroidProfile.OutOrientationRoll;
					TimedDouble OX = new TimedDouble();
					TimedDouble OY = new TimedDouble();
					TimedDouble OZ = new TimedDouble();
					outPortOrientationYaw = new OutPort<TimedDouble>(OrientXName, OX);
					outPortOrientationPitch = new OutPort<TimedDouble>(OrientYName, OY);
					outPortOrientationRoll = new OutPort<TimedDouble>(OrientZName, OZ);
					rtcService.addOutPort(outPortOrientationYaw);
					rtcService.addOutPort(outPortOrientationPitch);
					rtcService.addOutPort(outPortOrientationRoll);
					Orientation[0] = new TimedDouble();
					Orientation[1] = new TimedDouble();
					Orientation[2] = new TimedDouble();
					break;
				case 6:
					String RotationVXName = RTMonAndroidProfile.OutRotationVX;
					String RotationVYName = RTMonAndroidProfile.OutRotationVY;
					String RotationVZName = RTMonAndroidProfile.OutRotationVZ;
					String RotationVCName = RTMonAndroidProfile.OutRotationVCos;
					String RotationVAName = RTMonAndroidProfile.OutRotationVAccuracy;
					TimedDouble VX = new TimedDouble();
					TimedDouble VY = new TimedDouble();
					TimedDouble VZ = new TimedDouble();
					TimedDouble VC = new TimedDouble();
					TimedDouble VA = new TimedDouble();
					outPortRotationVX = new OutPort<TimedDouble>(RotationVXName, VX);
					outPortRotationVY = new OutPort<TimedDouble>(RotationVYName, VY);
					outPortRotationVZ = new OutPort<TimedDouble>(RotationVZName, VZ);
					outPortRotationVCos = new OutPort<TimedDouble>(RotationVCName, VC);
					outPortRotationVAccuracy = new OutPort<TimedDouble>(RotationVAName, VA);
					rtcService.addOutPort(outPortRotationVX);
					rtcService.addOutPort(outPortRotationVY);
					rtcService.addOutPort(outPortRotationVZ);
					rtcService.addOutPort(outPortRotationVCos);
					rtcService.addOutPort(outPortRotationVAccuracy);
					RotationV[0] = new TimedDouble();
					RotationV[1] = new TimedDouble();
					RotationV[2] = new TimedDouble();
					RotationV[3] = new TimedDouble();
					RotationV[4] = new TimedDouble();
					break;
				case 7:
					String LightName = RTMonAndroidProfile.OutLight;
					TimedDouble Li = new TimedDouble();
					outPortLight = new OutPort<TimedDouble>(LightName, Li);
					rtcService.addOutPort(outPortLight);
					break;
				case 8:
					String PressureName = RTMonAndroidProfile.OutPressure;
					TimedDouble Pre = new TimedDouble();
					outPortPressure = new OutPort<TimedDouble>(PressureName, Pre);
					rtcService.addOutPort(outPortPressure);
					break;
				case 9:
					String ProximityName = RTMonAndroidProfile.OutProximity;
					TimedDouble Pro = new TimedDouble();
					outPortProximity = new OutPort<TimedDouble>(ProximityName, Pro);
					rtcService.addOutPort(outPortProximity);
					break;
				case 10:
					String AmbientName = RTMonAndroidProfile.OutAmbient;
					TimedDouble Am = new TimedDouble();
					outPortAmbient = new OutPort<TimedDouble>(AmbientName, Am);
					rtcService.addOutPort(outPortAmbient);
					break;
				default:
					break;
				}
			}
		}
	}

	/**
	 * コンポーネントがActive化されたかを返す
	 * @return Activeされた時にtrueになるboolean
	 */
	public String getText(){
		return str;
	}
	
	/**
	 * 入出力コントロール
	 */
	private void ioControl(){
		Logger4RTC.spec("SPEC_LOG", "RTM_11j: onExecute in Application Started.");
		for(int i=0;i<11;i++){
			if(sensors[i]==true){
				switch(i){
				case 0:
					outPortAcceleX.write(Accele[0]);
					outPortAcceleY.write(Accele[1]);
					outPortAcceleZ.write(Accele[2]);
					break;
				case 1:
					outPortLinearAcceleX.write(LinearAccele[0]);
					outPortLinearAcceleY.write(LinearAccele[1]);
					outPortLinearAcceleZ.write(LinearAccele[2]);
					break;
				case 2:
					outPortGyroX.write(Gyro[0]);
					outPortGyroY.write(Gyro[1]);
					outPortGyroZ.write(Gyro[2]);
					break;
				case 3:
					outPortGravityX.write(Gravity[0]);
					outPortGravityY.write(Gravity[1]);
					outPortGravityZ.write(Gravity[2]);
					break;
				case 4:
					outPortMagneticX.write(Magnetic[0]);
					outPortMagneticY.write(Magnetic[1]);
					outPortMagneticZ.write(Magnetic[2]);
					break;
				case 5:
					outPortOrientationYaw.write(Orientation[0]);
					outPortOrientationPitch.write(Orientation[1]);
					outPortOrientationRoll.write(Orientation[2]);
					break;
				case 6:
					outPortRotationVX.write(RotationV[0]);
					outPortRotationVY.write(RotationV[1]);
					outPortRotationVZ.write(RotationV[2]);
					outPortRotationVCos.write(RotationV[3]);
					outPortRotationVAccuracy.write(RotationV[4]);
					break;
				case 7:
					outPortLight.write(LightData);
					break;
				case 8:
					outPortPressure.write(PressureData);
					break;
				case 9:
					outPortProximity.write(ProximityData);
					break;
				case 10:
					outPortAmbient.write(AmbientData);
					break;
				default:
					break;
				}
			}
		}
		Logger4RTC.debug(TAG, "onExecute");
		Logger4RTC.spec("SPEC_LOG", "RTM_18j: onExecute in Application Ended.");
	}
}
