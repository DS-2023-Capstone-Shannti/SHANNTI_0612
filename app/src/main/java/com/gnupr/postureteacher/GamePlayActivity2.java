package com.gnupr.postureteacher;
// 블루투스 없이
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.media.Image;
import android.os.CountDownTimer;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Rect;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.OptIn;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.camera.core.CameraSelector;
import androidx.camera.core.ExperimentalGetImage;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.ImageProxy;
import androidx.camera.core.Preview;
import androidx.camera.lifecycle.ProcessCameraProvider;
import androidx.camera.view.PreviewView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Set;

import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GamePlayActivity2 extends AppCompatActivity {
    private CountDownTimer gameTimer;

    private ImageView[][] imageViews = new ImageView[2][3];
    private TextView scoreTextView;
    private int score;
    private Handler handler;
    private Runnable runnable;

    private int DELAY = 2000;
    private int DURATION;
    Intent intent;
    final int PERMISSION = 1;
    private final String TAG = this.getClass().getSimpleName(); // log

    private static final int REQUEST_ENABLE_BT = 10; // 블루투스 활성화 상태
    private BluetoothAdapter bluetoothAdapter; // 블루투스 어댑터
    private Set<BluetoothDevice> devices; // 블루투스 디바이스 데이터 셋
    private BluetoothDevice bluetoothDevice; // 블루투스 디바이스
    private BluetoothSocket bluetoothSocket = null; // 블루투스 소켓
    private OutputStream outputStream = null; // 블루투스에 데이터를 출력하기 위한 출력 스트림
    private InputStream inputStream = null; // 블루투스에 데이터를 입력하기 위한 입력 스트림
    private Thread workerThread = null; // 문자열 수신에 사용되는 쓰레드
    private byte[] readBuffer; // 수신 된 문자열을 저장하기 위한 버퍼
    private int readBufferPosition; // 버퍼 내 문자 저장 위치
    int pariedDeviceCount;
    private static final int SPEECH_REQUEST_CODE_DIARY = 1001;// You can choose any integer value
    private static final int SPEECH_REQUEST_CODE = 102; // You can choose any integer value

//    private static final int GAME_DURATION = 60000; // 게임 시간 (밀리초 단위)

    private static final int GAME_DURATION = 60000; // 게임 시간 (밀리초 단위)
    private boolean gameRunning = false; // 게임이 실행 중인지 여부를 나타내는 플래그
    private AlertDialog gameEndDialog; // 게임 종료 시 팝업창
    private ImageView redBall;
    private MediaPlayer mediaPlayer1;
    private MediaPlayer mediaPlayer2;
    private MediaPlayer mediaPlayer3,mediaPlayer4;

    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private PreviewView previewView;
    private ProcessCameraProvider cameraProvider;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private FaceDetector detector;
    private static Toast customToast;
    public int countNum = 1;
    private boolean wasInTopHalf = false; // 상단 절반에 얼굴이 있었는지 여부를 추적
    private boolean previewVisible = false; // previewView 가시성 상태를 추적
    private TextView mission, count;
    private boolean missionAssigned = false; // 새로운 미션 할당 여부를 추적
    private int missionIndex, targetCount;
    private Random random = new Random();
    private List<Integer> missionList = new ArrayList<>(Arrays.asList(0, 1, 2));
    private boolean isBalanceCountdownRunning = false;

    ///////////////////////////////////////////////////////////////////////////////////////////////////
    private String difficulty;
    private boolean gameStarted = false;
    private Handler gameEndHandler = new Handler();
    private Runnable previewRunnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game_play2);

        // 게임 시작
        showPopup();

        scoreTextView = findViewById(R.id.scoreTextView);
        // 효과음을 위한 MediaPlayer 초기화
        mediaPlayer1 = MediaPlayer.create(this, R.raw.love); // 효과음 리소스 파일 지정
        mediaPlayer1.setVolume(0.5f, 0.5f); // 볼륨 설정
        mediaPlayer1.setLooping(false); // 반복 재생 설정 제거

        // 배경음악을 위한 MediaPlayer 초기화
        mediaPlayer2 = MediaPlayer.create(this, R.raw.gamemusic); // 배경음악 리소스 파일 지정
        mediaPlayer2.setVolume(0.3f, 0.3f); // 볼륨 설정
        mediaPlayer2.setLooping(true); // 배경음악 반복 재생 설정
        mediaPlayer2.start(); // 배경음악 재생 시작

        previewView = findViewById(R.id.previewView);
        previewView.setVisibility(View.INVISIBLE);
        mission = findViewById(R.id.mission);
        mission.setVisibility(View.INVISIBLE);
        count = findViewById(R.id.count);
        count.setVisibility(View.INVISIBLE);



        // 게임 종료를 처리할 AlertDialog 생성
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("게임 종료");
        builder.setMessage("게임이 종료되었습니다.\n총 점수: " + score);
        builder.setPositiveButton("확인", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                finish(); // 액티비티 종료
            }
        });
        gameEndDialog = builder.create();

        // 게임 타이머 설정
        gameTimer = new CountDownTimer(GAME_DURATION, 1000) {
            private int elapsedTime = 0; // 경과 시간을 추적하기 위한 변수

            @Override
            public void onTick(long millisUntilFinished) {
                // 게임이 시작되었고, 아직 게임이 진행 중인 경우에만 타이머를 업데이트합니다.
                if (gameRunning) {
                    // 경과 시간을 업데이트
                    elapsedTime += 1000;
                    Log.d("time", String.valueOf(elapsedTime));

                    // 25초에 한 번씩 previewView를 VISIBLE로 설정
                    if (elapsedTime >= 20000 && previewView.getVisibility() == View.INVISIBLE) {
                        mediaPlayer3 = MediaPlayer.create(GamePlayActivity2.this, R.raw.exercise_start); // 효과음 리소스 파일 지정
                        mediaPlayer3.setVolume(1.0f, 1.0f); // 볼륨 설정
                        mediaPlayer3.setLooping(false); // 반복 재생 설정 제거
                        mediaPlayer3.start();

                        previewView.setVisibility(View.VISIBLE);
                        mission.setVisibility(View.VISIBLE);
                        count.setVisibility(View.VISIBLE);
                        previewVisible = true; // previewView의 가시성 상태 업데이트
                        // assignMission 메서드를 호출하여 미션 및 카운트 업데이트
                        assignMission();
                        elapsedTime = 0; // 경과 시간 초기화
                    }
                }
            }

            @Override
            public void onFinish() {

            }
        };

        if(Build.VERSION.SDK_INT >=23)
        {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.INTERNET, android.Manifest.permission.CAMERA, android.Manifest.permission.RECORD_AUDIO}, PERMISSION);
        }
        // RecognizerIntent 생성
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName()); // 여분의 키
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR"); // 언어 설정


        // RecognizerIntent 생성
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName()); // 여분의 키
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR"); // 언어 설정

        //Camera Permission
        if (checkSelfPermission(android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        } else {
            cameraBind();
        }
        // 얼굴 감지기 초기화
        initFaceDetector();

        // 게임 타이머를 시작하기 전에 gameStarted 플래그를 설정합니다.
        gameStarted = true;
        gameTimer.start();
    }

    private void showPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.TransparentDialogTheme);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.hero_manual_popup, null);
        builder.setView(dialogView);
        builder.setCancelable(false); // 사용자가 팝업창을 닫을 수 없도록 설정

        // AlertDialog 객체 생성
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        // 팝업창 내부의 이미지와 버튼을 초기화
//        ImageView popupImage = dialogView.findViewById(R.id.popup_image);
//        View popupButton = dialogView.findViewById(R.id.popup_button);

        dialogView.findViewById(R.id.popup_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 팝업창 닫기
                alertDialog.dismiss();
                SetBluetooth();
                // 게임 시작
                gameTimer.start();
            }
        });

        // 팝업창 띄우기
        alertDialog.show();
    }

////////////////////////////////////////////////////////////////////////////////////////

    private void startGame() {
        gameRunning = true;

        // XML 레이아웃에서 timeLeftTextView를 가져옵니다.
        TextView timeLeftTextView = findViewById(R.id.timeLeftTextView);

        // 남은 시간을 업데이트하는 메서드를 사용하여 TextView를 업데이트합니다.
        updateRemainingTime(timeLeftTextView, GAME_DURATION);
//        gameEndHandler.postDelayed(gameEndRunnable, GAME_DURATION);
        // 게임 종료 타이머 설정
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                endGame();
            }
        }, GAME_DURATION);
    }
    // 남은 시간을 업데이트하는 메서드
    private void updateRemainingTime(final TextView timeLeftTextView, long gameDuration) {
        final long[] remainingTime = {gameDuration};

        // 핸들러를 사용하여 주기적으로 남은 시간을 업데이트합니다.
        final Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if (remainingTime[0] > 0) {
                    remainingTime[0] -= 1000; // 1초 감소
                    long secondsLeft = remainingTime[0] / 1000; // 남은 시간을 초 단위로 변환
                    timeLeftTextView.setText(secondsLeft + "초 남음");
                    handler.postDelayed(this, 1000);
                }
            }
        };

        // 처음 실행
        handler.postDelayed(runnable, 1000);
    }

    private Runnable gameEndRunnable = new Runnable() {
        @Override
        public void run() {
            endGame();
        }
    };

    private void endGame() {
        gameRunning = false;
        Intent intent = new Intent(this, GameFinishActivity.class);
        intent.putExtra("SCORE", score);
        startActivity(intent);
        finish();
    }

    @Override
    public void onBackPressed() {
        gameEndHandler.removeCallbacks(gameEndRunnable);
        // 뒤로 가기 버튼이 눌렸을 때 게임을 종료
        gameRunning = false;
        super.onBackPressed();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onPause() {
        super.onPause();
        gameRunning = false;
        gameEndHandler.removeCallbacks(gameEndRunnable);
        // 액티비티가 일시 정지되면 타이머를 중지합니다.
        gameTimer.cancel();
        mediaPlayer2.pause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 액티비티가 다시 시작되면 타이머를 재시작합니다.
        gameTimer.start();
        mediaPlayer2.start();
    }
    ////////////////////////////////////////
    @SuppressLint("MissingPermission")
    private void SetBluetooth() {
        // 블루투스 활성화하기

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter(); // 블루투스 어댑터를 디폴트 어댑터로 설정
        if (bluetoothAdapter == null) { // 디바이스가 블루투스를 지원하지 않을 때
            Toast.makeText(getApplicationContext(), "블루투스 미지원 기기입니다.", Toast.LENGTH_LONG).show();
            // 여기에 처리 할 코드를 작성하세요.
        } else { // 디바이스가 블루투스를 지원 할 때

            if (bluetoothAdapter.isEnabled()) { // 블루투스가 활성화 상태 (기기에 블루투스가 켜져있음)

                selectBluetoothDevice(); // 블루투스 디바이스 선택 함수 호출
            } else { // 블루투스가 비 활성화 상태 (기기에 블루투스가 꺼져있음)

                // 블루투스를 활성화 하기 위한 다이얼로그 출력
                Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                // 선택한 값이 onActivityResult 함수에서 콜백된다.
                startActivityForResult(intent, REQUEST_ENABLE_BT);

            }

        }
    }
    @SuppressLint("MissingPermission")
    public void selectBluetoothDevice() {
        // 이미 페어링 되어있는 블루투스 기기를 찾습니다.
        devices = bluetoothAdapter.getBondedDevices();
        // 페어링 된 디바이스의 크기를 저장
        pariedDeviceCount = devices.size();

        // 페어링 되어있는 장치가 없는 경우
        if (pariedDeviceCount == 0) {
            // 페어링을 하기위한 함수 호출
            Toast.makeText(getApplicationContext(), "먼저 Bluetooth 설정에 들어가 페어링 해주세요", Toast.LENGTH_SHORT).show();
        } else {
            // 디바이스를 선택하기 위한 다이얼로그 생성
            AlertDialog.Builder builder = new AlertDialog.Builder(this);

            builder.setTitle("페어링 되어있는 블루투스 디바이스 목록");
            // 페어링 된 각각의 디바이스의 이름과 주소를 저장
            List<String> list = new ArrayList<>();

            // 모든 디바이스의 이름을 리스트에 추가
            for (BluetoothDevice bluetoothDevice : devices) {
                list.add(bluetoothDevice.getName());
            }

            list.add("취소");

            // List를 CharSequence 배열로 변경
            final CharSequence[] charSequences = list.toArray(new CharSequence[list.size()]);

            list.toArray(new CharSequence[list.size()]);

            // 해당 아이템을 눌렀을 때 호출 되는 이벤트 리스너
            builder.setItems(charSequences, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int which) {
                    if (which == list.size() - 1) {
                        // "취소"가 선택된 경우 다이얼로그를 닫음
                        dialog.dismiss();
                    } else {
                        // 선택한 장치에 연결
                        connectDevice(charSequences[which].toString());
                    }
                }
            });
            // 뒤로가기 버튼 누를 때 창이 안닫히도록 설정
            builder.setCancelable(false);
            // 다이얼로그 생성
            AlertDialog alertDialog = builder.create();
            alertDialog.show();
        }
    }
    @SuppressLint("MissingPermission")
    public void connectDevice(String deviceName) {
        // 페어링 된 디바이스들을 모두 탐색
        for (BluetoothDevice tempDevice : devices) {
            // 사용자가 선택한 이름과 같은 디바이스로 설정하고 반복문 종료
            if (deviceName.equals(tempDevice.getName())) {
                bluetoothDevice = tempDevice;
                break;
            }
        }

        if (bluetoothDevice == null) {
            Toast.makeText(this, "디바이스를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
            return; // 디바이스가 없으면 함수 종료
        }

        Toast.makeText(this, bluetoothDevice + " 연결 완료", Toast.LENGTH_SHORT).show();

        startGame();
        mediaPlayer2.setLooping(true); // 배경음악 반복 재생 설정
        mediaPlayer2.start(); // 배경음악 재생 시작
        initializeGame();

        // UUID 생성
        UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

        // Rfcomm 채널을 통해 블루투스 디바이스와 통신하는 소켓 생성
        try {
            bluetoothSocket = bluetoothDevice.createRfcommSocketToServiceRecord(uuid);
            bluetoothSocket.connect();
            outputStream = bluetoothSocket.getOutputStream();
            inputStream = bluetoothSocket.getInputStream();

            // 데이터 수신 함수 호출
            receiveData();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // "start" 이후에만 버튼 이벤트 및 자이로 값을 처리하는 메서드
    private void handleEventsAndGyroValues(String text) {
        // "start" 이후에만 버튼 이벤트 및 자이로 값을 처리하는 로직을 추가합니다.
        if (text != null && text.equals("start")) {
            // 시스템이 데이터 수신을 시작한 경우
            // 여기에 "push" 및 "zero" 이벤트를 처리하는 로직을 추가할 수 있습니다.
            if ("push".equals(text.trim())) {
                handleClickOnMole();
            } else if ("zero".equals(text.trim())) {
                resetBallPosition();
            }

            // 자이로스코프 값을 처리하는 로직도 추가할 수 있습니다.
            // 예를 들어:
            String[] parts = text.split("/");
            if (parts.length >= 2) {
                try {
                    int gyroX = Integer.parseInt(parts[0].trim()); // gyroX 값
                    int gyroZ = Integer.parseInt(parts[1].trim()); // gyroZ 값
                    moveBall(gyroX, gyroZ);
                } catch (NumberFormatException e) {
                    // 유효하지 않은 자이로 값 처리
                }
            }
        } else {
            // 시스템이 아직 시작되지 않은 경우
            // 여기에 초기화 또는 기본 동작을 추가할 수 있습니다.
        }
    }


//////////////////////////////////////////////////////////////////////////////

    private void initializeGame() {

        // Set up imageViews
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                String imageViewID = "imageView_" + i + j;
                int resID = getResources().getIdentifier(imageViewID, "id", getPackageName());
                imageViews[i][j] = findViewById(resID);
                imageViews[i][j].setVisibility(View.VISIBLE);
                imageViews[i][j].setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ImageView imageView = (ImageView) v;
                        if (imageView.getVisibility() == View.VISIBLE) {
                            score++;
                            scoreTextView.setText(score);
                            mediaPlayer1.start(); // 효과음 재생
                            hideMole(imageView);
                        }
                    }
                });
            }
        }

        handler = new Handler();
        runnable = new Runnable() {
            @Override
            public void run() {
                setMole();
                handler.postDelayed(this, DELAY);
            }
        };
        handler.post(runnable);
        redBall = findViewById(R.id.redBall);


    }
    private void setMole() {
        Random random = new Random();
        int i = random.nextInt(2);
        int j = random.nextInt(3);

        // 클릭 리스너를 제거합니다.
        clearClickListeners();

        // 무작위로 선택된 imageView에 클릭 리스너를 추가합니다.
        imageViews[i][j].setVisibility(View.VISIBLE);
        imageViews[i][j].setImageResource(R.drawable.moleup); // moleup 이미지 설정
        imageViews[i][j].setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                score++; // 클릭 시 점수 증가
                mediaPlayer1.start(); // 효과음 재생
                scoreTextView.setText(score);
                hideMole((ImageView) v);
            }
        });

        // 일정 시간이 지나면 해당 이미지뷰를 숨기고 moledown으로 변경합니다.
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (imageViews[i][j].getVisibility() == View.VISIBLE) {
                    imageViews[i][j].setVisibility(View.VISIBLE);
                    imageViews[i][j].setImageResource(R.drawable.moledown); // moledown 이미지로 변경
                }
            }
        }, DELAY); // 일정 시간 후에 실행되도록 설정
    }


    // 모든 이미지뷰에 있는 클릭 리스너를 제거하는 메서드
    private void clearClickListeners() {
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                imageViews[i][j].setOnClickListener(null);
            }
        }
    }

    private void showMole(final ImageView imageView) {
        AlphaAnimation animation = new AlphaAnimation(0, 1); // 알파값 0에서 1까지
        animation.setDuration(DURATION);
        imageView.startAnimation(animation);
        imageView.setVisibility(View.VISIBLE);
    }
    private void hideMole(final ImageView imageView) {
        imageView.setVisibility(View.VISIBLE);
        imageView.setImageResource(R.drawable.moledown); // moledown 이미지로 변경
        setMole(); // 새로운 클릭 리스너를 추가하여 게임이 계속 진행될 수 있도록 합니다.
    }


    private boolean isCollision(View view1, View view2) {
        int[] location1 = new int[2];
        int[] location2 = new int[2];
        view1.getLocationOnScreen(location1);
        view2.getLocationOnScreen(location2);

        Rect rect1 = new Rect(location1[0], location1[1], location1[0] + view1.getWidth(), location1[1] + view1.getHeight());
        Rect rect2 = new Rect(location2[0], location2[1], location2[0] + view2.getWidth(), location2[1] + view2.getHeight());

        return rect1.intersect(rect2);
    }

    public void receiveData() {
        final Handler handler = new Handler();
        // 데이터를 수신하기 위한 버퍼를 생성
        readBufferPosition = 0;
        readBuffer = new byte[1024];

        // 데이터를 수신하기 위한 쓰레드 생성
        workerThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    if (Thread.currentThread().isInterrupted()) {
                        break;
                    }

                    try {
                        // 데이터를 수신했는지 확인합니다.
                        int byteAvailable = inputStream.available();
                        // 데이터가 수신된 경우
                        if (byteAvailable > 0) {
                            // 입력 스트림에서 바이트 단위로 읽어 옵니다.
                            byte[] bytes = new byte[byteAvailable];
                            inputStream.read(bytes);

                            // 입력 스트림 바이트를 한 바이트씩 읽어 옵니다.
                            for (int i = 0; i < byteAvailable; i++) {
                                byte tempByte = bytes[i];
                                // 개행문자를 기준으로 받음(한줄)
                                if (tempByte == '\n') {
                                    // readBuffer 배열을 encodedBytes로 복사
                                    byte[] encodedBytes = new byte[readBufferPosition];
                                    System.arraycopy(readBuffer, 0, encodedBytes, 0, encodedBytes.length);
                                    // 인코딩된 바이트 배열을 문자열로 변환
                                    final String text = new String(encodedBytes, "US-ASCII");
                                    readBufferPosition = 0;
                                    handler.post(new Runnable() {
                                        @Override
                                        public void run() {
                                            if ("push".equals(text.trim())) {
//                                                Toast.makeText(MainActivity4.this, "눌림", Toast.LENGTH_SHORT).show();
                                                handleClickOnMole();
                                            }
                                            else if ("zero".equals(text.trim())){
                                                resetBallPosition();
                                            }
                                            else
                                            {
                                                String[] parts = text.split("/");
                                                if (parts.length >= 2) { // "/"로 나눈 부분이 적어도 2개 이상인지 확인
                                                    try {
                                                        int gyroX = Integer.parseInt(parts[0].trim()); // gyroX 값
                                                        int gyroZ = Integer.parseInt(parts[1].trim()); // gyroZ 값
                                                        // moveBall 함수 호출
                                                        moveBall(gyroX, gyroZ);
                                                    } catch (NumberFormatException e) {
                                                        // 부적절한 형식의 데이터를 받은 경우, 무시
                                                    }
                                                }
                                            }
                                        }
                                    });
                                } else {
                                    readBuffer[readBufferPosition++] = tempByte;
                                }
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
        workerThread.start();
    }

    private void resetBallPosition() {
        // 화면의 중앙 좌표 계산
        int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        int screenHeight = getWindowManager().getDefaultDisplay().getHeight();
        int centerX = screenWidth / 2;
        int centerY = screenHeight / 2;

        // 공의 크기 계산
        int ballWidth = redBall.getWidth();
        int ballHeight = redBall.getHeight();

        // 공을 화면 중앙으로 이동
        redBall.setX(centerX - ballWidth / 2);
        redBall.setY(centerY - ballHeight / 2);
    }

    private void moveBall(int gyroX, int gyroZ) {
        // 이동량 조절을 위한 상수
        final float MOVE_AMOUNT_MULTIPLIER = 0.1f; // 이동량 보정 상수

        // 현재 공의 위치
        float currentX = redBall.getX();
        float currentY = redBall.getY();

        // 화면의 크기
        int screenWidth = getWindowManager().getDefaultDisplay().getWidth();
        int screenHeight = getWindowManager().getDefaultDisplay().getHeight();

        // 새로운 공의 위치 계산
        float newX = currentX + gyroX * MOVE_AMOUNT_MULTIPLIER;
        float newY = currentY + gyroZ * MOVE_AMOUNT_MULTIPLIER;

        // 화면 경계를 벗어나지 않도록 조정
        newX = Math.max(0, Math.min(newX, screenWidth - redBall.getWidth()));
        newY = Math.max(0, Math.min(newY, screenHeight - redBall.getHeight()));

        // 새로운 위치로 공을 이동
        redBall.setX(newX);
        redBall.setY(newY);
    }

    private void handleClickOnMole() {
        // 모든 mole 이미지에 대해 반복하여 공이 위치한지 확인
        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 3; j++) {
                final int finalI = i; // Final copy of i
                final int finalJ = j; // Final copy of j
                if (isCollision(redBall, imageViews[i][j]) && imageViews[i][j].getVisibility() == View.VISIBLE &&
                        imageViews[i][j].getDrawable().getConstantState().equals(getResources().getDrawable(R.drawable.moleup).getConstantState())) {
                    // 해당 위치에 있는 mole 이미지를 클릭한 것으로 간주하여 점수 획득 및 처리
                    mediaPlayer1.start();
                    score++;
                    scoreTextView.setText(score);
                    hideMole(imageViews[finalI][finalJ]); // 이미지를 숨깁니다.
                    imageViews[finalI][finalJ].setClickable(false); // 클릭 이벤트 비활성화
                    // 일정 시간 후에 다시 클릭 가능하도록 설정
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            imageViews[finalI][finalJ].setClickable(true); // 클릭 이벤트 다시 활성화
                        }
                    }, DELAY); // 일정 시간 지연 후 클릭 가능하도록 설정
                    // 여기에서 다른 필요한 작업 수행
                    return; // 이미지를 클릭했으므로 더 이상 확인할 필요 없음
                }
            }
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer2 != null) {
            mediaPlayer2.release();
            mediaPlayer2 = null;
        }

        if (bluetoothSocket != null) {
            try {
                // 블루투스 소켓이 연결되어 있는 경우에만 연결을 종료합니다.
                bluetoothSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    ///////////////////////////////////////////////////////camera/////////////////////

    private void initFaceDetector() {
        // FaceDetectorOptions 초기화
        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_NONE)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
                        .setMinFaceSize(0.15f)
                        .enableTracking()
                        .build();

        // FaceDetector 초기화
        detector = FaceDetection.getClient(options);
    }

    private void cameraBind() {
        cameraProviderFuture = ProcessCameraProvider.getInstance(this);
        previewView = findViewById(R.id.previewView); // 미리보기 뷰 초기화

        cameraProviderFuture.addListener(() -> {
            try {
                cameraProvider = cameraProviderFuture.get();
                bindPreview();
            } catch (ExecutionException | InterruptedException e) {
                Log.e(TAG, "Error starting camera", e);
            }
        }, ContextCompat.getMainExecutor(this));
    }

    private void bindPreview() {
        Preview preview = new Preview.Builder().build();

        CameraSelector cameraSelector = new CameraSelector.Builder()
                .requireLensFacing(CameraSelector.LENS_FACING_FRONT)
                .build();

        preview.setSurfaceProvider(previewView.getSurfaceProvider());

        ImageAnalysis imageAnalysis =
                new ImageAnalysis.Builder()
                        .setTargetResolution(new Size(640, 480))
                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) // Latest frame is shown
                        .build();

        Executor executor = Executors.newSingleThreadExecutor();
        imageAnalysis.setAnalyzer(executor, new ImageAnalysis.Analyzer() {
            @Override
            public void analyze(@NonNull ImageProxy imageProxy) {
                InputImage image = null;

                @SuppressLint("UnsafeExperimentalUsageError")
                @OptIn(markerClass = ExperimentalGetImage.class)
                Image mediaImage = imageProxy.getImage();

                if (mediaImage != null) {
                    image = InputImage.fromMediaImage(mediaImage, imageProxy.getImageInfo().getRotationDegrees());
                }

                // Process acquired image to detect faces
                Task<List<Face>> result =
                        detector.process(image)
                                .addOnSuccessListener(
                                        new OnSuccessListener<List<Face>>() {
                                            @Override
                                            public void onSuccess(List<Face> faces) {
                                                StringBuilder positionText = new StringBuilder();
                                                // Find the closest face
                                                Face closestFace = null;
                                                float closestDistance = Float.MAX_VALUE;
                                                for (Face face : faces) {
                                                    float distance = calculateDistanceToCamera(face);
                                                    if (distance < closestDistance) {
                                                        closestFace = face;
                                                        closestDistance = distance;
                                                    }
                                                }
                                                if (closestFace != null) {
                                                    // Get bounding box of the closest face
                                                    RectF boundingBox = new RectF(closestFace.getBoundingBox());
                                                    // 좌우 반전된 바운딩 박스
                                                    boundingBox = flipHorizontally(boundingBox, previewView.getWidth());
                                                    // Draw bounding box
                                                    drawBoundingBox(boundingBox);
                                                    // Determine position of face
                                                    String facePosition = getFacePosition(boundingBox);
                                                    // Append position to the text
                                                    positionText.append(facePosition).append("\n");

                                                    // Determine if direction needs to be changed
                                                    determineFacePosition(boundingBox);
                                                }
                                            }
                                        })
                                .addOnFailureListener(
                                        new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                // Task failed with an exception
                                                // ...
                                            }
                                        })
                                .addOnCompleteListener(new OnCompleteListener<List<Face>>() {
                                    @Override
                                    public void onComplete(@NonNull Task<List<Face>> task) {
                                        imageProxy.close(); // v.important to acquire next frame for analysis
                                    }
                                });
            }
        });

        cameraProvider.bindToLifecycle(this, cameraSelector, imageAnalysis, preview);
    }

    // Calculate the distance between the camera and the face
    private float calculateDistanceToCamera(Face face) {
        float faceCenterX = face.getBoundingBox().centerX();
        float faceCenterY = face.getBoundingBox().centerY();
        float imageViewCenterX = (float) previewView.getWidth() / 2;
        float imageViewCenterY = (float) previewView.getHeight() / 2;

        return (float) Math.sqrt(Math.pow(faceCenterX - imageViewCenterX, 2) + Math.pow(faceCenterY - imageViewCenterY, 2));
    }

    private RectF flipHorizontally(RectF rect, int viewWidth) {
        float left = viewWidth - rect.right;
        float right = viewWidth - rect.left;
        rect.left = left;
        rect.right = right;
        return rect;
    }

    private void drawBoundingBox(RectF boundingBox) {
        // 얼굴의 왼쪽 귀 위치
        float earX = boundingBox.left; // 얼굴의 왼쪽 귀 위치를 가져옵니다.
        float earY = boundingBox.centerY(); // 얼굴의 귀가 세로 중앙에 있도록 합니다.

        // 바운딩 박스의 가로, 세로 길이
        float width = boundingBox.width();
        float height = boundingBox.height();

        // 바운딩 박스의 좌상단, 우하단 좌표 계산
        float left = earX - width / 2; // 얼굴의 왼쪽 귀를 중심으로 바운딩 박스를 그립니다.
        float top = earY - height / 2;
        float right = earX + width / 2;
        float bottom = earY + height / 2;

        /*
        // 바운딩 박스의 가로, 세로 길이 및 좌상단, 우하단 좌표 계산
        float width = boundingBox.width();
        float height = boundingBox.height();
        float left = centerX - width / 2;
        float top = centerY - height / 2;
        float right = centerX + width / 2;
        float bottom = centerY + height / 2;
         */

        // 새로운 바운딩 박스 생성
        RectF newBoundingBox = new RectF(left, top, right, bottom);

        // 바운딩 박스를 나타내는 Drawable 생성
        ShapeDrawable shapeDrawable = new ShapeDrawable(new RectShape());
        shapeDrawable.getPaint().setColor(Color.TRANSPARENT);
        shapeDrawable.getPaint().setStyle(Paint.Style.STROKE);
        shapeDrawable.getPaint().setStrokeWidth(4f);
        shapeDrawable.setBounds((int) newBoundingBox.left, (int) newBoundingBox.top, (int) newBoundingBox.right, (int) newBoundingBox.bottom);

        // 이전 바운딩 박스 제거
        removeBoundingBox();

        // PreviewView의 오버레이에 Drawable 추가
        previewView.getOverlay().add(shapeDrawable);
    }

    // Determine position of face

    private String getFacePosition(RectF boundingBox) {
        float imageViewCenterX = (float) previewView.getWidth() / 2;
        float imageViewCenterY = (float) previewView.getHeight() / 2;
        float faceCenterX = boundingBox.centerX();
        float faceCenterY = boundingBox.centerY();

        if (faceCenterX < imageViewCenterX) {
            if (faceCenterY < imageViewCenterY) {
                determineFacePosition(boundingBox); // 바운딩 박스의 위치에 따라 화살표 방향 결정
                return "Top Left";
            } else {
                return "Bottom Left";
            }
        } else {
            if (faceCenterY < imageViewCenterY) {
                determineFacePosition(boundingBox); // 바운딩 박스의 위치에 따라 화살표 방향 결정
                return "Top Right";
            } else {
                return "Bottom Right";
            }
        }
    }

    private void determineFacePosition(RectF boundingBox) {
        float imageViewHeight = (float) previewView.getHeight();
        float thirdHeight = imageViewHeight / 3;
        float boundingBoxCenterY = boundingBox.centerY();

        // Check if the bounding box center Y is in the middle or bottom third
        if ((boundingBoxCenterY >= thirdHeight && boundingBoxCenterY < 2 * thirdHeight) ||
                (boundingBoxCenterY >= 2 * thirdHeight)) {

            if (wasInTopHalf && previewView.getVisibility() == View.VISIBLE) {
                switch (missionIndex) {
                    case 0: // Squat
                        countNum++;
                        count.setText("스쿼트 " + countNum + "회");
                        mission.setText("스쿼트 5회");
                        if (countNum == 5) {
                            completeMission();
                        }
                        break;

                    case 1: // Lunge
                        countNum++;
                        count.setText("런지 " + countNum + "회");
                        mission.setText("런지 5회");
                        if (countNum == 5) {
                            completeMission();
                        }
                        break;

                    case 2: // Balance
                        // Balance mission needs to be handled separately
                        if (!isBalanceCountdownRunning) {
                            startBalanceCountdown();
                        }

                        break;
                }
                wasInTopHalf = false;
            }
        } else if (boundingBoxCenterY < thirdHeight) {
            wasInTopHalf = true;
        }

        // Set a visibility change listener for the previewView
        previewView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                assignMission();
                if (previewView.getVisibility() == View.INVISIBLE) {
                    countNum = 0;
                    resetMission(); // Reset the mission
                    // Remove the listener to avoid unnecessary calls
                    previewView.removeOnLayoutChangeListener(this);
                }
            }
        });


        previewView.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                if (!missionAssigned) {
                    assignMission(); // 프리뷰가 VISIBLE일 때 미션 할당
                }
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                // 필요 시 추가 동작
            }
        });

        previewView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (previewView.getVisibility() == View.INVISIBLE) {
                    countNum = 0; // previewView가 다시 보일 때 countNum 초기화
                    previewView.getViewTreeObserver().removeOnGlobalLayoutListener(this); // 리스너 제거
                }
            }
        });
    }
    private void assignMission() {
        if (missionList.isEmpty()) {
            missionList = new ArrayList<>(Arrays.asList(0, 1, 2)); // 모든 미션이 사용되었을 때 초기화
        }
        missionIndex = missionList.remove(random.nextInt(missionList.size())); // 미션 타입을 랜덤으로 선택

        countNum = 0; // 카운트 초기화

        switch (missionIndex) {
            case 0:
                mission.setText("스쿼트 5회");
                count.setText("스쿼트 0회");
                break;
            case 1:
                mission.setText("런지 5회");
                count.setText("런지 0회");
                break;
            case 2:
                mission.setText("밸런스 10초");
                startBalanceCountdown();
                break;
        }
        missionAssigned = true;
        Log.d(TAG, "Assigned mission index: " + missionIndex); // 로그 추가
    }

    private void startBalanceCountdown() {
        isBalanceCountdownRunning = true;
        new CountDownTimer(10000, 1000) {
            public void onTick(long millisUntilFinished) {
                count.setText("남은 시간: " + millisUntilFinished / 1000 + "초");
            }

            public void onFinish() {
                count.setText("완료!");
                completeMission();
                isBalanceCountdownRunning = false;
            }
        }.start();
    }

    private void completeMission() {
        mission.setText("미션 완료!");
        count.setText("완료!");
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mediaPlayer4 = MediaPlayer.create(GamePlayActivity2.this, R.raw.exercise_finish); // 효과음 리소스 파일 지정
                mediaPlayer4.setLooping(false); // 반복 재생 설정 제거
                mediaPlayer4.setVolume(1.0f, 1.0f);
                mediaPlayer4.start();
                resetMission();
                previewView.setVisibility(View.INVISIBLE); // previewView를 INVISIBLE로 설정
            }
        }, 500);
    }

    private void resetMission() {
        countNum = 0;
        missionAssigned = false;
        mission.setText("");
        count.setText("");
    }

    // Remove previous bounding box
    private void removeBoundingBox() {
        // Remove previous bounding box
        previewView.getOverlay().clear();
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
            }
        }
    }

}
