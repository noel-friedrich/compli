package de.noelfriedrich.compli;

import android.graphics.Bitmap;
import android.graphics.Canvas;

public class CanvasStatistics {

    Canvas canvas;

    public CanvasStatistics(Canvas canvas) {
        this.canvas = canvas;
    }

    static CanvasStatistics makeCanvas(int width, int height) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        return new CanvasStatistics(canvas);
    }

}
