package com.example.myhealthconsultant.util

import android.content.Context
import android.content.Intent
import com.example.myhealthconsultant.wxapi.WeChatConstants
import com.tencent.mm.opensdk.modelmsg.SendAuth
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import kotlinx.coroutines.CompletableDeferred
import java.util.UUID

object WeChatLoginHelper {

    private var callbackDeferred: CompletableDeferred<WeChatAuthResult>? = null

    data class WeChatAuthResult(
        val code: String? = null,
        val errCode: Int = 0,
        val errMsg: String? = null
    ) {
        val isSuccess: Boolean get() = errCode == 0 && code != null
    }

    fun isWeChatInstalled(context: Context): Boolean {
        return try {
            val api = WXAPIFactory.createWXAPI(context, WeChatConstants.APP_ID, false)
            api.isWXAppInstalled
        } catch (e: Exception) {
            false
        }
    }

    suspend fun sendAuthRequest(context: Context): WeChatAuthResult {
        val api = WXAPIFactory.createWXAPI(context, WeChatConstants.APP_ID, false)
        api.registerApp(WeChatConstants.APP_ID)

        if (!api.isWXAppInstalled) {
            return WeChatAuthResult(
                errCode = -100,
                errMsg = "请先安装微信"
            )
        }

        val req = SendAuth.Req().apply {
            scope = WeChatConstants.SCOPE
            state = WeChatConstants.STATE + "_" + UUID.randomUUID().toString().take(8)
        }

        callbackDeferred = CompletableDeferred()
        api.sendReq(req)

        return callbackDeferred!!.await()
    }

    fun onWeChatCallback(intent: Intent) {
        val code = intent.getStringExtra("code")
        val errCode = intent.getIntExtra("errCode", -2)
        val errMsg = intent.getStringExtra("errMsg")

        callbackDeferred?.complete(
            WeChatAuthResult(code = code, errCode = errCode, errMsg = errMsg)
        )
        callbackDeferred = null
    }
}
