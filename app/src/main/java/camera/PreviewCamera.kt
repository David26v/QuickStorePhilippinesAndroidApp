package camera

import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.Camera
import android.util.Log
import android.view.TextureView
import java.io.IOException

class PreviewCamera {
    private val TAG = "PreviewCamera"
    private var mCamera: Camera? = null
    private var mirror = false // Mirror image (flip horizontally)
    private var rotate = -1    // Camera rotation relative to device natural orientation
    private var zoom = -1      // Zoom level (some cameras may not support)

    /** Set mirror mode */
    fun setMirror(mirror: Boolean) {
        this.mirror = mirror
    }

    /** Set rotation */
    fun setRotate(rotate: Int) {
        this.rotate = rotate
    }

    /** Set zoom */
    fun setZoom(zoom: Int) {
        this.zoom = zoom
    }

    /** Start camera preview */
    fun startCamera(
        textureView: TextureView,
        cameraIndex: Int,
        width: Int,
        height: Int,
        callback: ICallback
    ) {
        stopCamera()
        try {
            mCamera = Camera.open(cameraIndex)

            // Log min/max exposure
            val parameters = mCamera?.parameters
            val minExposure = parameters?.minExposureCompensation ?: 0
            val maxExposure = parameters?.maxExposureCompensation ?: 0
            Log.d(TAG, "Min exposure: $minExposure")
            Log.d(TAG, "Max exposure: $maxExposure")

            // Check if requested preview size is supported
            checkPreviewSize(width, height)

            // Set camera parameters
            val params = mCamera!!.parameters
            params.setPreviewSize(width, height)
            params.previewFormat = ImageFormat.NV21

            if (params.supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO)) {
                params.focusMode = Camera.Parameters.FOCUS_MODE_AUTO
            }

            if (zoom >= 0 && params.isZoomSupported && zoom <= params.maxZoom) {
                params.zoom = zoom
            }

            mCamera!!.parameters = params

            // Apply rotation
            if (rotate >= 0) {
                mCamera!!.setDisplayOrientation(rotate)
            }

            // Initialize preview
            initPreview(textureView)

            if (mirror) {
                textureView.rotationY = 180f
            }

            // Preview callback
            mCamera!!.setPreviewCallback { data, camera ->
                callback.onData(data, camera)
            }

            callback.onSucc(mCamera!!)
        } catch (e: Exception) {
            e.printStackTrace()
            callback.onError(e)
        }
    }

    /** Stop camera */
    fun stopCamera() {
        try {
            mCamera?.setPreviewCallback(null)
            mCamera?.stopPreview()
            mCamera?.release()
            mCamera = null
            Log.d(TAG, "Camera stopped")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /** Restart preview */
    fun startPreviewAgain() {
        try {
            mCamera?.startPreview()
            Log.d(TAG, "Preview started")
        } catch (e: IOException) {
            e.printStackTrace()
            Log.d(TAG, "Error starting preview: ${e.message}")
        }
    }

    /** Check if preview size is supported */
    private fun checkPreviewSize(width: Int, height: Int) {
        var sizeOk = false
        val sizeList = mCamera!!.parameters.supportedPreviewSizes
        for (size in sizeList) {
            if (size.width == width && size.height == height) {
                sizeOk = true
                break
            }
        }
        if (!sizeOk) {
            throw Exception("Unsupported preview size: [$width,$height]")
        }
    }

    /** Initialize preview with TextureView */
    private fun initPreview(textureView: TextureView) {
        textureView.surfaceTextureListener = object : TextureView.SurfaceTextureListener {
            override fun onSurfaceTextureAvailable(surface: SurfaceTexture, w: Int, h: Int) {
                try {
                    mCamera?.setPreviewTexture(surface)
                    mCamera?.startPreview()
                    Log.d(TAG, "Preview started")
                } catch (e: IOException) {
                    e.printStackTrace()
                    Log.d(TAG, "Error: ${e.message}")
                }
            }

            override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, w: Int, h: Int) {}

            override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
                surface.release()
                stopCamera()
                Log.d(TAG, "Surface destroyed and camera stopped")
                return false
            }

            override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
        }
    }

    /** Set exposure value */
    fun setExposure(exposureValue: Int) {
        val params = mCamera?.parameters
        params?.exposureCompensation = exposureValue
        mCamera?.parameters = params
    }

    interface ICallback {
        fun onSucc(camera: Camera)
        fun onData(data: ByteArray, camera: Camera)
        fun onError(e: Exception)
    }
}
