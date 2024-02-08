package com.erman.googlesignin.presentation.signin

data class SignInResult (val data: UserData?, val error: String?)

data class UserData(val userId: String, val username: String?, val ppUrl: String?)