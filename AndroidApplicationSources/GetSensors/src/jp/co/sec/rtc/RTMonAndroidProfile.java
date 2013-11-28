package jp.co.sec.rtc;

/**
 * コンポーネントのコンフィグレーションを定義
 */
public class RTMonAndroidProfile {
	public static final String DefaultNameServer= "192.168.0.111";		// host[:port]
	public static final String Name				= "GetSensors";
	public static final String ImplementationId	= "GetSensors";
	public static final String Type				= "GetSensorData";
	public static final String Description		= "Output the value of the acceleration sensor";
	public static final String Version			= "1.0";
	public static final String Vendor			= "Shibaura Institute of Technology / Systems Engineering Consultants Co.,LTD. (SEC)";
	public static final String Category			= "Sensor";

	public static final float execute_rate		= 5F; // 秒間何回処理をするか (wait = 1.0 / execute_rate * 1000000 usec)

	//
	// コンポーネントのコンフィグレーションを定義
	//	Non

	//
	// コンポーネントのデータポートを定義
	//
	// << in port >>
	//	Non

	// << out port >>
	public static final String OutAcceleX		= "AcceleDataX";
	public static final String OutAcceleY		= "AcceleDataY";
	public static final String OutAcceleZ		= "AcceleDataZ";
	public static final String OutLinearAcceleX	= "LinearAcceleDataX";
	public static final String OutLinearAcceleY	= "LinearAcceleDataY";
	public static final String OutLinearAcceleZ	= "LinearAcceleDataZ";
	public static final String OutGyroX			= "GyroDataRoll";
	public static final String OutGyroY			= "GyroDataPitch";
	public static final String OutGyroZ			= "GyroDataYaw";
	public static final String OutGravityX		= "GravityDataX";
	public static final String OutGravityY		= "GravityDataY";
	public static final String OutGravityZ		= "GravityDataZ";
	public static final String OutMagneticX		= "MagneticFieldDataX";
	public static final String OutMagneticY		= "MagneticFieldDataY";
	public static final String OutMagneticZ		= "MagneticFieldDataZ";
	public static final String OutOrientationYaw= "OrientationDataAzimuth";
	public static final String OutOrientationPitch	= "OrientationDataPitch";
	public static final String OutOrientationRoll	= "OrientationDataRoll";
	public static final String OutRotationVX	= "RotationVectorDataX";
	public static final String OutRotationVY	= "RotationVectorDataY";
	public static final String OutRotationVZ	= "RotationVectorDataZ";
	public static final String OutRotationVCos	= "RotationVectorDataCos";
	public static final String OutRotationVAccuracy	= "RotationVectorDataAccuracy";
	public static final String OutLight			= "LightDataLux";
	public static final String OutPressure		= "PressureDatahPa";
	public static final String OutProximity		= "ProximityDataCm";
	public static final String OutAmbient		= "AmbientTemperatureDataTa";
}
