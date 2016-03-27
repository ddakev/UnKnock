package kb.hackgsu.daniel.knockboard;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;

/**
 * Created by Daniel on 3/27/2016.
 */
public class CustomDrawableView extends View implements Drawable.Callback {
    private ShapeDrawable mDrawable;
    private int x,y,radius;
    public CustomDrawableView(Context context, int color, int radius, boolean fill) {
        super(context);
        Rect windowRect = new Rect();
        this.getWindowVisibleDisplayFrame(windowRect);
        int hWindow = windowRect.bottom - windowRect.top;
        int wWindow = windowRect.right - windowRect.left;
        this.x=wWindow/2;
        this.y=hWindow/2;
        this.radius=radius;
        mDrawable = new ShapeDrawable(new OvalShape());
        mDrawable.getPaint().setColor(color);
        if(fill) mDrawable.getPaint().setStyle(Paint.Style.FILL_AND_STROKE);
        else mDrawable.getPaint().setStyle(Paint.Style.STROKE);
        mDrawable.getPaint().setStrokeWidth(15);
        mDrawable.setBounds(this.x-this.radius, this.y-this.radius, this.x + this.radius, this.y + this.radius);
    }

    public void setRadius(int newRad)
    {
        radius=newRad;
        mDrawable.setBounds(x-radius, y-radius, x+radius, y+radius);
    }

    public void setColor(int newCol, boolean fill)
    {
        if(fill == false) mDrawable.getPaint().setStyle(Paint.Style.STROKE);
        else mDrawable.getPaint().setStyle(Paint.Style.FILL_AND_STROKE);
        mDrawable.getPaint().setColor(newCol);
        mDrawable.setBounds(x-radius, y-radius, x+radius, y+radius);
    }

    public void setXc(int newX)
    {
        x=newX;
        mDrawable.setBounds(x-radius, y-radius, x+radius, y+radius);
    }
    public int getXc() {return x;}

    public void setYc(int newY)
    {
        y=newY;
        mDrawable.setBounds(x-radius, y-radius, x+radius, y+radius);
    }
    public int getYc() {return y;}

    protected void onDraw(Canvas canvas) {
        mDrawable.draw(canvas);
    }
}
