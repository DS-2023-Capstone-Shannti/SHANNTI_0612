package com.gnupr.postureteacher;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class Login extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseRef;
    private EditText et_id, et_pass;
    private Button btn_login, btn_register;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("shantiPose");

        et_id = findViewById(R.id.Edt_input_id);
        et_pass = findViewById(R.id.Edt_input_pw);
        btn_login = findViewById(R.id.Btn_login);
        btn_register = findViewById(R.id.Btn_go_signup);

        // 회원가입 버튼을 클릭 시 수행
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Login.this, Signup.class);
                startActivity(intent);
            }
        });

        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // EditText에 현재 입력되어있는 값을 get(가져온다)해온다.
                String userID = et_id.getText().toString();
                String userPass = et_pass.getText().toString();

                if(userID.isEmpty() && userPass.isEmpty())
                {
                    Toast.makeText(Login.this,"아이디와 비밀번호를 모두 입력해주세요",Toast.LENGTH_SHORT).show();

                } else if(userID.isEmpty()){

                    Toast.makeText(Login.this,"아이디를 입력해주세요",Toast.LENGTH_SHORT).show();

                } else if(userPass.isEmpty()){

                    Toast.makeText(Login.this,"비밀번호를 입력해주세요",Toast.LENGTH_SHORT).show();
                }else {

                    mFirebaseAuth.signInWithEmailAndPassword(userID, userPass).addOnCompleteListener(Login.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {

                            if(task.isSuccessful()){
                                //로그인 성공
                                Intent intent = new Intent(Login.this, MainActivity.class);
                                startActivity(intent);
                                finish(); // 현재 액티비티 파괴
                            }
                            else{
                                Toast.makeText(Login.this,"로그인 실패",Toast.LENGTH_SHORT).show();

                            }
                        }
                    });

                }

            }
        });


    }
}