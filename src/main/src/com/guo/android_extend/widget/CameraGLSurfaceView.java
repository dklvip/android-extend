package com.guo.android_extend.widget;

import android.content.Context;
import android.graphics.Color;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.opengl.GLSurfaceView;
import android.util.AttributeSet;
import android.util.Log;

import com.guo.android_extend.GLES2Render;
import com.guo.android_extend.image.ImageConverter;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

/**
 * @Note create by gqjjqg,.
 *    easy to use opengl surface..
 */

public class CameraGLSurfaceView extends ExtGLSurfaceView implements GLSurfaceView.Renderer {
	private final String TAG = this.getClass().getSimpleName();

	private int mWidth, mHeight, mFormat, mRenderFormat;
	private int mDegree;
	private boolean mMirror;
	private boolean mDebugFPS;

	private BlockingQueue<byte[]> mImageRenderBuffers;
	private GLES2Render mGLES2Render;
	private OnRenderListener mOnRenderListener;
	private OnDrawListener mOnDrawListener;

	public interface OnDrawListener{
		public void onDrawOverlap(GLES2Render render);
	}

	public interface OnRenderListener {
		public void onBeforeRender(byte[] data, int width, int height, int format);
		public void onAfterRender(byte[] buffer);
	}

	public CameraGLSurfaceView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
		onCreate();
	}

	public CameraGLSurfaceView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
		onCreate();
	}

	private void onCreate() {
		setEGLContextClientVersion(2);
		setRenderer(this);
		setRenderMode(GLSurfaceView.RENDERMODE_WHEN_DIRTY);
		setZOrderMediaOverlay(true);
		mImageRenderBuffers = new LinkedBlockingQueue<>();
	}

	@Override
	public void onSurfaceCreated(GL10 gl, EGLConfig config) {
		mGLES2Render = new GLES2Render(mMirror, mDegree, mRenderFormat, mDebugFPS);
	}

	@Override
	public void onSurfaceChanged(GL10 gl, int width, int height) {
		mGLES2Render.setViewPort(width, height);
	}

	@Override
	public void onDrawFrame(GL10 gl) {
		byte[] buffer = mImageRenderBuffers.poll();
		if (buffer != null) {
			if (mOnRenderListener != null) {
				mOnRenderListener.onBeforeRender(buffer, mWidth, mHeight, mFormat);
			}
			mGLES2Render.render(buffer, mWidth, mHeight);
			if (mOnRenderListener != null) {
				mOnRenderListener.onAfterRender(buffer);
			}
		}
		if (mOnDrawListener != null) {
			mOnDrawListener.onDrawOverlap(mGLES2Render);
		}
	}

	public void requestRender(byte[] buffer) {
		if (!mImageRenderBuffers.offer(buffer)) {
			Log.e(TAG, "RENDER QUEUE FULL!");
		} else {
			requestRender();
		}
	}

	public void setOnDrawListener(OnDrawListener lis) {
		mOnDrawListener = lis;
	}

	public void setOnRenderListener(OnRenderListener lis) {
		mOnRenderListener = lis;
	}

	public void setImageConfig(int width, int height, int format) {
		mWidth = width;
		mHeight = height;
		mFormat = format;
		switch(format) {
			case ImageFormat.NV21 : mRenderFormat = ImageConverter.CP_PAF_NV21; break;
			case ImageFormat.RGB_565 : mRenderFormat = ImageConverter.CP_RGB565; break;
			default: Log.e(TAG, "Current camera preview format = " + format + ", render is not support!");
		}
	}

	public void setRenderConfig(int degree, boolean mirror) {
		mDegree = degree;
		mMirror = mirror;
	}

	public void debug_print_fps(boolean show) {
		mDebugFPS = show;
	}
}