package com.mtl.My_Hack_X.utils

import android.content.Context
import com.mtl.My_Hack_X.R

object SecretKeys {
    fun getWebApiKey(context: Context): String {
        return context.getString(R.string.web_api_key)
    }
} 