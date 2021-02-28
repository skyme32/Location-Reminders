package com.udacity.project4.authentication

import android.app.Activity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.firebase.ui.auth.IdpResponse
import androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult
import com.firebase.ui.auth.AuthUI
import android.view.View
import android.widget.Button


/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    companion object {
        const val TAG = "requestAuthenticationActivity"
    }

    private var resultLauncher = registerForActivityResult(StartActivityForResult()) { result ->
        val response = IdpResponse.fromResultIntent(result.data)
        if (result.resultCode == Activity.RESULT_OK) {
            // User successfully signed in
            Log.i(TAG, "Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}!")
        } else { Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}") }

    }

    public fun launchSignInFlow(view: View) {
        val providers = arrayListOf(
                AuthUI.IdpConfig.EmailBuilder().build(),
                AuthUI.IdpConfig.GoogleBuilder().build()
        )

        //TODO: a bonus is to customize the sign in flow to look nice using :
        //https://github.com/firebase/FirebaseUI-Android/blob/master/auth/README.md#custom-layout

        // We listen to the response of this activity with the
        resultLauncher.launch(AuthUI.getInstance()
                .createSignInIntentBuilder()
                .setAvailableProviders(providers)
                .build()
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)
    }
}
