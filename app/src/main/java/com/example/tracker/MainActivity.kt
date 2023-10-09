package com.example.tracker

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.telephony.TelephonyManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.util.*
import java.io.IOException

@Suppress("DEPRECATION")
class MainActivity : AppCompatActivity() {
    private val REQUEST_PHONE_CALL = 1
    private lateinit var callInfoTextView: TextView
    private lateinit var voiceMessage: TextView
    private lateinit var timer: Timer

    private val speechRecognizer: SpeechRecognizer by lazy {
        SpeechRecognizer.createSpeechRecognizer(this)
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        callInfoTextView = findViewById(R.id.callInfoTextView)
        voiceMessage = findViewById(R.id.voiceMessage)
        val call = findViewById<Button>(R.id.phonecall)

        call.setOnClickListener {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_PHONE_CALL
                )
            }
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.RECORD_AUDIO
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.RECORD_AUDIO),
                    REQUEST_PHONE_CALL
                )
            }
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    REQUEST_PHONE_CALL
                )
            }
            if (ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.CALL_PHONE
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(android.Manifest.permission.CALL_PHONE),
                    REQUEST_PHONE_CALL
                )
            } else {
                makeCall()
            }
            speechRecognizer.setRecognitionListener(object : RecognitionListener {
                override fun onReadyForSpeech(params: Bundle?) {
                    // Called when the speech recognition engine is ready for audio input
                }

                override fun onBeginningOfSpeech() {
                    // Called when the user starts speaking
                }

                override fun onRmsChanged(rmsdB: Float) {
                    // Called when the input audio's volume level changes
                }

                override fun onBufferReceived(buffer: ByteArray?) {
                    // Called when audio data is received
                }

                override fun onEndOfSpeech() {
                    // Called when the user stops speaking
                }

                override fun onError(error: Int) {
                    // Called when an error occurs in speech recognition
                }

                override fun onResults(results: Bundle?) {
                    // Called when speech recognition is successful
                    val resultData = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    val recognizedText = resultData?.get(0) ?: "No speech detected"
                    // Do something with recognizedText
                    voiceMessage.text = recognizedText
                }

                override fun onPartialResults(partialResults: Bundle?) {
                    // Called when partial recognition results are available
                }

                override fun onEvent(eventType: Int, params: Bundle?) {
                    // Called for events related to the recognition process
                }
            })
        }

        // Initialize and start the timer to update call information every second
        timer = Timer()
        timer.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                updateCallInformation()
            }
        }, 0, 1000) // Update every 1000 milliseconds (1 second)
    }

    private fun makeCall() {
        val mobnumberEditText = findViewById<EditText>(R.id.mobnumber)
        val mobnum = mobnumberEditText.text.toString()
        val intent = Intent(Intent.ACTION_CALL, Uri.fromParts("tel", mobnum, null))
        startActivity(intent)
    }

    private fun updateCallInformation() {
        val telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
        val callState = telephonyManager.callState
        val isCallSilent = audioManager.isMicrophoneMute
        val isLoud = audioManager.isSpeakerphoneOn

        var callData = ""
        if(callState == 0)
        {
            callData = "No call"
        }
        else if(callState == 1)
        {
            callData = "Incoming Call"
        }
        else if(callState == 2)
        {
            callData = "Ongoing Call"
        }
        else {
            callData = "Something went wrong"
        }

        val callInfo = "State: $callData \n isSilent: $isCallSilent \n isLoud: $isLoud \n Speech"


        runOnUiThread {
            callInfoTextView.text = callInfo

            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")

            speechRecognizer.startListening(intent)
            speechRecognizer.stopListening()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PHONE_CALL) {
            makeCall()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Cancel the timer when the activity is destroyed to prevent memory leaks
        timer.cancel()
    }

}

