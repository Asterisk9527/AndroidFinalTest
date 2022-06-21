package com.tgx.lasttest;

import android.content.Context;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.hardware.*;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.fragment.app.Fragment;
import com.baidu.platform.comapi.walknavi.fsm.RGStateBrowseMap;

import java.io.IOException;
import java.text.DecimalFormat;

/**
 * 利用加速度和磁场传感器
 * 指南针模块
 *
 */

public class Page2Fragment extends Fragment{

    ImageView compassCircle;
    View compassArrow;
    TextView compassInfo;
    private SensorManager sensorManager;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.page2, container, false);
        return view;
    }
    @Override
    public void onStart() {
        Log.d("demo","是第三个视图 notify");

        compassInfo =getView().findViewById(R.id.compassInfo);
        compassCircle=getView().findViewById(R.id.sensorCompass);
        compassArrow=getView().findViewById(R.id.sensorArrow);


        previewer = getView().findViewById(R.id.camera);

        sensorManager = (SensorManager) getContext().getSystemService(Context.SENSOR_SERVICE);
        if(sensorManager==null) {
            return;
        }
        Sensor magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        Sensor accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(sensorEventListener, magneticSensor, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorEventListener, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);

        mode = Mode.DEFAULT;
        super.onStart();
    }
    private SensorEventListener sensorEventListener = new SensorEventListener() {
        float[] accelerometerValues = new float[3];
        float[] magneticValues = new float[3];
        private float lastRotateDegree;

        @Override
        public void onSensorChanged(SensorEvent event) {

            //判断当前是加速度传感器还是地磁传感器
            switch (event.sensor.getType()){
                case Sensor.TYPE_ACCELEROMETER:
                    accelerometerValues = event.values.clone();
                    break;
                case Sensor.TYPE_MAGNETIC_FIELD:
                    magneticValues = event.values.clone();
                    break;
                default:
                    break;
            }
            float[] R = new float[9];
            float[] values = new float[3];
            //为R数组赋值
            SensorManager.getRotationMatrix(R, null, accelerometerValues, magneticValues);
            //为values数组赋值，values中就已经包含手机在所有方向上旋转的弧度了
            SensorManager.getOrientation(R, values);
            //1.values[0]表示手机围绕Z轴旋转的弧度；
            //3.Math.toDegrees()把弧度转换成角度；
            //将计算出的旋转角度取反，用于旋转指南针背景图
            float rotateDegree = -(float)Math.toDegrees(values[0]);

            float pitchDegree = -(float)Math.toDegrees(values[1]);
//            Log.i("123456", String.valueOf(pitchDegree));

            if ((mode == Mode.DEFAULT && pitchDegree > 65) ||
                    (mode == Mode.CAMERA && pitchDegree < 45)) {
                toggleMode();
                previewer.setBackground(null);
//                compassArrow.setVisibility(View.GONE);
                compassCircle.setVisibility(View.GONE);
            }

            if (mode == Mode.DEFAULT){
                previewer.setBackgroundColor(Color.BLACK);
                compassArrow.setVisibility(View.VISIBLE);
                compassCircle.setVisibility(View.VISIBLE);
            }

            if (mode == Mode.CAMERA) {
                SensorManager.remapCoordinateSystem(rotateVector,
                        SensorManager.AXIS_X, SensorManager.AXIS_Z,
                        remapRotateVector); // X轴 => Z轴
            }

            if(Math.abs(rotateDegree - lastRotateDegree) > 1){
                compassInfo.setText(degree2text((int)rotateDegree));
                RotateAnimation animation = new RotateAnimation(
                        lastRotateDegree, rotateDegree,
                        Animation.RELATIVE_TO_SELF, 0.5f,
                        Animation.RELATIVE_TO_SELF, 0.5f);
                animation.setFillAfter(true);
                compassCircle.startAnimation(animation);
                lastRotateDegree = rotateDegree;
            }
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            if (accuracy <= SensorManager.SENSOR_STATUS_ACCURACY_LOW) {
                compassInfo.setText("周围有磁场干扰，请拿起手机在空中画8字进行校准！");
            }else if (accuracy >= SensorManager.SENSOR_STATUS_ACCURACY_HIGH){

            }
        }
    };
    private float numFormat(float num){
        DecimalFormat decimalFormat=new DecimalFormat(".00");
        String p=decimalFormat.format(num);
        return Float.parseFloat(p);

    }
    private String degree2text(int degree)
    {
        if (-degree >= -15 && -degree <= 15) {
            return "当前角度为："+ -degree+"°" +"，此为正北方向";
        } else if (-degree > 15 && -degree <= 75) {
            return "当前角度为："+ -degree+"°" +"，此为东北方向";
        } else if (-degree > 75 && -degree <= 105) {
            return "当前角度为："+ -degree+"°" +"，此为正东方向";
        } else if (-degree > 105 && -degree <= 165) {
            return "当前角度为："+ -degree+"°" +"，此为东南方向";
        } else if ( (-degree > 165 && -degree <= 180) || (-degree >= -180 && -degree < -165) ) {
            return "当前角度为："+ -degree+"°" +"，此为正南方向";
        } else if (-degree > -165 && -degree <= -105) {
            return "当前角度为："+ (-degree+180)+"°" +"，此为西南方向";
        } else if (-degree > -105 && -degree <= -75) {
            return "当前角度为："+ (-degree+180)+"°" +"，此为正西方向";
        } else if (-degree > -75 && -degree < -15) {
            return "当前角度为："+ (-degree+180)+"°" +"，此为西北方向";
        }else {
            return "无法确定当前方向";
        }
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(sensorEventListener);
        }
    }

    enum Mode {
        // 水平（默认）模式 或 竖立（摄像头预览）模式
        DEFAULT, CAMERA
    }

    private Mode mode;
    private SurfaceView previewer;  // 摄像头预览
    private Camera camera;  // 摄像头

    private float[] rotateVector = new float[16];   // 旋转向量
    private float[] remapRotateVector = new float[16];   // 旋转向量
    private float[] orientationVector = new float[3];   // 方向值


    public void toggleMode() {
        if (mode == Mode.DEFAULT) { // 改为Camera模式
            camera = Camera.open(0);
            camera.setDisplayOrientation(90);
            Camera.Parameters parameters = camera.getParameters();
            parameters.setFocusMode(android.hardware.Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
            camera.setParameters(parameters);

            try {
                camera.setPreviewDisplay(previewer.getHolder());
                camera.startPreview();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mode = Mode.CAMERA;
        } else {
            camera.stopPreview();
            camera.release();
            camera = null;
            mode = Mode.DEFAULT;

        }
    }


}
