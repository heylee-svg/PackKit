package com.dhg.packkit.utils

import android.annotation.TargetApi
import android.graphics.Bitmap
import android.text.TextUtils
import java.io.*
import android.graphics.BitmapFactory
import android.util.Log


/**
 *
 * @author yanzhendong
 * @since 2019/1/18 下午5:26
 */
@Suppress("unused")
object FileUtils {

    fun saveBitmap(bitmap: Bitmap?, path: String): Boolean {
        var success = false
        if (TextUtils.isEmpty(path)) {
            return success
        }
        var out: FileOutputStream? = null
        try {
            if(File(path).parentFile.mkdirs()){
                out = FileOutputStream(path)
                bitmap?.compress(Bitmap.CompressFormat.PNG, 100, out) // bmp is your Bitmap instance
                // PNG is a lossless format, the compression factor (100) is ignored
                success = true
            }

        } finally {
            out?.close()
        }
        return success
    }

    @TargetApi(26)
    fun image2byte(path: String): ByteArray? {
        var data: ByteArray? = null
        try {

            var input = FileInputStream(File(path)) // content://media/external/images/media/318223

            val output = ByteArrayOutputStream()
            val buf = ByteArray(1024)
            var len = input.read(buf)
            while (len != -1) {
                output.write(buf, 0, len)
                len = input.read(buf)
            }
            data = output.toByteArray()
            output.close()
            input.close()


        } catch (ex1: FileNotFoundException) {
            ex1.printStackTrace()
        } catch (ex1: IOException) {
            ex1.printStackTrace()
        }

        val encoder = java.util.Base64.getEncoder() //转为Base64位的数组传输
        return encoder.encode(data)
    }

    @TargetApi(26)
    fun getImageArr(path: String): ByteArray {
        var inputStream: InputStream? = null
        var data: ByteArray? = null
        try {
            inputStream = FileInputStream(path)
            Log.i("FileUtils", "getImageStr available " + inputStream.available())
            if (inputStream.available() > 2000000) {   //超过2M的图片再做压缩
                var bitmap = BitmapFactory.decodeFile(path)
                var baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos)
                bitmap.recycle()
                data = baos.toByteArray()
            } else {
                inputStream = FileInputStream(path)
                data = ByteArray(inputStream.available())
                inputStream.read(data)
                inputStream.close()
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }

        // 加密
        val encoder = java.util.Base64.getEncoder() //转为Base64位的数组传输
        return encoder.encode(data)
    }

    @TargetApi(26)
    fun getImageStr(path: String): String {
        var inputStream: InputStream? = null
        var data: ByteArray? = null
        try {
            inputStream = FileInputStream(path)
            Log.i("FileUtils", "getImageStr available " + inputStream.available())
            if (inputStream.available() > 2000000) {   //超过2M的图片再做压缩
                var bitmap = BitmapFactory.decodeFile(path)
                var baos = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 60, baos)
                bitmap.recycle()
                data = baos.toByteArray()
            } else {
                inputStream = FileInputStream(path)
                data = ByteArray(inputStream.available())
                inputStream.read(data)
                inputStream.close()
            }

        } catch (e: IOException) {
            e.printStackTrace()
        }

        // 加密
        val encoder = java.util.Base64.getEncoder() //转为Base64位的字符串传输
        return encoder.encodeToString(data)
    }
}