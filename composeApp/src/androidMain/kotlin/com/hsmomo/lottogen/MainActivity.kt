package com.hsmomo.lottogen

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.google.android.gms.ads.MobileAds

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // AdMob 초기화
        MobileAds.initialize(this) {}

        enableEdgeToEdge()
        setContent {
            App()
        }
    }
}
