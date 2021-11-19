package com.example.hrlog

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.hrannotation.HRLog
@HRLog
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

}