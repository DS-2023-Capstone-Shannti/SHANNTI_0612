package com.gnupr.postureteacher;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.SurfaceTexture;

import android.util.Log;
import android.util.Size;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.gnupr.postureteacher.Databases.EntityClass.Measure3DatasEntity;
import com.gnupr.postureteacher.Databases.EntityClass.Measure3RoundsEntity;
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
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmark;
import com.google.mediapipe.formats.proto.LandmarkProto.NormalizedLandmarkList;
import com.google.mediapipe.components.CameraHelper;
import com.google.mediapipe.components.CameraXPreviewHelper;
import com.google.mediapipe.components.ExternalTextureConverter;
import com.google.mediapipe.components.FrameProcessor;
import com.google.mediapipe.components.PermissionHelper;
import com.google.mediapipe.framework.AndroidAssetUtil;
import com.google.mediapipe.framework.AndroidPacketCreator;
import com.google.mediapipe.framework.PacketGetter;
import com.google.mediapipe.framework.Packet;
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

public class BalanceActivity extends AppCompatActivity implements TextToSpeech.OnInitListener{

    private FirebaseAuth mFirebaseAuth; // 파이어베이스 인증
    private DatabaseReference mDatabaseRef; // 실시간 데이터베이스
    private TextToSpeech tts;
    private static final String TAG = "BalanceActivity";
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


    private int timer_hour, timer_minute, timer_second;
    //글로벌 시간
    private String text_hour, text_minute, text_second;
    //텍스트 상의 시간
    private String nowTime;
    //지금 시간
    private int totalTime = 0;
    //전체 시간
    private int globalTime = 0;
    //시작 이후의 시간
    public static int spareTime = 100;
    //감지 예비 시간
    private int spareTimeMinus = 1;
    //예비 시간 빼는 값
    private boolean spareTimeCheck = false;
    //예비 시간 측정해도 되는지
    private int tempTime = 0;
    //예비 시간의 임시 시간(정상화 최종 측정)

    LocalDateTime timeMeasure3DataStart = LocalDateTime.now();
    LocalDateTime timeMeasure3DataEnd = LocalDateTime.now();
    //현재 전체 측정 시간
    private boolean timeDataCheck = true;
    //측정 시간 측정해도 되는지

    LocalDateTime timeMeasure3RoundStart = LocalDateTime.now();
    LocalDateTime timeMeasure3RoundEnd = LocalDateTime.now();
    //현재 상세 시간
    private boolean timeRoundCheck = true;
    //상세 시간 측정해도 되는지


    private String[] divideTime;
    //문자열에서 분할된 시간
    private Timer timer = new Timer();
    private boolean pauseTimerCheck = false;
    //false = 흘러감, true = 멈춤

    LocalDate nowLocalDate = LocalDate.now();
    LocalTime nowLocalTime = LocalTime.now();
    String formatedNowLocalTime = nowLocalDate.format(DateTimeFormatter.ofPattern("yyMMdd")) + nowLocalTime.format(DateTimeFormatter.ofPattern("HHmmss"));
    //날짜, 시간 & 문자열에 맞게 날짜+시간 변환

    LocalDateTime Measure3RoundStart = LocalDateTime.now();
    LocalDateTime Measure3RoundEnd = LocalDateTime.now();
    //현재 측정 시간

    LocalDateTime Measure3DataStart = LocalDateTime.now();
    LocalDateTime Measure3DataEnd = LocalDateTime.now();
    //현재 상세 시간

    private String UseTimerTimeDB = "01:22:33";
    //템플릿 타이머 시간 (시간:분:초)
    private final long finishtimeed = 2500;
    private long presstime = 0;

    private int finalStopCheck = 0;
    //완전 종료 대기 확인
    //0 아무것도 아님, 1 돌입 대기, 2 돌입

    //private TextView tv2;
    //private TextView tv6;
    private TextView tv_TimeCounter;

    private ImageView iv1;
    private ImageView iv2;
    //private ImageView iv3;
    private ImageView iv4;
    private ImageView iv5;
    //private ImageView iv6;

    class markPoint {
        float x;
        float y;
        float z;
    }

    private NormalizedLandmark[] bodyAdvancePoint = new NormalizedLandmark[33];
    //임시 랜드마크 포인트 변수
    private markPoint[] bodyMarkPoint = new markPoint[35];
    //몸 랜드마크 포인트 변수
    private float[] bodyRatioMeasure3ment = new float[33];
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
    private int timeInSeconds;

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
                timeInSeconds = 23000;
                break;
            case "medium":
                timeInSeconds = 33000;
                break;
            case "hard":
                timeInSeconds = 43000;
                break;
            default:
                timeInSeconds = 23000;
                break;
        }
///////////////////////////////////////////////////////////////////////////////

        getTimeIntent();
        iv1= findViewById(R.id.imageView7);
        iv2= findViewById(R.id.imageView4);
        //iv3= findViewById(R.id.imageView5);
        iv4= findViewById(R.id.imageView3);
        iv5= findViewById(R.id.imageView6);
        //iv6= findViewById(R.id.imageView8);

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
                                bodyMarkPoint[i].x = bodyAdvancePoint[i].getX() * 1000f;
                                //tv6.setText("e");
                                bodyMarkPoint[i].y = bodyAdvancePoint[i].getY() * 1000f;
                                //tv6.setText("f");
                                bodyMarkPoint[i].z = bodyAdvancePoint[i].getZ() * 1000f;
                                //tv6.setText("g");
                                bodyRatioMeasure3ment[i] = bodyMarkPoint[i].x / (ratioPoint_1b - ratioPoint_1a);
                                //tv6.setText("h");
                                bodyRatioMeasure3ment[i] = bodyMarkPoint[i].y / (ratioPoint_1b - ratioPoint_1a);
                                //tv6.setText("i");
                                bodyRatioMeasure3ment[i] = bodyMarkPoint[i].z / (ratioPoint_1b - ratioPoint_1a);
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



    class ThreadClass extends Thread {
        private boolean isTtsRequested = false;
        private Handler uiHandler = new Handler(Looper.getMainLooper());
        @Override
        public void run() {
            // 정기적으로 실행할 코드를 UI 스레드에서 실행
            uiHandler.post(new Runnable() {
                @Override
                public void run() {
                    Log.d("timeInSeconds",String.valueOf(timeInSeconds));
                    // 타이머 설정 부분
                    if (!isTtsRequested) {
                        isTtsRequested = true;
                        Timer timer = new Timer();
                        timer.schedule(new TimerTask() {
                            @Override
                            public void run() {
                                // timeInSeconds만큼 시간이 지났을 때 TTS 출력 및 페이지 이동
                                uiHandler.post(new Runnable() {
                                    @Override
                                    public void run() {
                                        tts.speak("밸런스 운동이 끝났습니다.", TextToSpeech.QUEUE_FLUSH, null, "endExercise");
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
                                                            int newTime = 20;
                                                            int newKcal = 100;
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
                                                    newUserData.put("Time", 20);
                                                    newUserData.put("Kcal", 100);

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
                                        tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                                            @Override
                                            public void onStart(String utteranceId) {}

                                            @Override
                                            public void onDone(String utteranceId) {
                                                Intent intent = new Intent(getApplicationContext(), ShowExerciseActivity.class);
                                                startActivity(intent);
                                                finish();  // 현재 액티비티 종료
                                            }

                                            @Override
                                            public void onError(String utteranceId) {}
                                        });
                                    }
                                });
                            }
                        }, timeInSeconds);
                    }

                    // 여기에 원래의 비즈니스 로직을 계속 추가합니다
                    if (bodyMarkPoint[11].z > bodyMarkPoint[12].z)
                        getLandmarksAngleResult(0);
                    else
                        getLandmarksAngleResult(1);

                    if (!pauseTimerCheck) {
                        if (getResultPosture(resultPosture) == 2) {
                            if (spareTime >= 90) {
                                if (spareTimeCheck && finalStopCheck == 0) {
                                    if (tempTime >= 6) {
                                        saveMeasure3Datas();
                                        spareTimeCheck = false;
                                    } else if (tempTime < 6)
                                        tempTime++;
                                }
                            }
                            spareTime = 100;
                        } else if (getResultPosture(resultPosture) == 1) {
                            if (spareTime <= 0) {
                                if (!spareTimeCheck) {
                                    Measure3DataStart = LocalDateTime.now();
                                    spareTimeCheck = true;
                                }
                            }
                            if (spareTime > 0) {
                                spareTime -= spareTimeMinus;
                            }
                            if (tempTime > 0) {
                                tempTime = 0;
                            }
                        }
                    }

                    if (finalStopCheck == 0) {
                        tv_TimeCounter.setText(nowTime);
                    } else if (finalStopCheck == 1 || finalStopCheck == 2) {
                        tv_TimeCounter.setText(timer_second + "초 후 메인화면");
                    }

                    if (finalStopCheck == 1) {
                        saveMeasure3Rounds();
                        if (spareTimeCheck) {
                            saveMeasure3Datas();
                        }
                    }

                    if (finalStopCheck == 2 && timer_second <= 0) {
                        Intent intent = new Intent(getApplicationContext(), ShowExerciseActivity.class);
                        startActivity(intent);
                        pauseTimerCheck = true;
                        ui_HandlerCheck = false;
                        finish();
                    }

                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        startThreadCheck = true;
                    }
                    if (ui_HandlerCheck) {
                        uiHandler.post(this);
                    }
                }
            });
        }
    }




    TimerTask timerTask = new TimerTask() {
        @Override
        public void run() {
            // Increment global time
            globalTime++;
            if (!pauseTimerCheck) {
                // Decrease timer_second if it is greater than 0
                if (timer_second > 0) {
                    timer_second--;
                }

                // Update text_second with leading zero if necessary
                if (timer_second <= 9) {
                    text_second = "0" + timer_second;
                } else {
                    text_second = Integer.toString(timer_second);
                }

                // Update the nowTime string
                nowTime = "00:00:" + text_second + " (" + spareTime + "%) ";
            }

            // If timer reaches 0 seconds
            if (timer_second == 0) {
            /*timerTask.cancel();//타이머 종료
            timer.cancel();//타이머 종료
            timer.purge();//타이머 종료*/
                //중간에 잠시 멈추는 건 타이머를 죽이는 게 아니라 타이머를 보기로만 잠시 멈춰두고 다시 시작할 때 시간을 새로 갱신
                if (finalStopCheck == 0) {
                    timer_second += 3;
                    finalStopCheck = 1;
                }
            }
        }
    };

    private void startDialog() {
        //이건 자르던지 바꾸던지 하셈
        tv_TimeCounter = findViewById(R.id.TimeCounter);
        AlertDialog.Builder msgBuilder = new AlertDialog.Builder(BalanceActivity.this);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.custom_dialog_layout, null);
        ImageView gifImageView = dialogView.findViewById(R.id.gifImageView);
        TextView textView = dialogView.findViewById(R.id.Text);

        // GIF 파일 설정
        Glide.with(this).load(R.drawable.balancegif).into(gifImageView);
        textView.setText("양팔을 수평으로 펴고 오른쪽 다리를 왼쪽 허벅지 안쪽에 붙힌 채 중심을 잡으세요.");
        textView.setGravity(Gravity.CENTER);
        // 나머지 메시지 설정

        msgBuilder.setView(dialogView);

        AlertDialog msgDlg = msgBuilder.create();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                int timee = (timeInSeconds-3000)/1000;
                // 대화 상자가 null이 아니고, 표시되어 있을 경우에만 닫기
                if (msgDlg != null && msgDlg.isShowing()) {
                    msgDlg.dismiss();
                        tts.speak("밸런스를 시작합니다. 자세를" + timee + "초 유지하세요", TextToSpeech.QUEUE_FLUSH, null, null);

                    divideTime = UseTimerTimeDB.split(":");
                    timer_hour = Integer.parseInt(divideTime[0]);
                    timer_minute = Integer.parseInt(divideTime[1]);
                    timer_second = Integer.parseInt(divideTime[2]);
                    totalTime = ((((timer_hour * 60) + timer_minute) * 60) + timer_second) * 1000;
                    timer.scheduleAtFixedRate(timerTask, 10000, 1000); // Timer 실행
                }
            }
        }, 3500); // 5초 후에 닫기
        msgDlg.show();
    }



    public void onClickExit(View view) {
        if (finalStopCheck == 0) {
            saveMeasure3Rounds();
            if (spareTimeCheck) {
                saveMeasure3Datas();
            }
//            Intent intent = new Intent(this, ShowExerciseActivity.class);
//            startActivity(intent);
//            pauseTimerCheck = true;
//            ui_HandlerCheck = false;
//            finish();
        }

        Intent intent = new Intent(this, ShowExerciseActivity.class);
        startActivity(intent);
        pauseTimerCheck = true;
        ui_HandlerCheck = false;
        finish();
    }

    private void saveMeasure3Rounds() { //여기가 측정 시간 저장, 전체
        LocalDateTime Measure3RoundStartTime_num = Measure3RoundStart;
        Measure3RoundEnd = LocalDateTime.now();
        LocalDateTime Measure3RoundEndTime_num = Measure3RoundEnd;

        Measure3RoundsEntity Measure3RoundsTable = new Measure3RoundsEntity();
        Measure3RoundsTable.setMeasure3RoundStartTime(Measure3RoundStartTime_num);
        Measure3RoundsTable.setMeasure3RoundEndTime(Measure3RoundEndTime_num);
        MeasureRoomDatabase.getDatabase(getApplicationContext()).getMeasure3RoundsDao().insert(Measure3RoundsTable);
        //MeasureRoomDatabase.getDatabase(getApplicationContext()).getMeasureRoundsDao().deleteAll(); 이건 삭제

        Toast.makeText(this, "전체 시간 저장", Toast.LENGTH_SHORT).show();
        finalStopCheck = 2;
    }


    private void saveMeasure3Datas() { //여기가 감지 집중 시간 저장, 일시
        LocalDateTime Measure3DataStartTime_num = Measure3DataStart;
        Measure3DataEnd = LocalDateTime.now();
        LocalDateTime Measure3DataEndTime_num = Measure3DataEnd;
        LocalDateTime Measure3RoundStartTimeFK_num = Measure3RoundStart;

        Measure3DatasEntity Measure3DatasTable = new Measure3DatasEntity();
        Measure3DatasTable.setMeasure3DataStartTime(Measure3DataStartTime_num);
        Measure3DatasTable.setMeasure3DataEndTime(Measure3DataEndTime_num);
        Measure3DatasTable.setMeasure3RoundStartTimeFK(Measure3RoundStartTimeFK_num);
        MeasureRoomDatabase.getDatabase(getApplicationContext()).getMeasure3DatasDao().insert(Measure3DatasTable);
        //MeasureRoomDatabase.getDatabase(getApplicationContext()).getMeasureDatasDao().deleteAll(); 이건 삭제

        Toast.makeText(this, "상세 시간 저장", Toast.LENGTH_SHORT).show();
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
        //RIGHT LEG
        if (OutOfRangeSave[23] == true && OutOfRangeSave[25] == true && OutOfRangeSave[27] == true) { //범위 판별
            angleCalculationResult(23, 25, 27, 30f, 120f); //90f 120f | 70f 140f | 80f 130f
            //오른쪽 엉덩이 - 무릎 - 발목
            if (markResult[23][25][27] == true) { //각도 판별
                iv1.setImageResource(R.drawable.balance_leg_green);
                resultPosture[0] = 2;
            } else {
                iv1.setImageResource(R.drawable.balance_leg_red);
                resultPosture[0] = 1;
            }
        } else {
            //여기에 비감지(회색)
            iv1.setImageResource(R.drawable.balance_leg_gray);
            markResult[23][25][27] = true;
            resultPosture[0] = 0;
        }

        //SPINE & LEFT LEG
        if (OutOfRangeSave[8] == true && OutOfRangeSave[24] == true && OutOfRangeSave[26] == true) { //범위 판별
            angleCalculationResult(8, 24, 26, 150f, 210f); //130f 180f | 120f 180f | 140f 180f
            //왼쪽 귀-엉덩이- 무릎 엉덩이 각도
            if (markResult[8][24][26] == true) { //각도 판별
                iv2.setImageResource(R.drawable.balance_spine_green);
                resultPosture[1] = 2;
            } else {
                iv2.setImageResource(R.drawable.balance_spine_red);
                resultPosture[1] = 1;
            }
        } else {
            //여기에 비감지(회색)
            iv2.setImageResource(R.drawable.balance_spine_gray);
            markResult[8][24][26] = true;
            resultPosture[1] = 0;
        }


        bodyMarkPoint[33 + side] = new markPoint();
        if(side == 0)
            bodyMarkPoint[33 + side].x = bodyAdvancePoint[7].getX() * 1000f + 300;
        else
            bodyMarkPoint[33 + side].x = bodyAdvancePoint[7].getX() * 1000f - 300;
        bodyMarkPoint[33 + side].y = bodyAdvancePoint[7].getY() * 1000f - 10;
        bodyMarkPoint[33 + side].z = bodyAdvancePoint[7].getZ() * 1000f + 10;
        if (OutOfRangeSave[7 + side] == true && OutOfRangeSave[23 + side] == true) { //범위 판별
            if (!Double.isNaN(getLandmarksAngleTwo(bodyMarkPoint[33 + side], bodyMarkPoint[7 + side], bodyMarkPoint[30 + side], 'x', 'y'))) {
                if (getLandmarksAngleTwo(bodyMarkPoint[33 + side], bodyMarkPoint[7 + side], bodyMarkPoint[23 + side], 'x', 'y') >= 10f
                        && getLandmarksAngleTwo(bodyMarkPoint[33 + side], bodyMarkPoint[7 + side], bodyMarkPoint[23 + side], 'x', 'y') <= 440f)
                { //90f 140f | 80f 160f | 80f 120f | 80f 140f
                    markResult[7 + side][7 + side][23 + side] = true;
                } else {
                    markResult[7 + side][7 + side][23 + side] = false;
                }
                if (markResult[7 + side][7 + side][23 + side] == true) { //각도 판별
                    iv4.setImageResource(R.drawable.balance_head_green);
                    resultPosture[2] = 2;
                } else {
                    iv4.setImageResource(R.drawable.balance_head_red);
                    resultPosture[2] = 1;
                }
            }
            //어깨-귀-귀너머(x+300)
        } else {
            //여기에 비감지(회색)
            iv4.setImageResource(R.drawable.balance_head_gray);
            markResult[7 + side][7 + side][11 + side] = true;
            resultPosture[2] = 0;
        }



        //ARM
        if (OutOfRangeSave[21 + side] == true && OutOfRangeSave[13 + side] == true && OutOfRangeSave[12 + side] == true) { //범위 판별
            angleCalculationResult(21 + side, 13 + side, 12 + side, 160f, 200f); //90f 120f | 70f 140f
            //엄지-왼쪽 팔꿈치-오른쪽 어깨 각도
            if (markResult[21 + side][13 + side][12 + side] == true) { //각도 판별
                iv5.setImageResource(R.drawable.balance_arm_green);
                resultPosture[3] = 2;
            } else {
                iv5.setImageResource(R.drawable.balance_arm_red);
                resultPosture[3] = 1;
            }
        } else {
            //여기에 비감지(회색)
            iv5.setImageResource(R.drawable.balance_arm_gray);
            markResult[21 + side][13 + side][12 + side] = true;
            resultPosture[3] = 0;
        }

        if (markResult[23][25][27] && markResult[8][24][26]
                && markResult[7 + side][7 + side][23 + side] && markResult[21 + side][13 + side][12 + side])
            sideTotalResult[side] = true;
        else
            sideTotalResult[side] = false;
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
                spareTimeMinus = 4;
                return 1;
            }
        }
        else if(zeroCount == 2) {
            if(oneCount == 0) {
                spareTimeMinus = 0;
                return 2;
            }
            else if(oneCount == 1) {
                spareTimeMinus = 2;
                return 1;
            }
            else {
                spareTimeMinus = 4;
                return 1;
            }
        }
        else if(zeroCount == 1) {
            if(oneCount == 0) {
                spareTimeMinus = 0;
                return 2;
            }
            else if(oneCount == 1) {
                spareTimeMinus = 1;
                return 1;
            }
            else if(oneCount == 2) {
                spareTimeMinus = 2;
                return 1;
            }
            else {
                spareTimeMinus = 4;
                return 1;
            }
        }
        else {
            if(oneCount == 0) {
                spareTimeMinus = 0;
                return 2;
            }
            else if(oneCount == 1) {
                spareTimeMinus = 1;
                return 1;
            }
            else if(oneCount == 2) {
                spareTimeMinus = 2;
                return 1;
            }
            else if(oneCount == 3) {
                spareTimeMinus = 3;
                return 1;
            }
            else {
                spareTimeMinus = 4;
                return 1;
            }
        }
    }

    @Override
    public void onBackPressed() {
        long tempTimeOBP = System.currentTimeMillis();
        long intervalTime = tempTimeOBP - presstime;

        if (0 <= intervalTime && finishtimeed >= intervalTime)
        {
            if(1 <= globalTime) {
                if (finalStopCheck == 0) {
                    saveMeasure3Rounds();
                    if (spareTimeCheck) {
                        saveMeasure3Datas();
                    }
                }

                Intent intent = new Intent(this, ShowExerciseActivity.class);
                startActivity(intent);
                pauseTimerCheck = true;
                ui_HandlerCheck = false;
                finish();
            }
        }
        else
        {
            presstime = tempTimeOBP;
            Toast.makeText(getApplicationContext(), "한 번 더 누르면 뒤로 갑니다", Toast.LENGTH_SHORT).show();
        }
    }

    public void getTimeIntent() {
        Intent intent = getIntent();
        int intentSecond = intent.getIntExtra("second", 20); // Changed to get seconds from intent
        timer_second = intentSecond;
        UseTimerTimeDB = "00:00:" + (timer_second <= 9 ? "0" + timer_second : timer_second);
    }

    //pose
    protected int getContentViewLayoutResId() {
        return R.layout.activity_balance;
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


}