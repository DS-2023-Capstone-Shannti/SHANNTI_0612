package com.gnupr.postureteacher;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.graphics.Color;

import androidx.appcompat.app.AppCompatActivity;

import com.github.mikephil.charting.charts.BarChart;
import com.gnupr.postureteacher.Databases.DaoClass.Measure3DatasDAO;
import com.gnupr.postureteacher.Databases.DaoClass.Measure3RoundsDAO;
import com.gnupr.postureteacher.Databases.DaoClass.MeasureDatasDAO;
import com.gnupr.postureteacher.Databases.DaoClass.MeasureRoundsDAO;
import com.gnupr.postureteacher.Databases.EntityClass.Measure3DatasEntity;
import com.gnupr.postureteacher.Databases.EntityClass.Measure3RoundsEntity;
import com.gnupr.postureteacher.Databases.EntityClass.MeasureDatasEntity;
import com.gnupr.postureteacher.Databases.EntityClass.MeasureRoundsEntity;
import com.gnupr.postureteacher.Databases.MeasureRoomDatabase;
import com.github.mikephil.charting.components.Description;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;

import java.time.Duration;
import java.util.List;

public class CalendarActivity extends AppCompatActivity {
    private ArrayList<String> dayList;

    ProgressBar Prog_time;
    ProgressBar Prog_kcal;
    ProgressBar Prog_score;

    TextView txtTotalCalories;
    TextView txtTotalTime; // 추가: 총 운동 시간을 표시할 텍스트뷰
    TextView txtTotalScore;

    private int totalExerciseTime;
    private int totalCaloriesBurned;
    private int score;

    private String id_str;

    private ArrayList<StatsModel> arrayList;
    private ArrayList<Stats3Model> arrayList2;

    private MeasureRoomDatabase db;

    @Override
    public void onBackPressed() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.putExtra("SCORE", score * 10); // 점수
        mainIntent.putExtra("TOTAL_EXERCISE_TIME", totalExerciseTime); // 총 운동 시간
        mainIntent.putExtra("TOTAL_EXERCISE_kcal", totalCaloriesBurned);
        startActivity(mainIntent);
        finish(); // 현재 Activity를 종료합니다.
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar);

        View homeButtonOverlay = findViewById(R.id.overlay_home_button);
        homeButtonOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent buttonIntent = new Intent(CalendarActivity.this, MainActivity.class);
                buttonIntent.putExtra("SCORE", score * 10); // 점수
                buttonIntent.putExtra("TOTAL_EXERCISE_TIME", totalExerciseTime); // 총 운동 시간
                buttonIntent.putExtra("TOTAL_EXERCISE_kcal", totalCaloriesBurned);
                startActivity(buttonIntent);
                finish();
            }
        });

        dayList = new ArrayList<>(Arrays.asList("6/10", "6/11", "6/12", "6/13", "6/14"));

        Prog_time = findViewById(R.id.Prog_time);
        Prog_kcal = findViewById(R.id.Prog_kcal);
        Prog_score = findViewById(R.id.Prog_score);
        txtTotalTime = findViewById(R.id.exercise_time);
        txtTotalCalories = findViewById(R.id.exercise_kcal);
        txtTotalScore = findViewById(R.id.exercise_score); // 추가: 텍스트뷰 초기화

        BarChart Squatchart = findViewById(R.id.squatchart);
        BarChart Plankchart = findViewById(R.id.plankchart);
        BarChart Lungechart = findViewById(R.id.lungechart);
        BarChart Balancechart = findViewById(R.id.balancechart);

        initBarChart(Squatchart);
        initBarChart(Plankchart);
        initBarChart(Lungechart);
        initBarChart(Balancechart);


        float goalCalories = 400.0f;

        // 데이터베이스에서 운동 시간을 가져옴
        db = MeasureRoomDatabase.getDatabase(this);
        arrayList = new ArrayList<>();
        arrayList2 = new ArrayList<>();


        MeasureDatasDAO measureDatasDAO = db.getMeasureDatasDao();
        MeasureRoundsDAO measureRoundsDAO = db.getMeasureRoundsDao();
        Measure3DatasDAO measure3DatasDAO = db.getMeasure3DatasDao();
        Measure3RoundsDAO measure3RoundsDAO = db.getMeasure3RoundsDao();



        List<MeasureRoundsEntity> mrarray1 = measureRoundsDAO.getAllData();
        List<Measure3RoundsEntity> mrarray2 = measure3RoundsDAO.getAllData();

        MeasureRoundsEntity mrentity1;
        MeasureDatasEntity mdentity1;

        Measure3RoundsEntity mrentity2;
        Measure3DatasEntity mdentity2;


        int plankExerciseTime = 0;
        int balanceExerciseTime = 0;
        float balancepercent = 0f;

        int mralen1 = mrarray1.size();
        for (int i = 0; i < mralen1; i++) {
            StatsModel statsModel = new StatsModel();
            mrentity1 = mrarray1.get(i);
            statsModel.setId(mrentity1.getMeasureRoundID());
            Duration diff1 = Duration.between(mrentity1.getMeasureRoundStartTime(), mrentity1.getMeasureRoundEndTime());
            plankExerciseTime += diff1.getSeconds();
            statsModel.setTime(Integer.toString(plankExerciseTime));
            Log.d("DEBUG", "플랭크 운동 시간: " + plankExerciseTime + "초");
        }


        int mralen2 = mrarray2.size();
        for (int i=0; i < mralen2; i++) {
            Stats3Model stats3Model = new Stats3Model();
            mrentity2 = mrarray2.get(i);
            stats3Model.setId(mrentity2.getMeasure3RoundID());
            Duration diff = Duration.between(mrentity2.getMeasure3RoundStartTime(), mrentity2.getMeasure3RoundEndTime());
            balanceExerciseTime += diff.getSeconds();
            stats3Model.setTime(Integer.toString(balanceExerciseTime));
            Log.d("DEBUG", "밸런스 운동 시간: " + balanceExerciseTime + "초");
        }


        // SquatActivity에서 가져온 countnum

        float squatCaloriesBurned = SquatActivity.countNum * 3; // countnum 당 소모 칼로리를 10으로 설정
        int squatExerciseTime = SquatActivity.countNum * 3; // countnum 당 운동 시간을 3초로 설정

        float lungeCaloriesBurned = LungeActivity.countNum * 3; // countnum 당 소모 칼로리를 10으로 설정
        int lungeExerciseTime = LungeActivity.countNum * 3; // countnum 당 운동 시간을 3초로 설정


        setSquatData(Squatchart, SquatActivity.countNum);
        setLungeData(Lungechart, LungeActivity.countNum);
        setPlankData(Plankchart, PlankActivity.spareTime);
        setBalanceData(Balancechart, BalanceActivity.spareTime);

        // 운동 시간을 초로 변환
        this.totalExerciseTime = plankExerciseTime + balanceExerciseTime + squatExerciseTime +lungeExerciseTime; // 추가: 총 운동 시간 계산

        float plankexerciseTimeInSeconds = (float) plankExerciseTime;
        float balanceexerciseTimeInSeconds = (float) balanceExerciseTime;

        // 초당 소모 칼로리를 15kcal로 설정
        final float plankcaloriePerSecond = 5.0f;
        final float balancecaloriePerSecond = 5.0f;

        // 총 소모 칼로리
        final float plankCaloriesBurned = plankcaloriePerSecond * plankexerciseTimeInSeconds;
        final float balanceCaloriesBurned = balancecaloriePerSecond * balanceexerciseTimeInSeconds;

        final float totalCaloriesBurned = plankCaloriesBurned + balanceCaloriesBurned + squatCaloriesBurned + lungeCaloriesBurned; // 추가: 스쿼트 운동으로 인한 칼로리 소모 추가
        this.totalCaloriesBurned = (int)totalCaloriesBurned;

        // 총 소모 칼로리에 대한 달성률을 프로그래스 바에 반영
        float progress_kcal = (totalCaloriesBurned / goalCalories) * 100;
        Prog_kcal.setProgress((int) progress_kcal);


        // 소모한 총 칼로리를 텍스트에 표시
        txtTotalCalories.setText(String.valueOf((int) totalCaloriesBurned));
        // 총 운동 시간을 텍스트에 표시
        txtTotalTime.setText(String.valueOf(totalExerciseTime));

//////////////////////////////////////////////////////////////////////////
        // 인텐트로부터 점수를 가져옵니다.
        Intent intent = getIntent();

        this.score = intent.getIntExtra("SCORE1", 11);
        txtTotalScore.setText(String.valueOf(score*10));

//////////////////////////////////////////////////////////////////////////////////
        int goalScore = 100;
        float progress_score = ((float) score * 5 / goalScore) * 100;
        Prog_score.setProgress((int) progress_score);

        int goalTime = 60;
        // 운동 시간을 분으로 변환
//        int totalExerciseTimeMinutes = totalExerciseTime / 60;
        float progress_time = (totalExerciseTime / goalTime) * 100;
        Prog_time.setProgress((int) progress_time);
    }





    private void initBarChart(BarChart barChart) {
        // 차트 회색 배경 설정 (default = false)
        barChart.setDrawGridBackground(false);
        // 막대 그림자 설정 (default = false)
        barChart.setDrawBarShadow(false);
        // 차트 테두리 설정 (default = false)
        barChart.setDrawBorders(false);

//        Description description = new Description();
//        // 오른쪽 하단 모서리 설명 레이블 텍스트 표시 (default = false)
//        description.setEnabled(false);
//        barChart.setDescription(description);

        // X, Y 바의 애니메이션 효과
        barChart.animateY(1000);
        barChart.animateX(1000);

        // X, Y 축 간격 설정
        barChart.setExtraBottomOffset(10f); // x축과 차트 아래쪽 간의 여백을 10dp로 설정
        // 바텀 좌표 값
        XAxis xAxis = barChart.getXAxis();
        // x축 위치 설정
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        // 그리드 선 수평 거리 설정
        xAxis.setGranularity(1f);
        // x축 텍스트 컬러 설정
        xAxis.setTextColor(Color.BLACK);
        // x축 선 설정 (default = true)
        xAxis.setDrawAxisLine(false);
        // 격자선 설정 (default = true)
        xAxis.setDrawGridLines(false);
        // 각 막대에 대한 x축 레이블 설정
        final String[] days = {"6/10", "6/11", "6/12", "6/13", "6/14"};
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                if (index >= 0 && index < days.length) {
                    return days[index];
                } else {
                    return "";
                }
            }
        });

        YAxis leftAxis = barChart.getAxisLeft();
        // 좌측 선 설정 (default = true)
        leftAxis.setDrawAxisLine(false);
        // 좌측 텍스트 컬러 설정
        leftAxis.setTextColor(Color.BLACK);
        // y축 최대값 설정
        leftAxis.setAxisMaximum(5);
        // y축 최소값 설정
        leftAxis.setAxisMinimum(0); // 원하는 최소값으로 설정
        // y축 라벨 간격 설정
        leftAxis.setGranularity(1f); // 간격을 1로 설정하여 정수값만 표시되도록 함

        YAxis rightAxis = barChart.getAxisRight();
        // 우측 선 설정 (default = true)
        rightAxis.setEnabled(false);

        // 바차트의 타이틀
        Legend legend = barChart.getLegend();
        // 범례 모양 설정 (default = 정사각형)
        legend.setForm(Legend.LegendForm.LINE);
        // 타이틀 텍스트 사이즈 설정
        legend.setTextSize(15f);
        // 타이틀 텍스트 컬러 설정
        legend.setTextColor(Color.BLACK);
        // 범례 위치 설정
        legend.setVerticalAlignment(Legend.LegendVerticalAlignment.BOTTOM);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        // 범례 방향 설정
        legend.setOrientation(Legend.LegendOrientation.HORIZONTAL);
        // 차트 내부 범례 위치하게 함 (default = false)
        legend.setDrawInside(false);
    }

    // 차트 데이터 설정
    private void setSquatData(BarChart barChart, int squatcount) {
        // Zoom In / Out 가능 여부 설정
        barChart.setScaleEnabled(false);

        ArrayList<BarEntry> valueList = new ArrayList<>();
        String title = "스쿼트 횟수";


        ArrayList<Integer> countList = new ArrayList<>(Arrays.asList(1, 1, 2, 2, squatcount));
        ArrayList<String> dayList = this.dayList;// 임의 데이터
        for (int i = 0; i < 5; i++) {
            int count = countList.get(i);
            String day = dayList.get(i);
            valueList.add(new BarEntry(i,count));
        }

        BarDataSet barDataSet = new BarDataSet(valueList, title);
        // 바 색상 설정 (ColorTemplate.LIBERTY_COLORS)
        barDataSet.setColors(
                Color.rgb(207, 248, 246), Color.rgb(148, 212, 212), Color.rgb(136, 180, 187),
                Color.rgb(118, 174, 175), Color.rgb(42, 109, 130));

        BarData data = new BarData(barDataSet);

        // 막대 그래프의 둥근 윗부분 설정
        data.setBarWidth(0.15f);
        // 막대 그래프의 각 막대 위에 값을 표시하는 것을 비활성화
        barDataSet.setDrawValues(false);
        // 차트 설명 비활성화
        Description description = new Description();
        description.setEnabled(false);
        barChart.setDescription(description);
        // 범례 숨기기
        Legend legend = barChart.getLegend();
        legend.setEnabled(false);

        barChart.setData(data);
        barChart.invalidate();
    }

    private void setLungeData(BarChart barChart, int lungecount) {
        // Zoom In / Out 가능 여부 설정
        barChart.setScaleEnabled(false);

        ArrayList<BarEntry> valueList = new ArrayList<>();
        String title = "런지 횟수";


        ArrayList<Integer> countList = new ArrayList<>(Arrays.asList(1, 1, 2, 2, 2));
        ArrayList<String> dayList = this.dayList;// 임의 데이터
        for (int i = 0; i < 5; i++) {
            int count = countList.get(i);
            String day = dayList.get(i);
            valueList.add(new BarEntry(i,count));
        }

        BarDataSet barDataSet = new BarDataSet(valueList, title);
        // 바 색상 설정 (ColorTemplate.LIBERTY_COLORS)
        barDataSet.setColors(
                Color.rgb(207, 248, 246), Color.rgb(148, 212, 212), Color.rgb(136, 180, 187),
                Color.rgb(118, 174, 175), Color.rgb(42, 109, 130));

        BarData data = new BarData(barDataSet);

        // 막대 그래프의 둥근 윗부분 설정
        data.setBarWidth(0.15f);
        // 막대 그래프의 각 막대 위에 값을 표시하는 것을 비활성화
        barDataSet.setDrawValues(false);
        // 차트 설명 비활성화
        Description description = new Description();
        description.setEnabled(false);
        barChart.setDescription(description);
        // 범례 숨기기
        Legend legend = barChart.getLegend();
        legend.setEnabled(false);

        barChart.setData(data);
        barChart.invalidate();
    }

    private void setPlankData(BarChart barChart, int plankaccuracy) {
        // Zoom In / Out 가능 여부 설정
        barChart.setScaleEnabled(false);

        ArrayList<BarEntry> valueList = new ArrayList<>();
        String title = "밸런스 정확도";


        ArrayList<Integer> countList = new ArrayList<>(Arrays.asList(50, 60, 75, 55, plankaccuracy));
        ArrayList<String> dayList = this.dayList;// 임의 데이터
        for (int i = 0; i < 5; i++) {
            int count = countList.get(i);
            String day = dayList.get(i);
            valueList.add(new BarEntry(i,count));
        }

        BarDataSet barDataSet = new BarDataSet(valueList, title);
        // 바 색상 설정 (ColorTemplate.LIBERTY_COLORS)
        barDataSet.setColors(
                Color.rgb(207, 248, 246), Color.rgb(148, 212, 212), Color.rgb(136, 180, 187),
                Color.rgb(118, 174, 175), Color.rgb(42, 109, 130));

        BarData data = new BarData(barDataSet);

        // 막대 그래프의 둥근 윗부분 설정
        data.setBarWidth(0.15f);
        // 막대 그래프의 각 막대 위에 값을 표시하는 것을 비활성화
        barDataSet.setDrawValues(false);
        // 차트 설명 비활성화
        Description description = new Description();
        description.setEnabled(false);
        barChart.setDescription(description);
        // 범례 숨기기
        Legend legend = barChart.getLegend();
        legend.setEnabled(false);

        barChart.setData(data);
        barChart.invalidate();

        barChart.getAxisLeft().setAxisMaximum(100); // 최대값을 100으로 설정
        barChart.getAxisLeft().setAxisMinimum(50); // 최소값을 0으로 설정
    }

    private void setBalanceData(BarChart barChart, int balanceaccuracy) {
        // Zoom In / Out 가능 여부 설정
        barChart.setScaleEnabled(false);

        ArrayList<BarEntry> valueList = new ArrayList<>();
        String title = "밸런스 정확도";


        ArrayList<Integer> countList = new ArrayList<>(Arrays.asList(90, 85, 77, 62, 80));
        ArrayList<String> dayList = this.dayList;// 임의 데이터
        for (int i = 0; i < 5; i++) {
            int percent = countList.get(i);
            String day = dayList.get(i);
            valueList.add(new BarEntry(i,percent));
        }

        BarDataSet barDataSet = new BarDataSet(valueList, title);
        // 바 색상 설정 (ColorTemplate.LIBERTY_COLORS)
        barDataSet.setColors(
                Color.rgb(207, 248, 246), Color.rgb(148, 212, 212), Color.rgb(136, 180, 187),
                Color.rgb(118, 174, 175), Color.rgb(42, 109, 130));

        BarData data = new BarData(barDataSet);

        // 막대 그래프의 둥근 윗부분 설정
        data.setBarWidth(0.15f);
        // 막대 그래프의 각 막대 위에 값을 표시하는 것을 비활성화
        barDataSet.setDrawValues(false);
        // 차트 설명 비활성화
        Description description = new Description();
        description.setEnabled(false);
        barChart.setDescription(description);
        // 범례 숨기기
        Legend legend = barChart.getLegend();
        legend.setEnabled(false);

        barChart.setData(data);
        barChart.invalidate();

        barChart.getAxisLeft().setAxisMaximum(100); // 최대값을 100으로 설정
        barChart.getAxisLeft().setAxisMinimum(50); // 최소값을 0으로 설정
    }
}