package de.noelfriedrich.compli;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;

import java.util.ArrayList;

public class StatisticsView extends View {

    Paint textPaint;
    Paint fillPaint;
    Paint gridPaint;
    Paint labelPaint;

    private String title = null;
    private float[] dataPoints = new float[0];
    public int lineWidth = 5;

    private String[] labels = new String[0];

    public Integer selectedIndex = -1;

    public static float pxFromDp(Context context, float dp) {
        return dp * context.getResources().getDisplayMetrics().density;
    }

    public StatisticsView(Context context) {
        super(context);

        textPaint = new Paint(Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(pxFromDp(context, 24));
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setTypeface(ResourcesCompat.getFont(context, R.font.lora_bold));

        labelPaint = new Paint(Paint.LINEAR_TEXT_FLAG | Paint.ANTI_ALIAS_FLAG);
        labelPaint.setColor(Color.WHITE);
        labelPaint.setTextSize(pxFromDp(context, 18));
        labelPaint.setTextAlign(Paint.Align.CENTER);
        labelPaint.setTypeface(ResourcesCompat.getFont(context, R.font.lora_medium));

        fillPaint = new Paint();
        fillPaint.setStyle(Paint.Style.FILL);
        fillPaint.setColor(Color.WHITE);

        gridPaint = new Paint();
        gridPaint.setStrokeWidth(3);
        gridPaint.setStyle(Paint.Style.FILL);
        gridPaint.setColor(ContextCompat.getColor(context, R.color.primary));
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setData(float[] dataPoints) {
        this.dataPoints = dataPoints;
    }

    public void setData(int[] dataPoints) {
        this.dataPoints = new float[dataPoints.length];
        for (int i = 0; i < dataPoints.length; i++) {
            this.dataPoints[i] = (float) dataPoints[i];
        }
    }

    public void setData(Integer[] dataPoints) {
        this.dataPoints = new float[dataPoints.length];
        for (int i = 0; i < dataPoints.length; i++) {
            this.dataPoints[i] = (float) dataPoints[i];
        }
    }

    public float getMaxDataPoint() {
        float max = -Float.MAX_VALUE;
        for (int i = 0; i < dataPoints.length; i++) {
            if (dataPoints[i] > max) {
                max = dataPoints[i];
            }
        }
        if (max < 3)
            max = 3f;
        return max;
    }

    public Integer getRealSelectedIndex() {
        if (selectedIndex == null)
            return null;

        Integer realIndex = selectedIndex;
        if (realIndex < 0) {
            realIndex = dataPoints.length + realIndex;
        }

        if (realIndex >= dataPoints.length || realIndex < 0) {
            return null;
        } else {
            return realIndex;
        }
    }

    public void drawStatistic(Canvas canvas, int xStart, int yStart, int width, int height) {
        float maxVal = getMaxDataPoint();
        float xStep = width / (dataPoints.length - 1);
        float yStep = height / maxVal;

        // draw grid
        if (yStep > 20) {
            int maxYStepVal = (int) Math.floor(maxVal);
            for (int y = 0; y <= maxYStepVal; y++) {
                float startY = y * yStep + yStart;
                canvas.drawLine(
                        xStart,
                        startY,
                        width + xStart,
                        startY,
                        gridPaint
                );
            }
        }

        // draw Data points
        fillPaint.setStrokeWidth(lineWidth);
        Integer realSelectedIndex = getRealSelectedIndex();
        for (int i = 0; i < dataPoints.length; i++) {
            float x = xStep * i + xStart;
            float y = height - yStep * dataPoints[i] + yStart;
            if (i + 1 < dataPoints.length) {
                canvas.drawLine(
                        x, y,
                        xStep * (i + 1) + xStart,
                        height - yStep * dataPoints[i + 1] + yStart,
                        fillPaint
                );
            }

            if (realSelectedIndex.equals(i)) {
                canvas.drawCircle(x, y, 20f, fillPaint);
            }
        }
    }

    public void setLabels(String[] labels) {
        this.labels = labels;
    }

    public String getLabel(int index) {
        if (index < 0 || index > labels.length) {
            return null;
        }

        return labels[index];
    }

    public void drawLabels(Canvas canvas, int xStart, int y, int width) {
        float xStep = width / (dataPoints.length - 1);
        for (int i = 0; i < dataPoints.length; i++) {
            String label = getLabel(i);
            if (label == null)
                break;

            float x = xStep * i + xStart;
            if (i == 0) {
                labelPaint.setTextAlign(Paint.Align.LEFT);
            } else if (i == dataPoints.length - 1) {
                labelPaint.setTextAlign(Paint.Align.RIGHT);
            } else {
                labelPaint.setTextAlign(Paint.Align.CENTER);
            }
            canvas.drawText(label, x, y, labelPaint);
        }
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        int padding = 20;
        int xStart = padding;
        int yStart = padding;
        int width = canvas.getWidth() - padding * 2;
        int height = canvas.getHeight() - padding * 2;

        if (title != null) {
            yStart += (int) textPaint.getTextSize();
            canvas.drawText(title, width / 2f + xStart, yStart, textPaint);
            yStart += (int) textPaint.getTextSize();
            height -= yStart;
        }

        if (labels.length > 0) {
            drawLabels(canvas, xStart, yStart + height, width);
            height -= labelPaint.getTextSize() + 20;
        }

        if (dataPoints.length > 1) {
            drawStatistic(canvas, xStart, yStart, width, height);
        }
    }

}