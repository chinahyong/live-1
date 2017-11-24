package tv.live.bx.live.filter;

import android.content.Context;

import java.nio.FloatBuffer;

/**
 * Created by jerikc on 16/2/23.
 */
public abstract class AbstractFilter {
    protected abstract int createProgram(Context context1);

    protected abstract void getGLSLValues();

    protected abstract void useProgram();

    protected abstract void bindTexture(int textureId);

    protected abstract void bindGLSLValues(float[] mvpMatrix, FloatBuffer vertexBuffer,
                                           int coordsPerVertex, int vertexStride, FloatBuffer texBuffer,
                                           int texStride);

    protected abstract void drawArrays(int firstVertex, int vertexCount);

    protected abstract void unbindGLSLValues();

    protected abstract void unbindTexture();

    protected abstract void disuseProgram();
}
