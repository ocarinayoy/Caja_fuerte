package com.tdm.cajafuerte

import android.os.Bundle
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var swipeListener: SwipeGestureListener
    private var currentFrame = "content" // Rastrea la ventana actual
    private var currentView: Int = R.id.principal // Vista inicial

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Referencias a los layouts
        val contentFrame = findViewById<View>(R.id.principal)
        val frameClave = findViewById<View>(R.id.frame_clave)
        val frameWifi = findViewById<View>(R.id.frame_wifi)
        val frameConfig = findViewById<View>(R.id.frame_config)

        // Inicializar el detector de gestos
        swipeListener = SwipeGestureListener(this) { direction ->
            when (direction) {
                "left" -> {
                    when (currentFrame) {
                        "clave" -> switchFrame(frameWifi, "wifi", R.anim.slide_in_right)
                        "wifi" -> switchFrame(frameConfig, "config", R.anim.slide_in_right)
                    }
                }
                "right" -> {
                    when (currentFrame) {
                        "config" -> switchFrame(frameWifi, "wifi", R.anim.slide_in_left)
                        "wifi" -> switchFrame(frameClave, "clave", R.anim.slide_in_left)
                    }
                }
            }
        }

        // Configuración inicial para mostrar la ventana principal
        setOnIconClick(contentFrame, frameClave, frameWifi, frameConfig, "clave", R.anim.slide_in_left, R.id.icon_clave)
        setOnIconClick(contentFrame, frameWifi, frameClave, frameConfig, "wifi", R.anim.slide_in_bottom, R.id.icon_red)
        setOnIconClick(contentFrame, frameConfig, frameClave, frameWifi, "config", R.anim.slide_in_right, R.id.icon_config)
    }

    // Cambia el frame actual según los gestos o íconos
    private fun switchFrame(nextFrame: View, frameName: String, animation: Int) {
        // Si la vista ya está activa, no hace nada
        if (currentFrame == frameName) return

        // Oculta las demás vistas
        findViewById<View>(R.id.principal).visibility = View.GONE
        findViewById<View>(R.id.frame_clave).visibility = View.GONE
        findViewById<View>(R.id.frame_wifi).visibility = View.GONE
        findViewById<View>(R.id.frame_config).visibility = View.GONE

        // Muestra la siguiente vista
        nextFrame.visibility = View.VISIBLE
        nextFrame.startAnimation(AnimationUtils.loadAnimation(this, animation))

        // Actualiza el estado actual
        currentFrame = frameName
    }

    // Configura los eventos de clic para los íconos
    private fun setOnIconClick(
        contentFrame: View, nextFrame: View, hideFrame1: View, hideFrame2: View,
        frameName: String, animation: Int, iconId: Int
    ) {
        findViewById<View>(iconId).setOnClickListener {
            // Si ya está visible, no hace nada
            if (currentFrame == frameName) return@setOnClickListener

            contentFrame.visibility = View.GONE
            hideFrame1.visibility = View.GONE
            hideFrame2.visibility = View.GONE
            nextFrame.visibility = View.VISIBLE
            nextFrame.startAnimation(AnimationUtils.loadAnimation(this, animation))
            currentFrame = frameName
        }
    }

    // Detectar el gesto en el área principal
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let { swipeListener.onTouch(it) }
        return super.onTouchEvent(event)
    }
}
