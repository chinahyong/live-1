package com.bixin.bixin.live.filter;

import android.content.Context;
import android.opengl.GLES20;
import android.util.Log;

import java.nio.FloatBuffer;

import tv.live.bx.R;
import com.bixin.bixin.live.gles.GlUtil;

/**
 * Created by Live on 16/3/3.
 */
public class CameraFilterBeauty extends CameraFilter {
	private int singleStepOffset;
	private int mParamsLocation;
	private static final float ARRAY_VALUE = 2;

	private static final float offset_array[] = {ARRAY_VALUE, ARRAY_VALUE,};
	private float params_array[] = {0.33f, 0.63f, 0.4f, 0.35f};

	public CameraFilterBeauty(Context context, int mFilterLevel) {
		super(context);
		setBeautyLevel(mFilterLevel);
		Log.e("FBO", "CameraFilterBeauty");
		// offset_array[0] = offset_array[0] / 90;
		// offset_array[1] = offset_array[1] / 160;
	}

	public CameraFilterBeauty(Context context, float percentLevel) {
		super(context);
		setBeautyLevelPercent(percentLevel);
		Log.e("FBO", "CameraFilterBeauty");
		// offset_array[0] = offset_array[0] / 90;
		// offset_array[1] = offset_array[1] / 160;
	}

	@Override
	protected int createProgram(Context applicationContext) {
		return GlUtil.createProgram(applicationContext, R.raw.vertex_shader, R.raw.beautify_fragment);
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
		Log.e("CameraFilterBeauty", "setTextureSize width:" + width + " height:" + height);
		offset_array[0] = ARRAY_VALUE / 540;
		offset_array[1] = ARRAY_VALUE / 960;
	}

	@Override
	protected void getGLSLValues() {
		super.getGLSLValues();

		singleStepOffset = GLES20.glGetUniformLocation(mProgramHandle, "singleStepOffset");
		mParamsLocation = GLES20.glGetUniformLocation(mProgramHandle, "params");
		// GLES20.glUniform4fv(mParamsLocation, 1, FloatBuffer.wrap(new
		// float[]{0.33f, 0.63f, 0.4f, 0.35f}));
	}

	@Override
	protected void bindGLSLValues(float[] mvpMatrix, FloatBuffer vertexBuffer, int coordsPerVertex, int vertexStride,
			FloatBuffer texBuffer, int texStride) {
		super.bindGLSLValues(mvpMatrix, vertexBuffer, coordsPerVertex, vertexStride, texBuffer, texStride);
		// GLES20.glUniform2fv(singleStepOffset, 1, offset_array, 0);
		GLES20.glUniform2fv(singleStepOffset, 1, FloatBuffer.wrap(offset_array));
	}

	public void setBeautyLevelPercent(float percent){
		if(percent > 1.0f)
			percent = 1.0f;
		if (percent < 0.0f)
			percent = 0.0f;
		float array1 = 1.0f - percent * 0.67f;
		float array2 = 1.0f - percent * 0.27f;
		float array3 = 0.15f + percent * 0.25f;
		float array4 = 0.15f + percent * 0.2f;
		params_array = new float[]{array1, array2, array3, array4};
		setFloatVec4(mParamsLocation, params_array);

	}

	public void setBeautyLevel(int level) {
		switch (level) {
		case 1:
			params_array = new float[]{1.0f, 1.0f, 0.15f, 0.15f};
			setFloatVec4(mParamsLocation, params_array);
			break;
		case 2:
			params_array = new float[]{0.8f, 0.9f, 0.2f, 0.2f};
			setFloatVec4(mParamsLocation, params_array);
			break;
		case 3:
			params_array = new float[]{0.6f, 0.8f, 0.25f, 0.25f};
			setFloatVec4(mParamsLocation, params_array);
			break;
		case 4:
			params_array = new float[]{0.4f, 0.7f, 0.38f, 0.3f};
			setFloatVec4(mParamsLocation, params_array);
			break;
		case 5:
			params_array = new float[]{0.33f, 0.63f, 0.4f, 0.35f};
			setFloatVec4(mParamsLocation, params_array);
			break;
		default:
			break;
		}
	}

}
