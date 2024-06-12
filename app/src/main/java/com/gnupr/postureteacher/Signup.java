package com.gnupr.postureteacher;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Signup extends AppCompatActivity {

    private FirebaseAuth mFirebaseAuth;
    private DatabaseReference mDatabaseRef;
    private EditText mEtEmail, mEtPw, mEtName, mEtNumber;
    private RadioGroup genderRadioGroup;
    private Button mBtnRegister;

    private String getTime() { // 현재 시간 가져오기
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        String getTime = dateFormat.format(date);

        return getTime;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        mFirebaseAuth = FirebaseAuth.getInstance();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Shannti");

        mEtEmail = findViewById(R.id.Edt_email);
        mEtPw = findViewById(R.id.Edt_password);
        mEtName = findViewById(R.id.Edt_studentID);
        mBtnRegister = findViewById(R.id.Btn_signup);
        mEtNumber = findViewById(R.id.Edt_phone);

        mBtnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String strEmail = mEtEmail.getText().toString();
                String strPw = mEtPw.getText().toString();
                String strName = mEtName.getText().toString();
                String strPhone = mEtNumber.getText().toString();

                if (strEmail.isEmpty() || strPw.isEmpty() ||strName.isEmpty()) {
                    Toast.makeText(Signup.this, "모든 항목을 입력하세요", Toast.LENGTH_SHORT).show();
                    return; // Stop further execution
                }

                mFirebaseAuth.createUserWithEmailAndPassword(strEmail, strPw).addOnCompleteListener(Signup.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task)
                    {
                        if(task.isSuccessful())
                        {
                            FirebaseUser firebaseUser = mFirebaseAuth.getCurrentUser();
                            UserAccount accout = new UserAccount();
                            accout.setUserIdToken(firebaseUser.getUid());
                            accout.setUserId(firebaseUser.getEmail());
                            accout.setUserPw(strPw);
                            accout.setUserName(strName);
                            accout.setUserPhone(strPhone);
                            mDatabaseRef.child("UserAccount").child(firebaseUser.getUid()).setValue(accout);

                            Toast.makeText(Signup.this, "회원가입 성공, 얼굴 등록 하러 가기", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Signup.this,  RegisterActivity.class);

                            // 데이터를 Intent에 추가
                            intent.putExtra("USER_ID", strEmail);
                            intent.putExtra("USER_PW", strPw);
                            intent.putExtra("USER_NAME", strName);
                            intent.putExtra("USER_PHONE", strPhone);

                            startActivity(intent);
                            finish();

                        }else{
                            Toast.makeText(Signup.this, "회원가입 실패", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
            }
        });
    }
    private String getSelectedGender() {
        int selectedId = genderRadioGroup.getCheckedRadioButtonId();

        if (selectedId != -1) {
            RadioButton selectedRadioButton = findViewById(selectedId);
            return selectedRadioButton.getText().toString();
        } else {
            // Handle the case where no gender is selected
            return "";
        }
    }
}