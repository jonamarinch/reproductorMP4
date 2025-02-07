package com.example.reproductormp4

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import android.widget.VideoView
import androidx.activity.ComponentActivity

class MainActivity : ComponentActivity() {
    private lateinit var videoView: VideoView
    private lateinit var botonPlay: ImageButton
    private var isPlaying = false
    private lateinit var duracion: TextView
    private lateinit var barra: SeekBar
    private lateinit var anterior: ImageButton
    private lateinit var siguiente: ImageButton
    private val handler = Handler(Looper.getMainLooper())
    private val videos = arrayOf(R.raw.cinema_intro, R.raw.film_intro, R.raw.make_films)
    private var videoActual = 0
    private var currentVideoUri: Uri? = null
    private var videoPrepared = false

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        if (videoPrepared && videoView.isPlaying) {
            outState.putInt("currentTime", videoView.currentPosition)
            outState.putInt("videoActual", videoActual)
            outState.putBoolean("isPlaying", isPlaying)
        } else {
            outState.putInt("videoActual", videoActual)
        }
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        videoActual = savedInstanceState.getInt("videoActual")
        val currentTime = savedInstanceState.getInt("currentTime", 0)
        val wasPlaying = savedInstanceState.getBoolean("isPlaying", false)

        videoView.setOnPreparedListener {
            videoPrepared = true
            videoView.seekTo(currentTime)
            barra.max = videoView.duration
            actualizarTiempo()

            if (wasPlaying) {
                videoView.start()
                botonPlay.setImageResource(android.R.drawable.ic_media_pause)
                isPlaying = true
            }
        }

        currentVideoUri = Uri.parse("android.resource://$packageName/${videos[videoActual]}")
        videoView.setVideoURI(currentVideoUri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        videoView = findViewById(R.id.videoView)
        botonPlay = findViewById(R.id.botonPlay)
        duracion = findViewById(R.id.duracion)
        barra = findViewById(R.id.seekBar)
        anterior = findViewById(R.id.anterior)
        siguiente = findViewById(R.id.siguiente)

        if (savedInstanceState == null) {
            currentVideoUri = Uri.parse("android.resource://$packageName/${videos[videoActual]}")
            videoView.setVideoURI(currentVideoUri)
        } else {
            currentVideoUri = Uri.parse("android.resource://$packageName/${videos[videoActual]}")
            videoView.setVideoURI(currentVideoUri)
        }

        videoView.setOnPreparedListener {
            videoPrepared = true
            barra.max = videoView.duration
            actualizarTiempo()

            if (savedInstanceState != null) {
                val currentTime = savedInstanceState.getInt("currentTime")
                val isPlaying = savedInstanceState.getBoolean("isPlaying")

                videoView.seekTo(currentTime)
                if (isPlaying) {
                    videoView.start()
                    botonPlay.setImageResource(android.R.drawable.ic_media_pause)
                    this.isPlaying = true
                }
            } else {
                reproducirVideo()
            }
        }

        videoView.setOnCompletionListener {
            siguienteVideo()
        }

        botonPlay.setOnClickListener {
            if (isPlaying) {
                pausarVideo()
            } else {
                reproducirVideo()
            }
        }

        siguiente.setOnClickListener {
            siguienteVideo()
        }

        anterior.setOnClickListener {
            anteriorVideo()
        }

        barra.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    videoView.seekTo(progress)
                    actualizarTiempo()
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        handler.postDelayed(updateProgressTask, 1000)
    }

    private fun reproducirVideoSeleccionado(videoId: Int) {
        videoPrepared = false
        videoActual = videoActual // Update the current video index
        currentVideoUri = Uri.parse("android.resource://$packageName/$videoId")
        videoView.setVideoURI(currentVideoUri)
        videoView.setOnPreparedListener {
            videoPrepared = true
            barra.max = videoView.duration
            actualizarTiempo()
            reproducirVideo()
        }
    }

    private fun reproducirVideo() {
        videoView.start()
        botonPlay.setImageResource(android.R.drawable.ic_media_pause)
        isPlaying = true
    }

    private fun pausarVideo() {
        videoView.pause()
        botonPlay.setImageResource(android.R.drawable.ic_media_play)
        isPlaying = false
    }

    private fun siguienteVideo() {
        videoActual = if (videoActual < videos.size - 1) videoActual + 1 else 0
        reproducirVideoSeleccionado(videos[videoActual])
    }

    private fun anteriorVideo() {
        videoActual = if (videoActual > 0) videoActual - 1 else videos.size - 1
        reproducirVideoSeleccionado(videos[videoActual])
    }

    private val updateProgressTask = object : Runnable {
        override fun run() {
            if (videoPrepared && videoView.isPlaying) { // Check if prepared before updating
                barra.progress = videoView.currentPosition
                actualizarTiempo()
            }
            handler.postDelayed(this, 1000)
        }
    }

    private fun actualizarTiempo() {
        if (videoPrepared) { // Check if prepared before accessing duration
            val currentTime = tiempo(videoView.currentPosition)
            val totalTime = tiempo(videoView.duration)
            duracion.text = "$currentTime / $totalTime"
        }
    }

    private fun tiempo(milisec: Int): String {
        val seconds = (milisec / 1000) % 60
        val minutes = (milisec / (1000 * 60)) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateProgressTask)
        videoView.stopPlayback()
    }
}