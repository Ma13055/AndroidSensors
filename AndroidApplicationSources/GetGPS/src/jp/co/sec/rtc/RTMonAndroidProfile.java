package jp.co.sec.rtc;

/**
 * コンポーネントのコンフィグレーションを定義
 */
public class RTMonAndroidProfile {
	public static final String DefaultNameServer= "192.168.0.111";		// host[:port]
	public static final String Name				= "AccelerationSensor";
	public static final String ImplementationId	= "Sensor_instance";
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
	public static final String OutPort1			= "S_out_port_1";
	public static final String OutPort2			= "S_out_port_2";
	public static final String OutPort3			= "S_out_port_3";
	public static final String OutPort4			= "S_out_port_4";
	public static final String OutPort5			= "S_out_port_5";
}
