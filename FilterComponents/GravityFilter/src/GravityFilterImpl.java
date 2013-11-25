// -*- Java -*-
/*!
 * @file  GravityFilterImpl.java
 * @brief Filtering of the GravitySensor information
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
import RTC.Time;
import RTC.TimedDouble;
import RTC.TimedAcceleration3D;
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
 * @class GravityFilterImpl
 * @brief Filtering of the GravitySensor information
 *
 * Android�Z���T�擾�p�A�v���P�[�V�����uGetGravity�v�ɑΉ�����t�B���^�R���|�[�l���g
 * ���肳�ꂽ�^�ł������M���邱�Ƃ̂ł��Ȃ��Z���T���ɑ΂��A
 * AccelerationSensor�ɓK�����f�[�^�^�ɕϊ����ďo�͂��s���B
 * �Z���T���Ƀm�C�Y�Ȃǂ����₷���ꍇ�ɂ́A������x�̒l�̊ۂ߂��s�����Ƃ��ł��A
 * �Z���T�̒l�̊�l�O�����݂̃Z���T�l�ɒu�������ďo�͂��邱�Ƃ��ł���B
 * ���̓|�[�g�ɓ����Ă�������A�e�L�X�g�t�@�C���ɏo�͂��邱�Ƃ��ł��A
 * ���̃e�L�X�g�t�@�C����ǂݍ��ނ��ƂŁAAndroid�[���Ƃ̒ʐM���s���Ă��Ȃ���Ԃł��A
 * �[���I�ɃZ���T�l���擾���Ă���悤�ɏo�͂��邱�Ƃ��ł���B
 *
 * In:GravitySensorData
 * Port1:x-axis data (TimedDouble)
 * Port2:y-axis data (TimedDouble)
 * Port3:z-axis data (TimedDouble)
 * out:FilteredData
 * Port1:Three axis data x, y, and z (TimedAcceleration3D)
 * Port2:x-axis data (TimedDouble)
 * Port3:y-axis data (TimedDouble)
 * Port4:z-axis data (TimedDouble)
 * Port5:Three axis data x, y, and z(TimedDoubelSeq)
 * Port6:x-axis data (TimedFloat)
 * Port7:y-axis data (TimedFloat)
 * Port8:z-axis data (TimedFloat)
 * Port9:Three axis data x, y, and z(TimedFloatSeq)
 *
 */
public class GravityFilterImpl extends DataFlowComponentBase {

	/**
	 * �ۂߏ����p�f�[�^�z��
	 */
    private double[] arrayDataX = new double[1];
    private double[] arrayDataY = new double[1];
    private double[] arrayDataZ = new double[1];
    
    /**
     * �e�L�X�g���o�͗p�ϐ�
     */
    private String textName = null;
    private File text = null;
    private BufferedWriter bw = null;
    private boolean can_read = false;	//�t�@�C���̓ǂݍ��݂������������Ƃ�����boolean : true-����
    private boolean read_flag = false;	//�e�L�X�g�̓ǂݍ��݃��[�h���s���Ă��邱�Ƃ�����boolean : true-�ǂݍ���  false-�����o��
    private int read_counter =0;		//�ǂݍ��ݎ��̃J�E���^
    private String[] dataLine;	//�ǂݍ��񂾃e�L�X�g����ێ�
	private boolean getInport_flag = false;	//���̓|�[�g�̏�񂪍X�V���ꂽ�ꍇ��true
    
    /**
     * ���s�J�n���ԗp�ϐ�
     * �i�����o�����Ƀt�@�C�����Ɏg�p�j
     */
    private Calendar startTime = null;
    private int startYear = 0;
    private int startMonth = 0;
    private int startDay = 0;
    private int startHour = 0;
    private int startMinute = 0;
    
    /**
     * ��l���������p�ϐ�
     */
    private boolean changeBaseline_flag = false;
    private Object[] baseline;
	
  /*!
   * @brief constructor
   * @param manager Maneger Object
   */
	public GravityFilterImpl(Manager manager) {  
        super(manager);
        // <rtc-template block="initializer">
        m_inDataX_val = new TimedDouble();
        m_inDataX = new DataRef<TimedDouble>(m_inDataX_val);
        m_InTimedDoubleDataXIn = new InPort<TimedDouble>("InTimedDoubleDataX", m_inDataX);
        m_inDataY_val = new TimedDouble();
        m_inDataY = new DataRef<TimedDouble>(m_inDataY_val);
        m_InTimedDoubleDataYIn = new InPort<TimedDouble>("InTimedDoubleDataY", m_inDataY);
        m_inDataZ_val = new TimedDouble();
        m_inDataZ = new DataRef<TimedDouble>(m_inDataZ_val);
        m_InTimedDoubleDataZIn = new InPort<TimedDouble>("InTimedDoubleDataZ", m_inDataZ);
        m_outData3D_val = new TimedAcceleration3D();
        m_outData3D = new DataRef<TimedAcceleration3D>(m_outData3D_val);
        m_OutTimedAcceleration3DOut = new OutPort<TimedAcceleration3D>("OutTimedAcceleration3D", m_outData3D);
        m_outDoubleX_val = new TimedDouble();
        m_outDoubleX = new DataRef<TimedDouble>(m_outDoubleX_val);
        m_OutTimedDoubleDataXOut = new OutPort<TimedDouble>("OutTimedDoubleDataX", m_outDoubleX);
        m_outDoubleY_val = new TimedDouble();
        m_outDoubleY = new DataRef<TimedDouble>(m_outDoubleY_val);
        m_OutTimedDoubleDataYOut = new OutPort<TimedDouble>("OutTimedDoubleDataY", m_outDoubleY);
        m_outDoubleZ_val = new TimedDouble();
        m_outDoubleZ = new DataRef<TimedDouble>(m_outDoubleZ_val);
        m_OutTimedDoubleDataZOut = new OutPort<TimedDouble>("OutTimedDoubleDataZ", m_outDoubleZ);
        m_outDoubleSeq_val = new TimedDoubleSeq();
        m_outDoubleSeq = new DataRef<TimedDoubleSeq>(m_outDoubleSeq_val);
        m_OutTimedDoubleDataSeqOut = new OutPort<TimedDoubleSeq>("OutTimedDoubleDataSeq", m_outDoubleSeq);
        m_outFloatX_val = new TimedFloat();
        m_outFloatX = new DataRef<TimedFloat>(m_outFloatX_val);
        m_OutTimedFloatDataXOut = new OutPort<TimedFloat>("OutTimedFloatDataX", m_outFloatX);
        m_outFloatY_val = new TimedFloat();
        m_outFloatY = new DataRef<TimedFloat>(m_outFloatY_val);
        m_OutTimedFloatDataYOut = new OutPort<TimedFloat>("OutTimedFloatDataY", m_outFloatY);
        m_outFloatZ_val = new TimedFloat();
        m_outFloatZ = new DataRef<TimedFloat>(m_outFloatZ_val);
        m_OutTimedFloatDataZOut = new OutPort<TimedFloat>("OutTimedFloatDataZ", m_outFloatZ);
        m_outFloatSeq_val = new TimedFloatSeq();
        m_outFloatSeq = new DataRef<TimedFloatSeq>(m_outFloatSeq_val);
        m_OutTimedFloatDataSeqOut = new OutPort<TimedFloatSeq>("OutTimedFloatDataSeq", m_outFloatSeq);
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
        addInPort("InTimedDoubleDataX", m_InTimedDoubleDataXIn);
        addInPort("InTimedDoubleDataY", m_InTimedDoubleDataYIn);
        addInPort("InTimedDoubleDataZ", m_InTimedDoubleDataZIn);
        
        // Set OutPort buffer
        addOutPort("OutTimedAcceleration3D", m_OutTimedAcceleration3DOut);
        addOutPort("OutTimedDoubleDataX", m_OutTimedDoubleDataXOut);
        addOutPort("OutTimedDoubleDataY", m_OutTimedDoubleDataYOut);
        addOutPort("OutTimedDoubleDataZ", m_OutTimedDoubleDataZOut);
        addOutPort("OutTimedDoubleDataSeq", m_OutTimedDoubleDataSeqOut);
        addOutPort("OutTimedFloatDataX", m_OutTimedFloatDataXOut);
        addOutPort("OutTimedFloatDataY", m_OutTimedFloatDataYOut);
        addOutPort("OutTimedFloatDataZ", m_OutTimedFloatDataZOut);
        addOutPort("OutTimedFloatDataSeq", m_OutTimedFloatDataSeqOut);
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
    	startMonth = startTime.get(Calendar.MONTH);
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
     * �f�[�^�̏��������s��
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
		arrayDataY = new double[1];
		arrayDataZ = new double[1];
    }
    
	/**
	 * ���o�̓R���g���[��
	 * �f�[�^�����͂���Ă���o�͂����܂ł̈�A�̓��������
	 */
	private void ioControl(){
		Object[] dataArray = new Object[5];
		TimedDouble subDataX = null;
		TimedDouble subDataY = null;
		TimedDouble subDataZ = null;
		
		if(read_flag == true){
			if(can_read != true)dataLine = readText(text);
			dataArray = getOutData(dataLine[read_counter].split(","));

			if(dataLine[read_counter + 1]!=null) read_counter++;
		}else{
			if(m_InTimedDoubleDataXIn.isNew() && m_InTimedDoubleDataYIn.isNew() && m_InTimedDoubleDataZIn.isNew()){
				m_InTimedDoubleDataXIn.read();
				m_InTimedDoubleDataYIn.read();
				m_InTimedDoubleDataZIn.read();
				
				subDataX = m_InTimedDoubleDataXIn.extract();
				subDataY = m_InTimedDoubleDataYIn.extract();
				subDataZ = m_InTimedDoubleDataZIn.extract();
				
				System.out.println("In Data X:"+subDataX.data+" Y:"+subDataY.data+" Z:"+subDataZ.data);
				
				getInport_flag = true;
			}
			
			if(getInport_flag == true){
				dataArray[0] = new Double(subDataX.data);
				dataArray[1] = new Double(subDataY.data);
				dataArray[2] = new Double(subDataZ.data);
				dataArray[3] = new Integer(subDataX.tm.sec);
				dataArray[4] = new Integer(subDataX.tm.nsec);
				 
				writeText(text,stringData(dataArray));
			}
		}
		
		if(read_flag == true || getInport_flag == true){
			getInport_flag = false;
			
			dataArray = roundingData(getErrorFromBaseline(dataArray));
			
			setOutPort(dataArray);
			if(read_flag == true){
				try{
			    Thread.sleep(m_PitchTime.value); //�P��[msec]
			    }catch(InterruptedException e){}
			}

		}
	}
	
	/**
	 * �f�[�^�̊ۂߏ������s��
	 * @param array�@���݂̃f�[�^�z��
	 * @return�@�ۂߏ�����̃f�[�^�z��
	 */
	private Object[] roundingData(Object array[]){

		int configRounding = m_configRounding.getValue();
		double subDataX = new Double((double)array[0]);
		double subDataY = new Double((double)array[1]);
		double subDataZ = new Double((double)array[2]);
		arrayDataX = insertData(arrayDataX,subDataX,configRounding);
		arrayDataY = insertData(arrayDataY,subDataY,configRounding);
		arrayDataZ = insertData(arrayDataZ,subDataZ,configRounding);
		
		array[0] = new Double(average(arrayDataX));
		array[1] = new Double(average(arrayDataY));
		array[2] = new Double(average(arrayDataZ));
		
		return array;
	}
	
	/**
	 * ���݂̃f�[�^�ɑ΂���������̕΍���Ԃ�
	 * @param array�@���݂̃f�[�^�̓������z��
	 * @return�@�΍��̔z��
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
	 * �ǂݍ��݃f�[�^�̈�s����A�o�͂������f�[�^�𔲂��o��
	 * @param now_line�@�ǂݍ��݃f�[�^�̈�s
	 * @return�@�o�͂������f�[�^
	 */
	private Object[] getOutData(String now_line[]){
		Object[] array = new Object[5];
		array[0] = new Double(Double.valueOf(now_line[1]));
		array[1] = new Double(Double.valueOf(now_line[3]));
		array[2] = new Double(Double.valueOf(now_line[5]));
		array[3] = new Integer(Integer.valueOf(now_line[7]));
		array[4] = new Integer(Integer.valueOf(now_line[9]));
		return array;
	}
	
	/**
	 * OurPort�Ƀf�[�^���Z�b�g����
	 * @param array�@�Z�b�g�������f�[�^�̓������z��
	 */
	private void setOutPort(Object array[]){
		
		double x = (double)array[0];
		double y = (double)array[1];
		double z = (double)array[2];
		double[] xyz = {x,y,z};
		Time tm = new Time((int)array[3],(int)array[4]);
		
		float xf = (float)x;
		float yf = (float)y;
		float zf = (float)z;
		float[] xyzf = {xf,yf,zf};

		
		System.out.println("Out Data x:"+x+" y:"+y+" z:"+z);
        try {
        	TimedAcceleration3D subAccele3D = new TimedAcceleration3D(tm,new Acceleration3D(x,y,z));
        	TimedDouble subDataX = new TimedDouble(tm,x);
        	TimedDouble subDataY = new TimedDouble(tm,y);
        	TimedDouble subDataZ = new TimedDouble(tm,z);
        	TimedDoubleSeq subDoubleSeq = new TimedDoubleSeq(tm,xyz);
        	TimedFloat subFloatX = new TimedFloat(tm,xf);
        	TimedFloat subFloatY = new TimedFloat(tm,yf);
        	TimedFloat subFloatZ = new TimedFloat(tm,zf);
        	TimedFloatSeq subFloatSeq = new TimedFloatSeq(tm,xyzf);
        	
			m_OutTimedAcceleration3DOut.write(subAccele3D);		
			m_OutTimedDoubleDataXOut.write(subDataX);
			m_OutTimedDoubleDataYOut.write(subDataY);
			m_OutTimedDoubleDataZOut.write(subDataZ);
			m_OutTimedDoubleDataSeqOut.write(subDoubleSeq);
			m_OutTimedFloatDataXOut.write(subFloatX);
			m_OutTimedFloatDataYOut.write(subFloatY);
			m_OutTimedFloatDataZOut.write(subFloatZ);
			m_OutTimedFloatDataSeqOut.write(subFloatSeq);
        } catch(Exception e){
        	
        }
	}
	
	/**
	 * �e�L�X�g�t�@�C�������擾����
	 * �R���t�B�M�����[�V�����p�����[�^�Ńt�@�C�������w�肳��Ă���΂��̃t�@�C�������A
	 * �w�肪�Ȃ���΁AActive���̎��Ԃ����ɂ����t�@�C�������擾����
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
	 * Active�ɂ��ꂽ���Ԃ�����String���쐬
	 * @return�@�쐬���ꂽString
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
		
		String Name = "GetGravity_"+startYear+"_"+sMonth+"_"+sDay+"_"+sHour+"_"+sMinute+".txt";
		return Name;
	}
	
	/**
	 * �e�L�X�g�t�@�C����ǂݍ��݁A���̃e�L�X�g�t�@�C���̓��e��Ԃ�
	 * @param fileName�@�ǂݍ��݂����e�L�X�g�t�@�C����
	 * @return�@�e�L�X�g�t�@�C���̓��e (��s���̔z��A�Ō�̃f�[�^��null�j
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
			System.out.println(e.getMessage()+"�t�@�C�����J���܂���");
		}
		return line;
	}
	
	/**
	 * �e�L�X�g�t�@�C���Ƀf�[�^����������
	 * @param fileName�@�������ޑΏ̂̃e�L�X�g�t�@�C��
	 * @param str�@�������݂����f�[�^
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
	 * �������ݗp�e�L�X�g�t�@�C�����擾����
	 * @param text�@�e�L�X�g�t�@�C����
	 * @return�@�e�L�X�g�t�@�C����BufferedWrite�f�[�^
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
	 * �e�L�X�g�ɏ������ނ��߂�String���쐬����
	 * @param array�@�������ރf�[�^�̓������z��
	 * @return�@�쐬����String
	 */
	private String stringData(Object array[]){
		String str="Xdata:,"+(double)array[0]+",Ydata:,"+(double)array[1]+",Zdata:,"+(double)array[2]+
				",Time(sec):,"+(int)array[3]+",Time(nsec):,"+(int)array[4]+",\n";
		return str;
	}
	
	/**
	 * �ۂߏ����̂��߂̕��ό��z��Ƀf�[�^��}��
	 * @param array�@���ό��z��
	 * @param newData�@�}���������f�[�^
	 * @param round�@�ۂߏ����̘g��
	 * @return�@�}��������̕��ό��z��
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
	 * ���l�z����󂯎��A���̔z��̕��ϒl��Ԃ�
	 * @param array
	 * @return �z��̕��ϒl (double)
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
     * - DefaultValue: 0
     * - Unit: msec
     * - Range: 0 <= x
     */
    protected IntegerHolder m_PitchTime = new IntegerHolder();
	// </rtc-template>

    // DataInPort declaration
    // <rtc-template block="inport_declare">
    protected TimedDouble m_inDataX_val;
    protected DataRef<TimedDouble> m_inDataX;
    /*!
     * Output dataX from "GetGravity" is input.
     * - Type: TimedDouble
     * - Number: 1
     * - Unit: m/s^2
     */
    protected InPort<TimedDouble> m_InTimedDoubleDataXIn;

    protected TimedDouble m_inDataY_val;
    protected DataRef<TimedDouble> m_inDataY;
    /*!
     * Output dataY from "GetGravity" is input.
     * - Type: TimedDouble
     * - Number: 1
     * - Unit: m/s^2
     */
    protected InPort<TimedDouble> m_InTimedDoubleDataYIn;

    protected TimedDouble m_inDataZ_val;
    protected DataRef<TimedDouble> m_inDataZ;
    /*!
     * Output dataZ from "GetGravity" is input.
     * - Type: TimedDouble
     * - Number: 1
     * - Unit: m/s^2
     */
    protected InPort<TimedDouble> m_InTimedDoubleDataZIn;

    
    // </rtc-template>

    // DataOutPort declaration
    // <rtc-template block="outport_declare">
    protected TimedAcceleration3D m_outData3D_val;
    protected DataRef<TimedAcceleration3D> m_outData3D;
    /*!
     * Gravity sensor data in the X , Y and Z axis of the
     * filtered are output.
     * - Type: TimedAcceleration3D
     * - Number: 1
     * - Unit: m/s^2
     */
    protected OutPort<TimedAcceleration3D> m_OutTimedAcceleration3DOut;

    protected TimedDouble m_outDoubleX_val;
    protected DataRef<TimedDouble> m_outDoubleX;
    /*!
     * Gravity sensor data in the X-axis of the filtered is
     * output.
     * - Type: TimedDouble
     * - Number: 1
     * - Unit: m/s^2
     */
    protected OutPort<TimedDouble> m_OutTimedDoubleDataXOut;

    protected TimedDouble m_outDoubleY_val;
    protected DataRef<TimedDouble> m_outDoubleY;
    /*!
     * Gravity sensor data in the Y-axis of the filtered is
     * output.
     * - Type: TimedDouble
     * - Number: 1
     * - Unit: m/s^2
     */
    protected OutPort<TimedDouble> m_OutTimedDoubleDataYOut;

    protected TimedDouble m_outDoubleZ_val;
    protected DataRef<TimedDouble> m_outDoubleZ;
    /*!
     * Gravity sensor data in the Z-axis of the filtered is
     * output.
     * - Type: TimedDouble
     * - Number: 1
     * - Unit: m/s^2
     */
    protected OutPort<TimedDouble> m_OutTimedDoubleDataZOut;

    protected TimedDoubleSeq m_outDoubleSeq_val;
    protected DataRef<TimedDoubleSeq> m_outDoubleSeq;
    /*!
     * Gravity sensor data in the X , Y and Z axis of the
     * filtered are output.
     * seq[0] = dataX
     * seq[1] = dataY
     * seq[2] = dataZ
     * - Type: TimedDoubleSeq
     * - Number: 3
     * - Unit: m/s^2
     */
    protected OutPort<TimedDoubleSeq> m_OutTimedDoubleDataSeqOut;

    protected TimedFloat m_outFloatX_val;
    protected DataRef<TimedFloat> m_outFloatX;
    /*!
     * Gravity sensor data in the X-axis of the filtered is
     * output.
     * - Type: TimedFloat
     * - Number: 1
     * - Unit: m/s^2
     */
    protected OutPort<TimedFloat> m_OutTimedFloatDataXOut;

    protected TimedFloat m_outFloatY_val;
    protected DataRef<TimedFloat> m_outFloatY;
    /*!
     * Gravity sensor data in the Y-axis of the filtered is
     * output.
     * - Type: TimedFloat
     * - Number: 1
     * - Unit: m/s^2
     */
    protected OutPort<TimedFloat> m_OutTimedFloatDataYOut;

    protected TimedFloat m_outFloatZ_val;
    protected DataRef<TimedFloat> m_outFloatZ;
    /*!
     * Gravity sensor data in the Z-axis of the filtered is
     * output.
     * - Type: TimedFloat
     * - Number: 1
     * - Unit: m/s^2
     */
    protected OutPort<TimedFloat> m_OutTimedFloatDataZOut;

    protected TimedFloatSeq m_outFloatSeq_val;
    protected DataRef<TimedFloatSeq> m_outFloatSeq;
    /*!
     * Gravity sensor data in the X , Y and Z axis of the
     * filtered are output.
     * seq[0] = dataX
     * seq[1] = dataY
     * seq[2] = dataZ
     * - Type: TimedFloat
     * - Number: 3
     * - Unit: m/s^2
     */
    protected OutPort<TimedFloatSeq> m_OutTimedFloatDataSeqOut;

    
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
