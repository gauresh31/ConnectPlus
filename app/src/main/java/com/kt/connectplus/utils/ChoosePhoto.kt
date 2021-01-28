package com.kt.connectplus.utils

import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.ResolveInfo
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import java.io.*


class ChoosePhoto {
    companion object {
        var CHOOSE_PHOTO_INTENT = 101
        var SELECTED_IMG_CROP = 102
        var SELECT_PICTURE_CAMERA = 103
        lateinit var cameraUrl:Uri
    }

    var currentAndroidDeviceVersion = Build.VERSION.SDK_INT

    private val ASPECT_X = 1
    private val ASPECT_Y = 1
    private val OUT_PUT_X = 300
    private val OUT_PUT_Y = 300
    private val SCALE = true

    lateinit var cropPictureUrl: Uri
    lateinit var  selectedImageUri:Uri
    lateinit var  mContext: Context

    public fun initialize(context: Context) {
        mContext = context
    }

    fun init() {
        val permissionUtil = PermissionUtil()
        if (permissionUtil.checkMarshMellowPermission()) {
            if (permissionUtil.verifyPermissions(
                    mContext,
                    permissionUtil.cameraPermissions
                ) && permissionUtil.verifyPermissions(
                    mContext,
                    permissionUtil.galleryPermissions
                )
            ) //showAlertDialog()
                else {
                ActivityCompat.requestPermissions(
                    (mContext as Activity?)!!,
                    permissionUtil.cameraPermissions,
                    SELECT_PICTURE_CAMERA
                )
            }
        } else {
//            showAlertDialog()
        }
    }

    fun showAlertDialog() {
        val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryIntent.type = "image/*"
        cameraUrl = FileUtil.getInstance(mContext).createImageUri()
        //Create any other intents you want
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        cameraIntent.addFlags(Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, cameraUrl)


        //Add them to an intent array
        val intents = arrayOf(cameraIntent)

        //Create a choose from your first intent then pass in the intent array
        val chooserIntent =
            Intent.createChooser(galleryIntent, "Capturing...")
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, intents)
        (mContext as Activity?)!!.startActivityForResult(chooserIntent, CHOOSE_PHOTO_INTENT)
    }

    fun handleGalleryResult(data: Intent) {
        try {
            cropPictureUrl = Uri.fromFile(
                FileUtil.getInstance(mContext)
                    .createImageTempFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES))
            )
            val realPathFromURI: String = FileUtil.getRealPathFromURI(mContext, data.data)
            val file = File(realPathFromURI)
            if (file.exists()) {
                if (currentAndroidDeviceVersion > 23) {
                    cropImage(
                        FileProvider.getUriForFile(
                            mContext,
                            mContext.applicationContext.packageName
                                .toString() + ".provider",
                            file
                        ), cropPictureUrl
                    )
                } else {
                    cropImage(Uri.fromFile(file), cropPictureUrl)
                }
            } else {
                data.data?.let { cropImage(it, cropPictureUrl) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun getImageUrlWithAuthority(context: Context?, uri: Uri?): String? {
        var `is`: InputStream? = null
        if (uri != null) {
            if (uri.authority != null) {
                try {
                    if (context != null) {
                        `is` = context.contentResolver.openInputStream(uri)
                    }
                    val bmp = BitmapFactory.decodeStream(`is`)
                    return writeToTempImageAndGetPathUri(context, bmp).toString()
                } catch (e: FileNotFoundException) {
                    e.printStackTrace()
                } finally {
                    try {
                        `is`?.close()
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                }
            }
        }
        return null
    }

    fun writeToTempImageAndGetPathUri(inContext: Context?, inImage: Bitmap): Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(
            inContext?.contentResolver,
            inImage,
            "Title",
            null
        )
        return Uri.parse(path)
    }


    fun handleCameraResult(cameraPictureUrl: Uri) {
        try {
            cropPictureUrl = Uri.fromFile(
                FileUtil.getInstance(mContext)
                    .createImageTempFile(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES))
            )
            cropImage(cameraPictureUrl, cropPictureUrl)
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun getCameraUri(): Uri? {
        return cameraUrl
    }

    fun getCropImageUrl(): Uri? {
        return selectedImageUri
    }

    private fun cropImage(sourceImage: Uri, destinationImage: Uri) {
        val intent = Intent("com.android.camera.action.CROP")
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        intent.type = "image/*"
        val list: List<ResolveInfo> = mContext.packageManager.queryIntentActivities(intent, 0)
        val size = list.size
        if (size == 0) {
            //Utils.showToast(mContext, mContext.getString(R.string.error_cant_select_cropping_app));
            selectedImageUri = sourceImage
            intent.putExtra(MediaStore.EXTRA_OUTPUT, sourceImage)
            (mContext as Activity?)!!.startActivityForResult(intent, SELECTED_IMG_CROP)
            return
        } else {
            intent.setDataAndType(sourceImage, "image/*")
            intent.putExtra("aspectX", ASPECT_X)
            intent.putExtra("aspectY", ASPECT_Y)
            intent.putExtra("outputY", OUT_PUT_Y)
            intent.putExtra("outputX", OUT_PUT_X)
            intent.putExtra("scale", SCALE)

            //intent.putExtra("return-data", true);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, destinationImage)
            selectedImageUri = destinationImage
            if (size == 1) {
                val i = Intent(intent)
                val res = list[0]
                i.component = ComponentName(res.activityInfo.packageName, res.activityInfo.name)
                (mContext as Activity?)!!.startActivityForResult(intent, SELECTED_IMG_CROP)
            } else {
                val i = Intent(intent)
                i.putExtra(Intent.EXTRA_INITIAL_INTENTS, list.toTypedArray())
                (mContext as Activity?)!!.startActivityForResult(intent, SELECTED_IMG_CROP)
            }
        }
    }
}