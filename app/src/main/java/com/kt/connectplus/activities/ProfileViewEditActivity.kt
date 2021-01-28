package com.kt.connectplus.activities

import android.annotation.TargetApi
import android.app.Activity
import android.app.DatePickerDialog
import android.app.DatePickerDialog.OnDateSetListener
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.util.Log
import android.widget.RadioButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.adroitandroid.chipcloud.ChipListener
import com.hbb20.CountryCodePicker
import com.kt.connectplus.R
import com.kt.connectplus.utils.ChoosePhoto
import com.kt.connectplus.utils.ChoosePhoto.Companion.CHOOSE_PHOTO_INTENT
import com.kt.connectplus.utils.ChoosePhoto.Companion.SELECTED_IMG_CROP
import com.kt.connectplus.utils.ChoosePhoto.Companion.cameraUrl
import com.kt.connectplus.utils.PreferenceUtils
import kotlinx.android.synthetic.main.activity_profile_view_edit.*
import org.json.JSONObject
import java.io.File
import java.text.SimpleDateFormat
import java.util.*


class ProfileViewEditActivity : AppCompatActivity(), CountryCodePicker.OnCountryChangeListener {

    private lateinit var strItrStatus: String
    private var maleRadioButton: RadioButton? = null
    private val jsonProfile: JSONObject = JSONObject()
    private lateinit var date: OnDateSetListener
    private lateinit var myCalendar: Calendar
    private val capturePhoto = 1
    lateinit var mUri: Uri
    private lateinit var choosePhoto: ChoosePhoto
    private lateinit var context: Context
    private var countryCode: String? = null
    private var countryName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        setContentView(R.layout.activity_profile_view_edit)

        initialiseValues()
        setDefaults()
        setListeners()
    }

    private fun setDefaults() {
        var jsonExistProfile: JSONObject
        val intentVal: Intent = intent
        jsonExistProfile = if (null != intentVal.getStringExtra(getString(R.string.fb_data))) {
            JSONObject(intentVal.getStringExtra(getString(R.string.fb_data)))
        } else {
            val profileData =
                PreferenceUtils.getStringPreference(context, getString(R.string.profile_data))
            if (!PreferenceUtils.isEmpty(profileData)) {
                JSONObject(profileData)
            } else {
                JSONObject()
            }
        }
        edtFullName.setText(jsonExistProfile.optString(getString(R.string.user_full_name)))
        edtMobile.setText(jsonExistProfile.optString(getString(R.string.user_mobile)))
        edtDob.setText(jsonExistProfile.optString(getString(R.string.user_dob)))
        edt_country.setText(jsonExistProfile.optString(getString(R.string.user_country)))

        val gender: String = jsonExistProfile.optString(getString(R.string.user_gender))
        if (getString(R.string.str_male) == gender) {
            maleRadioButton?.isSelected ?: true
        } else {
            femaleRadioButton.isSelected = true
        }
    }

    private fun initialiseValues() {
        context = this@ProfileViewEditActivity
        choosePhoto = ChoosePhoto()
        choosePhoto.initialize(context)

        myCalendar = Calendar.getInstance()
        maleRadioButton = findViewById(R.id.maleRadioButton)
        date =
            OnDateSetListener { _, year, monthOfYear, dayOfMonth ->
                myCalendar[Calendar.YEAR] = year
                myCalendar[Calendar.MONTH] = monthOfYear
                myCalendar[Calendar.DAY_OF_MONTH] = dayOfMonth
                updateDob()
            }
        country_code_picker!!.setOnCountryChangeListener(this)
        country_code_picker!!.setDefaultCountryUsingNameCode("IN")
    }

    private fun setListeners() {
        chip_cloud.setChipListener(object : ChipListener {
            override fun chipSelected(index: Int) {
            }

            override fun chipDeselected(index: Int) {
            }
        })

        btn_camera.setOnClickListener { capturePhoto() }
        btn_gallery.setOnClickListener { openGallery() }


        edtDob.setOnClickListener {
            DatePickerDialog(
                this@ProfileViewEditActivity, date, myCalendar
                    .get(Calendar.YEAR), myCalendar.get(Calendar.MONTH),
                myCalendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }

        btn_save.setOnClickListener {
            saveProfile()
        }

        btn_logout.setOnClickListener {
            logoutApp()
        }
    }

    private fun logoutApp() {
        PreferenceUtils.removeAll(this)
        val myIntent = Intent(this, LoginActivity::class.java)
        startActivity(myIntent)
        finish()
    }

    private fun saveProfile() {
        jsonProfile.put(getString(R.string.user_full_name), edtFullName.text.toString().trim())
        jsonProfile.put(getString(R.string.user_dob), edtDob.text.toString().trim())
        jsonProfile.put(getString(R.string.user_country), edt_country.text.toString().trim())
        jsonProfile.put(getString(R.string.user_mobile), edtMobile.text.toString().trim())

        if (isSelected()) {
            if (strItrStatus.equals(getString(R.string.str_male), ignoreCase = true)) {
                jsonProfile.put(getString(R.string.user_gender), getString(R.string.str_male))
            } else {
                jsonProfile.put(getString(R.string.user_gender), getString(R.string.str_female))
            }
        } else {
            Toast.makeText(this, "Please Select Gender", Toast.LENGTH_SHORT).show()
        }
        PreferenceUtils.setStringPreference(context, "profileData", jsonProfile.toString())
        Toast.makeText(this, "Data Saved Successfully", Toast.LENGTH_LONG).show()

    }

    private fun updateDob() {
        val myFormat = "dd/MM/yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.US)
        edtDob.setText(sdf.format(myCalendar.time))
    }

    private fun isSelected(): Boolean {
        val selectedId: Int = segmented_gender.checkedRadioButtonId
        if (selectedId != -1) {
            maleRadioButton = segmented_gender.findViewById(selectedId)
            strItrStatus = maleRadioButton.toString()
            return true
        }
        return false
    }

    private fun openGallery() {
        val intent = Intent("android.intent.action.GET_CONTENT")
        intent.type = "image/*"
        startActivityForResult(intent, CHOOSE_PHOTO_INTENT)
    }

    private fun capturePhoto() {
        choosePhoto.init()
        val capturedImage = File(externalCacheDir, "My_Captured_Photo.jpg")
        if (capturedImage.exists()) {
            capturedImage.delete()
        }
        capturedImage.createNewFile()
        mUri = if (Build.VERSION.SDK_INT >= 24) {
            FileProvider.getUriForFile(
                this, "com.kt.connectplus.fileprovider",
                capturedImage
            )
        } else {
            Uri.fromFile(capturedImage)
        }
        cameraUrl = mUri
        val intent = Intent("android.media.action.IMAGE_CAPTURE")
        intent.putExtra(MediaStore.EXTRA_OUTPUT, mUri)
        startActivityForResult(intent, CHOOSE_PHOTO_INTENT)
    }

    private fun renderImage(imagePath: String?) {
        if (imagePath != null) {
            val bitmap = BitmapFactory.decodeFile(imagePath)
            imgProfile?.setImageBitmap(bitmap)
        } else {
            Toast.makeText(
                this,
                "No Path Found.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun getImagePath(uri: Uri, selection: String?): String {
        var path: String? = null
        val cursor = contentResolver.query(uri, null, selection, null, null)
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA))
            }
            cursor.close()
        }
        return path!!
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantedResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantedResults)
        when (requestCode) {
            1 ->
                if (grantedResults.isNotEmpty() && grantedResults.get(0) ==
                    PackageManager.PERMISSION_GRANTED
                ) {
                    openGallery()
                } else {
                    Toast.makeText(
                        this,
                        "Unfortunately You are Denied Permission to Perform this Operation.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == CHOOSE_PHOTO_INTENT) {
                if (data?.data != null) {
                    choosePhoto.handleGalleryResult(data)
                } else {
                    choosePhoto.getCameraUri()?.let { choosePhoto.handleCameraResult(it) }
                }
            } else if (requestCode == SELECTED_IMG_CROP) {
                Log.i("inside img profile ", "$choosePhoto?.getCropImageUrl()")
                imgProfile.setImageURI(choosePhoto.getCropImageUrl())
            }
        }
    }

    @TargetApi(19)
    private fun handleImageOnKitkat(data: Intent?) {
        var imagePath: String? = null
        val uri = data!!.data
        //DocumentsContract defines the contract between a documents provider and the platform.
        if (DocumentsContract.isDocumentUri(this, uri)) {
            val docId = DocumentsContract.getDocumentId(uri)
            if (uri != null) {
                if ("com.android.providers.media.documents" == uri.authority) {
                    val id = docId.split(":")[1]
                    val selection = MediaStore.Images.Media._ID + "=" + id
                    imagePath = getImagePath(
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                        selection
                    )
                } else if (uri != null) {
                    if ("com.android.providers.downloads.documents" == uri.authority) {
                        val contentUri = ContentUris.withAppendedId(
                            Uri.parse(
                                "content://downloads/public_downloads"
                            ), java.lang.Long.valueOf(docId)
                        )
                        imagePath = getImagePath(contentUri, null)
                    }
                }
            }
        } else if (uri != null) {
            if ("content".equals(uri.scheme, ignoreCase = true)) {
                imagePath = getImagePath(uri, null)
            } else if ("file".equals(uri.scheme, ignoreCase = true)) {
                imagePath = uri.path
            }
        }
        renderImage(imagePath)
    }

    fun crop(uri: Uri?) {
        this.grantUriPermission(
            "com.android.camera", uri,
            Intent.FLAG_GRANT_WRITE_URI_PERMISSION or Intent.FLAG_GRANT_READ_URI_PERMISSION
        )
        val intent = Intent("com.android.camera.action.CROP")
        intent.setDataAndType(uri, "image/*")
        //Android N need set permission to uri otherwise system camera don't has permission to access file wait crop
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
        intent.putExtra("crop", "true")
        //The proportion of the crop box is 1:1
        intent.putExtra("aspectX", 1)
        intent.putExtra("aspectY", 1)
        //Crop the output image size
        intent.putExtra("outputX", 800)
        intent.putExtra("outputY", 800)
        //image type
        intent.putExtra("outputFormat", "JPEG")
        intent.putExtra("noFaceDetection", true)
        //true - don't return uri |  false - return uri
        intent.putExtra("return-data", true)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, uri)
        startActivityForResult(intent, capturePhoto)
    }

    override fun onCountrySelected() {
        countryCode = country_code_picker!!.selectedCountryCode
        countryName = country_code_picker!!.selectedCountryName
    }
}