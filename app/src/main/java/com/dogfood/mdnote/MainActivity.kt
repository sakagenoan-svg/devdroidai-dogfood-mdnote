package com.dogfood.mdnote

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.dogfood.mdnote.ui.EditorScreen
import com.dogfood.mdnote.ui.theme.AppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            AppTheme {
                EditorScreen()
            }
        }
    }
}
