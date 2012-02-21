package com.test;


import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

public class ClickableToast extends FrameLayout
{
    protected WindowManager windowManager;
    
    public ClickableToast( Context paramContext )
    {
        super( paramContext );
        init();
    }
    
    public static void show( Context paramContext, View paramView, int paramInt )
    {
        ClickableToast localClickableToast = new ClickableToast( paramContext );
        localClickableToast.addView( paramView );
        localClickableToast.show( paramInt );
    }
    
    public WindowManager.LayoutParams getWmParams()
    {
        WindowManager.LayoutParams localLayoutParams = new WindowManager.LayoutParams();
        localLayoutParams.height = -1;
        localLayoutParams.width = -1;
        localLayoutParams.alpha = 1.0f;
        localLayoutParams.format = 1;
        localLayoutParams.gravity = Gravity.CENTER;
        //localLayoutParams.verticalMargin = 1028443341;
        localLayoutParams.flags = 296;//WindowManager.LayoutParams.
        return localLayoutParams;
    }
    
    public void hide()
    {
        try{windowManager.removeView(this);}
        catch (RuntimeException localRuntimeException2){}
    }
    
    public void init()
    {
        setBackgroundColor( 0 );
        WindowManager localWindowManager = (WindowManager) getContext().getSystemService( "window" );
        this.windowManager = localWindowManager;
    }
    
    public void onAttachedToWindow()
    {
        WindowManager.LayoutParams param = getWmParams();
        try{windowManager.updateViewLayout( this, param );}
        catch( RuntimeException e ){}
    }
    
//    public void onDraw( Canvas paramCanvas )
//    {
//        paramCanvas.rotate( 1127481344 );
//        super.onDraw( paramCanvas );
//        paramCanvas.rotate( 1127481344 );
//    }
    
    public void show()
    {
        WindowManager.LayoutParams localLayoutParams = getWmParams();
        localLayoutParams.type = 2005;
        windowManager.addView( this, localLayoutParams );
    }
    
    public void show( int delay )
    {
        Runnable r = new Runnable()
        {
            @Override
            public void run()
            {
                show();
            }
        };
        postDelayed(r, delay);
    }
}
