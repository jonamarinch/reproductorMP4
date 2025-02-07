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
    private val videos = arrayOf(R.raw.cinema_intro, R.raw.film_intro, R.raw.make_films) // Agrega tus videos aquí
    private var videoActual = 0
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        videoView = findViewById(R.id.videoView)
        botonPlay = findViewById(R.id.botonPlay)
        duracion = findViewById(R.id.duracion)
        barra = findViewById(R.id.seekBar)
        anterior = findViewById(R.id.anterior)
        siguiente = findViewById(R.id.siguiente)
// Inicializar el VideoView con el primer video
        reproducirVideoSeleccionado(videos[videoActual])
// Control de reproducción
        botonPlay.setOnClickListener {
            if (isPlaying) {
                pausarVideo()
            } else {
                reproducirVideo()
            }
        }
// Botón siguiente
        siguiente.setOnClickListener {
            siguienteVideo()
        }
// Botón anterior
        anterior.setOnClickListener {
            anteriorVideo()
        }
// Escucha del SeekBar
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
// Actualizar la barra de progreso y el tiempo
        handler.postDelayed(updateProgressTask, 1000)
    }
    private fun reproducirVideoSeleccionado(videoId: Int) {
        val videoUri = Uri.parse("android.resource://$packageName/$videoId")
        videoView.setVideoURI(videoUri)
        videoView.setOnPreparedListener {
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
            if (videoView.isPlaying) {
                barra.progress = videoView.currentPosition
                actualizarTiempo()
            }
            handler.postDelayed(this, 1000)
        }
    }
    private fun actualizarTiempo() {
        val currentTime = tiempo(videoView.currentPosition)
        val totalTime = tiempo(videoView.duration)
        duracion.text = "$currentTime / $totalTime"
    }
    private fun tiempo(milisec: Int): String {
        val seconds = (milisec / 1000) % 60
        val minutes = (milisec / (1000 * 60)) % 60
        return String.format("%02d:%02d", minutes, seconds)
    }
    /*
    Se guarda la posición en cualquier cambio de configuración (como una rotación de pantalla)
     */
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putInt("currentTime", videoView.currentPosition)
        outState.putInt("videoActual", videoActual)
        outState.putBoolean("isPlaying", isPlaying)
    }
    /*
 * Se restaura la posición y el estado de reproducción del video
 * después de un cambio de configuración.
 */
    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)

        val videoActual = savedInstanceState.getInt("videoActual") ?: 0
        reproducirVideoSeleccionado(videos[videoActual])

        val currentTime = savedInstanceState?.getInt("currentTime") ?: 0 // Obtén el valor guardado o 0 si es nulo
        videoView.seekTo(currentTime) // Usa seekTo para establecer la posición

        val isPlaying = savedInstanceState?.getBoolean("isPlaying") ?: false // Obtén el valor guardado o false si es nulo
        if (isPlaying) {
            videoView.start()
        }
    }
    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(updateProgressTask)
        videoView.stopPlayback()
    }
}