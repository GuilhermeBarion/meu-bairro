package br.com.urbantech.meubairro.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;

import androidx.exifinterface.media.ExifInterface;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

public final class ImageUtils {
    private static final int LADO_MAX = 1280;
    private static final int QUALIDADE = 80;

    private ImageUtils() { }

    /** Redimensiona (lado máx. 1280px), corrige a rotação EXIF e comprime em JPEG. Rodar fora da thread principal. */
    public static byte[] comprimir(Context ctx, Uri uri) throws IOException {
        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream in = ctx.getContentResolver().openInputStream(uri)) {
            BitmapFactory.decodeStream(in, null, bounds);
        }
        int amostra = 1;
        while (Math.max(bounds.outWidth, bounds.outHeight) / amostra > LADO_MAX * 2) amostra *= 2;

        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inSampleSize = amostra;
        Bitmap bmp;
        try (InputStream in = ctx.getContentResolver().openInputStream(uri)) {
            bmp = BitmapFactory.decodeStream(in, null, opts);
        }
        if (bmp == null) throw new IOException("Imagem inválida");

        Matrix m = new Matrix();
        int rot = rotacao(ctx, uri);
        if (rot != 0) m.postRotate(rot);
        float escala = Math.min(1f, (float) LADO_MAX / Math.max(bmp.getWidth(), bmp.getHeight()));
        m.postScale(escala, escala);
        Bitmap fim = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), m, true);

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        fim.compress(Bitmap.CompressFormat.JPEG, QUALIDADE, out);
        return out.toByteArray();
    }

    private static int rotacao(Context ctx, Uri uri) {
        try (InputStream in = ctx.getContentResolver().openInputStream(uri)) {
            if (in == null) return 0;
            int o = new ExifInterface(in).getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
            switch (o) {
                case ExifInterface.ORIENTATION_ROTATE_90: return 90;
                case ExifInterface.ORIENTATION_ROTATE_180: return 180;
                case ExifInterface.ORIENTATION_ROTATE_270: return 270;
                default: return 0;
            }
        } catch (IOException e) {
            return 0;
        }
    }
}
