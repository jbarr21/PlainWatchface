package com.github.jbarr21.watchfaces.widget;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.View;

import com.github.jbarr21.watchfaces.R;
import com.github.jbarr21.watchfaces.util.DeviceUtils;
import com.twotoasters.watchface.gears.widget.IWatchface;
import com.twotoasters.watchface.gears.widget.Watch;

import java.util.Calendar;

import hugo.weaving.DebugLog;
import timber.log.Timber;

public class NexusWatchface extends View implements IWatchface {

    private Watch mWatch;

    private Path arcPath, minHandPath, hrHandPath;
    private Paint arcPaint, handPaint;

    int[] ringColors;

    private boolean mInflated;
    private boolean mActive;

    public NexusWatchface(Context context) {
        super(context);
        init(context, null, 0);
    }

    public NexusWatchface(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs, 0);
    }

    public NexusWatchface(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context, attrs, defStyle);
    }

    @DebugLog
    private void init(Context context, AttributeSet attrs, int defStyle) {
        Resources res = getResources();
        setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        mWatch = new Watch(this);

        arcPath = createArcPath();
        minHandPath = createHandPath(res.getDimension(R.dimen.ring_thickness) * 4);
        hrHandPath = createHandPath(res.getDimension(R.dimen.ring_thickness) * 6);

        handPaint = new Paint();
        handPaint.setAntiAlias(true);
        handPaint.setColor(res.getColor(android.R.color.white));
        handPaint.setShadowLayer(res.getDimension(R.dimen.hand_shadow_radius), 0, 0, res.getColor(R.color.nexus_hand_shadow_color));
        handPaint.setStyle(Style.FILL);

        arcPaint = new Paint();
        arcPaint.setAntiAlias(true);
        arcPaint.setStyle(Style.STROKE);
        arcPaint.setStrokeWidth(res.getDimension(R.dimen.ring_thickness));

        ringColors = new int[] {
                res.getColor(R.color.nexus_blue), res.getColor(R.color.nexus_red),
                res.getColor(R.color.nexus_green), res.getColor(R.color.nexus_yellow)};
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
        drawArcs(canvas, cx, cy);
        drawHands(canvas, cx, cy);
    }

    private void drawArcs(Canvas canvas, int cx, int cy) {
        float ringThickness = getResources().getDimension(R.dimen.ring_thickness);
        for (int i = 0; i < 4; i++) {
            arcPaint.setColor(ringColors[i]);
            canvas.save();
            canvas.clipRect(0, 0, cx - ringThickness / 2f, cy - ringThickness / 2f);
            canvas.drawPath(arcPath, arcPaint);
            canvas.restore();
            canvas.rotate(90, cx, cy);
        }
    }

    private void drawHands(Canvas canvas, int cx, int cy) {
        int hr = mWatch.getTime().get(Calendar.HOUR_OF_DAY) % 12;
        int min = mWatch.getTime().get(Calendar.MINUTE);
        int sec = mWatch.getTime().get(Calendar.SECOND);

        // hour hand
        canvas.save();
        canvas.rotate(30 * hr + 0.5f * min, cx, cy);
        canvas.drawPath(hrHandPath, handPaint);
        canvas.restore();

        // minute hand
        canvas.save();
        canvas.rotate(6 * min, cx, cy);
        canvas.drawPath(minHandPath, handPaint);
        canvas.restore();
    }

    private Path createArcPath() {
        Resources res = getResources();
        Pair<Float, Float> screenDimens = DeviceUtils.getScreenDimensPx(getContext());
        float w = screenDimens.first, h = screenDimens.second, r = w / 2.0f, cx = r, cy = r;
        float ringOuterMargin = res.getDimension(R.dimen.ring_outer_margin);
        float ringThickness = res.getDimension(R.dimen.ring_thickness);

        RectF arcBounds = new RectF(
                ringOuterMargin + ringThickness,
                ringOuterMargin + ringThickness,
                w - ringThickness - ringOuterMargin,
                h - ringThickness - ringOuterMargin);

        Path path = new Path();
        path.addArc(arcBounds, 180, 90);
        return path;
    }

    private Path createHandPath(float handEdgeMargin) {
        Resources res = getResources();
        Pair<Float, Float> screenDimens = DeviceUtils.getScreenDimensPx(getContext());
        float w = screenDimens.first, h = screenDimens.second, r = w / 2.0f, cx = r, cy = r;
        float rCenter = res.getDimension(R.dimen.hand_radius_center), rTop = res.getDimension(R.dimen.hand_radius_top);
        float cyTop = handEdgeMargin - rTop;

        Path path = new Path();
        path.moveTo(cx + rCenter, cy);
        // start right of center, curve down and left
        path.cubicTo(cx + rCenter, cy + rCenter, cx, cy + rCenter, cx, cy + rCenter);
        // curve left and up
        path.cubicTo(cx - rCenter, cy + rCenter, cx - rCenter, cy, cx - rCenter, cy);
        // straight line to left of center of top
        path.lineTo(cx - rTop, cyTop);
        // curve up and to the right
        path.cubicTo(cx - rTop, cyTop - rTop, cx, cyTop - rTop, cx, cyTop - rTop);
        // curve right and down
        path.cubicTo(cx + rTop, cyTop - rTop, cx + rTop, cyTop, cx + rTop, cyTop);
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
        // no op
    }
}
