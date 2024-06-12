// db랑 시간 주석처리한거

package com.gnupr.postureteacher;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.util.Size;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.mediapipe.components.CameraHelper;
import com.google.mediapipe.components.CameraXPreviewHelper;
import com.google.mediapipe.components.ExternalTextureConverter;
import com.google.mediapipe.components.FrameProcessor;
import com.google.mediapipe.components.PermissionHelper;
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmark;
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmarkList;
import com.google.mediapipe.framework.AndroidAssetUtil;
import com.google.mediapipe.framework.AndroidPacketCreator;
import com.google.mediapipe.framework.Packet;
import com.google.mediapipe.framework.PacketGetter;
import com.google.mediapipe.glutil.EglManager;
import com.google.protobuf.InvalidProtocolBufferException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class LungeAnalysisActivity extends AppCompatActivity{
    private static final String TAG = "LungeAnalysisActivity";
    private static final String BINARY_GRAPH_NAME = "pose_tracking_gpu.binarypb";
    private static final String INPUT_VIDEO_STREAM_NAME = "input_video";
    private static final String OUTPUT_VIDEO_STREAM_NAME = "output_video";
    private static final String OUTPUT_LANDMARKS_STREAM_NAME = "pose_landmarks";
    private static final int NUM_FACES = 1;
    private static final CameraHelper.CameraFacing CAMERA_FACING = CameraHelper.CameraFacing.FRONT;
    private static final boolean FLIP_FRAMES_VERTICALLY = true;



    static {
        System.loadLibrary("mediapipe_jni");
        System.loadLibrary("opencv_java3");
    }

    private SurfaceTexture previewFrameTexture;
    private SurfaceView previewDisplayView;
    private EglManager eglManager;
    private FrameProcessor processor;
    private ExternalTextureConverter converter;
    private ApplicationInfo applicationInfo;
    private CameraXPreviewHelper cameraHelper;


    Handler ui_Handler = null;
    //UI 스레드 용 핸들러
    boolean ui_HandlerCheck = true;
    //UI 스레드 체크용
    private boolean startThreadCheck = true;

    private boolean startDialogCheck = true;
    //타이머 다이얼로그 시작 확인

    private int spareTimeMinusMult = 2;
    private static final long ANALYSIS_TIME = 5000; // 5초
    private static final long COUNT_RESET_TIME = 5000; // 5초


    private ImageView lungehead;
    private ImageView lungearm;
    private ImageView lungespine;
    private ImageView lungeleg;

    private TextView headtext;
    private TextView spinetext;
    private TextView armtext;
    private TextView legtext;

    private TextView timerTextView;
    private CountDownTimer timer;
    class markPoint {
        float x;
        float y;
        float z;
    }

    private NormalizedLandmark[] bodyAdvancePoint = new NormalizedLandmark[33];
    //임시 랜드마크 포인트 변수
    private markPoint[] bodyMarkPoint = new markPoint[35];
    //몸 랜드마크 포인트 변수
    private float[] bodyRatioMeasurement = new float[33];
    //비율 계산값 변수(정규화 값)
    private boolean[][][] markResult = new boolean[33][33][33];
    //검사 결과 true/false 변수
    private boolean[] sideTotalResult = new boolean[2];
    //0=왼쪽, 1=오른쪽
    private boolean[] OutOfRangeSave = new boolean[33];
    //범위 벗어남 감지 저장 변수
    private int[] resultPosture = new int[4];
    //부위 별 최종 결과 0=미감지, 1=실패, 2=정상

    private float ratioPoint_1a, ratioPoint_1b, ratioPoint_2a, ratioPoint_2b;
    //비율 계산에 쓰일 포인트 변수 (왼쪽, 오른쪽)


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentViewLayoutResId());

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        getTimeIntent();
        lungehead= findViewById(R.id.headimage);
        lungearm= findViewById(R.id.armimage);
        lungespine= findViewById(R.id.spineimage);
        lungeleg= findViewById(R.id.legimage);

        headtext = findViewById(R.id.lungehead);
        spinetext = findViewById(R.id.lungespine);
        armtext = findViewById(R.id.lungearm);
        legtext = findViewById(R.id.lungeleg);

        timerTextView = findViewById(R.id.timer);

        if (startDialogCheck) {

            startDialogCheck = false;
        }
        try {
            applicationInfo =
                    getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Cannot find application info: " + e);
        }


        //tv.setText("111");
        previewDisplayView = new SurfaceView(this);
        setupPreviewDisplayView();
        //tv.setText("222");

        AndroidAssetUtil.initializeNativeAssetManager(this);
        eglManager = new EglManager(null);
        //tv.setText("333");
        processor =
                new FrameProcessor(
                        this,
                        eglManager.getNativeContext(),
                        BINARY_GRAPH_NAME,
                        INPUT_VIDEO_STREAM_NAME,
                        OUTPUT_VIDEO_STREAM_NAME);
        processor
                .getVideoSurfaceOutput()
                .setFlipY(FLIP_FRAMES_VERTICALLY);

        //tv.setText("444");
        PermissionHelper.checkAndRequestCameraPermissions(this);
        //tv.setText("555");
        AndroidPacketCreator packetCreator = processor.getPacketCreator();
        //tv.setText("666");
        Map<String, Packet> inputSidePackets = new HashMap<>();
        //tv.setText("888");
        processor.setInputSidePackets(inputSidePackets);
        //tv.setText("999");

        ui_Handler = new Handler();
        ThreadClass callThread = new ThreadClass();

        if (Log.isLoggable(TAG, Log.WARN)) {
            processor.addPacketCallback(
                    OUTPUT_LANDMARKS_STREAM_NAME,
                    (packet) -> {
                        byte[] landmarksRaw = PacketGetter.getProtoBytes(packet);
                        try {
                            NormalizedLandmarkList poseLandmarks = NormalizedLandmarkList.parseFrom(landmarksRaw);
                            //tv6.setText("a");
                            ratioPoint_1a = poseLandmarks.getLandmark(11).getY() * 1000f;
                            ratioPoint_1b = poseLandmarks.getLandmark(13).getY() * 1000f;
                            ratioPoint_2a = poseLandmarks.getLandmark(12).getY() * 1000f;
                            ratioPoint_2b = poseLandmarks.getLandmark(14).getY() * 1000f;
                            //tv6.setText("b");
                            for (int i = 0; i <= 32; i++) {
                                bodyMarkPoint[i] = new markPoint();
                                //tv6.setText("c");
                                bodyAdvancePoint[i] = poseLandmarks.getLandmark(i);
                                //tv6.setText("d");
                                bodyMarkPoint[i].x = bodyAdvancePoint[i].getY() * 1000f; //사실은 y축을 x축이라 속이는 것
                                //tv6.setText("e");
                                bodyMarkPoint[i].y = bodyAdvancePoint[i].getX() * 1000f; //사실은 x축을 y축이라 속이는 것
                                //tv6.setText("f");
                                bodyMarkPoint[i].z = bodyAdvancePoint[i].getZ() * 1000f;
                                //tv6.setText("g");
                                bodyRatioMeasurement[i] = bodyMarkPoint[i].x / (ratioPoint_1b - ratioPoint_1a);
                                //tv6.setText("h");
                                bodyRatioMeasurement[i] = bodyMarkPoint[i].y / (ratioPoint_1b - ratioPoint_1a);
                                //tv6.setText("i");
                                bodyRatioMeasurement[i] = bodyMarkPoint[i].z / (ratioPoint_1b - ratioPoint_1a);
                                //tv6.setText("k");
                                if ((-100f <= bodyMarkPoint[i].x && bodyMarkPoint[i].x <= 1100f) && (-100f <= bodyMarkPoint[i].y && bodyMarkPoint[i].y <= 1100f))
                                    OutOfRangeSave[i] = true;
                                else
                                    OutOfRangeSave[i] = false;
                            }
                            //tv.setText("X:" + bodyMarkPoint[25].x + " / Y:" + bodyMarkPoint[25].y + " / Z:" + bodyMarkPoint[25].z + "\n/ANGLE:" + getLandmarksAngleTwo(bodyMarkPoint[23], bodyMarkPoint[25], bodyMarkPoint[27], 'x', 'y'));

                            if (startThreadCheck) {
                                ui_Handler.post(callThread);
                                // 핸들러를 통해 안드로이드 OS에게 작업을 요청
                                startThreadCheck = false;
                            }
                        } catch (InvalidProtocolBufferException e) {
                            Log.e(TAG, "Couldn't Exception received - " + e);
                            return;
                        }
                    }
            );
        }
    }




    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    // resultPosture 배열의 인덱스에 따라 부위 이름을 반환하는 메서드
    private String getBodyPartName(int index) {
        switch (index) {
            case 0:
                return "등";
            case 1:
                return "팔";
            case 2:
                return "머리";
            case 3:
                return "다리";
            default:
                return "";
        }
    }
    // resultPosture 배열을 확인하여 어떤 부위가 잘못됐는지 알려주는 메서드
    private void speakIncorrectPosture(String side, int[] posture) {
        Log.d("lllll","tts");
        List<String> incorrectParts = new ArrayList<>();
        for (int i = 0; i < posture.length; i++) {
            if (posture[i] == 1) {
                incorrectParts.add(getBodyPartName(i));
            }
        }
        if (!incorrectParts.isEmpty()) {
            // 잘못된 부위가 있는 경우
            StringBuilder message = new StringBuilder(side + " ");
            for (int i = 0; i < incorrectParts.size(); i++) {
                message.append(incorrectParts.get(i));
                if (i < incorrectParts.size() - 1) {
                    message.append("과 ");
                }
            }
            message.append(" 자세가 잘못됐어요");
            Log.d("message:", message.toString());
        }
    }



    private volatile long lastCountTime = 0;

    class ThreadClass extends Thread {

        private boolean isAnalyzing = true;
        private boolean isTimerStarted = false;
        private long startTime;

        private Handler timerHandler = new Handler();
        private Runnable timerRunnable;

        @Override
        public void run() {

            while (ui_HandlerCheck && isAnalyzing) {
                if (!isTimerStarted) {
                    startTime = System.currentTimeMillis();
                    isTimerStarted = true;
                }

                long currentTime = System.currentTimeMillis();
                long elapsedMillis = currentTime - startTime;
                long remainingMillis = ANALYSIS_TIME - elapsedMillis;

                if (remainingMillis > 0) {
                    long secondsRemaining = remainingMillis / 1000;
                    timerTextView.setText(String.format(Locale.getDefault(), "%d", secondsRemaining));
                    timerHandler.postDelayed(this, 1000);
                } else {
                    timerTextView.setText("런지 분석 결과");
                }
                timerHandler.post(timerRunnable);

                if (elapsedMillis >= ANALYSIS_TIME) {
                    isAnalyzing = false;
                }

                if (isAnalyzing) {
                    if ((Arrays.stream(resultPosture).allMatch(x -> x == 2)) && currentTime - lastCountTime >= COUNT_RESET_TIME) {
                        lastCountTime = currentTime;
                    } else if (sideTotalResult[1] && currentTime - lastCountTime >= COUNT_RESET_TIME) {
                        speakIncorrectPosture("왼쪽", resultPosture);
                        lastCountTime = currentTime;
                    } else if (sideTotalResult[0] && currentTime - lastCountTime >= COUNT_RESET_TIME) {
                        speakIncorrectPosture("오른쪽", resultPosture);
                        lastCountTime = currentTime;
                    } else {
                        if (currentTime - lastCountTime >= COUNT_RESET_TIME) {
                            lastCountTime = currentTime;
                        }
                    }

                    if (bodyMarkPoint[11].z > bodyMarkPoint[12].z)
                        getLandmarksAngleResult(0);
                    else
                        getLandmarksAngleResult(1);
                }

                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                    startThreadCheck = true;
                }
                if (!isAnalyzing) {
                    startThreadCheck = true;
                    startDialogCheck = true;
                    try {
                        Thread.sleep(1000 * spareTimeMinusMult);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            ViewGroup viewGroup = findViewById(R.id.preview_display_layout);
                            viewGroup.removeAllViews();
                        }
                    });
                    break;
                }

                if (ui_HandlerCheck) {
                    ui_Handler.post(this);
                }
            }
        }
    }

    public void onClickExit(View view) {

        Intent intent = new Intent(this, ShowExerciseActivity.class);
        startActivity(intent);
        ui_HandlerCheck = false;
        finish();
    }
////////////////////////////////////////////////////////////////////////////////

    public void angleCalculationResult(int firstPoint, int secondPoint, int thirdPoint, float oneAngle, float twoAngle) {
        float userAngle = getLandmarksAngleTwo(bodyMarkPoint[firstPoint], bodyMarkPoint[secondPoint], bodyMarkPoint[thirdPoint], 'x', 'y');
        if (userAngle >= oneAngle && userAngle <= twoAngle) {
            markResult[firstPoint][secondPoint][thirdPoint] = true;
        } else {
            markResult[firstPoint][secondPoint][thirdPoint] = false;
        }
    }

    public float calculateAngle(int firstPoint, int secondPoint, int thirdPoint) {
        return getLandmarksAngleTwo(bodyMarkPoint[firstPoint], bodyMarkPoint[secondPoint], bodyMarkPoint[thirdPoint], 'x', 'y');
    }
/////////////////////////////////////////////////////////////////////////////////

    public void getLandmarksAngleResult(int side) { //0=왼쪽, 1=오른쪽
        //첫번째 true if는 범위 내에 있을 때, 첫번째 false if는 범위 밖에 있을 때
        //두번째 true if는 검사 결과가 정상일 때, 두번째 false if는 검사 결과가 비정상일 때
        if (OutOfRangeSave[7 + side] == true && OutOfRangeSave[11 + side] == true && OutOfRangeSave[23 + side] == true) { //범위 판별
            angleCalculationResult(7 + side, 11 + side, 23 + side, 150f, 210f); //90f 120f | 70f 140f | 80f 130f
            //무릎-엉덩이-허리
            if (markResult[7 + side][11 + side][23 + side] == true) { //각도 판별
                lungespine.setImageResource(R.drawable.lunge_spine_green);
                resultPosture[0] = 2;
                spinetext.setText("올바른 자세입니다.");
            } else {
                lungespine.setImageResource(R.drawable.lunge_spine_red);
                resultPosture[0] = 1;
                spinetext.setText("허리를 꼿꼿하게 세우고 정면을 바라봐주세요.");
            }
        } else {
            //여기에 비감지(회색)
            lungespine.setImageResource(R.drawable.lunge_spine_gray);
            markResult[7 + side][11 + side][23 + side] = true;
            resultPosture[0] = 0;
            spinetext.setText("해당 부위가 감지되지 않았습니다.");
        }


        //ARM
        if (OutOfRangeSave[11 + side] == true && OutOfRangeSave[13 + side] == true && OutOfRangeSave[15 + side] == true) { //범위 판별
            angleCalculationResult(11 + side, 13 + side, 15 + side, 10f, 180f); //140f 180f | 120f 180f X //90f 120f
            //어깨-팔꿈치-엄지
            if (markResult[11 + side][13 + side][15 + side] == true) { //각도 판별
                lungearm.setImageResource(R.drawable.lunge_arm_green);
                resultPosture[1] = 2;
                armtext.setText("올바른 자세입니다.");
            } else {
                lungearm.setImageResource(R.drawable.lunge_arm_red);
                resultPosture[1] = 1;
                float angle = calculateAngle(11 + side, 13 + side, 15 + side);
                if (angle < 10f) {
                    armtext.setText("손이 허리에 위치하도록 손을 내려주세요.");
                } else if (angle >= 180f) {
                    armtext.setText("손이 허리에 위치하도록 손을 올려주세요.");
                }
            }
        } else {
            //여기에 비감지(회색)
            lungearm.setImageResource(R.drawable.lunge_arm_gray);
            markResult[11 + side][13 + side][15 + side] = true;
            resultPosture[1] = 0;
            armtext.setText("해당 부위가 감지되지 않았습니다.");
        }

        bodyMarkPoint[33 + side] = new markPoint();
        if(side == 0)
            bodyMarkPoint[33 + side].x = bodyAdvancePoint[7].getX() * 1000f + 300;
        else
            bodyMarkPoint[33 + side].x = bodyAdvancePoint[7].getX() * 1000f - 300;
        bodyMarkPoint[33 + side].y = bodyAdvancePoint[7].getY() * 1000f - 10;
        bodyMarkPoint[33 + side].z = bodyAdvancePoint[7].getZ() * 1000f + 10;
        if (OutOfRangeSave[7 + side] == true && OutOfRangeSave[11 + side] == true) { //범위 판별
            if (!Double.isNaN(getLandmarksAngleTwo(bodyMarkPoint[33 + side], bodyMarkPoint[7 + side], bodyMarkPoint[11 + side], 'x', 'y'))) {
                if (getLandmarksAngleTwo(bodyMarkPoint[33 + side], bodyMarkPoint[7 + side], bodyMarkPoint[11 + side], 'x', 'y') >= 10f
                        && getLandmarksAngleTwo(bodyMarkPoint[33 + side], bodyMarkPoint[7 + side], bodyMarkPoint[11 + side], 'x', 'y') <= 440f)
                { //90f 140f | 80f 160f | 80f 120f | 80f 140f
                    markResult[7 + side][7 + side][11 + side] = true;
                } else {
                    markResult[7 + side][7 + side][11 + side] = false;
                }
                if (markResult[7 + side][7 + side][11 + side] == true) { //각도 판별
                    lungehead.setImageResource(R.drawable.lunge_head_green);
                    resultPosture[2] = 2;
                    headtext.setText("올바른 자세입니다.");
                } else {
                    lungehead.setImageResource(R.drawable.lunge_head_red);
                    resultPosture[2] = 1;
                    headtext.setText("정면을 똑바로 봐주세요.");
                }
            }
            //어깨-귀-귀너머 머리각도(x+300)
        } else {
            //여기에 비감지(회색)
            lungehead.setImageResource(R.drawable.lunge_head_gray);
            markResult[7 + side][7 + side][11 + side] = true;
            resultPosture[2] = 0;
            headtext.setText("해당 부위가 감지되지 않았습니다.");
        }

        if (OutOfRangeSave[23 + side] == true && OutOfRangeSave[25 + side] == true && OutOfRangeSave[27 + side] == true) { //범위 판별
            angleCalculationResult(23 + side, 25 + side, 27 + side, 40f, 100f); //90f 120f | 70f 140f
            //발목-무릎-엉덩이 무릎각도
            if (markResult[23 + side][25 + side][27 + side] == true) { //각도 판별
                lungeleg.setImageResource(R.drawable.lunge_leg_green);
                resultPosture[3] = 2;
                legtext.setText("올바른 자세입니다.");
            } else {
                lungeleg.setImageResource(R.drawable.lunge_leg_red);
                resultPosture[3] = 1;
                legtext.setText("양쪽 무릎이 직각이 되도록 해주세요.");
            }
        } else {
            //여기에 비감지(회색)
            lungeleg.setImageResource(R.drawable.lunge_leg_gray);
            markResult[23 + side][25 + side][27 + side] = true;
            resultPosture[3] = 0;
            legtext.setText("해당 부위가 감지되지 않았습니다.");
        }

        if (markResult[7 + side][11 + side][23 + side] && markResult[11 + side][13 + side][15 + side]
                && markResult[7 + side][7 + side][11 + side] && markResult[23 + side][25 + side][27 + side])
            sideTotalResult[side] = true;
        else
            sideTotalResult[side] = false;
    }


////////////////////////////////////////////////////////////////////////////////////////////


    public static float getLandmarksAngleTwo(markPoint p1, markPoint p2, markPoint p3, char a, char b) {
        float p1_2 = 0f, p2_3 = 0f, p3_1 = 0f;
        if (a == b) {
            return 0;
        } else if ((a == 'x' || b == 'x') && (a == 'y' || b == 'y')) {
            p1_2 = (float) Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.y - p2.y, 2));
            p2_3 = (float) Math.sqrt(Math.pow(p2.x - p3.x, 2) + Math.pow(p2.y - p3.y, 2));
            p3_1 = (float) Math.sqrt(Math.pow(p3.x - p1.x, 2) + Math.pow(p3.y - p1.y, 2));
        } else if ((a == 'x' || b == 'x') && (a == 'z' || b == 'z')) {
            p1_2 = (float) Math.sqrt(Math.pow(p1.x - p2.x, 2) + Math.pow(p1.z - p2.z, 2));
            p2_3 = (float) Math.sqrt(Math.pow(p2.x - p3.x, 2) + Math.pow(p2.z - p3.z, 2));
            p3_1 = (float) Math.sqrt(Math.pow(p3.x - p1.x, 2) + Math.pow(p3.z - p1.z, 2));
        } else if ((a == 'y' || b == 'y') && (a == 'z' || b == 'z')) {
            p1_2 = (float) Math.sqrt(Math.pow(p1.y - p2.y, 2) + Math.pow(p1.z - p2.z, 2));
            p2_3 = (float) Math.sqrt(Math.pow(p2.y - p3.y, 2) + Math.pow(p2.z - p3.z, 2));
            p3_1 = (float) Math.sqrt(Math.pow(p3.y - p1.y, 2) + Math.pow(p3.z - p1.z, 2));
        }
        float radian = (float) Math.acos((p1_2 * p1_2 + p2_3 * p2_3 - p3_1 * p3_1) / (2 * p1_2 * p2_3));
        float degree = (float) (radian / Math.PI * 180);
        return degree;
    }

    private void getTimeIntent() {
        Intent getTime = getIntent();
        spareTimeMinusMult = getTime.getIntExtra("MULT", 2);
    }
    //pose
    protected int getContentViewLayoutResId() {
        return R.layout.activity_lunge_analysis;
    }

    @Override
    protected void onResume() {
        super.onResume();
        converter =
                new ExternalTextureConverter(
                        eglManager.getContext(), 2);
        converter.setFlipY(FLIP_FRAMES_VERTICALLY);
        converter.setConsumer(processor);
        if (PermissionHelper.cameraPermissionsGranted(this)) {
            startCamera();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        converter.close();
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionHelper.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    protected void onCameraStarted(SurfaceTexture surfaceTexture) {
        previewFrameTexture = surfaceTexture;
        previewDisplayView.setVisibility(View.VISIBLE);
    }

    protected Size cameraTargetResolution() {
        return null;
    }

    public void startCamera() {
        cameraHelper = new CameraXPreviewHelper();
        cameraHelper.setOnCameraStartedListener(
                surfaceTexture -> {
                    onCameraStarted(surfaceTexture);
                });
        CameraHelper.CameraFacing cameraFacing = CameraHelper.CameraFacing.FRONT;
        cameraHelper.startCamera(
                this, cameraFacing, previewFrameTexture, cameraTargetResolution());
    }

    protected Size computeViewSize(int width, int height) {
        return new Size(width, height);
    }

    protected void onPreviewDisplaySurfaceChanged(
            SurfaceHolder holder, int format, int width, int height) {

        // 디바이스의 회전 상태 가져오기 지연
        int rotation = getWindowManager().getDefaultDisplay().getRotation();
        // 화면 회전에 따라 미리보기 화면을 회전시키기
        switch (rotation) {
            case Surface.ROTATION_0:
            case Surface.ROTATION_180:
                // 디바이스가 세로로 세워진 경우 또는 세로로 뒤집힌 경우
                // 화면 크기 유지
                break;
            case Surface.ROTATION_90:
            case Surface.ROTATION_270:
                // 디바이스가 가로로 눕혀진 경우 또는 가로로 뒤집힌 경우
                // 미리보기 화면 크기 회전
                Size viewSize = computeViewSize(width, height);
                Size displaySize = cameraHelper.computeDisplaySizeFromViewSize(viewSize);
                boolean isCameraRotated = cameraHelper.isCameraRotated();
                converter.setSurfaceTextureAndAttachToGLContext(
                        previewFrameTexture,
                        isCameraRotated ? displaySize.getHeight() : displaySize.getWidth(),
                        isCameraRotated ? displaySize.getWidth() : displaySize.getHeight());
                break;
        }
    }

    private void setupPreviewDisplayView() {
        previewDisplayView.setVisibility(View.GONE);
        ViewGroup viewGroup = findViewById(R.id.preview_display_layout);
        viewGroup.addView(previewDisplayView);

        previewDisplayView
                .getHolder()
                .addCallback(
                        new SurfaceHolder.Callback() {
                            @Override
                            public void surfaceCreated(SurfaceHolder holder) {
                                processor.getVideoSurfaceOutput().setSurface(holder.getSurface());
                            }

                            @Override
                            public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                                onPreviewDisplaySurfaceChanged(holder, format, width, height);
                            }

                            @Override
                            public void surfaceDestroyed(SurfaceHolder holder) {
                                processor.getVideoSurfaceOutput().setSurface(null);
                            }
                        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (converter != null) {
            converter.close();
        }
        if (processor != null) {
            processor.close();
        }
    }


    private static String getPoseLandmarksDebugString(NormalizedLandmarkList poseLandmarks) {
        String poseLandmarkStr = "Pose landmarks: " + poseLandmarks.getLandmarkCount() + "\n";
        int landmarkIndex = 0;
        for (NormalizedLandmark landmark : poseLandmarks.getLandmarkList()) {
            poseLandmarkStr +=
                    "\tLandmark ["
                            + landmarkIndex
                            + "]: ("
                            + landmark.getX()
                            + ", "
                            + landmark.getY()
                            + ", "
                            + landmark.getZ()
                            + ")\n";
            ++landmarkIndex;
        }
        return poseLandmarkStr;
    }

    private void backToMainActivity() {
        Intent intent = new Intent(this, ShowExerciseActivity.class);
        startActivity(intent);
        finish(); // 현재 Activity를 종료합니다.
    }

    public void backClick(View view) {
        // 고급 게임 시작
        backToMainActivity();
    }
}