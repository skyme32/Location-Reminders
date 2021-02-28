package com.udacity.project4.authentication

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R


/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    companion object {
        const val TAG = "requestAuthentication"
    }

    private var resultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        val response = IdpResponse.fromResultIntent(result.data)
        if (result.resultCode == Activity.RESULT_OK) {
            // User successfully signed in
            Log.i(TAG, "Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!")
        } else { Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}") }

    }

    /**
     * Click R.id.button_login
     */
    fun launchSignInFlow(view: View) {
        val providers = arrayListOf(
                AuthUI.IdpConfig.EmailBuilder().build(),
                AuthUI.IdpConfig.GoogleBuilder().build()
        )

        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout
        val customLayout = AuthMethodPickerLayout.Builder(R.layout.login_custom_layout)
                .setGoogleButtonId(R.id.btn_gmail)
                .setEmailButtonId(R.id.btn_mail) // ...
                .build()

        // We listen to the response of this activity with the
        resultLauncher.launch(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .setAuthMethodPickerLayout(customLayout)
                .build()

        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
    }
}
