package com.erman.googlesignin.presentation.signin

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.erman.googlesignin.R
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.BeginSignInRequest.GoogleIdTokenRequestOptions
import com.google.android.gms.auth.api.identity.SignInClient
import com.google.firebase.Firebase
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.auth
import kotlinx.coroutines.tasks.await

class GoogleAuthUiClient(private val context: Context, private val oneTapClient: SignInClient) {
    private val auth = Firebase.auth

    suspend fun signIn(): IntentSender? {
        return try {
            oneTapClient.beginSignIn(
                buildSignInRequest()
            ).await()
        } catch (err: Exception) {
            err.printStackTrace()
            null
        }?.pendingIntent?.intentSender
    }

    suspend fun getSignInWithIntent(intent: Intent): SignInResult {
        val credential = oneTapClient.getSignInCredentialFromIntent(intent)
        val googleIdToken = credential.googleIdToken
        val googleCredentials = GoogleAuthProvider.getCredential(googleIdToken, null)

        return try {
            val user = auth.signInWithCredential(googleCredentials).await().user
            SignInResult(data = user?.run {
                UserData(userId = uid, username = displayName, ppUrl = photoUrl.toString())
            }, error = null)
        } catch (err: Exception) {
            err.printStackTrace()
            SignInResult(data = null, error = err.message)
        }
    }

    suspend fun signOut() {
        try {
            oneTapClient.signOut().await()
            auth.signOut()  //So we will be required to sign in again with our account
        } catch (err: Exception) {
            err.printStackTrace()
        }
    }

    fun getSignedInUser(): UserData? = auth.currentUser?.run {
        UserData(userId = uid, username = displayName, ppUrl = photoUrl?.toString())
    }

    private fun buildSignInRequest(): BeginSignInRequest {
        return BeginSignInRequest.Builder()
            .setGoogleIdTokenRequestOptions(
                GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setFilterByAuthorizedAccounts(false)   //Checks if we have signed in before and shows that account as an option. When it si set to false, it shows all the google accounts that we use for theat device
                    .setServerClientId(context.getString(R.string.client_id))
                    .build()
            )
            .setAutoSelectEnabled(true) //If we have only one google account on the device, it will automatically select that one
            .build()
    }
}