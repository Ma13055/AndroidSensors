// -*- Java -*-
/*!
 * @file  GPSFilterImpl.java
 * @brief Filtering of the GPS information
 * @date  $Date$
 *
 * @author Masaru Tatekawa
 * ma13055@shibaura-it.ac.jp
 *
 * $Id$
 */

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Calendar;

import RTC.Acceleration3D;
import RTC.GPSFixType;
import RTC.GPSTime;
import RTC.Time;
import RTC.TimedAcceleration3D;
import RTC.TimedDouble;
import RTC.GPSData;
import RTC.TimedDoubleSeq;
import RTC.TimedFloat;
import RTC.TimedFloatSeq;
import jp.go.aist.rtm.RTC.DataFlowComponentBase;
import jp.go.aist.rtm.RTC.Manager;
import jp.go.aist.rtm.RTC.port.InPort;
import jp.go.aist.rtm.RTC.port.OutPort;
import jp.go.aist.rtm.RTC.util.DataRef;
import jp.go.aist.rtm.RTC.util.StringHolder;
import jp.go.aist.rtm.RTC.util.IntegerHolder;
import RTC.ReturnCode_t;

/*!
 * @class GPSFilterImpl
 * @brief Filtering of the GPS information
 *
 * Androidセンサ取得用アプリケーション「GetGPS」に対応するフィルタコンポーネント
 * 限定された型でしか送信することのできないセンサ情報に対し、
 * GPSSensorに適したデータ型に変換して出力を行う。
 * センサ情報にノイズなどが乗りやすい場合には、ある程度の値の丸めを行うことができ、
 * センサの値の基準値０を現在のセンサ値に置き換えて出力することができる。
 * 入力ポートに入ってくる情報を、テキストファイルに出力することができ、
 * そのテキストファイルを読み込むことで、Android端末との通信を行っていない状態でも、
 * 擬似的にセンサ値を取得しているように出力することができる。
 *
 * In:GPSData
 * Port1:Latitude data (TimedDouble)
 * Port2:Longitude data (TimedDouble)
 * Port3:Altitude data (TimedDouble)
 * Port4:Error data (TimedDouble)
 * Port5:Heading data (TimedDouble)
 * Port6:Speed data (TimedDouble)
 * out:FilteredData
 * Port1:GPS sensor data (GPSData)
 * Port2:Latitude, Longitude and Altitude (TimedDoubleSeq)
 * Port3:Latitude data (TimedDouble)
 * Port4:Longitude data (TimedDouble)
 * Port5:Altitude data (TimedDouble)
 * Port6:Error data(TimedDouble)
 * Port7:Speed data (TimedDouble)
 *
 */
public class GPSFilterImpl extends DataFlowComponentBase {

	/**
	 * 丸め処理用データ配列
	 */
    private double[] arrayDataLatitude = new double[1];
    private double[] arrayDataLongitude = new double[1];
    private double[] arrayDataAltitude = new double[1];
    
    /**
     * テキスト入出力用変数
     */
    private String textName = null;
    private File text = null;
    private BufferedWriter bw = null;
    private boolean can_read = false;	//ファイルの読み込みが成功したことを示すboolean : true-成功
    private boolean read_flag = false;	//テキストの読み込みモードを行っていることを示すboolean : true-読み込み  false-書き出し
    private int read_counter =0;		//読み込み時のカウンタ
    private String[] dataLine;	//読み込んだテキスト情報を保持
	private boolean getInport_flag = false;	//入力ポートの情報が更新された場合にtrue
    
    /**
     * 実行開始時間用変数
     * （書き出し時にファイル名に使用）
     */
    private Calendar startTime = null;
    private int startYear = 0;
    private int startMonth = 0;
    private int startDay = 0;
    private int startHour = 0;
    private int startMinute = 0;
    
    /**
     * 基準値書き換え用変数
     */
    private boolean changeBaseline_flag = false;
    private Object[] baseline;
    
  /*!
   * @brief constructor
   * @param manager Maneger Object
   */
	public GPSFilterImpl(Manager manager) {  
        super(manager);
        // <rtc-template block="initializer">
        m_inDataLatitude_val = new TimedDouble();
        m_inDataLatitude = new DataRef<TimedDouble>(m_inDataLatitude_val);
        m_InTimedDoubleDataLatitudeIn = new InPort<TimedDouble>("InTimedDoubleDataLatitude", m_inDataLatitude);
        m_inDataLongitude_val = new TimedDouble();
        m_inDataLongitude = new DataRef<TimedDouble>(m_inDataLongitude_val);
        m_InTimedDoubleDataLongitudeIn = new InPort<TimedDouble>("InTimedDoubleDataLongitude", m_inDataLongitude);
        m_inDataAltitude_val = new TimedDouble();
        m_inDataAltitude = new DataRef<TimedDouble>(m_inDataAltitude_val);
        m_InTimedDoubleDataAltitudeIn = new InPort<TimedDouble>("InTimedDoubleDataAltitude", m_inDataAltitude);
        m_inDataError_val = new TimedDouble();
        m_inDataError = new DataRef<TimedDouble>(m_inDataError_val);
        m_InTimedDoubleDataErrorIn = new InPort<TimedDouble>("InTimedDoubleDataError", m_inDataError);
        m_inDataHeading_val = new TimedDouble();
        m_inDataHeading = new DataRef<TimedDouble>(m_inDataHeading_val);
        m_InTimedDoubleDataHeadingIn = new InPort<TimedDouble>("InTimedDoubleDataHeading", m_inDataHeading);
        m_inDataSpeed_val = new TimedDouble();
        m_inDataSpeed = new DataRef<TimedDouble>(m_inDataSpeed_val);
        m_InTimedDoubleDataSpeedIn = new InPort<TimedDouble>("InTimedDoubleDataSpeed", m_inDataSpeed);
        m_outGPSData_val = new GPSData();
        m_outGPSData = new DataRef<GPSData>(m_outGPSData_val);
        m_OutTimedGPSDataOut = new OutPort<GPSData>("OutTimedGPSData", m_outGPSData);
        m_outPositionSeq_val = new TimedDoubleSeq();
        m_outPositionSeq = new DataRef<TimedDoubleSeq>(m_outPositionSeq_val);
        m_OutTimedPositionSeqOut = new OutPort<TimedDoubleSeq>("OutTimedPositionSeq", m_outPositionSeq);
        m_outDoubleLatitude_val = new TimedDouble();
        m_outDoubleLatitude = new DataRef<TimedDouble>(m_outDoubleLatitude_val);
        m_OutTimedDoubleDataLatitudeOut = new OutPort<TimedDouble>("OutTimedDoubleDataLatitude", m_outDoubleLatitude);
        m_outDoubleLongitude_val = new TimedDouble();
        m_outDoubleLongitude = new DataRef<TimedDouble>(m_outDoubleLongitude_val);
        m_OutTimedDoubleDataLongitudeOut = new OutPort<TimedDouble>("OutTimedDoubleDataLongitude", m_outDoubleLongitude);
        m_outDoubleAltitude_val = new TimedDouble();
        m_outDoubleAltitude = new DataRef<TimedDouble>(m_outDoubleAltitude_val);
        m_OutTimedDoubleDataAltitudeOut = new OutPort<TimedDouble>("OutTimedDoubleDataAltitude", m_outDoubleAltitude);
        m_outDoubleError_val = new TimedDouble();
        m_outDoubleError = new DataRef<TimedDouble>(m_outDoubleError_val);
        m_OutTimedDoubleDataErrorOut = new OutPort<TimedDouble>("OutTimedDoubleDataError", m_outDoubleError);
        m_outDoubleHeading_val = new TimedDouble();
        m_outDoubleHeading = new DataRef<TimedDouble>(m_outDoubleHeading_val);
        m_OutTimedDoubleDataHeadingOut = new OutPort<TimedDouble>("OutTimedDoubleDataHeading", m_outDoubleHeading);
        m_outDataSpeed_val = new TimedDouble();
        m_outDataSpeed = new DataRef<TimedDouble>(m_outDataSpeed_val);
        m_OutTimedDoubleDataSpeedOut = new OutPort<TimedDouble>("OutTimedDoubleDataSpeed", m_outDataSpeed);
        // </rtc-template>

    }

    /*!
     *
     * The initialize action (on CREATED->ALIVE transition)
     * formaer rtc_init_entry() 
     *
     * @return RTC::ReturnCode_t
     * 
     * 
     */
    @Override
    protected ReturnCode_t onInitialize() {
        // Registration: InPort/OutPort/Service
        // <rtc-template block="registration">
        // Set InPort buffers
        addInPort("InTimedDoubleDataLatitude", m_InTimedDoubleDataLatitudeIn);
        addInPort("InTimedDoubleDataLongitude", m_InTimedDoubleDataLongitudeIn);
        addInPort("InTimedDoubleDataAltitude", m_InTimedDoubleDataAltitudeIn);
        addInPort("InTimedDoubleDataError", m_InTimedDoubleDataErrorIn);
        addInPort("InTimedDoubleDataHeading", m_InTimedDoubleDataHeadingIn);
        addInPort("InTimedDoubleDataSpeed", m_InTimedDoubleDataSpeedIn);
        
        // Set OutPort buffer
        addOutPort("OutTimedGPSData", m_OutTimedGPSDataOut);
        addOutPort("OutTimedPositionSeq", m_OutTimedPositionSeqOut);
        addOutPort("OutTimedDoubleDataLatitude", m_OutTimedDoubleDataLatitudeOut);
        addOutPort("OutTimedDoubleDataLongitude", m_OutTimedDoubleDataLongitudeOut);
        addOutPort("OutTimedDoubleDataAltitude", m_OutTimedDoubleDataAltitudeOut);
        addOutPort("OutTimedDoubleDataError", m_OutTimedDoubleDataErrorOut);
        addOutPort("OutTimedDoubleDataHeading", m_OutTimedDoubleDataHeadingOut);
        addOutPort("OutTimedDoubleDataSpeed", m_OutTimedDoubleDataSpeedOut);
        // </rtc-template>
        bindParameter("ImportTextName", m_configTextName, "null");
        bindParameter("ChangedOfBaseline", m_configBaseline, "0");
        bindParameter("VariableToRounding", m_configRounding, "1");
        bindParameter("PitchTime", m_PitchTime, "1000");

        variableInitialization();

        return super.onInitialize();
    }

    /***
     *
     * The finalize action (on ALIVE->END transition)
     * formaer rtc_exiting_entry()
     *
     * @return RTC::ReturnCode_t
     * 
     * 
     */
//    @Override
//    protected ReturnCode_t onFinalize() {
//        return super.onFinalize();
//    }

    /***
     *
     * The startup action when ExecutionContext startup
     * former rtc_starting_entry()
     *
     * @param ec_id target ExecutionContext Id
     *
     * @return RTC::ReturnCode_t
     * 
     * 
     */
//    @Override
//    protected ReturnCode_t onStartup(int ec_id) {
//        return super.onStartup(ec_id);
//    }

    /***
     *
     * The shutdown action when ExecutionContext stop
     * former rtc_stopping_entry()
     *
     * @param ec_id target ExecutionContext Id
     *
     * @return RTC::ReturnCode_t
     * 
     * 
     */
//    @Override
//    protected ReturnCode_t onShutdown(int ec_id) {
//        return super.onShutdown(ec_id);
//    }

    /***
     * 1:Start time of execution is initialized.
     * 2:Read/write processing of files.
     *
     * The activated action (Active state entry action)
     * former rtc_active_entry()
     *
     * @param ec_id target ExecutionContext Id
     *
     * @return RTC::ReturnCode_t
     * 
     * @pre 2:If configTextName is inputted.
     * 
     */
    @Override
    protected ReturnCode_t onActivated(int ec_id) {
       	startTime = Calendar.getInstance();
    	startYear = startTime.get(Calendar.YEAR);
    	startMonth = startTime.get(Calendar.MONTH)+1;
    	startDay = startTime.get(Calendar.DAY_OF_MONTH);
    	startHour = startTime.get(Calendar.HOUR_OF_DAY);
    	startMinute = startTime.get(Calendar.MINUTE);
    	getTextFile();
        return super.onActivated(ec_id);
    }

    /***
     * Variable for reading file is initialized.
     *
     * The deactivated action (Active state exit action)
     * former rtc_active_exit()
     *
     * @param ec_id target ExecutionContext Id
     *
     * @return RTC::ReturnCode_t
     * 
     * @pre If is reading a text file.
     * 
     */
    @Override
    protected ReturnCode_t onDeactivated(int ec_id) {
    	
    	variableInitialization();
    	
        return super.onDeactivated(ec_id);
    }

    /***
     * 1 : Write data of InPort to textfile. And the data is
     * outputted to OutPort.
     * 2 : It writes to OutPort periodically external data.(If
     * output data is EOF, rewrite output data to final sensor
     * value.)
     * 3 : Rounding of the input data.
     * 4 : Output of error from the baseline.
     *
     * The execution action that is invoked periodically
     * former rtc_active_do()
     *
     * @param ec_id target ExecutionContext Id
     *
     * @return RTC::ReturnCode_t
     * 
     * @pre 1 : Default setting.
     * 2 : If is reading a text file.
     * 3 : Default setting.
     * 4 : if the check box is checked.
     * 
     */
    @Override
    protected ReturnCode_t onExecute(int ec_id) {

    	ioControl();

        return super.onExecute(ec_id);
    }

    /***
     *
     * The aborting action when main logic error occurred.
     * former rtc_aborting_entry()
     *
     * @param ec_id target ExecutionContext Id
     *
     * @return RTC::ReturnCode_t
     * 
     * 
     */
//  @Override
//  public ReturnCode_t onAborting(int ec_id) {
//      return super.onAborting(ec_id);
//  }

    /***
     *
     * The error action in ERROR state
     * former rtc_error_do()
     *
     * @param ec_id target ExecutionContext Id
     *
     * @return RTC::ReturnCode_t
     * 
     * 
     */
//    @Override
//    public ReturnCode_t onError(int ec_id) {
//        return super.onError(ec_id);
//    }

    /***
     *
     * The reset action that is invoked resetting
     * This is same but different the former rtc_init_entry()
     *
     * @param ec_id target ExecutionContext Id
     *
     * @return RTC::ReturnCode_t
     * 
     * 
     */
//    @Override
//    protected ReturnCode_t onReset(int ec_id) {
//        return super.onReset(ec_id);
//    }

    /***
     *
     * The state update action that is invoked after onExecute() action
     * no corresponding operation exists in OpenRTm-aist-0.2.0
     *
     * @param ec_id target ExecutionContext Id
     *
     * @return RTC::ReturnCode_t
     * 
     * 
     */
//    @Override
//    protected ReturnCode_t onStateUpdate(int ec_id) {
//        return super.onStateUpdate(ec_id);
//    }

    /***
     *
     * The action that is invoked when execution context's rate is changed
     * no corresponding operation exists in OpenRTm-aist-0.2.0
     *
     * @param ec_id target ExecutionContext Id
     *
     * @return RTC::ReturnCode_t
     * 
     * 
     */
//    @Override
//    protected ReturnCode_t onRateChanged(int ec_id) {
//        return super.onRateChanged(ec_id);
//    }
//
    
    /**
     * データの初期化を行う
     */
    private void variableInitialization(){
        textName = null;
        text = null;
        bw = null;
        can_read = false;
        read_flag = false;
        read_counter =0;
        dataLine = null;
        baseline = null;
		changeBaseline_flag = false;
		getInport_flag = false;
		arrayDataLatitude = new double[1];
		arrayDataLongitude = new double[1];
		arrayDataAltitude = new double[1];
    }
    
	/**
	 * 入出力コントロール
	 * データが入力されてから出力されるまでの一連の動作を持つ
	 */
	private void ioControl(){
		Object[] dataArray = new Object[10];
		TimedDouble subDataLatitude = null;
		TimedDouble subDataLongitude = null;
		TimedDouble subDataAltitude = null;
		TimedDouble subDataError = null;
		TimedDouble subDataHeading = null;
		TimedDouble subDataSpeed = null;
		
		if(read_flag == true){
			if(can_read != true)dataLine = readText(text);
			dataArray = getOutData(dataLine[read_counter].split(","));

			if(dataLine[read_counter + 1]!=null) read_counter++;
		}else{
			if(m_InTimedDoubleDataLatitudeIn.isNew() && m_InTimedDoubleDataLongitudeIn.isNew() && m_InTimedDoubleDataAltitudeIn.isNew()){
				m_InTimedDoubleDataLatitudeIn.read();
				m_InTimedDoubleDataLongitudeIn.read();
				m_InTimedDoubleDataAltitudeIn.read();
				m_InTimedDoubleDataErrorIn.read();
				m_InTimedDoubleDataHeadingIn.read();
				m_InTimedDoubleDataSpeedIn.read();
				
				subDataLatitude = m_InTimedDoubleDataLatitudeIn.extract();
				subDataLongitude = m_InTimedDoubleDataLongitudeIn.extract();
				subDataAltitude = m_InTimedDoubleDataAltitudeIn.extract();
				subDataError = m_InTimedDoubleDataErrorIn.extract();
				subDataHeading = m_InTimedDoubleDataHeadingIn.extract();
				subDataSpeed = m_InTimedDoubleDataSpeedIn.extract();

				
				System.out.println("In data Lat:"+subDataLatitude.data+" Lon:"+subDataLongitude.data+" Alt:"+subDataAltitude.data
						+" Err:"+subDataError.data+" Hea:"+subDataHeading.data+" Spe:"+subDataSpeed.data);
				
				getInport_flag = true;
			}
			
			if(getInport_flag == true){
				dataArray[0] = new Double(subDataLatitude.data);
				dataArray[1] = new Double(subDataLongitude.data);
				dataArray[2] = new Double(subDataAltitude.data);
				dataArray[3] = new Double(subDataError.data);
				dataArray[4] = new Double(subDataHeading.data);
				dataArray[5] = new Double(subDataSpeed.data);

				dataArray[6] = new Integer(subDataLatitude.tm.sec);
				dataArray[7] = new Integer(subDataLatitude.tm.nsec);
				dataArray[8] = new Integer(subDataLongitude.tm.sec);
				dataArray[9] = new Integer(subDataLongitude.tm.nsec);
				 
				writeText(text,stringData(dataArray));
			}
		}
		
		if(read_flag == true || getInport_flag == true){
			getInport_flag = false;
			
			dataArray = roundingData(getErrorFromBaseline(dataArray));
			
			setOutPort(dataArray);
			if(read_flag == true){
				try{
			    Thread.sleep(m_PitchTime.value); //単位[msec]
			    }catch(InterruptedException e){}
			}

		}
	}
	
	/**
	 * データの丸め処理を行う
	 * @param array　現在のデータ配列
	 * @return　丸め処理後のデータ配列
	 */
	private Object[] roundingData(Object array[]){

		int configRounding = m_configRounding.getValue();
		double subDataLatitude = new Double((double)array[0]);
		double subDataLongitude = new Double((double)array[1]);
		double subDataAltitude = new Double((double)array[2]);
		arrayDataLatitude = insertData(arrayDataLatitude,subDataLatitude,configRounding);
		arrayDataLongitude = insertData(arrayDataLongitude,subDataLongitude,configRounding);
		arrayDataAltitude = insertData(arrayDataAltitude,subDataAltitude,configRounding);
		
		array[0] = new Double(average(arrayDataLatitude));
		array[1] = new Double(average(arrayDataLongitude));
		array[2] = new Double(average(arrayDataAltitude));
		
		return array;
	}
	
	/**
	 * 現在のデータに対する基準線からの偏差を返す
	 * @param array　現在のデータの入った配列
	 * @return　偏差の配列
	 */
	private Object[] getErrorFromBaseline(Object array[]){
		if(m_configBaseline.value == 0){
			changeBaseline_flag = false;
		}else{
			if(changeBaseline_flag == false){
				baseline = Arrays.copyOf(array, 3);
				changeBaseline_flag = true;
			}
			for(int i=0;i<3;i++)array[i] = new Double((double)array[i] - (double)baseline[i]);
		}
		return array;
	}
	
	/**
	 * 読み込みデータの一行から、出力したいデータを抜き出す
	 * @param now_line　読み込みデータの一行
	 * @return　出力したいデータ
	 */
	private Object[] getOutData(String now_line[]){
		Object[] array = new Object[10];
		array[0] = new Double(Double.valueOf(now_line[1]));
		array[1] = new Double(Double.valueOf(now_line[3]));
		array[2] = new Double(Double.valueOf(now_line[5]));
		array[3] = new Double(Double.valueOf(now_line[7]));
		array[4] = new Double(Double.valueOf(now_line[9]));
		array[5] = new Double(Double.valueOf(now_line[11]));
		array[6] = new Integer(Integer.valueOf(now_line[13]));
		array[7] = new Integer(Integer.valueOf(now_line[15]));
		array[8] = new Integer(Integer.valueOf(now_line[17]));
		array[9] = new Integer(Integer.valueOf(now_line[19]));
		return array;
	}
	
	/**
	 * OurPortにデータをセットする
	 * @param array　セットしたいデータの入った配列
	 */
	private void setOutPort(Object array[]){
		
		double lat = (double)array[0];
		double lon = (double)array[1];
		double alt = (double)array[2];
		double err = (double)array[3];
		double hea = (double)array[4];
		double spe = (double)array[5];
		double[] lla = {lat,lon,alt};
		
		Time tm = new Time((int)array[6],(int)array[7]);
		GPSTime gpstm = new GPSTime((int)array[8],(int)array[9]);
		
		System.out.println("Out data Lat:"+lat+" Lon:"+lon+" Alt:"+alt
				+" Err:"+err+" Hea:"+hea+" Spe:"+spe);
        try {
        	GPSData subGPSData = new GPSData(tm,gpstm,lat,lon,alt,err,0.0,hea,spe,0.0,(short)0,GPSFixType.GPS_FIX_NONE);
        	TimedDouble subDataLatitude = new TimedDouble(tm,lat);
        	TimedDouble subDataLongitude = new TimedDouble(tm,lon);
        	TimedDouble subDataAltitude = new TimedDouble(tm,alt);
        	TimedDouble subDataError = new TimedDouble(tm,err);
        	TimedDouble subDataHeading = new TimedDouble(tm,hea);
        	TimedDouble subDataSpeed = new TimedDouble(tm,spe);
        	TimedDoubleSeq subDoubleSeq = new TimedDoubleSeq(tm,lla);
        	
			m_OutTimedGPSDataOut.write(subGPSData);	
			m_OutTimedDoubleDataLatitudeOut.write(subDataLatitude);
			m_OutTimedDoubleDataLongitudeOut.write(subDataLongitude);
			m_OutTimedDoubleDataAltitudeOut.write(subDataAltitude);
			m_OutTimedDoubleDataErrorOut.write(subDataError);
			m_OutTimedDoubleDataHeadingOut.write(subDataHeading);
			m_OutTimedDoubleDataSpeedOut.write(subDataSpeed);
			m_OutTimedPositionSeqOut.write(subDoubleSeq);
        } catch(Exception e){
        	
        }
	}
	
	/**
	 * テキストファイル名を取得する
	 * コンフィギュレーションパラメータでファイル名が指定されていればそのファイル名を、
	 * 指定がなければ、Active時の時間を元にしたファイル名を取得する
	 */
	private void getTextFile(){
		if(m_configTextName.value.equals("") != true && m_configTextName.value.equals("null") != true && m_configTextName.value.equals(null) != true){
			System.out.println(m_configTextName.value);
			textName = m_configTextName.value;
			text = new File(textName);
			dataLine = readText(text);
			read_flag = true;
		}else{
			textName = createFilename();
			text = new File(textName);
			System.out.println("Create File:"+textName);
			read_flag = false;
		}
	}
	
	/**
	 * Activeにされた時間を元にStringを作成
	 * @return　作成されたString
	 */
	private String createFilename(){
		String sMonth = Integer.toString(startMonth);
		String sDay = Integer.toString(startDay);
		String sHour = Integer.toString(startHour);
		String sMinute = Integer.toString(startMinute);
		
		if(startMonth < 10) sMonth = "0"+sMonth;
		if(startDay < 10) sDay = "0"+sDay;
		if(startHour < 10) sHour = "0"+sHour;
		if(startMinute < 10) sMinute = "0"+sMinute;
		
		String Name = "GetGPS_"+startYear+"_"+sMonth+"_"+sDay+"_"+sHour+"_"+sMinute+".txt";
		return Name;
	}
	
	/**
	 * テキストファイルを読み込み、そのテキストファイルの内容を返す
	 * @param fileName　読み込みたいテキストファイル名
	 * @return　テキストファイルの内容 (一行毎の配列、最後のデータはnull）
	 */
	private String[] readText(File fileName){
		String[] line = new String[1];
		String setDataStr = null;
		try{
			FileInputStream fis = new FileInputStream(fileName);
			InputStreamReader isr = new InputStreamReader(fis);
			BufferedReader br = new BufferedReader(isr);
			while(true){
				if(line[line.length-1] != null)line = Arrays.copyOf(line, line.length + 1);
				if((setDataStr = br.readLine()) != null){
					line[line.length-1] = setDataStr;
					setDataStr = null;
				}else{
					break;
				}
			}
			read_counter=0;
			can_read = true;
		}catch(Exception e){
			can_read = false;
			System.out.println(e.getMessage()+"ファイルが開けません");
		}
		return line;
	}
	
	/**
	 * テキストファイルにデータを書き込む
	 * @param fileName　書き込む対称のテキストファイル
	 * @param str　書き込みたいデータ
	 */
	private void writeText(File fileName,String str){
		bw = openFile(fileName);
		try{
			bw.write(str);
			bw.close();
		}catch(IOException e){			
			System.out.println(e.getMessage());
		}
	}
	
	/**
	 * 書き込み用テキストファイルを取得する
	 * @param text　テキストファイル名
	 * @return　テキストファイルのBufferedWriteデータ
	 */
	private BufferedWriter openFile(File text){
		try{
			bw = new BufferedWriter(new FileWriter(text,true));
		}catch(FileNotFoundException e){
			System.out.println("File Not Found");
		}catch(IOException e){
			System.out.println(e.getMessage());
		}
		return bw;
	}

	/**
	 * テキストに書き込むためのStringを作成する
	 * @param array　書き込むデータの入った配列
	 * @return　作成したString
	 */
	private String stringData(Object array[]){
		String str="Latitudedata:,"+(double)array[0]+",Longitudedata:,"+(double)array[1]+",Altitudedata:,"+(double)array[2]+
				",Error:,"+(double)array[3]+",Heading:,"+(double)array[4]+",Speed:,"+(double)array[5]+
				",Time(sec):,"+(int)array[6]+",Time(nsec):,"+(int)array[7]+",GPSTime(sec):,"+(int)array[8]+",GPSTime(nsec):,"+(int)array[9]+",\n";
		return str;
	}
	
	/**
	 * 丸め処理のための平均元配列にデータを挿入
	 * @param array　平均元配列
	 * @param newData　挿入したいデータ
	 * @param round　丸め処理の枠数
	 * @return　挿入処理後の平均元配列
	 */
	private double[] insertData(double array[],double newData,int round){
		if(array.length < round){
			array = Arrays.copyOf(array, array.length+1);
			array[array.length-1] = newData;
		}else{
			array = Arrays.copyOf(Arrays.copyOfRange(array, 1, array.length),round);
			array[round-1] = newData;
		}
		return array;
	}

	
	/**
	 * 数値配列を受け取り、その配列の平均値を返す
	 * @param array
	 * @return 配列の平均値 (double)
	 */
	private double average(double array[]){
		double sum = 0.0;
		int counter = 0;
		
		for(int i=0;i<array.length;i++){
			sum += array[i];
			counter++;
		}
		return sum/counter;
	}
    

    
    // Configuration variable declaration
	// <rtc-template block="config_declare">
    /*!
     * File name you want to load.
     * - Name: configTextName configTextName
     * - DefaultValue: null
     */
    protected StringHolder m_configTextName = new StringHolder();
    /*!
     * Change the baseline of the sensor to current value.
     * - Name: configBaseline configBaseline
     * - DefaultValue: 0
     */
    protected IntegerHolder m_configBaseline = new IntegerHolder();
    /*!
     * Variable rounding of input data.
     * Sensor sensitivity reduces variable increases.
     * - Name: configRounding configRounding
     * - DefaultValue: 1
     * - Range: x>0
     */
    protected IntegerHolder m_configRounding = new IntegerHolder();
    /*!
     * cycle of the read mode.
     * - Name: int PitchTime
     * - DefaultValue: 1000
     * - Unit: msec
     * - Range: 0 <= x
     */
    protected IntegerHolder m_PitchTime = new IntegerHolder();
	// </rtc-template>

    // DataInPort declaration
    // <rtc-template block="inport_declare">
    protected TimedDouble m_inDataLatitude_val;
    protected DataRef<TimedDouble> m_inDataLatitude;
    /*!
     * Output dataLatitude from "GetGPS" is input.
     * - Type: TimedDouble
     * - Number: 1
     */
    protected InPort<TimedDouble> m_InTimedDoubleDataLatitudeIn;

    protected TimedDouble m_inDataLongitude_val;
    protected DataRef<TimedDouble> m_inDataLongitude;
    /*!
     * Output dataLongitude from "GetGPS" is input.
     * - Type: TimedDouble
     * - Number: 1
     */
    protected InPort<TimedDouble> m_InTimedDoubleDataLongitudeIn;

    protected TimedDouble m_inDataAltitude_val;
    protected DataRef<TimedDouble> m_inDataAltitude;
    /*!
     * Output dataAltitude from "GetGPS" is input.
     * - Type: TimedDouble
     * - Number: 1
     */
    protected InPort<TimedDouble> m_InTimedDoubleDataAltitudeIn;

    protected TimedDouble m_inDataError_val;
    protected DataRef<TimedDouble> m_inDataError;
    /*!
     * Output dataError from "GetGPS" is input.
     * - Type: TimedDouble
     * - Number: 1
     */
    protected InPort<TimedDouble> m_InTimedDoubleDataErrorIn;

    protected TimedDouble m_inDataHeading_val;
    protected DataRef<TimedDouble> m_inDataHeading;
    /*!
     * Output dataHeading from "GetGPS" is input.
     * - Type: TimedDouble
     * - Number: 1
     */
    protected InPort<TimedDouble> m_InTimedDoubleDataHeadingIn;

    protected TimedDouble m_inDataSpeed_val;
    protected DataRef<TimedDouble> m_inDataSpeed;
    /*!
     * Output dataSpeed from "GetGPS" is input.
     * - Type: TimedDouble
     * - Number: 1
     */
    protected InPort<TimedDouble> m_InTimedDoubleDataSpeedIn;

    
    // </rtc-template>

    // DataOutPort declaration
    // <rtc-template block="outport_declare">
    protected GPSData m_outGPSData_val;
    protected DataRef<GPSData> m_outGPSData;
    /*!
     * GPS sensor data of the filtered are output.
     * NotSupport:verticalError,verticalSpeed,numSatellites,fixTyp
     * e
     * - Type: GPSData
     * - Number: 1
     */
    protected OutPort<GPSData> m_OutTimedGPSDataOut;

    protected TimedDoubleSeq m_outPositionSeq_val;
    protected DataRef<TimedDoubleSeq> m_outPositionSeq;
    /*!
     * GPS sensor data in the Latitude, Longitude and Altitude of
     * the filtered are output.
     * Seq[0] = Latitude;
     * Seq[1] = Longitude;
     * Seq[2] = Altitude;
     * - Type: TimedDoubleSeq
     * - Number: 3
     */
    protected OutPort<TimedDoubleSeq> m_OutTimedPositionSeqOut;

    protected TimedDouble m_outDoubleLatitude_val;
    protected DataRef<TimedDouble> m_outDoubleLatitude;
    /*!
     * GPS sensor data in the Latitude of the filtered is output.
     * - Type: TimedDouble
     * - Number: 1
     */
    protected OutPort<TimedDouble> m_OutTimedDoubleDataLatitudeOut;

    protected TimedDouble m_outDoubleLongitude_val;
    protected DataRef<TimedDouble> m_outDoubleLongitude;
    /*!
     * GPS sensor data in the Longitude of the filtered is
     * output.
     * - Type: TimedDouble
     * - Number: 1
     */
    protected OutPort<TimedDouble> m_OutTimedDoubleDataLongitudeOut;

    protected TimedDouble m_outDoubleAltitude_val;
    protected DataRef<TimedDouble> m_outDoubleAltitude;
    /*!
     * GPS sensor data in the Altitude of the filtered is output.
     * - Type: TimedDouble
     * - Number: 1
     */
    protected OutPort<TimedDouble> m_OutTimedDoubleDataAltitudeOut;

    protected TimedDouble m_outDoubleError_val;
    protected DataRef<TimedDouble> m_outDoubleError;
    /*!
     * GPS sensor data in the Error of the filtered is output.
     * - Type: TimedDouble
     * - Number: 1
     */
    protected OutPort<TimedDouble> m_OutTimedDoubleDataErrorOut;

    protected TimedDouble m_outDoubleHeading_val;
    protected DataRef<TimedDouble> m_outDoubleHeading;
    /*!
     * GPS sensor data in the Heading of the filtered is output.
     * - Type: TimedDouble
     * - Number: 1
     */
    protected OutPort<TimedDouble> m_OutTimedDoubleDataHeadingOut;

    protected TimedDouble m_outDataSpeed_val;
    protected DataRef<TimedDouble> m_outDataSpeed;
    /*!
     * GPS sensor data in the Speed of the filtered is output.
     * - Type: TimedDouble
     * - Number: 1
     */
    protected OutPort<TimedDouble> m_OutTimedDoubleDataSpeedOut;

    
    // </rtc-template>

    // CORBA Port declaration
    // <rtc-template block="corbaport_declare">
    
    // </rtc-template>

    // Service declaration
    // <rtc-template block="service_declare">
    
    // </rtc-template>

    // Consumer declaration
    // <rtc-template block="consumer_declare">
    
    // </rtc-template>


}
