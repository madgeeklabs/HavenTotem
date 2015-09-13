package com.mgl.totem.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;

public class ImageHelper {

    public static Bitmap cropImageCircular(Bitmap original, Context mContext) {
        Bitmap mask = Bitmap.createBitmap(original.getWidth(), original.getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(mask);
        Paint paint = new Paint();

        // canvas.drawColor(Color.BLACK);
        paint.setColor(Color.WHITE);
        paint.setAntiAlias(true);
        canvas.drawCircle(original.getWidth() / 2, original.getHeight() / 2, original.getWidth() / 2, paint);

        Bitmap result = Bitmap.createBitmap(mask.getWidth(), mask.getHeight(), Config.ARGB_8888);
        Canvas mCanvas = new Canvas(result);
        Paint paintT = new Paint(Paint.ANTI_ALIAS_FLAG);
        paintT.setAntiAlias(true);

        mCanvas.drawBitmap(original, 0, 0, null);
        paintT.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
        mCanvas.drawBitmap(mask, 0, 0, paintT);
        paintT.setXfermode(null);
        return result;
    }
}
