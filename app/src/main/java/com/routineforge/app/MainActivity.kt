package com.routineforge.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.routineforge.app.ui.navigation.RoutineForgeNavGraph
import com.routineforge.app.ui.theme.RoutineForgeTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            RoutineForgeTheme {
                RoutineForgeNavGraph()
            }
        }
    }
}
