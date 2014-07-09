package com.github.jbarr21.watchfaces.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Paint.Cap;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.Path.FillType;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.view.View;

import com.github.jbarr21.watchfaces.R;
import com.twotoasters.watchface.gears.widget.IWatchface;
import com.twotoasters.watchface.gears.widget.Watch;

import java.util.Calendar;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public class AeonNutWatchface extends View implements IWatchface {

    private Watch mWatch;

    private static final float INACTIVE_BRIGHTNESS = 0.5f; // let N% of light through
    private static float TICK_WIDTH, TICK_LENGTH, TICK_MARGIN;

    private Paint bgPaint, tickPaint, dateTextPaint, logoTextPaint, handPaint, secHandPaint, inactiveOverlayPaint;
    private Rect textBounds;
    private int textWidth, textHeight;

    private boolean mInflated;
    private boolean mActive;

    public AeonNutWatchface(Context context) {
        super(context);
        init(context, null, 0);
    }

    public AeonNutWatchface(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public AeonNutWatchface(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    @DebugLog
    private void init(Context context, AttributeSet attrs, int defStyle) {
        mWatch = new Watch(this);

        textBounds = new Rect();

        Resources res = getResources();
        TICK_WIDTH = res.getDimension(R.dimen.tick_width);
        TICK_LENGTH = res.getDimension(R.dimen.tick_length);
        TICK_MARGIN = res.getDimension(R.dimen.tick_margin);

        setLayerType(LAYER_TYPE_SOFTWARE, null);

        tickPaint = new Paint();
        tickPaint.setAntiAlias(true);
        tickPaint.setStrokeCap(Cap.SQUARE);

        logoTextPaint = new Paint();
        logoTextPaint.setTypeface(loadTypeface(R.string.font_steiner_light));
        logoTextPaint.setTextSize(res.getDimension(R.dimen.logo_text_size));
        logoTextPaint.setAntiAlias(true);
        logoTextPaint.setTextAlign(Align.CENTER);

        dateTextPaint = new Paint(logoTextPaint);
        dateTextPaint.setTextSize(res.getDimension(R.dimen.date_text_size));
        dateTextPaint.setTypeface(loadTypeface(R.string.font_roboto_condensed_regular));
        dateTextPaint.setStyle(Style.STROKE);

        handPaint = new Paint();
        handPaint.setAntiAlias(true);
        handPaint.setStyle(Style.FILL);
        handPaint.setStrokeWidth(3);
        //handPaint.setShadowLayer(4.5f, 0, 7.5f, Color.BLACK);

        secHandPaint = new Paint();
        secHandPaint.setAntiAlias(true);
        secHandPaint.setStyle(Style.FILL);
        //secHandPaint.setShadowLayer(4.5f, 0, 7.5f, Color.BLACK);

        inactiveOverlayPaint = new Paint();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        mInflated = true;
    }

    @DebugLog
    @Override
    public void onAttachedToWindow() {
        super.onAttachedToWindow();
        mWatch.onAttachedToWindow();
    }

    @DebugLog
    @Override
    public void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mWatch.onDetachedFromWindow();
    }

    @Override
    public void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);
        int w = getWidth(), h = getHeight(), r = w / 2, cx = r, cy = r;

        drawTicks(canvas, w, h, cx, cy);
        drawLogoText(canvas, w, h, cx, cy);
        drawDateBox(canvas, w, h, cx, cy);
        drawHands(canvas, w, h, cx, cy);

        if (!mActive) {
            canvas.drawRect(0, 0, getWidth(), getHeight(), inactiveOverlayPaint);
        }
    }

    private void drawTicks(Canvas canvas, int w, int h, int cx, int cy) {
        for (int i = 0; i < 12; i++) {
            int tickMargin = (int) ((i % 3 == 0 ? 1 : -3) * TICK_MARGIN);
            tickPaint.setStrokeWidth(i % 3 == 0 ? TICK_WIDTH * 2 : TICK_WIDTH);
            canvas.drawLine(cx, tickMargin, cx, tickMargin + TICK_LENGTH, tickPaint);
            canvas.rotate(30, cx, cy);
        }
    }

    private void drawLogoText(Canvas canvas, int w, int h, int cx, int cy) {
        int logoTop = (int) (TICK_MARGIN + TICK_LENGTH + getResources().getDimension(R.dimen.logo_margin_top));

        String logoText = "android";
        logoTextPaint.getTextBounds(logoText, 0, logoText.length(), textBounds);
        textWidth = (int) logoTextPaint.measureText(logoText);
        textHeight = textBounds.height();

        canvas.drawText(logoText, textWidth / 2 + (w - textWidth) / 2, logoTop + textHeight / 2, logoTextPaint);
    }

    private void drawDateBox(Canvas canvas, int w, int h, int cx, int cy) {
        int boxSize = (int) getResources().getDimension(R.dimen.date_box_size);
        int boxBottom = (int) (h - (TICK_MARGIN + TICK_LENGTH) - getResources().getDimension(R.dimen.date_margin_bottom));
        int cyBox = boxBottom - boxSize / 2;

        String dateText = String.valueOf(mWatch.getTime().get(Calendar.DAY_OF_MONTH));
        dateTextPaint.getTextBounds(dateText, 0, dateText.length(), textBounds);
        textWidth = (int) dateTextPaint.measureText(dateText);
        textHeight = textBounds.height();

        canvas.drawRect(cx - boxSize / 2, boxBottom - boxSize, cx + boxSize / 2, boxBottom, dateTextPaint);
        canvas.drawText(dateText, textWidth / 2 + (w - textWidth) / 2, cyBox + textHeight / 2, dateTextPaint);
    }

    private void drawHands(Canvas canvas, int w, int h, int cx, int cy) {
        int hr = mWatch.getTime().get(Calendar.HOUR_OF_DAY) % 12;
        int min = mWatch.getTime().get(Calendar.MINUTE);
        int sec = mWatch.getTime().get(Calendar.SECOND);

        // draw the center knob
        canvas.drawCircle(cx, cy, (int) (TICK_WIDTH * 2.5), handPaint);

        if (mActive) {
            // draw the second hand
            canvas.save();
            canvas.rotate(6 * sec, cx, cy);
            canvas.drawRect(cx - 2, 0, cx + 2, cy, secHandPaint);
            canvas.restore();
        }

        // draw the minute hand
        canvas.save();
        canvas.rotate(6 * min, cx, cy);
        canvas.drawPath(getHandPath(cx, cy, true), handPaint);
        canvas.restore();

        // draw the hr hand
        canvas.save();
        canvas.rotate(30 * hr + 0.5f * min, cx, cy);
        canvas.drawPath(getHandPath(cx, cy, false), handPaint);
        canvas.restore();
    }

    private Path getHandPath(int cx, int cy, boolean isMinuteHand) {
        Point cl = new Point((int) (cx - TICK_WIDTH * 2), cy);
        Point cr = new Point((int) (cx + TICK_WIDTH * 2), cy);
        Point tl = new Point(cx - 1, (int) (isMinuteHand ? 0 : (1 - 0.6) * cy));
        Point tr = new Point(cx + 1, (int) (isMinuteHand ? 0 : (1 - 0.6) * cy));

        Path path = new Path();
        path.setFillType(FillType.EVEN_ODD);
        path.moveTo(tl.x, tl.y);
        path.lineTo(tr.x, tr.y);
        path.lineTo(cr.x, cr.y);
        path.lineTo(cl.x, cl.y);
        path.close();
        return path;
    }

    @Override
    public void onTimeChanged(Calendar time) {
        Timber.v("onTimeChanged()");
        invalidate();
    }

    @Override
    public boolean handleSecondsInDimMode() {
        return false;
    }

    @DebugLog
    @Override
    public void onActiveStateChanged(boolean active) {
        this.mActive = active;
        setColors();
    }

    @DebugLog
    private void setColors() {
        tickPaint.setColor(Color.WHITE);
        dateTextPaint.setColor(Color.WHITE);
        logoTextPaint.setColor(Color.WHITE);
        handPaint.setColor(Color.WHITE);
        secHandPaint.setColor(Color.LTGRAY);
        inactiveOverlayPaint.setColor(Color.argb((int) ((1 - INACTIVE_BRIGHTNESS) * 255), 0, 0, 0));
    }

    private Typeface loadTypeface(int typefaceNameResId) {
        String typefaceName = getResources().getString(typefaceNameResId);
        return Typeface.createFromAsset(getContext().getAssets(), typefaceName);
    }
}
