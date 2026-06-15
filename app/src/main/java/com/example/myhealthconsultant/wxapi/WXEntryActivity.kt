package com.example.myhealthconsultant.wxapi

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import com.tencent.mm.opensdk.modelbase.BaseReq
import com.tencent.mm.opensdk.modelbase.BaseResp
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler
import com.example.myhealthconsultant.util.WeChatLoginHelper
import com.tencent.mm.opensdk.openapi.WXAPIFactory

class WXEntryActivity : Activity(), IWXAPIEventHandler {

    private lateinit var iwxapi: IWXAPI

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        iwxapi = WXAPIFactory.createWXAPI(this, WeChatConstants.APP_ID, false)
        try {
            iwxapi.handleIntent(intent, this)
        } catch (e: Exception) {
            e.printStackTrace()
            finish()
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        setIntent(intent)
        try {
            iwxapi.handleIntent(intent, this)
        } catch (e: Exception) {
            e.printStackTrace()
            finish()
        }
    }

    override fun onReq(baseReq: BaseReq?) {
        finish()
    }

    override fun onResp(baseResp: BaseResp?) {
        val resultIntent = Intent()
        when (baseResp?.errCode) {
            BaseResp.ErrCode.ERR_OK -> {
                val resp = baseResp as? com.tencent.mm.opensdk.modelmsg.SendAuth.Resp
                resultIntent.putExtra("code", resp?.code)
                resultIntent.putExtra("errCode", 0)
            }
            BaseResp.ErrCode.ERR_USER_CANCEL -> {
                resultIntent.putExtra("errCode", -1)
                resultIntent.putExtra("errMsg", "用户取消")
            }
            else -> {
                resultIntent.putExtra("errCode", baseResp?.errCode ?: -2)
                resultIntent.putExtra("errMsg", baseResp?.errStr ?: "未知错误")
            }
        }
        WeChatLoginHelper.onWeChatCallback(resultIntent)
        finish()
    }
}

object WeChatConstants {
    const val APP_ID = "wx_demo_app_id"
    const val SCOPE = "snsapi_userinfo"
    const val STATE = "wechat_login_health"
}
