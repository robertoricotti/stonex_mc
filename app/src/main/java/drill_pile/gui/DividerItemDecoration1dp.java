package drill_pile.gui;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

public class DividerItemDecoration1dp extends RecyclerView.ItemDecoration {

    private final Paint paint;
    private final int heightPx;

    public DividerItemDecoration1dp(Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        heightPx = (int) (1 * density); // 1dp

        paint = new Paint();
        paint.setColor(0xFFB0B0B0); // grigio medio (cambia se vuoi)
        paint.setStyle(Paint.Style.FILL);
    }

    @Override
    public void onDraw(Canvas c, RecyclerView parent, RecyclerView.State state) {
        int left = parent.getPaddingLeft();
        int right = parent.getWidth() - parent.getPaddingRight();

        int childCount = parent.getChildCount();
        for (int i = 0; i < childCount - 1; i++) { // no divider dopo ultima riga
            View child = parent.getChildAt(i);

            RecyclerView.LayoutParams params =
                    (RecyclerView.LayoutParams) child.getLayoutParams();

            int top = child.getBottom() + params.bottomMargin;
            int bottom = top + heightPx;

            c.drawRect(left, top, right, bottom, paint);
        }
    }

    @Override
    public void getItemOffsets(Rect outRect, View view,
                               RecyclerView parent, RecyclerView.State state) {
        // spazio per la linea
        outRect.bottom = heightPx;
    }
}
