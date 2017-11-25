package tv.live.bx.live.filter;

import android.content.Context;
import android.opengl.GLES11Ext;
import android.opengl.GLES20;
import android.util.Log;

import java.nio.FloatBuffer;
import java.util.LinkedList;

import tv.live.bx.R;
import tv.live.bx.live.gles.GlUtil;

/**
 * Created by jerikc on 16/2/23.
 */
public class CameraFilter extends AbstractFilter implements IFilter {
    public static final String NO_FILTER_VERTEX_SHADER = "" +
            "attribute vec4 aPosition;\n" +
            "attribute vec4 aTextureCoord;\n" +
            " \n" +
            "uniform mat4 uMVPMatrix;\n" +
            "varying vec2 vTextureCoord;\n" +
            " \n" +
            "void main()\n" +
            "{\n" +
            "    gl_Position = uMVPMatrix * aPosition;\n" +
            "    vTextureCoord = aTextureCoord.xy;\n" +
            "}";

    protected int mProgramHandle;
    private int maPositionLoc;
    private int muMVPMatrixLoc;
    private int maTextureCoordLoc;
    private int mTextureLoc;

    protected int mIncomingWidth, mIncomingHeight;

    private final LinkedList<Runnable> mRunOnDraw;

    public CameraFilter(Context applicationContext) {
        mRunOnDraw = new LinkedList<Runnable>();
        mProgramHandle = createProgram(applicationContext);
        if (mProgramHandle == 0) {
            throw new RuntimeException("Unable to create program");
        }
        getGLSLValues();
    }

    @Override
    public int getTextureTarget() {
        return GLES11Ext.GL_TEXTURE_EXTERNAL_OES;
//        return GLES20.GL_TEXTURE_2D;
    }

    @Override
    public void setTextureSize(int width, int height) {
        if (width == 0 || height == 0) {
            return;
        }
        if (width == mIncomingWidth && height == mIncomingHeight) {
            return;
        }
        mIncomingWidth = width;
        mIncomingHeight = height;
    }

    @Override
    protected int createProgram(Context applicationContext) {
        return GlUtil.createProgram(applicationContext, R.raw.vertex_shader,
                R.raw.fragment_shader_mosaic);
    }

    @Override
    protected void getGLSLValues() {
        mTextureLoc = GLES20.glGetUniformLocation(mProgramHandle, "uTexture");
        maPositionLoc = GLES20.glGetAttribLocation(mProgramHandle, "aPosition");
        muMVPMatrixLoc = GLES20.glGetUniformLocation(mProgramHandle, "uMVPMatrix");
        maTextureCoordLoc = GLES20.glGetAttribLocation(mProgramHandle, "aTextureCoord");

    }

    @Override
    public void onDraw(float[] mvpMatrix, FloatBuffer vertexBuffer, int firstVertex,
                       int vertexCount, int coordsPerVertex, int vertexStride,
                       FloatBuffer texBuffer, int textureId, int texStride) {

        GlUtil.checkGlError("draw start");

        useProgram();

        bindTexture(textureId);

        bindGLSLValues(mvpMatrix, vertexBuffer, coordsPerVertex, vertexStride, texBuffer,
                texStride);

        drawArrays(firstVertex, vertexCount);

        unbindGLSLValues();

        unbindTexture();

        disuseProgram();
    }

    @Override
    protected void useProgram() {
        GLES20.glUseProgram(mProgramHandle);
        //GlUtil.checkGlError("glUseProgram");
    }

    @Override
    protected void bindTexture(int textureId) {
        GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
        GLES20.glBindTexture(getTextureTarget(), textureId);
        GLES20.glUniform1i(mTextureLoc, 0);
    }

    @Override
    protected void bindGLSLValues(float[] mvpMatrix, FloatBuffer vertexBuffer, int coordsPerVertex,
                                  int vertexStride, FloatBuffer texBuffer, int texStride) {

        GLES20.glUniformMatrix4fv(muMVPMatrixLoc, 1, false, mvpMatrix, 0);
        GLES20.glEnableVertexAttribArray(maPositionLoc);
        GLES20.glVertexAttribPointer(maPositionLoc, coordsPerVertex, GLES20.GL_FLOAT, false,
                vertexStride, vertexBuffer);
        GLES20.glEnableVertexAttribArray(maTextureCoordLoc);
        GLES20.glVertexAttribPointer(maTextureCoordLoc, 2, GLES20.GL_FLOAT, false, texStride,
                texBuffer);
        runPendingOnDrawTasks();
    }

    protected void runPendingOnDrawTasks() {
        while (!mRunOnDraw.isEmpty()) {
            mRunOnDraw.removeFirst().run();
            Log.e("CameraFilter", "runPendingOnDrawTasks ");
        }
    }

    @Override
    protected void drawArrays(int firstVertex, int vertexCount) {
        GLES20.glClearColor(0f, 0f, 0f, 1f);
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, firstVertex, vertexCount);
    }

    @Override
    protected void unbindGLSLValues() {
        GLES20.glDisableVertexAttribArray(maPositionLoc);
        GLES20.glDisableVertexAttribArray(maTextureCoordLoc);
    }

    @Override
    protected void unbindTexture() {
        GLES20.glBindTexture(getTextureTarget(), 0);
    }

    @Override
    protected void disuseProgram() {
        GLES20.glUseProgram(0);
    }

    @Override
    public void releaseProgram() {
        GLES20.glDeleteProgram(mProgramHandle);
        mProgramHandle = -1;
    }

    protected void runOnDraw(final Runnable runnable) {
        synchronized (mRunOnDraw) {
            mRunOnDraw.addLast(runnable);
        }
    }

    protected void setFloatVec4(final int location, final float[] arrayValue) {
        runOnDraw(new Runnable() {
            @Override
            public void run() {
                GLES20.glUniform4fv(location, 1, FloatBuffer.wrap(arrayValue));
            }
        });
    }
}
