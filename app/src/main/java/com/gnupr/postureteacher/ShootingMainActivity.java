package com.gnupr.postureteacher;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.media.Image;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.RecognizerIntent;
import android.util.Log;
import android.util.Size;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
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
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class ShootingMainActivity extends AppCompatActivity {

    private MediaPlayer mediaPlayer1;
    private MediaPlayer mediaPlayer2;
    private MediaPlayer mediaPlayer3;

    private long lastCallTime = 0;
    private static final long CALL_DELAY = 5000; // 딜레이 시간 (5초)

    private ImageView[][] imageViews = new ImageView[3][4];
    private TextView scoreTextView;
    private int score = 0;
    private Handler handler;
    private Runnable runnable;
    private int DELAY = 1700;
    private int DURATION = 840;
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
    private static final int GAME_DURATION = 60000; // 게임 시간 (밀리초 단위)
    private boolean gameRunning = false; // 게임이 실행 중인지 여부를 나타내는 플래그
    private AlertDialog gameEndDialog; // 게임 종료 시 팝업창
    private ImageView redBall, fire;
    private boolean moveRight = true;
    private ImageView rocket;
    private boolean RmoveRight = true;
    private Handler Rhandler;
    private ImageView lifeItem, shotItem;
    private Handler lifeHandler, shotHandler;
    private boolean moveLifeItem = true, moveShotItem = true;
    private int collisionCount = 4, collisionCount2 = 0;
    private boolean canCollide = true, canCollide2 = true; // 충돌 허용 여부를 나타내는 플래그
    private static final int COLLISION_DELAY = 1000; // 충돌 감지 딜레이 (밀리초)
    private boolean canShoot = false;
    private TextView count, hitscore, time;
    private int countDownValue = 3;
    float distance= 1.0f;
    private ListenableFuture<ProcessCameraProvider> cameraProviderFuture;
    private PreviewView previewView;
    private ProcessCameraProvider cameraProvider;
    private static final int MY_CAMERA_REQUEST_CODE = 100;
    private FaceDetector detector;
    private TextView position;
    private static Toast customToast;
    private RectF previousBoundingBox = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shooting_main);
        count = findViewById(R.id.count);
        hitscore = findViewById(R.id.ShotTextView);
        time = findViewById(R.id.Time);
        redBall = findViewById(R.id.redBall);
        rocket = findViewById(R.id.rocket2);
        Rhandler = new Handler();
        handler = new Handler();
        fire = findViewById(R.id.fire);

        lifeItem = findViewById(R.id.lifeItem);
        shotItem = findViewById(R.id.redshot);

        previewView = findViewById(R.id.previewView);

        showPopup();

        // 효과음을 위한 MediaPlayer 초기화
        mediaPlayer1 = MediaPlayer.create(this, R.raw.hit); // 효과음 리소스 파일 지정
        mediaPlayer1.setVolume(0.5f, 0.5f); // 볼륨 설정
        mediaPlayer1.setLooping(false); // 반복 재생 설정 제거

        // 배경음악을 위한 MediaPlayer 초기화
        mediaPlayer2 = MediaPlayer.create(this, R.raw.hitgame); // 배경음악 리소스 파일 지정
        mediaPlayer2.setVolume(0.3f, 0.3f); // 볼륨 설정
        mediaPlayer2.setLooping(true);
        mediaPlayer2.start(); // 배경음악 재생 시작

        mediaPlayer3 = MediaPlayer.create(this, R.raw.bounce); // 효과음 리소스 파일 지정
        mediaPlayer3.setVolume(0.5f, 0.5f); // 볼륨 설정
        mediaPlayer3.setLooping(false); // 반복 재생 설정 제거

        SharedPreferences sharedPref = getSharedPreferences("Distance",Context.MODE_PRIVATE);
        distance = sharedPref.getFloat("distance",1.00f);


        if (Build.VERSION.SDK_INT >= 23) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.INTERNET, Manifest.permission.RECORD_AUDIO}, PERMISSION);
        }
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName()); // 여분의 키
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR"); // 언어 설정


        redBall.setVisibility(View.INVISIBLE);
        rocket.setVisibility(View.INVISIBLE);
        lifeItem.setVisibility(View.INVISIBLE);
        shotItem.setVisibility(View.INVISIBLE);
        fire.setVisibility(View.VISIBLE);

        //Camera Permission
        if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_CAMERA_REQUEST_CODE);
        }else {
            cameraBind();
        }
        // 얼굴 감지기 초기화
        initFaceDetector();

    }

    private void showPopup() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, R.style.TransparentDialogTheme);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.shooting_manual_popup, null);
        builder.setView(dialogView);
        builder.setCancelable(false); // 사용자가 팝업창을 닫을 수 없도록 설정

        // AlertDialog 객체 생성
        final AlertDialog alertDialog = builder.create();
        alertDialog.show();
        // 확인 버튼 클릭 리스너 설정
        dialogView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 팝업창 닫기
                alertDialog.dismiss();
                SetBluetooth();
                // 게임 시작
                startCountDown();
            }
        });

        // 팝업창 띄우기
        alertDialog.show();
    }


    private void initializeGame() {
        collisionCount2 = 0;
        hitscore.setText("Hit : " + collisionCount2);
        redBall.setVisibility(View.VISIBLE);
        rocket.setVisibility(View.VISIBLE);
        lifeItem.setVisibility(View.VISIBLE);
        shotItem.setVisibility(View.VISIBLE);
        // Rocket 이동
        Rhandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                moveRocket();
                Rhandler.postDelayed(this, 50); // 50밀리초마다 실행
            }
        }, 50);
        shotItem = findViewById(R.id.redshot);
        shotHandler = new Handler();

        // Life Item 이동
        lifeItem = findViewById(R.id.lifeItem);
        lifeHandler = new Handler();
        lifeHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                moveLifeItem();
                lifeHandler.postDelayed(this, 100); // 50밀리초마다 실행
            }
        }, 100);

        shotHandler.postDelayed(new Runnable() {
            @Override
            public void run() {
                moveShotItem();
                shotHandler.postDelayed(this, 50); // 50밀리초마다 실행
            }
        }, 50);
    }


    private void startCountDown() {
        count.setText(String.valueOf(countDownValue));

        final ImageView startImage = findViewById(R.id.startImage);
        startImage.setVisibility(View.INVISIBLE);

        if (countDownValue > 0) {
            // 카운트 다운이 끝날 때까지 숫자를 하나씩 줄이면서 다음 숫자를 표시
            final int imageResource = getResources().getIdentifier("count" + countDownValue, "drawable", getPackageName());
            count.setCompoundDrawablesWithIntrinsicBounds(0, imageResource, 0, 0);
            countDownValue--;

            // 다음 숫자를 표시하기 위해 1초 후에 다시 실행
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    startCountDown();
                }
            }, 1500); // 1초 딜레이
        } else {
            // 카운트 다운이 완료되면 텍스트뷰를 숨기고 게임 시작 이미지를 표시
            count.setVisibility(View.INVISIBLE);
            startImage.setVisibility(View.VISIBLE);

            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startImage.setVisibility(View.INVISIBLE);
                    // 게임 시작 또는 원하는 동작 수행
                    startGame();
                    initializeGame();
                }
            }, 1000);
        }
    }





    private void startGame() {
        gameRunning = true;
        updateRemainingTime(time, GAME_DURATION);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                endGame();
            }
        }, GAME_DURATION);

    }

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
    private void endGame() {
        gameRunning = false;
        super.onDestroy();
        Intent intent = new Intent(this, ShootingGameOver.class);
        intent.putExtra("collisionCount", collisionCount2); // Pass the collision count
        startActivity(intent);
        finish();
    }

    private String extractNumber(String str) {
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            if (Character.isDigit(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    // Life Item 이동 및 충돌 감지
    private void moveLifeItem() {
        int currentX = (int) rocket.getX();
        lifeItem.setX(currentX); // 로켓의 x 좌표를 하트의 초기 x 좌표로 설정
        lifeItem.setImageResource(R.drawable.explosion0);

        int newY = 0;
        int currentY = (int) lifeItem.getY();
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int lifeItemHeight = lifeItem.getHeight();

        if (moveLifeItem) {
            newY = currentY + 20; // 20씩 이동
            if (newY + lifeItemHeight >= screenHeight) {
                // 아이템이 화면 아래에 도달하면 다시 위로 이동
                newY = 0;
            }
        }

        lifeItem.setY(newY); // lifeItem의 새로운 Y 위치 설정

        if (canCollide && isCollision(lifeItem, redBall)) {
            // 충돌 시 이미지 변경
            lifeItem.setImageResource(R.drawable.explosion8);

            // 충돌 횟수에 따라 하트 이미지뷰 변경
            findViewById(R.id.heart4).setVisibility(View.VISIBLE);
            findViewById(R.id.heart3).setVisibility(View.VISIBLE);
            findViewById(R.id.heart2).setVisibility(View.VISIBLE);
            findViewById(R.id.heart1).setVisibility(View.VISIBLE);
            collisionCount--;
            Log.v(TAG, "collisionCount: " + collisionCount); // 로그에 출력
            mediaPlayer3.start();

            switch (collisionCount) {
                case 4:
                case 3:
                    findViewById(R.id.heart4).setVisibility(View.GONE);
                    findViewById(R.id.heart3).setVisibility(View.VISIBLE);
                    findViewById(R.id.heart2).setVisibility(View.VISIBLE);
                    findViewById(R.id.heart1).setVisibility(View.VISIBLE);
                    break;
                case 2:
                    findViewById(R.id.heart4).setVisibility(View.GONE);
                    findViewById(R.id.heart3).setVisibility(View.GONE);
                    findViewById(R.id.heart2).setVisibility(View.VISIBLE);
                    findViewById(R.id.heart1).setVisibility(View.VISIBLE);
                    break;
                case 1:
                    findViewById(R.id.heart4).setVisibility(View.GONE);
                    findViewById(R.id.heart3).setVisibility(View.GONE);
                    findViewById(R.id.heart2).setVisibility(View.GONE);
                    findViewById(R.id.heart1).setVisibility(View.VISIBLE);
                    break;
                default:
                    break;
            }


// 충돌 횟수를 TextView에 업데이트
//            scoreTextView.setText("Life: " + collisionCount);

// 충돌 횟수가 0이면 게임 종료
            if (collisionCount <= 0) {
                endGame();
            }

            // 충돌 감지 후 일정 시간 동안 충돌 허용하지 않음
            canCollide = false;
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    canCollide = true;
                }
            }, COLLISION_DELAY);
        }
    }

    private void moveShotItem() {
//        shotItem.setVisibility(View.INVISIBLE);
        int currentX = (int) redBall.getX();
        shotItem.setX(currentX); // 로켓의 x 좌표를 총알의 초기 x 좌표로 설정
        shotItem.setImageResource(R.drawable.shot);

        int newY = 0;
        int currentY = (int) shotItem.getY();
        int screenHeight = getResources().getDisplayMetrics().heightPixels;
        int shotItemHeight = shotItem.getHeight();

        if (moveShotItem) {
            newY = currentY - 20; // 20씩 이동
            if (newY <= 0) {
                // 아이템이 화면 위로 사라지면 다시 아래로 이동
                newY = screenHeight;
            }
        }
        shotItem.setY(newY); // shotItem의 새로운 Y 위치 설정

        if (canCollide2 && isCollision(shotItem, rocket)) {
            // 충돌 시 이미지 변경
            shotItem.setImageResource(R.drawable.explosion8);
            // 충돌 횟수를 1 증가
            collisionCount2++;
            mediaPlayer1.start();
            // 충돌 횟수를 TextView에 업데이트
            hitscore.setText("Hit : " + collisionCount2);

            // 충돌 감지 후 일정 시간 동안 충돌 허용하지 않음
            canCollide2 = false;
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    canCollide2 = true;
                }
            }, COLLISION_DELAY);
        }
    }

    private void startToastMessageRepeater() {
        // 마지막 호출 시간과 현재 시간을 비교하여 딜레이 적용
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastCallTime < CALL_DELAY) {
            // 딜레이 중이므로 함수를 호출하지 않음
            return;
        }

        // 딜레이가 지난 경우 토스트 메시지 표시
        //showCustomToast(getApplicationContext(), "스쿼트 1회!", 1000); // 1초 동안 토스트 메시지 표시
        collisionCount2 += 2;
        hitscore.setText("Hit : " + collisionCount2);
        fire.setVisibility(View.GONE);


        // 함수 호출 시간 기록
        lastCallTime = currentTime;

        // 딜레이를 적용하기 위해 다시 함수를 호출하지 않도록 함
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 여기에 딜레이 이후 실행할 작업이 있으면 추가할 수 있습니다.
                fire.setVisibility(View.VISIBLE);
            }
        }, CALL_DELAY);
    }

    // Rocket 이동
    private void moveRocket() {
        int newX;
        int currentX = (int) rocket.getX();
        int screenWidth = getResources().getDisplayMetrics().widthPixels;
        int rocketWidth = rocket.getWidth();

        // 오른쪽으로 이동 중이면
        if (RmoveRight) {
            newX = currentX + 10; // 10씩 이동
            // 오른쪽 벽에 닿으면 방향을 바꿈
            if (newX + rocketWidth >= screenWidth - 200) {
                RmoveRight = false;
            }
        } else { // 왼쪽으로 이동 중이면
            newX = currentX - 10; // 10씩 이동
            // 왼쪽 벽에 닿으면 방향을 바꿈
            if (newX <= 0) {
                RmoveRight = true;
            }
        }
        rocket.setX(newX); // 로켓의 새로운 x 위치 설정
    }

    // 두 뷰의 충돌 여부 확인
    private boolean isCollision(View view1, View view2) {
        Rect rect1 = new Rect();
        view1.getHitRect(rect1);
        Rect rect2 = new Rect();
        view2.getHitRect(rect2);
        return Rect.intersects(rect1, rect2);
    }

    // Red Ball 위치 초기화
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

    // Red Ball 이동 방향 변경
    private void changeDirection() {
        float currentX = redBall.getX();
        // 화면의 크기
        int screenWidth = getWindowManager().getDefaultDisplay().getWidth();

        // 이동할 거리
        float distance = moveRight ? 40 : -40;
        // 새로운 공의 위치 계산
        float newX = currentX + distance;
        // 화면 경계를 벗어나지 않도록 조정
        newX = Math.max(0, Math.min(newX, (screenWidth - 200) - redBall.getWidth()));

        // 새로운 위치로 공을 이동
        redBall.setX(newX);
        redBall.setImageResource(R.drawable.rocket1);
    }

    private void fastchangeDirection() {
        float currentX = redBall.getX();
        // 화면의 크기
        int screenWidth = getWindowManager().getDefaultDisplay().getWidth();

        // 이동할 거리
        float distance = moveRight ? 45 : -45;
        // 새로운 공의 위치 계산
        float newX = currentX + distance;
        // 화면 경계를 벗어나지 않도록 조정
        newX = Math.max(0, Math.min(newX, screenWidth - redBall.getWidth()));

        // 새로운 위치로 공을 이동
        redBall.setX(newX);
        redBall.setImageResource(R.drawable.rocket1f); // 빠른 경우 이미지 설정

    }

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
        // UUID 생성
        mediaPlayer2.setLooping(true); // 배경음악 반복 재생 설정
        mediaPlayer2.start();

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
                                            }
                                            else if ("zero".equals(text.trim())){
                                                // resetBallPosition();
                                                // Toast.makeText(MainActivity.this, "영점 눌림", Toast.LENGTH_SHORT).show();
                                            }
                                            else if ("run".equals(text.trim())){
                                                changeDirection();
                                                // Toast.makeText(MainActivity.this, "영점 눌림", Toast.LENGTH_SHORT).show();
                                            }
                                            else if ("fast".equals(text.trim())){
                                                fastchangeDirection();
                                                // Toast.makeText(MainActivity.this, "영점 눌림", Toast.LENGTH_SHORT).show();
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
        // Convert RectF to Rect with rounding
        Rect rect = new Rect(
                Math.round(boundingBox.left),
                Math.round(boundingBox.top),
                Math.round(boundingBox.right),
                Math.round(boundingBox.bottom)
        );

        // Create a Drawable representing the bounding box
        ShapeDrawable shapeDrawable = new ShapeDrawable(new RectShape());
        shapeDrawable.getPaint().setColor(Color.TRANSPARENT);
        shapeDrawable.getPaint().setStyle(Paint.Style.STROKE);
        shapeDrawable.getPaint().setStrokeWidth(4f);
        shapeDrawable.setBounds(rect);

        // Remove previous bounding box
        removeBoundingBox();
        // Add the Drawable to the PreviewView's overlay
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
                moveRight = false; // 왼쪽 방향으로 설정
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
        float imageViewWidth = (float) previewView.getWidth();
        float imageViewHeight = (float) previewView.getHeight();

        float faceCenterX = boundingBox.centerX();
        float faceCenterY = boundingBox.centerY();

        // Determine horizontal region (left, right)
        boolean isLeft = faceCenterX < imageViewWidth / 2;
        boolean isRight = faceCenterX >= imageViewWidth / 2;

        // Determine vertical region (top, middle, bottom)
        boolean isTop = faceCenterY < imageViewHeight / 3;
        boolean isMiddleOrBottom = faceCenterY >= imageViewHeight / 3;

        // Check if the bounding box has moved from top to bottom
        boolean movedFromTopToBottom = previousBoundingBox != null &&
                previousBoundingBox.centerY() < imageViewHeight / 3 &&
                faceCenterY >= imageViewHeight / 3;

        // Apply logic based on position
        if (isLeft) {
            if (isTop) {
                moveRight = false; // 왼쪽 방향으로 설정
                redBall.setScaleX(1); // 이미지를 원래 방향으로
            } else {
                moveRight = false; // 왼쪽 방향으로 설정
                redBall.setScaleX(-1); // 이미지를 좌우 반전
                if (movedFromTopToBottom) {
                    startToastMessageRepeater();
                }
            }
        } else if (isRight) {
            if (isTop) {
                moveRight = true; // 오른쪽 방향으로 설정
                redBall.setScaleX(1); // 이미지를 원래 방향으로
            } else {
                moveRight = true; // 오른쪽 방향으로 설정
                redBall.setScaleX(-1); // 이미지를 좌우 반전
                if (movedFromTopToBottom) {
                    startToastMessageRepeater();
                }
            }
        }
        lastCallTime = System.currentTimeMillis();

        // 빨간 공 이미지를 좌우로 반전시킴
        if (moveRight) {
            redBall.setScaleX(1); // 원래 방향으로
        } else {
            redBall.setScaleX(-1); // 좌우 반전
        }

        // Update the previous bounding box
        previousBoundingBox = new RectF(boundingBox);
    }

//    private void determineFacePosition(RectF boundingBox) {
//        float imageViewWidth = (float) previewView.getWidth();
//        float imageViewHeight = (float) previewView.getHeight();
//
//        float faceCenterX = boundingBox.centerX();
//        float faceCenterY = boundingBox.centerY();
//
//        // Determine horizontal region (left, right)
//        boolean isLeft = faceCenterX < imageViewWidth / 2;
//        boolean isRight = faceCenterX >= imageViewWidth / 2;
//
//        // Determine vertical region (top, middle, bottom)
//        boolean isTop = faceCenterY < imageViewHeight / 3;
//        boolean isMiddleOrBottom = faceCenterY >= imageViewHeight / 3;
//
//        // Apply logic based on position
//        if (isLeft) {
//            if (isTop) {
//                moveRight = false; // 왼쪽 방향으로 설정
//                redBall.setScaleX(1); // 이미지를 원래 방향으로
//            } else {
//                moveRight = false; // 왼쪽 방향으로 설정
//                redBall.setScaleX(-1); // 이미지를 좌우 반
//                startToastMessageRepeater();
//            }
//        } else if (isRight) {
//            if (isTop) {
//                moveRight = true; // 오른쪽 방향으로 설정
//                redBall.setScaleX(1); // 이미지를 원래 방향으로
//            } else {
//                moveRight = true; // 오른쪽 방향으로 설정
//                redBall.setScaleX(-1); // 이미지를 좌우 반전
//                startToastMessageRepeater();
//            }
//        }
//
//        lastCallTime = System.currentTimeMillis();
//
//        // 빨간 공 이미지를 좌우로 반전시킴
//        if (moveRight) {
//            redBall.setScaleX(1); // 원래 방향으로
//        } else {
//            redBall.setScaleX(-1); // 좌우 반전
//        }
//    }



    // Remove previous bounding box
    private void removeBoundingBox() {
        // Remove previous bounding box
        previewView.getOverlay().clear();
    }


    public static void showCustomToast(Context context, String message, int durationInMillis) {
        // 이전에 표시된 토스트 메시지가 있다면 취소
        if (customToast != null) {
            customToast.cancel();
        }

        // 새로운 토스트 메시지 표시
        customToast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        customToast.show();

        // 일정 시간이 지난 후에 토스트 메시지 숨기기
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (customToast != null) {
                    customToast.cancel();
                }
            }
        }, durationInMillis);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == MY_CAMERA_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "camera permission granted", Toast.LENGTH_LONG).show();
                // 카메라 권한이 허용되었을 때의 동작 추가
            } else {
                Toast.makeText(this, "camera permission denied", Toast.LENGTH_LONG).show();
                // 카메라 권한이 거부되었을 때의 동작 추가
            }
        }
    }

}