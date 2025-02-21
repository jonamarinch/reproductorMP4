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
    // Inicialización de variables para la interfaz y control de reproducción
    private lateinit var videoView: VideoView
    private lateinit var botonPlay: ImageButton
    private var isPlaying = false
    private lateinit var duracion: TextView
    private lateinit var barra: SeekBar
    private lateinit var anterior: ImageButton
    private lateinit var siguiente: ImageButton
    private val handler = Handler(Looper.getMainLooper())
    // recursos de video
    private val videos = arrayOf(R.raw.cinema_intro, R.raw.film_intro, R.raw.make_films)
    private var videoActual = 0
    private var currentVideoUri: Uri? = null
    private var videoPrepared = false

    // Guarda el estado actual del video al rotar la pantalla o cambiar de actividad
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

    // Restaura el estado del video tras una rotación o cambio de actividad
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

        // Configura el video actual
        currentVideoUri = Uri.parse("android.resource://$packageName/${videos[videoActual]}")
        videoView.setVideoURI(currentVideoUri)
    }

    // Configuración inicial de la actividad
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        videoView = findViewById(R.id.videoView)
        botonPlay = findViewById(R.id.botonPlay)
        duracion = findViewById(R.id.duracion)
        barra = findViewById(R.id.seekBar)
        anterior = findViewById(R.id.anterior)
        siguiente = findViewById(R.id.siguiente)

        // Carga el primer video o recupera el estado guardado
        if (savedInstanceState == null) {
            currentVideoUri = Uri.parse("android.resource://$packageName/${videos[videoActual]}")
            videoView.setVideoURI(currentVideoUri)
        } else {
            currentVideoUri = Uri.parse("android.resource://$packageName/${videos[videoActual]}")
            videoView.setVideoURI(currentVideoUri)
        }

        // cuando el video está listo para reproducirse
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

        // Avanza al siguiente video cuando termina la reproducción
        videoView.setOnCompletionListener {
            siguienteVideo()
        }

        // listeners de los botones
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

        // Configuración de la barra de progreso
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

        // actualización periódica de la barra
        handler.postDelayed(updateProgressTask, 1000)
    }

    // Reproduce el video seleccionado a partir de posicion de array
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

    // Función para iniciar la reproducción
    private fun reproducirVideo() {
        videoView.start()
        botonPlay.setImageResource(android.R.drawable.ic_media_pause)
        isPlaying = true
    }

    // Función para pausar la reproducción
    private fun pausarVideo() {
        videoView.pause()
        botonPlay.setImageResource(android.R.drawable.ic_media_play)
        isPlaying = false
    }

    // Función para reproducir el siguiente
    private fun siguienteVideo() {
        videoActual = if (videoActual < videos.size - 1) videoActual + 1 else 0
        reproducirVideoSeleccionado(videos[videoActual])
    }

    // Función para reproducir el anterior
    private fun anteriorVideo() {
        videoActual = if (videoActual > 0) videoActual - 1 else videos.size - 1
        reproducirVideoSeleccionado(videos[videoActual])
    }

    // actualización periódica de la barra
    private val updateProgressTask = object : Runnable {
        override fun run() {
            if (videoPrepared && videoView.isPlaying) { // Check if prepared before updating
                barra.progress = videoView.currentPosition
                actualizarTiempo()
            }
            handler.postDelayed(this, 1000)
        }
    }

    // Actualiza el texto con la duración actual del video
    private fun actualizarTiempo() {
        if (videoPrepared) { // Check if prepared before accessing duration
            val currentTime = tiempo(videoView.currentPosition)
            val totalTime = tiempo(videoView.duration)
            duracion.text = "$currentTime / $totalTime"
        }
    }

    // Convierte los milisegundos en formato MM:SS
    private fun tiempo(milisec: Int): String {
        val seconds = (milisec / 1000) % 60
        val minutes = (milisec / (1000 * 60)) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }

    // Detiene la reproducción y limpia los recursos al cerrar la actividad
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateProgressTask)
        videoView.stopPlayback()
    }
}