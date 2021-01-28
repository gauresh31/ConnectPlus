package com.kt.connectplus.activities

import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.Animation
import android.view.animation.TranslateAnimation
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.facebook.*
import com.facebook.AccessToken
import com.facebook.login.BuildConfig
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.auth.FirebaseAuth
import com.kt.connectplus.R
import com.kt.connectplus.utils.PreferenceUtils
import kotlinx.android.synthetic.main.activity_login.*
import org.json.JSONObject


class LoginActivity : AppCompatActivity() {

    lateinit var anim: Animation
    lateinit var callbackManager: CallbackManager

    var id = ""
    var firstName = ""
    var middleName = ""
    var lastName = ""
    var name = ""
    var picture = ""
    var email = ""
    var accessToken = ""

    lateinit var mGoogleSignInClient: GoogleSignInClient
    val Req_Code: Int = 123
    private val animDuration = 700


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
        setContentView(R.layout.activity_login)


        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()

        mGoogleSignInClient = GoogleSignIn.getClient(this@LoginActivity, gso)
        val firebaseAuth = FirebaseAuth.getInstance()

        callbackManager = CallbackManager.Factory.create()

        if (isLoggedIn()) {
            Log.d("LoggedIn? :", "YES")
            // Show the Activity with the logged in user
        } else {
            Log.d("LoggedIn? :", "NO")
            // Show the Home Activity
        }

        setDefaults()

//       Login Button made by Facebook
//
//
//        val loginButton = findViewById<LoginButton>(R.id.login_button)
//        loginButton.setReadPermissions(listOf("public_profile", "email"))
//        // If you are using in a fragment, call loginButton.setFragment(this)
//
//        // Callback registration
//        // If you are using in a fragment, call loginButton.setFragment(this)
//
//        // Callback registration
//        loginButton.registerCallback(callbackManager, object : FacebookCallback<LoginResult?> {
//            override fun onSuccess(loginResult: LoginResult?) {
//                // Get User's Info
//            }
//
//            override fun onCancel() {
//                // App code
//            }
//
//            override fun onError(exception: FacebookException) {
//                // App code
//            }
//        })

        createBottomUpAnimation(
            facebook_login_btn,
            null,
            facebook_login_btn.getHeight().toFloat()
        )?.start();
//        slideUp(conn_title, facebook_login_btn)
//        facebook_login_btn.startAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_up));

//      Custom Login Button

//        anim = AnimationUtils.loadAnimation(this, R.anim.slide_up)
//        facebook_login_btn.startAnimation(anim)

        facebook_login_btn.setOnClickListener {
            LoginManager.getInstance()
                .logInWithReadPermissions(this, listOf("public_profile", "email"))
        }

        // Callback registration
        LoginManager.getInstance()
            .registerCallback(callbackManager, object : FacebookCallback<LoginResult?> {
                override fun onSuccess(loginResult: LoginResult?) {
                    Log.d("TAG", "Success Login")
                    getUserProfile(loginResult?.accessToken, loginResult?.accessToken?.userId)
                }

                override fun onCancel() {
                    Toast.makeText(this@LoginActivity, "Login Cancelled", Toast.LENGTH_LONG).show()
                }

                override fun onError(exception: FacebookException) {
                    Toast.makeText(this@LoginActivity, exception.message, Toast.LENGTH_LONG).show()
                }
            })
    }

    private fun setDefaults() {
        val jsonExistingData = PreferenceUtils.getStringPreference(this,getString(R.string.profile_data))
        if(!PreferenceUtils.isEmpty(jsonExistingData)){
            val myIntent = Intent(this, ProfileViewEditActivity::class.java)
            startActivity(myIntent)
        }
    }

    @SuppressLint("LongLogTag")
    fun getUserProfile(token: AccessToken?, userId: String?) {

        val parameters = Bundle()
        parameters.putString(
            "fields",
            "id, first_name, middle_name, last_name, name, picture, email"
        )
        GraphRequest(token,
            "/$userId/",
            parameters,
            HttpMethod.GET,
            GraphRequest.Callback { response ->
                val jsonObject = response.jsonObject

                // Facebook Access Token
                // You can see Access Token only in Debug mode.
                // You can't see it in Logcat using Log.d, Facebook did that to avoid leaking user's access token.
                if (BuildConfig.DEBUG) {
                    FacebookSdk.setIsDebugEnabled(true)
                    FacebookSdk.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS)
                }
                accessToken = token.toString()

                // Facebook Id
                if (jsonObject.has("id")) {
                    val facebookId = jsonObject.getString("id")
                    Log.i("Facebook Id: ", facebookId.toString())
                    id = facebookId.toString()
                } else {
                    Log.i("Facebook Id: ", "Not exists")
                    id = "Not exists"
                }


                // Facebook First Name
                if (jsonObject.has("first_name")) {
                    val facebookFirstName = jsonObject.getString("first_name")
                    Log.i("Facebook First Name: ", facebookFirstName)
                    firstName = facebookFirstName
                } else {
                    Log.i("Facebook First Name: ", "Not exists")
                    firstName = "Not exists"
                }


                // Facebook Middle Name
                if (jsonObject.has("middle_name")) {
                    val facebookMiddleName = jsonObject.getString("middle_name")
                    Log.i("Facebook Middle Name: ", facebookMiddleName)
                    middleName = facebookMiddleName
                } else {
                    Log.i("Facebook Middle Name: ", "Not exists")
                    middleName = "Not exists"
                }


                // Facebook Last Name
                if (jsonObject.has("last_name")) {
                    val facebookLastName = jsonObject.getString("last_name")
                    Log.i("Facebook Last Name: ", facebookLastName)
                    lastName = facebookLastName
                } else {
                    Log.i("Facebook Last Name: ", "Not exists")
                    lastName = "Not exists"
                }


                // Facebook Name
                if (jsonObject.has("name")) {
                    val facebookName = jsonObject.getString("name")
                    Log.i("Facebook Name: ", facebookName)
                    name = facebookName
                } else {
                    Log.i("Facebook Name: ", "Not exists")
                    name = "Not exists"
                }


                // Facebook Profile Pic URL
                if (jsonObject.has("picture")) {
                    val facebookPictureObject = jsonObject.getJSONObject("picture")
                    if (facebookPictureObject.has("data")) {
                        val facebookDataObject = facebookPictureObject.getJSONObject("data")
                        if (facebookDataObject.has("url")) {
                            val facebookProfilePicURL = facebookDataObject.getString("url")
                            Log.i("Facebook Profile Pic URL: ", facebookProfilePicURL)
                            picture = facebookProfilePicURL
                        }
                    }
                } else {
                    Log.i("Facebook Profile Pic URL: ", "Not exists")
                    picture = "Not exists"
                }

                // Facebook Email
                if (jsonObject.has("email")) {
                    val facebookEmail = jsonObject.getString("email")
                    Log.i("Facebook Email: ", facebookEmail)
                    email = facebookEmail
                } else {
                    Log.i("Facebook Email: ", "Not exists")
                    email = "Not exists"
                }

                openDetailsActivity()
            }).executeAsync()
    }

    fun isLoggedIn(): Boolean {
        val accessToken = AccessToken.getCurrentAccessToken()
        val isLoggedIn = accessToken != null && !accessToken.isExpired
        return isLoggedIn
    }


    fun logOutUser() {
        LoginManager.getInstance().logOut()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        callbackManager.onActivityResult(requestCode, resultCode, data)
        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun openDetailsActivity() {
        val myIntent = Intent(this, ProfileViewEditActivity::class.java)
        val jsonExistProfile = JSONObject()
        jsonExistProfile.put(getString(R.string.fb_id), id)
        jsonExistProfile.put(getString(R.string.fb_first_name), firstName)
        jsonExistProfile.put(getString(R.string.fb_middle_name), middleName)
        jsonExistProfile.put(getString(R.string.fb_last_name), lastName)
        jsonExistProfile.put(getString(R.string.user_full_name), name)
        jsonExistProfile.put(getString(R.string.fb_picture), picture)
        jsonExistProfile.put(getString(R.string.fb_email), email)
        jsonExistProfile.put(getString(R.string.fb_access_token), accessToken)
        myIntent.putExtra(getString(R.string.fb_data), jsonExistProfile.toString())
        myIntent.putExtra(getString(R.string.fb_intent), true)
        startActivity(myIntent)
    }

    fun slideUp(mainView: View, footer_view: View) {
        footer_view.visibility = View.VISIBLE
        val animate_footer = TranslateAnimation(
            0F,  // fromXDelta
            0F,  // toXDelta
            footer_view.height.toFloat(),  // fromYDelta
            0F
        ) // toYDelta
        animate_footer.duration = animDuration.toLong()
        animate_footer.fillAfter = true
        footer_view.startAnimation(animate_footer)
        mainView.visibility = View.VISIBLE
        val animate_main = TranslateAnimation(
            0F,  // fromXDelta
            0F,  // toXDelta
            0F,  // fromYDelta
            (0 - footer_view.height).toFloat()
        ) // toYDelta
        animate_main.duration = animDuration.toLong()
        animate_main.fillAfter = true
        mainView.startAnimation(animate_main)
    }

    private fun createBottomUpAnimation(
        view: View,
        listener: AnimatorListenerAdapter?, distance: Float
    ): ObjectAnimator? {
        val animator = ObjectAnimator.ofFloat(view, "translationY", -distance)
        //        animator.setDuration(???)
        animator.removeAllListeners()
        if (listener != null) {
            animator.addListener(listener)
        }
        return animator
    }
}