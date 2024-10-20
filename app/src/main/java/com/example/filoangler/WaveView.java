package com.example.filoangler;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

public class WaveView extends View {
    private Path wavePath;
    private Paint wavePaint;

    private int width, height;
    private float waveLength;
    private float amplitude = 40f;
    private float phase = 0f;
    private float frequency = 50f;
    private float waterLevel = 1;

    public WaveView(Context context) {
        super(context);
        init();
    }

    public WaveView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        wavePath = new Path();
        wavePaint = new Paint();
        wavePaint.setColor(0xA11DA2D8);
        wavePaint.setStyle(Paint.Style.FILL);
        wavePaint.setAntiAlias(true);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        width = w;
        height = h;
        waveLength = width * 3f;  // Set wavelength to 1.5 times the width
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        Path roundedRectPath = new Path();
        roundedRectPath.addRoundRect(0, 0, width, height, 50f, 50f, Path.Direction.CW);  // Adjust radius as needed

        // Clip the canvas to the rounded rectangle
        canvas.clipPath(roundedRectPath);

        wavePath.reset();
        float waterHeight = height * (1 - waterLevel);

        wavePath.moveTo(0, waterHeight);

        for (int i = 0; i <= width; i++) {
            float x = i;
            float y = (float) (waterHeight +
                    amplitude * Math.sin((x + phase) / waveLength * 2 * Math.PI) +
                    amplitude / 2 * Math.sin((x + phase) / (waveLength / 2) * 2 * Math.PI));
            wavePath.lineTo(x, y);
        }

        wavePath.lineTo(width, height);
        wavePath.lineTo(0, height);
        wavePath.close();

        canvas.drawPath(wavePath, wavePaint);

        phase += frequency / 10;
        phase %= waveLength;
        invalidate();
    }

    public void setWaveColor(int color) {
        wavePaint.setColor(color);
    }

    public void setWaveLength(float length){
        this.waveLength = length;
    }

    public void setAmplitude(float amplitude) {
        this.amplitude = amplitude;
    }

    public void setFrequency(float frequency) {
        this.frequency = frequency;
    }

    public void setWaterLevel(float level) {
        this.waterLevel = Math.max(0, Math.min(1, level));
        invalidate();
    }
}