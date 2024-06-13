package com.gnupr.postureteacher;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.SurfaceTexture;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class SquatAnalysisActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{
    private TextToSpeech tts;
    private static final long INITIAL_DELAY = 5000;

    private static final String TAG = "SquatAnalysisActivity";
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


    private ImageView squathead;
    private ImageView squatarm;
    private ImageView squatspine;
    private ImageView squatleg;

    private TextView headtext;
    private TextView spinetext;
    private TextView armtext;
    private TextView legtext;

    private TextView timerTextView;
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

        // TTS 초기화
        tts = new TextToSpeech(this, this);
        timerTextView = findViewById(R.id.timer);
        timerTextView.setText("자세 분석을 시작합니다");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                speak("분석 중.. 자세를 유지해 주세요!");
                timerTextView.setText("분석 중.. 자세를 유지해 주세요!");
            }
        }, INITIAL_DELAY);


        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        getTimeIntent();
        squathead= findViewById(R.id.headimage);
        squatarm= findViewById(R.id.armimage);
        squatspine= findViewById(R.id.spineimage);
        squatleg= findViewById(R.id.legimage);

        headtext = findViewById(R.id.squathead);
        spinetext = findViewById(R.id.squatspine);
        armtext = findViewById(R.id.squatarm);
        legtext = findViewById(R.id.squatleg);

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


    @Override
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            int result = tts.setLanguage(Locale.KOREAN);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "이 언어는 지원되지 않습니다.");
            } else {
                // TTS 출력
                speak("자세 분석을 시작합니다");
            }
        } else {
            Log.e("TTS", "초기화 실패");
        }
    }
    private void speak(String text) {
        tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, null);
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
                    speak("스쿼트 분석 결과입니다.");
                    timerTextView.setText("스쿼트 분석 결과");
                }
                timerHandler.post(timerRunnable);

                if (elapsedMillis >= ANALYSIS_TIME) {
                    isAnalyzing = false;
                }

                if (isAnalyzing) {
                    if ((Arrays.stream(resultPosture).allMatch(x -> x == 2)) && currentTime - lastCountTime >= COUNT_RESET_TIME) {
                        lastCountTime = currentTime;
                    } else if (sideTotalResult[1] && currentTime - lastCountTime >= COUNT_RESET_TIME) {
                        lastCountTime = currentTime;
                    } else if (sideTotalResult[0] && currentTime - lastCountTime >= COUNT_RESET_TIME) {
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


    public void getLandmarksAngleResult(int side) {
        // SPINE
        if (OutOfRangeSave[11 + side] == true && OutOfRangeSave[23 + side] == true && OutOfRangeSave[25 + side] == true) {
            angleCalculationResult(11 + side, 23 + side, 25 + side, 40f, 90f);

            if (markResult[11 + side][23 + side][25 + side] == true) {
                squatspine.setImageResource(R.drawable.squat_spine_green);
                resultPosture[0] = 2;
                spinetext.setText("올바른 자세입니다.");
            } else {
                squatspine.setImageResource(R.drawable.squat_spine_red);
                resultPosture[0] = 1;
                float spineangle = calculateAngle(11 + side, 23 + side, 25 + side);
                if (spineangle < 40f) {
                    spinetext.setText("엉덩이를 더 들어주세요.");
                } else if (spineangle >= 90f) {
                    spinetext.setText("엉덩이를 더 내려주세요.");
                }
            }
        } else {
            squatspine.setImageResource(R.drawable.squat_spine_gray);
            resultPosture[0] = 0;
            markResult[11 + side][23 + side][25 + side] = true;
            spinetext.setText("해당 부위가 감지되지 않았습니다.");
        }

        // ARM
        if (OutOfRangeSave[11 + side] == true && OutOfRangeSave[13 + side] == true && OutOfRangeSave[15 + side] == true) {
            angleCalculationResult(11 + side, 13 + side, 15 + side, 160f, 200f);

            if (markResult[11 + side][13 + side][15 + side] == true) {
                squatarm.setImageResource(R.drawable.squat_arm_green);
                resultPosture[1] = 2;
                armtext.setText("올바른 자세입니다.");
            } else {
                squatarm.setImageResource(R.drawable.squat_arm_red);
                resultPosture[1] = 1;
                armtext.setText("팔을 일자로 쭉 뻗어주세요.");
            }
        } else {
            squatarm.setImageResource(R.drawable.squat_arm_gray);
            resultPosture[1] = 0;
            markResult[11 + side][13 + side][15 + side] = true;
            armtext.setText("해당 부위가 감지되지 않았습니다.");
        }

        // HEAD
        bodyMarkPoint[33 + side] = new SquatAnalysisActivity.markPoint();
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
                    squathead.setImageResource(R.drawable.squat_head_green);
                    resultPosture[2] = 2;
                    headtext.setText("올바른 자세입니다.");
                } else {
                    squathead.setImageResource(R.drawable.squat_head_red);
                    resultPosture[2] = 1;
                    headtext.setText("정면을 똑바로 봐주세요.");
                }
            }
            //어깨-귀-귀너머 머리각도(x+300)
        } else {
            //여기에 비감지(회색)
            squathead.setImageResource(R.drawable.squat_head_gray);
            markResult[7 + side][7 + side][11 + side] = true;
            resultPosture[2] = 0;
            headtext.setText("해당 부위가 감지되지 않았습니다.");
        }

        // LEG
        if (OutOfRangeSave[23 + side] == true && OutOfRangeSave[25 + side] == true && OutOfRangeSave[27 + side] == true) { //범위 판별 // 30 90
            angleCalculationResult(23 + side, 25 + side, 27 + side, 30f, 90f); //90f 120f | 70f 140f
            //발목-무릎-엉덩이 무릎각도
            if (markResult[23 + side][25 + side][27 + side] == true) { //각도 판별
                squatleg.setImageResource(R.drawable.squat_leg_green);
                legtext.setText("올바른 자세입니다.");
                resultPosture[3] = 2;
            } else {
                squatleg.setImageResource(R.drawable.squat_leg_red);
                resultPosture[3] = 1;
                float legangle = calculateAngle(23 + side, 25 + side, 27 + side);
                if (legangle < 30f) {
                    legtext.setText("무릎을 더 들어주세요.");
                } else if (legangle >= 90f) {
                    legtext.setText("무릎을 더 내려주세요.");
                }
            }
        } else {
            //여기에 비감지(회색)
            squatleg.setImageResource(R.drawable.squat_leg_gray);
            markResult[23 + side][25 + side][27 + side] = true;
            legtext.setText("해당 부위가 감지되지 않았습니다.");
            resultPosture[3] = 0;
        }



        Log.i("resultPosture", Arrays.toString(resultPosture));
        Log.i("Side 0 Result", String.valueOf(sideTotalResult[0]));
        Log.i("Side 1 Result", String.valueOf(sideTotalResult[1]));

        if (markResult[11 + side][23 + side][25 + side] && markResult[11 + side][13 + side][15 + side]
                && markResult[7 + side][7 + side][11 + side] && markResult[23 + side][25 + side][27 + side]) { // && markResult[25 + side][29 + side][31 + side] 일단 이건 제외]
            sideTotalResult[side] = true;
        }
        else {
            sideTotalResult[side] = false;
        }
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
        return R.layout.activity_squat_analysis;
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

        previewDisplayView.setVisibility(View.GONE);
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
        // 프리뷰 프레임의 크기와 형식을 조정합니다.
        Size viewSize = computeViewSize(width, height);
        Size displaySize = cameraHelper.computeDisplaySizeFromViewSize(viewSize);
        boolean isCameraRotated = cameraHelper.isCameraRotated();

        // 카메라가 회전되었다면 너비와 높이를 바꿔줍니다.
        converter.setSurfaceTextureAndAttachToGLContext(
                previewFrameTexture,
                isCameraRotated ? displaySize.getHeight() : displaySize.getWidth(),
                isCameraRotated ? displaySize.getWidth() : displaySize.getHeight());
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
        // TTS 리소스 해제
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
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

}