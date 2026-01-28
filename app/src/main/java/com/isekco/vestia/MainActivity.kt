package com.isekco.vestia

import android.os.Bundle
import androidx.activity.ComponentActivity
import android.widget.TextView
import android.widget.Button

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        val titleText = findViewById<TextView>(R.id.titleText)
        val button = findViewById<Button>(R.id.actionButton)

        button.setOnClickListener {
            titleText.text = "This is really Vestia"
        }
    }
}
