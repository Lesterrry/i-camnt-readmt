package com.retsel.icamntreadmt

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import org.w3c.dom.Text

class MainActivity : AppCompatActivity() {

    //*********************************************************************
    // MARK: OUTLETS
    //*********************************************************************
    private lateinit var titleTextView: TextView
    private lateinit var startButton: Button
    private lateinit var selectFileButton: Button
    private lateinit var skipRateEditTextNumber: TextView
    private lateinit var progressEditTextNumber: TextView
    private lateinit var currentTextView: TextView
    private lateinit var precedingTextView: TextView
    private lateinit var followingTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        titleTextView = findViewById(R.id.titleTextView)
        startButton = findViewById(R.id.startButton)
        selectFileButton = findViewById(R.id.selectFileButton)
        skipRateEditTextNumber = findViewById(R.id.skipRateEditTextNumber)
        progressEditTextNumber = findViewById(R.id.progressEditTextNumber)
        currentTextView = findViewById(R.id.currentTextView)
        precedingTextView = findViewById(R.id.precedingTextView)
        followingTextView = findViewById(R.id.followingTextView)

        selectFileButton.setOnClickListener {

            val intent = Intent()
                .setType("*/*")
                .setAction(Intent.ACTION_GET_CONTENT)

            startActivityForResult(Intent.createChooser(intent, "semlect a file"), 111)
        }
    }
}