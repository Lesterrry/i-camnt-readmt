package com.retsel.icamntreadmt

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.widget.Button
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import java.io.BufferedReader
import java.io.File
import java.util.*
import kotlin.concurrent.timerTask
import kotlin.math.roundToInt

class MainActivity : AppCompatActivity() {
    //*********************************************************************
    // MARK: ENUMS
    //*********************************************************************
    enum class AppState {
        UNREADY,
        READY,
        READING
    }

    //*********************************************************************
    // MARK: VARIABLES & OUTLETS
    //*********************************************************************
    private lateinit var titleTextView: TextView
    private lateinit var startButton: Button
    private lateinit var selectFileButton: Button
    private lateinit var skipRateEditTextNumber: TextView
    private lateinit var progressEditTextNumber: TextView
    private lateinit var currentTextView: TextView
    private lateinit var precedingTextView: TextView
    private lateinit var followingTextView: TextView

    private var sharedPreferences: SharedPreferences? = null
    private var timer = Timer()
    var count: Int = 0
    var book: List<String>? = null
    var filePath: Uri? = null
        set(value) {
            if (value != filePath) sharedPreferences?.edit()?.putString("file_path", value.toString())?.apply()
            field = value
        }
    var index: Int = 0
    var skipRate: Long = 300
        set(value) {
            if (value != skipRate) sharedPreferences?.edit()?.putLong("skip_rate", value)?.apply()
            field = value
        }
    var appState = AppState.UNREADY
        set(value) {
            when (value) {
                AppState.UNREADY -> {
                    currentTextView.text = ""
                    precedingTextView.text = ""
                    followingTextView.text = ""
                    startButton.text = "starmt"
                }
                AppState.READING -> startButton.text = "stomp"
                AppState.READY -> {
                    startButton.text = "starmt"
                    titleTextView.text = "reamdy to readmt"
                }
            }
            startButton.isEnabled = value != AppState.UNREADY
            progressEditTextNumber.isEnabled = value == AppState.READY
            skipRateEditTextNumber.isEnabled = value == AppState.READY
            field = value
        }

    //*********************************************************************
    // MARK: FUNCTIONS
    //*********************************************************************
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
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                Intent().setType("*/*").setAction(Intent.ACTION_OPEN_DOCUMENT)
            } else {
                TODO("VERSION.SDK_INT < KITKAT")
            }
            startActivityForResult(Intent.createChooser(intent, "semlect a file"), 111)
        }
        startButton.setOnClickListener {
             sharedPreferences?.edit()?.putInt("index", index)?.apply()
            when (appState) {
                AppState.READY -> { schedule(); appState = AppState.READING }
                AppState.READING -> { timer.cancel(); timer = Timer(); appState = AppState.READY }
                else -> {}
            }
        }
        progressEditTextNumber.setOnEditorActionListener { textView, _, _ ->
            try {
                val n = Integer.parseInt(textView.text.toString())
                if (n in 0 until count) {
                    index = n
                    display()
                }
                else {
                    progressEditTextNumber.text = index.toString()
                }
            } catch (e: java.lang.NumberFormatException) {
                progressEditTextNumber.text = index.toString()
                return@setOnEditorActionListener true
            }
            true
        }
        skipRateEditTextNumber.setOnEditorActionListener { textView, _, _ ->
            try {
                skipRate = textView.text.toString().toLong()
                if (appState == AppState.READING) {
                    timer.cancel()
                    timer = Timer()
                    schedule()
                }
            } catch(e: java.lang.NumberFormatException) {
                textView.text = skipRate.toString()
            }
            true
        }

        sharedPreferences = super.getSharedPreferences("icamntreadmt.pref", Context.MODE_PRIVATE)
        prepare()
    }

    @RequiresApi(Build.VERSION_CODES.KITKAT)
    @SuppressLint("SetTextI18n")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 111 && resultCode == RESULT_OK) {
            data?.let {
                filePath = data.data
                contentResolver.takePersistableUriPermission(data.data ?: run { return }, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                parseBook()
                // TODO: check file type
            }
        }
    }

    fun schedule() {
        timer.scheduleAtFixedRate(timerTask {
            display(index)
            if (index < count - 1) {
                index++
            } else {
                timer.cancel()
                timer = Timer()
                Handler(mainLooper).post { appState = AppState.READY }
            }
                                            }, skipRate, skipRate)
    }

    fun prepare() {
        filePath = sharedPreferences?.getString("file_path", null)?.toUri()
        skipRate = sharedPreferences?.getLong("skip_rate", 300) ?: 300
        index = sharedPreferences?.getInt("index", 0) ?: 0
        parseBook()
    }

    fun parseBook(){
        filePath?.let {
            val bufferedReader: BufferedReader = contentResolver.openInputStream(filePath!!)?.bufferedReader() ?: run { return }
            var list = bufferedReader.use { it.readLines() } as MutableList<String>
            list.removeAll(listOf(" ", ""))
            val string = list.joinToString(" ")
            string.replace('\n', ' ')
            book = string.split(" ")
            appState = AppState.READY
            count = book!!.count()
            progressEditTextNumber.hint = "wormd (<$count)"
            display()
        } ?: run { appState = AppState.UNREADY }
    }

    fun display(withIndex: Int? = null) {
        val i = withIndex ?: index
        book?.let {
            progressEditTextNumber.text = index.toString()
            if (appState == AppState.READING) Handler(mainLooper).post { titleTextView.text = "reamding (${((i / count.toDouble()) * 100).roundToInt()}%)" }
            currentTextView.text = book!![i]
            precedingTextView.text = book!!.subList(if (i < 30) 0 else index - 30, i).joinToString(" ")
            followingTextView.text = book!!.subList(if (i < count - 1) i + 1 else i, if (i < count - 30) i + 30 else count).joinToString(" ")
        }
    }
}