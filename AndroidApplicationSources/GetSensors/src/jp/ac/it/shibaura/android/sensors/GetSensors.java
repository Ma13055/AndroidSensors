package jp.ac.it.shibaura.android.sensors;

import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

public class GetSensors extends Activity {	
	//センサ情報取得用変数
	private SensorManager sm;
	
	CheckBox[] chkbox = new CheckBox[11];

	LinearLayout layout;
	
	//使用センサ選択用boolean配列
	boolean[] sensors = new boolean[11];
	boolean[] chose = new boolean[11];
	
	/**
	 * アプリ生成
	 */
	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		
		for(int i=0;i<11;i++){
			sensors[i]=false;
			chose[i]=false;
			chkbox[i]=null;
		}
		
		TextView text = new TextView(this);
		text.setText("使用したいセンサを選択してください");
		LinearLayout layout = new LinearLayout(this);
		layout.setOrientation(LinearLayout.VERTICAL);
		layout.addView(text);
		
		sm = (SensorManager)getSystemService(SENSOR_SERVICE);
		List<Sensor> sensorsList = sm.getSensorList(Sensor.TYPE_ALL);
		
		for(Sensor s : sensorsList){
			switch(s.getType()){
			case Sensor.TYPE_ACCELEROMETER:
				sensors[0] = true;
				break;
			case Sensor.TYPE_LINEAR_ACCELERATION:
				sensors[1] = true;
				break;
			case Sensor.TYPE_GYROSCOPE:
				sensors[2] = true;
				break;
			case Sensor.TYPE_GRAVITY:
				sensors[3] = true;
				break;
			case Sensor.TYPE_MAGNETIC_FIELD:
				sensors[4] = true;
				break;
			case Sensor.TYPE_ORIENTATION:
				sensors[5] = true;
				break;
			case Sensor.TYPE_ROTATION_VECTOR:
				sensors[6] = true;
				break;
			case Sensor.TYPE_LIGHT:
				sensors[7] = true;
				break;
			case Sensor.TYPE_PRESSURE:
				sensors[8] = true;
				break;
			case Sensor.TYPE_PROXIMITY:
				sensors[9] = true;
				break;
			case Sensor.TYPE_AMBIENT_TEMPERATURE:
				sensors[10] = true;
				break;
			default:
				break;
			}
		}
		
		for(int i=0;i<11;i++){
			if(sensors[i]==true){
				chkbox[i] = new CheckBox(this);
				switch(i){
				case 0:
					chkbox[i].setText("Accelerometer");
					break;
				case 1:
					chkbox[i].setText("LinearAccelerometer");
					break;
				case 2:
					chkbox[i].setText("Gyroscope");
					break;
				case 3:
					chkbox[i].setText("Gravity");
					break;
				case 4:
					chkbox[i].setText("MagneticField");
					break;
				case 5:
					chkbox[i].setText("Orientation");
					break;
				case 6:
					chkbox[i].setText("RotationVector");
					break;
				case 7:
					chkbox[i].setText("Light");
					break;
				case 8:
					chkbox[i].setText("Pressure");
					break;
				case 9:
					chkbox[i].setText("proximity");
					break;
				case 10:
					chkbox[i].setText("AmbientTmperature");
					break;
				}
				layout.addView(chkbox[i]);
				chkbox[i].setChecked(false);
			}else{
				chkbox[i]=null;
			}
		}
		
		Button btn = new Button(this);
		btn.setText("決定");
		layout.addView(btn);

		setContentView(layout);
		
		btn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO 自動生成されたメソッド・スタブ
				
				for(int i=0;i<11;i++){
					if(sensors[i]==true)chose[i]=chkbox[i].isChecked();
				}

				Intent intent = new Intent(GetSensors.this,RTMonAndroidOfSensors.class);
				intent.putExtra("Sensors", chose);
				startActivity(intent);
			}
		});
	}
}