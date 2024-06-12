package com.gnupr.postureteacher;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.List;

public class SkeletonView extends View {
    private List<PointF> poseLandmarks;
    private Paint paint;

    public SkeletonView(Context context) {
        super(context);
        init();
    }

    public SkeletonView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public SkeletonView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint();
        paint.setColor(Color.RED);
        paint.setStrokeWidth(8.0f);
    }

    public void updateLandmarks(List<PointF> poseLandmarks) {
        this.poseLandmarks = poseLandmarks;
        invalidate(); // View를 다시 그리도록 요청
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (poseLandmarks != null && poseLandmarks.size() > 0) {
            // 첫 번째 랜드마크부터 시작하여 다음 랜드마크와 선을 그립니다.
            PointF prevLandmark = poseLandmarks.get(0);
            for (int i = 1; i < poseLandmarks.size(); i++) {
                PointF currLandmark = poseLandmarks.get(i);
                canvas.drawLine(prevLandmark.x, prevLandmark.y, currLandmark.x, currLandmark.y, paint);
                // 현재 랜드마크를 이전 랜드마크로 설정하여 다음 랜드마크와 연결
                prevLandmark = currLandmark;
            }
        }
    }
}
