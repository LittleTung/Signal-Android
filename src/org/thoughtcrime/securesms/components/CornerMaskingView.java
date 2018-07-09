package org.thoughtcrime.securesms.components;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class CornerMaskingView extends FrameLayout {

  private final float[] radii   = new float[8];
  private final Paint   paint   = new Paint();
  private final Path    corners = new Path();
  private final RectF   bounds  = new RectF();

  public CornerMaskingView(@NonNull Context context) {
    super(context);
    init();
  }

  public CornerMaskingView(@NonNull Context context, @Nullable AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public CornerMaskingView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    setLayerType(LAYER_TYPE_HARDWARE, null);

    paint.setColor(Color.BLACK);
    paint.setStyle(Paint.Style.FILL);
    paint.setAntiAlias(true);
    paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
  }

  @Override
  protected void dispatchDraw(Canvas canvas) {
    super.dispatchDraw(canvas);

    bounds.left   = 0;
    bounds.top    = 0;
    bounds.right  = canvas.getWidth();
    bounds.bottom = canvas.getHeight();

    corners.reset();
    corners.addRoundRect(bounds, radii, Path.Direction.CW);

    canvas.drawPath(corners, paint);
  }

  public void setRadius(int radius) {
    setRadii(radius, radius, radius, radius);
  }

  public void setRadii(@NonNull CornerSpec spec) {
    setRadii(spec.getTopLeft(getContext()), spec.getTopRight(getContext()), spec.getBottomRight(getContext()), spec.getBottomLeft(getContext()));
  }

  public void setRadii(int topLeft, int topRight, int bottomRight, int bottomLeft) {
    radii[0] = radii[1] = topLeft;
    radii[2] = radii[3] = topRight;
    radii[4] = radii[5] = bottomRight;
    radii[6] = radii[7] = bottomLeft;
  }

  public interface CornerSpec {
    int getTopLeft(@NonNull Context context);
    int getTopRight(@NonNull Context context);
    int getBottomRight(@NonNull Context context);
    int getBottomLeft(@NonNull Context context);
  }
}
