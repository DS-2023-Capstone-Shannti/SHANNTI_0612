package com.gnupr.postureteacher;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.SurfaceTexture;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.Size;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.gnupr.postureteacher.Databases.EntityClass.Measure4DatasEntity;
import com.gnupr.postureteacher.Databases.EntityClass.Measure4RoundsEntity;
import com.gnupr.postureteacher.Databases.MeasureRoomDatabase;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
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

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import android.speech.tts.UtteranceProgressListener;
import android.speech.tts.TextToSpeech;

public class LungeActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{
    private TextToSpeech tts;
    private static final String TAG = "LungeActivity";
    private static final String BINARY_GRAPH_NAME = "pose_tracking_gpu.binarypb";
    private static final String INPUT_VIDEO_STREAM_NAME = "input_video";
    private static final String OUTPUT_VIDEO_STREAM_NAME = "output_video";
    private static final String OUTPUT_LANDMARKS_STREAM_NAME = "pose_landmarks";
    private static final int NUM_FACES = 1;
    private static final CameraHelper.CameraFacing CAMERA_FACING = CameraHelper.CameraFacing.FRONT;
    private static final boolean FLIP_FRAMES_VERTICALLY = true;
    private FirebaseAuth mFirebaseAuth; // 파이어베이스 인증
    private DatabaseReference mDatabaseRef; // 실시간 데이터베이스

    static {
        System.loadLibrary("mediapipe_jni");
        System.loadLibrary("opencv_java3");
    }
    private boolean isKneeAngleSatisfied = false;
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


    private int timer_minute, timer_second;
    //글로벌 시간
    private String text_minute, text_second;
    //텍스트 상의 시간
    private String nowTime;
    //지금 시간
    private int totalTime = 0;
    //전체 시간
    private int globalTime = 0;
    //시작 이후의 시간
    private int spareTime = 100;
    //감지 예비 시간
    private int spareTimeMinus = 1;
    //예비 시간 빼는 값
    private int spareTimeMinusMult = 2;
    //예비 시간 빼는 값에 배율을 곱해주기(빠르게 전용)
    private boolean spareTimeCheck = false;
    //예비 시간 측정해도 되는지
    private int tempTime = 0;
    //예비 시간의 임시 시간(정상화 최종 측정)
    private int lungeAllTimer = 0;
    // 플랭크 시간을 초로 바꾸어 놓은 것

    LocalDateTime timeMeasure4DataStart = LocalDateTime.now();
    LocalDateTime timeMeasure4DataEnd = LocalDateTime.now();
    //현재 전체 측정 시간
    private boolean timeDataCheck = true;
    //측정 시간 측정해도 되는지

    LocalDateTime timeMeasure4RoundStart = LocalDateTime.now();
    LocalDateTime timeMeasure4RoundEnd = LocalDateTime.now();
    //현재 상세 시간
    private boolean timeRoundCheck = true;
    //상세 시간 측정해도 되는지
    private int lungeBreakTime = 20;
    //쉬는 시간 초수


    private String[] divideTime;
    //문자열에서 분할된 시간
    private Timer timer = new Timer();
    private boolean pauseTimerCheck = false;
    //타이머 정지, false = 흘러감, true = 멈춤
    private boolean breakTimeCheck = false;
    //쉬는 시간 여부, false = 쉬는 시간 아님, true = 쉬는 시간임

    LocalDate nowLocalDate = LocalDate.now();
    LocalTime nowLocalTime = LocalTime.now();
    String formatedNowLocalTime = nowLocalDate.format(DateTimeFormatter.ofPattern("yyMMdd")) + nowLocalTime.format(DateTimeFormatter.ofPattern("HHmmss"));
    //날짜, 시간 & 문자열에 맞게 날짜+시간 변환

    LocalDateTime Measure4RoundStart = LocalDateTime.now();
    LocalDateTime Measure4RoundEnd = LocalDateTime.now();
    //현재 측정 시간

    LocalDateTime Measure4DataStart = LocalDateTime.now();
    LocalDateTime Measure4DataEnd = LocalDateTime.now();
    //현재 상세 시간

    private String UseTimerTimeDB = "11:22";
    //템플릿 타이머 시간 (시간:분:초)
    private final long finishtimeed = 2500;
    private long presstime = 0;

    private int lungeTargetCount = 1;
    //플랭크할 횟수
    private int lungeCurrentCount = 1;
    //현재 플랭크 횟수



    private int finalStopCheck = 0;
    //완전 종료 대기 확인
    //0 아무것도 아님, 1 쉬는 시간, 2 돌입 대기, 3 돌입

    //private TextView tv2;
    //private TextView tv6;
    private TextView tv_TimeCounter;

    private ImageView iv1;
    private ImageView iv2;
    private ImageView iv3;
    private ImageView iv4;
    private ImageView iv5;
    private ImageView iv6;

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
    private float[][] resultAngleSave = new float[2][6];
    //부위 사라짐 감지용 0.5초 딜레이 저장 변수
    private int[] resultPosture = new int[4];
    //부위 별 최종 결과 0=미감지, 1=실패, 2=정상


    private float ratioPoint_1a, ratioPoint_1b, ratioPoint_2a, ratioPoint_2b;
    //비율 계산에 쓰일 포인트 변수 (왼쪽, 오른쪽)

    int timeInSeconds;
    public static int countNum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getContentViewLayoutResId());
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

///////////////////////////////////////////////////////////////////////////////
        Intent intent = getIntent();
        String selectedDifficulty = intent.getStringExtra("difficulty");

        // 선택한 난이도에 따라 시간을 설정
        switch (selectedDifficulty) {
            case "easy":
                timeInSeconds = 2000;
                break;
            case "medium":
                timeInSeconds = 3000;
                break;
            case "hard":
                timeInSeconds = 4000;
                break;
            default:
                timeInSeconds = 2000;
                break;
        }
///////////////////////////////////////////////////////////////////////////////

        getTimeIntent();
        iv1= findViewById(R.id.imageView7);
        //iv2= findViewById(R.id.imageView4);
        iv3= findViewById(R.id.imageView3);
        iv4= findViewById(R.id.imageView4);
        //iv5= findViewById(R.id.imageView6);
        iv6= findViewById(R.id.imageView6);

        //tv.setText("000");
        if (startDialogCheck) {
            startDialog();
            startDialogCheck = false;
        }
        try {
            applicationInfo =
                    getPackageManager().getApplicationInfo(getPackageName(), PackageManager.GET_META_DATA);
        } catch (NameNotFoundException e) {
            Log.e(TAG, "Cannot find application info: " + e);
        }

        // TextToSpeech 객체 초기화
        tts = new TextToSpeech(this, this);

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


    // TextToSpeech 초기화 콜백
    public void onInit(int status) {
        if (status == TextToSpeech.SUCCESS) {
            // 초기화 성공
            int result = tts.setLanguage(Locale.KOREAN);
            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS", "한국어를 지원하지 않음");
            }
        } else {
            Log.e("TTS", "TextToSpeech 초기화 실패");
        }
    }
    protected void onDestroy() {
        // TextToSpeech 해제
        if (tts != null) {
            tts.stop();
            tts.shutdown();
        }
        super.onDestroy();
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
            tts.speak(message.toString(), TextToSpeech.QUEUE_ADD, null, null);
        }
    }


    //////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    class ThreadClass extends Thread {
        // 이전 무릎 각도가 기준을 충족했는지 여부를 나타내는 변수
        private boolean wasKneeAngleSatisfied = false;

        // utteranceId 선언
        private final String UTTERANCE_ID_COMPLETE = "utteranceComplete";

        @Override
        public void run() {
            int totalCount = timeInSeconds / 1000;

            UtteranceProgressListener listener = new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {}

                @Override
                public void onDone(String utteranceId) {
                    if (utteranceId.equals(UTTERANCE_ID_COMPLETE)) {
                        Intent intent = new Intent(getApplicationContext(), ShowExerciseActivity.class);
                        startActivity(intent);
                        pauseTimerCheck = true;
                        ui_HandlerCheck = false;
                        finish();
                    }
                }

                @Override
                public void onError(String utteranceId) {}
            };
            tts.setOnUtteranceProgressListener(listener);

            //정상판별
            if (wasKneeAngleSatisfied && !isKneeAngleSatisfied) {
                countNum++;
                tv_TimeCounter.setText(String.valueOf(countNum));
                if (countNum == totalCount) {
                    tts.speak("런지 횟수 " + countNum + "입니다. 운동을 종료합니다.", TextToSpeech.QUEUE_FLUSH, null, UTTERANCE_ID_COMPLETE);

                    mFirebaseAuth = FirebaseAuth.getInstance();
                    mDatabaseRef = FirebaseDatabase.getInstance().getReference("Shannti");

                    FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

                    mDatabaseRef.child("UserAccount").child(firebaseUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.exists()) {
                                // 현재 날짜 가져오기
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
                                String currentDate = sdf.format(new Date());

                                // 날짜 경로 하위로 Time과 Kcal 데이터 가져오기
                                mDatabaseRef.child("UserAccount").child(firebaseUser.getUid()).child(currentDate).addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot dateSnapshot) {
                                        int newTime = 6;
                                        int newKcal = 6;
                                        int updatedTime = newTime;
                                        int updatedKcal = newKcal;

                                        if (dateSnapshot.exists()) {
                                            Integer existingTime = dateSnapshot.child("Time").getValue(Integer.class);
                                            Integer existingKcal = dateSnapshot.child("Kcal").getValue(Integer.class);

                                            if (existingTime != null) {
                                                updatedTime += existingTime;
                                            }
                                            if (existingKcal != null) {
                                                updatedKcal += existingKcal;
                                            }
                                        }

                                        Map<String, Object> updateData = new HashMap<>();
                                        updateData.put("Time", updatedTime);
                                        updateData.put("Kcal", updatedKcal);

                                        mDatabaseRef.child("UserAccount").child(firebaseUser.getUid()).child(currentDate).updateChildren(updateData)
                                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                                    @Override
                                                    public void onSuccess(Void aVoid) {
                                                        // 점수 업데이트가 성공적으로 완료됨을 알리는 메시지
//                                                                                Toast.makeText(SquatActivity.this, "점수가 성공적으로 업데이트되었습니다. Time : " + updatedTime + ", Kcal : " + updatedKcal, Toast.LENGTH_SHORT).show();
                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        // 점수 업데이트 실패 메시지
//                                                                                Toast.makeText(L.this, "점수 업데이트에 실패했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                        // 데이터베이스 오류 메시지
//                                                                Toast.makeText(SquatActivity.this, "데이터베이스 오류: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                    }
                                });

                            } else {
                                // 기존 점수가 없는 경우 새로운 점수 저장
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());
                                String currentDate = sdf.format(new Date());

                                Map<String, Object> newUserData = new HashMap<>();
                                newUserData.put("Time", 6);
                                newUserData.put("Kcal", 6);

                                mDatabaseRef.child("UserAccount").child(firebaseUser.getUid()).child(currentDate).setValue(newUserData)
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
//                                                                        Toast.makeText(SquatActivity.this, "새로운 점수가 성공적으로 저장되었습니다. Time : 6, Kcal : 6", Toast.LENGTH_SHORT).show();
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                //  Toast.makeText(SquatActivity.this, "점수 저장에 실패했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                            }
                                        });
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            //    Toast.makeText(SquatActivity.this, "데이터베이스 오류: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else {
                    tts.speak("런지 횟수 " + countNum + "입니다", TextToSpeech.QUEUE_FLUSH, null, null);
                }
            }
            // 이전 상태를 현재 상태로 업데이트
            wasKneeAngleSatisfied = isKneeAngleSatisfied;

            if (bodyMarkPoint[11].z > bodyMarkPoint[12].z)
                getLandmarksAngleResult(0);
                //왼쪽
            else
                getLandmarksAngleResult(1);
            //오른쪽
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
                startThreadCheck = true;
            }
            if (ui_HandlerCheck) {
                ui_Handler.post(this);
            }
        }
    }

//////////////////////////////////////////////////////////////////////////////////



    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            // 반복실행할 구문
            globalTime++;
            if(!pauseTimerCheck) {
                // 0초 이상이면
                if (timer_second != 0) {
                    //1초씩 감소
                    timer_second--;

                    // 0분 이상이면
                } else if (timer_minute != 0) {
                    // 1분 = 60초
                    timer_second = 60;
                    timer_second--;
                    timer_minute--;
                }

                if(globalTime == 10) {
                    Measure4RoundStart = LocalDateTime.now();
                }

                //시, 분, 초가 10이하(한자리수) 라면
                // 숫자 앞에 0을 붙인다 ( 8 -> 08 )
                if (timer_second <= 9) {
                    text_second = "0" + timer_second;
                } else {
                    text_second = Integer.toString(timer_second);
                }

                if (timer_minute <= 9) {
                    text_minute = "0" + timer_minute;
                } else {
                    text_minute = Integer.toString(timer_minute);
                }

                nowTime = text_minute + ":" + text_second + " | " + lungeCurrentCount + "/" + lungeTargetCount + "세트\n(" + spareTime + "% / " + tempTime + "pt)";
            }

            if (timer_minute == 0 && timer_second == 0) {
                /*timerTask.cancel();//타이머 종료
                timer.cancel();//타이머 종료
                timer.purge();//타이머 종료*/
                //중간에 잠시 멈추는 건 타이머를 죽이는 게 아니라 타이머를 보기로만 잠시 멈춰두고 다시 시작할 때 시간을 새로 갱신
                if(finalStopCheck == 0) {
                    timer_second += lungeBreakTime;
                    //플랭크 쉬는 시간 설정
                    finalStopCheck = 1;
                    breakTimeCheck = true;
                }
                else if(finalStopCheck == 1) {
                    if (lungeTargetCount <= lungeCurrentCount) {
                        timer_second += 3;
                        finalStopCheck = 2;
                    }
                    else {
                        timer_minute = Integer.parseInt(divideTime[0]);
                        timer_second = Integer.parseInt(divideTime[1]);
                        lungeCurrentCount++;
                        breakTimeCheck = false;
                        finalStopCheck = 0;
                    }
                }
            }
        }
    };
    private void startDialog() {
        //이건 자르던지 바꾸던지 하셈
        tv_TimeCounter = findViewById(R.id.TimeCounter);
        AlertDialog.Builder msgBuilder = new AlertDialog.Builder(LungeActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_dialog_layout, null);
        ImageView gifImageView = dialogView.findViewById(R.id.gifImageView);
        TextView textView = dialogView.findViewById(R.id.Text);

        // GIF 파일 설정
        Glide.with(this).load(R.drawable.lungegif).into(gifImageView);
        textView.setText("양손을 허리에 올리고 한 다리씩 90도로 굽히세요.\n 이때 굽힌 다리의 무릎에 땅에 닿지 않도록 주의하세요.");
        textView.setGravity(Gravity.CENTER);
        // 나머지 메시지 설정

        msgBuilder.setView(dialogView);

        AlertDialog msgDlg = msgBuilder.create();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                int timee = timeInSeconds/1000;
                Log.d("timee",String.valueOf(timee));
                // 대화 상자가 null이 아니고, 표시되어 있을 경우에만 닫기
                if (msgDlg != null && msgDlg.isShowing()) {
                    msgDlg.dismiss();
                    tts.speak("운동을 시작합니다. 런지를" + timee +"회 하세요.", TextToSpeech.QUEUE_FLUSH, null, null);
                }
            }
        }, 3500); // 5초 후에 닫기
        msgDlg.show();
    }

/////////////////////////////////////////////////////////////////////////////////
    public void onClickExit(View view) {
        Intent intent = new Intent(this, ShowExerciseActivity.class);
        startActivity(intent);
        pauseTimerCheck = true;
        ui_HandlerCheck = false;
        finish();
    }
////////////////////////////////////////////////////////////////////////////////

    private void saveMeasure4Rounds() {
        int currentCount = countNum; // countNum 값을 가져와서 저장

        Measure4RoundsEntity measure4RoundsTable = new Measure4RoundsEntity();
        measure4RoundsTable.setMeasure4RoundCurrentCount(currentCount);

        MeasureRoomDatabase.getDatabase(getApplicationContext()).getMeasure4RoundsDao().insert(measure4RoundsTable);

        finalStopCheck = 3;
    }

    private void saveMeasure4Datas(int countNum) {
        Measure4DatasEntity measure4DatasTable = new Measure4DatasEntity();
        measure4DatasTable.setMeasure4DataDetectCount(countNum);

        MeasureRoomDatabase.getDatabase(getApplicationContext()).getMeasure4DatasDao().insert(measure4DatasTable);
    }

    public void angleCalculationResult(int firstPoint, int secondPoint, int thirdPoint, float oneAngle, float twoAngle) {
        float userAngle = getLandmarksAngleTwo(bodyMarkPoint[firstPoint], bodyMarkPoint[secondPoint], bodyMarkPoint[thirdPoint], 'x', 'y');
        if (userAngle >= oneAngle && userAngle <= twoAngle) {
            markResult[firstPoint][secondPoint][thirdPoint] = true;
        } else {
            markResult[firstPoint][secondPoint][thirdPoint] = false;
        }
    }

    public void getLandmarksAngleResult(int side) { //0=왼쪽, 1=오른쪽
        //첫번째 true if는 범위 내에 있을 때, 첫번째 false if는 범위 밖에 있을 때
        //두번째 true if는 검사 결과가 정상일 때, 두번째 false if는 검사 결과가 비정상일 때
        if (OutOfRangeSave[7 + side] == true && OutOfRangeSave[11 + side] == true && OutOfRangeSave[23 + side] == true) { //범위 판별
            angleCalculationResult(7 + side, 11 + side, 23 + side, 150f, 210f); //90f 120f | 70f 140f | 80f 130f
            //무릎-엉덩이-허리
            if (markResult[7 + side][11 + side][23 + side] == true) { //각도 판별
                iv1.setImageResource(R.drawable.lunge_spine_green);
                resultPosture[0] = 2;
            } else {
                iv1.setImageResource(R.drawable.lunge_spine_red);
                resultPosture[0] = 1;
            }
        } else {
            //여기에 비감지(회색)
            iv1.setImageResource(R.drawable.lunge_spine_gray);
            markResult[7 + side][11 + side][23 + side] = true;
            resultPosture[0] = 0;
        }


        //ARM
        if (OutOfRangeSave[11 + side] == true && OutOfRangeSave[13 + side] == true && OutOfRangeSave[15 + side] == true) { //범위 판별
            angleCalculationResult(11 + side, 13 + side, 15 + side, 10f, 180f); //140f 180f | 120f 180f X //90f 120f
            //어깨-팔꿈치-엄지
            if (markResult[11 + side][13 + side][15 + side] == true) { //각도 판별
                iv3.setImageResource(R.drawable.lunge_arm_green);
                resultPosture[1] = 2;
            } else {
                iv3.setImageResource(R.drawable.lunge_arm_red);
                resultPosture[1] = 1;
            }
        } else {
            //여기에 비감지(회색)
            iv3.setImageResource(R.drawable.lunge_arm_gray);
            markResult[11 + side][13 + side][15 + side] = true;
            resultPosture[1] = 0;
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
                    iv4.setImageResource(R.drawable.lunge_head_green);
                    resultPosture[2] = 2;
                } else {
                    iv4.setImageResource(R.drawable.lunge_head_red);
                    resultPosture[2] = 1;
                }
            }
            //어깨-귀-귀너머 머리각도(x+300)
        } else {
            //여기에 비감지(회색)
            iv4.setImageResource(R.drawable.lunge_head_gray);
            markResult[7 + side][7 + side][11 + side] = true;
            resultPosture[2] = 0;
        }

        if (OutOfRangeSave[23 + side] == true && OutOfRangeSave[25 + side] == true && OutOfRangeSave[27 + side] == true) { //범위 판별
            angleCalculationResult(23 + side, 25 + side, 27 + side, 40f, 100f); //90f 120f | 70f 140f
            //발목-무릎-엉덩이 무릎각도
            if (markResult[23 + side][25 + side][27 + side] == true) { //각도 판별
                iv6.setImageResource(R.drawable.lunge_leg_green);
                resultPosture[3] = 2;
            } else {
                iv6.setImageResource(R.drawable.lunge_leg_red);
                resultPosture[3] = 1;
            }
        } else {
            //여기에 비감지(회색)
            iv6.setImageResource(R.drawable.lunge_leg_gray);
            markResult[23 + side][25 + side][27 + side] = true;
            resultPosture[3] = 0;
        }

        if (markResult[7 + side][11 + side][23 + side] && markResult[11 + side][13 + side][15 + side]
                && markResult[7 + side][7 + side][11 + side] && markResult[23 + side][25 + side][27 + side])
            sideTotalResult[side] = true;
        else
            sideTotalResult[side] = false;
        if (markResult[23 + side][25 + side][27 + side]) { // 무릎 각도가 기준을 충족하는지 확인
            isKneeAngleSatisfied = true;
        } else {
            isKneeAngleSatisfied = false;
        }
    }


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

    public int getResultPosture(int[] rP) {
        int twoCount = 0, oneCount = 0, zeroCount = 0; //녹색, 적색, 회색
        for(int i = 0;i<4;i++) {
            if (rP[i] == 2) {
                twoCount++;
            }
            else if (rP[i] == 1) {
                oneCount++;
            }
            else {
                zeroCount++;
            }
        }

        if(zeroCount == 4) {
            spareTimeMinus = 0;
            return 0;
        }
        else if(zeroCount == 3) {
            if(oneCount == 0) {
                spareTimeMinus = 0;
                return 2;
            }
            else {
                spareTimeMinus = 4 * spareTimeMinusMult;
                return 1;
            }
        }
        else if(zeroCount == 2) {
            if(oneCount == 0) {
                spareTimeMinus = 0;
                return 2;
            }
            else if(oneCount == 1) {
                spareTimeMinus = 2 * spareTimeMinusMult;
                return 1;
            }
            else {
                spareTimeMinus = 4 * spareTimeMinusMult;
                return 1;
            }
        }
        else if(zeroCount == 1) {
            if(oneCount == 0) {
                spareTimeMinus = 0;
                return 2;
            }
            else if(oneCount == 1) {
                spareTimeMinus = 1 * spareTimeMinusMult;
                return 1;
            }
            else if(oneCount == 2) {
                spareTimeMinus = 2 * spareTimeMinusMult;
                return 1;
            }
            else {
                spareTimeMinus = 4 * spareTimeMinusMult;
                return 1;
            }
        }
        else {
            if(oneCount == 0) {
                spareTimeMinus = 0;
                return 2;
            }
            else if(oneCount == 1) {
                spareTimeMinus = 1 * spareTimeMinusMult;
                return 1;
            }
            else if(oneCount == 2) {
                spareTimeMinus = 2 * spareTimeMinusMult;
                return 1;
            }
            else if(oneCount == 3) {
                spareTimeMinus = 3 * spareTimeMinusMult;
                return 1;
            }
            else {
                spareTimeMinus = 4 * spareTimeMinusMult;
                return 1;
            }
        }
    }

    @Override
    public void onBackPressed() {
        Toast.makeText(getApplicationContext(), "한 번 더 누르면 뒤로 갑니다", Toast.LENGTH_SHORT).show();
    }

    public void getTimeIntent() {
        Intent intent = getIntent();
        int intentMinute = intent.getIntExtra("minute", 1);
        int intentSecond = intent.getIntExtra("second", 0);
        UseTimerTimeDB = intentMinute + ":" + intentSecond;
        lungeTargetCount = intent.getIntExtra("count", 2);
        lungeAllTimer = (intentMinute*60) + intentSecond;
    }





    //pose
    protected int getContentViewLayoutResId() {
        return R.layout.activity_lunge;
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
        Size viewSize = computeViewSize(width, height);
        Size displaySize = cameraHelper.computeDisplaySizeFromViewSize(viewSize);
        boolean isCameraRotated = cameraHelper.isCameraRotated();

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
        Intent intent = new Intent(this, MainActivity.class);
        startActivity(intent);
        finish(); // 현재 Activity를 종료합니다.
    }

    public void backClick(View view) {
        // 고급 게임 시작
        backToMainActivity();
    }
}