// -*- Java -*-
/*!
 * @file  ProximityFilterImpl.java
 * @brief Filtering of the ProximitySensor information
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

import RTC.Time;
import RTC.TimedDouble;
import RTC.TimedFloat;
import jp.go.aist.rtm.RTC.DataFlowComponentBase;
import jp.go.aist.rtm.RTC.Manager;
import jp.go.aist.rtm.RTC.port.InPort;
import jp.go.aist.rtm.RTC.port.OutPort;
import jp.go.aist.rtm.RTC.util.DataRef;
import jp.go.aist.rtm.RTC.util.StringHolder;
import jp.go.aist.rtm.RTC.util.IntegerHolder;
import RTC.ReturnCode_t;

/*!
 * @class ProximityFilterImpl
 * @brief Filtering of the ProximitySensor information
 *
 * Androidセンサ取得用アプリケーション「GetProximity」に対応するフィルタコンポーネント
 * 限定された型でしか送信することのできないセンサ情報に対し、
 * ProximitySensorに適したデータ型に変換して出力を行う。
 * センサ情報にノイズなどが乗りやすい場合には、ある程度の値の丸めを行うことができ、
 * センサの値の基準値０を現在のセンサ値に置き換えて出力することができる。
 * 入力ポートに入ってくる情報を、テキストファイルに出力することができ、
 * そのテキストファイルを読み込むことで、Android端末との通信を行っていない状態でも、
 * 擬似的にセンサ値を取得しているように出力することができる。
 *
 * In:ProximitySensorData
 * Port1:cm data (TimedDouble)
 * out:FilteredData
 * Port1:cm data (TimedDouble)
 * Port2:cm data (TimedFloat)
 *
 */
public class ProximityFilterImpl extends DataFlowComponentBase {

	/**
	 * 丸め処理用データ配列
	 */
    private double[] arrayDataX = new double[1];
    
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
	public ProximityFilterImpl(Manager manager) {  
        super(manager);
        // <rtc-template block="initializer">
        m_inData_val = new TimedDouble();
        m_inData = new DataRef<TimedDouble>(m_inData_val);
        m_InTimedDoubleDataIn = new InPort<TimedDouble>("InTimedDoubleData", m_inData);
        m_outDouble_val = new TimedDouble();
        m_outDouble = new DataRef<TimedDouble>(m_outDouble_val);
        m_OutTimedDoubleDataOut = new OutPort<TimedDouble>("OutTimedDoubleData", m_outDouble);
        m_outFloat_val = new TimedFloat();
        m_outFloat = new DataRef<TimedFloat>(m_outFloat_val);
        m_OutTimedFloatDataOut = new OutPort<TimedFloat>("OutTimedFloatData", m_outFloat);
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
        addInPort("InTimedDoubleData", m_InTimedDoubleDataIn);
        
        // Set OutPort buffer
        addOutPort("OutTimedDoubleData", m_OutTimedDoubleDataOut);
        addOutPort("OutTimedFloatData", m_OutTimedFloatDataOut);
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
		arrayDataX = new double[1];
    }
    
	/**
	 * 入出力コントロール
	 * データが入力されてから出力されるまでの一連の動作を持つ
	 */
	private void ioControl(){
		Object[] dataArray = new Object[5];
		TimedDouble subDataX = null;
		
		if(read_flag == true){
			if(can_read != true)dataLine = readText(text);
			dataArray = getOutData(dataLine[read_counter].split(","));

			if(dataLine[read_counter + 1]!=null) read_counter++;
		}else{
			if(m_InTimedDoubleDataIn.isNew()){
				m_InTimedDoubleDataIn.read();
				
				subDataX = m_InTimedDoubleDataIn.extract();
				
				System.out.println("In Data:"+subDataX.data);
				
				getInport_flag = true;
			}
			
			if(getInport_flag == true){
				dataArray[0] = new Double(subDataX.data);
				dataArray[1] = new Integer(subDataX.tm.sec);
				dataArray[2] = new Integer(subDataX.tm.nsec);
				 
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
		double subDataX = new Double((double)array[0]);
		arrayDataX = insertData(arrayDataX,subDataX,configRounding);
		
		array[0] = new Double(average(arrayDataX));
		
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
		Object[] array = new Object[5];
		array[0] = new Double(Double.valueOf(now_line[1]));
		array[1] = new Integer(Integer.valueOf(now_line[3]));
		array[2] = new Integer(Integer.valueOf(now_line[5]));
		return array;
	}
	
	/**
	 * OurPortにデータをセットする
	 * @param array　セットしたいデータの入った配列
	 */
	private void setOutPort(Object array[]){
		
		double x = (double)array[0];
		Time tm = new Time((int)array[1],(int)array[2]);
		
		float xf = (float)x;
		
		System.out.println("Out Data:"+x);
        try {
        	TimedDouble subDataX = new TimedDouble(tm,x);
        	TimedFloat subFloatX = new TimedFloat(tm,xf);
        	
			m_OutTimedDoubleDataOut.write(subDataX);
			m_OutTimedFloatDataOut.write(subFloatX);
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
		
		String Name = "GetProximity_"+startYear+"_"+sMonth+"_"+sDay+"_"+sHour+"_"+sMinute+".txt";
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
		String str="data:,"+(double)array[0]+",Time(sec):,"+(int)array[1]+",Time(nsec):,"+(int)array[2]+",\n";
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
    protected TimedDouble m_inData_val;
    protected DataRef<TimedDouble> m_inData;
    /*!
     * Output dataX from "GetProximity" is input.
     * - Type: TimedDouble
     * - Number: 1
     * - Unit: cm
     */
    protected InPort<TimedDouble> m_InTimedDoubleDataIn;

    
    // </rtc-template>

    // DataOutPort declaration
    // <rtc-template block="outport_declare">
    protected TimedDouble m_outDouble_val;
    protected DataRef<TimedDouble> m_outDouble;
    /*!
     * Proximity sensor data of the filtered is output.
     * - Type: TimedDouble
     * - Number: 1
     * - Unit: cm
     */
    protected OutPort<TimedDouble> m_OutTimedDoubleDataOut;

    protected TimedFloat m_outFloat_val;
    protected DataRef<TimedFloat> m_outFloat;
    /*!
     * Proximity sensor data of the filtered is output.
     * - Type: TimedFloat
     * - Number: 1
     * - Unit: cm
     */
    protected OutPort<TimedFloat> m_OutTimedFloatDataOut;

    
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
