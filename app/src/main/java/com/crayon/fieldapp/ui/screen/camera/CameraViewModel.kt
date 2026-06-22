package com.crayon.fieldapp.ui.screen.camera

import android.media.Image
import android.os.Environment
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.crayon.fieldapp.ui.base.BaseViewModel
import java.io.File
import java.io.FileOutputStream

class CameraViewModel() : BaseViewModel() {
    private val _imageSaved = MutableLiveData<Boolean>()
    val imageSaved: LiveData<Boolean> get() = _imageSaved

    fun saveImage(image: Image) {
        val buffer = image.planes[0].buffer
        val bytes = ByteArray(buffer.remaining())
        buffer.get(bytes)

        val file = File(
            Environment.getExternalStorageDirectory().toString(),
            "captured_image_${System.currentTimeMillis()}.jpg"
        )
        FileOutputStream(file).use {
            it.write(bytes)
        }

        image.close()
        _imageSaved.postValue(true)
    }
}