package com.finmate.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.finmate.app.ui.FinMateAppRoot
import com.finmate.app.ui.theme.FinMateTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val app = application as FinMateApp
        setContent {
            FinMateTheme {
                FinMateAppRoot(app)
            }
        }
    }
}
