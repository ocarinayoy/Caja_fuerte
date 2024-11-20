package com.tdm.cajafuerte

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*

class MainActivity : AppCompatActivity() {

    private lateinit var swipeListener: SwipeGestureListener
    private var currentFrame = "content" // Track de la ventana actual
    private val client = OkHttpClient() // Cliente HTTP

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Referencias a los layouts
        val contentFrame = findViewById<View>(R.id.principal)
        val frameClave = findViewById<View>(R.id.frame_clave)
        val frameWifi = findViewById<View>(R.id.frame_wifi)
        val frameConfig = findViewById<View>(R.id.frame_config)

        // Referenciar el EditText y el Button del frame_clave
        val inputClave = findViewById<EditText>(R.id.input_clave)
        val btnEnviarClave = findViewById<Button>(R.id.btn_enviar_clave)

        // Configurar el evento del botón
        btnEnviarClave.setOnClickListener {
            val claveIngresada = inputClave.text.toString()

            if (claveIngresada.isEmpty()) {
                Toast.makeText(this, "Por favor ingresa una clave.", Toast.LENGTH_SHORT).show()
            } else {
                enviarClaveESP32(claveIngresada)
            }
        }

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
        setOnIconClick(
            contentFrame,
            frameClave,
            frameWifi,
            frameConfig,
            "clave",
            R.anim.slide_in_left,
            R.id.icon_clave
        )
        setOnIconClick(
            contentFrame,
            frameWifi,
            frameClave,
            frameConfig,
            "wifi",
            R.anim.slide_in_bottom,
            R.id.icon_red
        )
        setOnIconClick(
            contentFrame,
            frameConfig,
            frameClave,
            frameWifi,
            "config",
            R.anim.slide_in_right,
            R.id.icon_config
        )
    }

    private fun enviarClaveESP32(clave: String) {
        val url = "http://192.168.100.59/recibir_clave" // Reemplaza con la IP del ESP32

        // Crea el cuerpo de la petición
        val body = FormBody.Builder()
            .add("clave", clave)
            .build()

        // Crea la solicitud HTTP POST
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        // Enviar la petición en un hilo de fondo
        Thread {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Clave enviada con éxito", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Error al enviar clave", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Error en la conexión: ${e.message}", Toast.LENGTH_SHORT).show()
                    Log.d("MiApp", "${e.message}")

                }
            }
        }.start()
    }

    // Cambia el frame actual según los gestos o íconos
    private fun switchFrame(nextFrame: View, frameName: String, animation: Int) {
        findViewById<View>(R.id.principal).visibility = View.GONE
        findViewById<View>(R.id.frame_clave).visibility = View.GONE
        findViewById<View>(R.id.frame_wifi).visibility = View.GONE
        findViewById<View>(R.id.frame_config).visibility = View.GONE

        nextFrame.visibility = View.VISIBLE
        nextFrame.startAnimation(AnimationUtils.loadAnimation(this, animation))
        currentFrame = frameName
    }

    // Configura los eventos de clic para los íconos
    private fun setOnIconClick(
        contentFrame: View, nextFrame: View, hideFrame1: View, hideFrame2: View,
        frameName: String, animation: Int, iconId: Int
    ) {
        findViewById<View>(iconId).setOnClickListener {
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
