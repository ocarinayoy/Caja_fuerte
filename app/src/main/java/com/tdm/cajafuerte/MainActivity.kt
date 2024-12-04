package com.tdm.cajafuerte

import android.os.Bundle
import android.util.Log
import android.view.MotionEvent
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*

class MainActivity : AppCompatActivity() {

    private lateinit var swipeListener: SwipeGestureListener
    private var currentFrame = "clave" // Track de la ventana actual
    private val client = OkHttpClient() // Cliente HTTP
    private lateinit var sharedPreferences: android.content.SharedPreferences

    // Layouts inflados
    private lateinit var claveLayout: View
    private lateinit var wifiLayout: View
    private lateinit var configLayout: View

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Inicializar SharedPreferences
        sharedPreferences = getSharedPreferences("AppPreferences", MODE_PRIVATE)

        // Inflar los layouts
        claveLayout = layoutInflater.inflate(R.layout.layout_clave, null)
        wifiLayout = layoutInflater.inflate(R.layout.layout_wifi, null)
        configLayout = layoutInflater.inflate(R.layout.layout_config, null)

        // Referencias a los elementos de cada layout
        val inputClave = claveLayout.findViewById<EditText>(R.id.input_clave)
        val btnEnviarClave = claveLayout.findViewById<Button>(R.id.btn_enviar_clave)
        val switchBloquear = claveLayout.findViewById<Switch>(R.id.switchBloquear)
        val btnCerrarCaja = claveLayout.findViewById<Button>(R.id.btn_cerrar_caja)

        val inputIp = configLayout.findViewById<EditText>(R.id.input_ip)
        val btnGuardarIp = configLayout.findViewById<Button>(R.id.btn_guardar_config)

        // Cargar la IP guardada
        val savedIp =
            sharedPreferences.getString("ip_address", "192.168.100.59") // Valor por defecto
        inputIp.setText(savedIp)

        // Guardar la IP al presionar el botón
        btnGuardarIp.setOnClickListener {
            val nuevaIp = inputIp.text.toString().trim()
            if (isValidIp(nuevaIp)) {
                val editor = sharedPreferences.edit()
                editor.putString("ip_address", nuevaIp)
                editor.apply()
                Toast.makeText(this, "IP guardada: $nuevaIp", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Por favor, introduce una IP válida.", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        // Configurar el evento del botón para enviar la clave
        btnEnviarClave.setOnClickListener {
            val claveIngresada = inputClave.text.toString()

            if (claveIngresada.isEmpty()) {
                Toast.makeText(this, "Por favor ingresa una clave.", Toast.LENGTH_SHORT).show()
            } else {
                enviarClaveESP32(claveIngresada)
            }
        }

        // Configurar el evento del botón para enviar el estado
        btnCerrarCaja.setOnClickListener {
            val estadoPuerta = true // Estado que indica que la puerta se cierra

            CerrarCajaESP32(estadoPuerta)
        }

        switchBloquear.setOnCheckedChangeListener { _, isChecked ->

            if (isChecked) {
                Toast.makeText(this, "Bloqueo de acceso activado", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Bloqueo de acceso desactivado", Toast.LENGTH_SHORT).show()
            }

        // Inicializar el detector de gestos
        swipeListener = SwipeGestureListener(this) { direction ->
            when (direction) {
                "left" -> {
                    when (currentFrame) {
                        "clave" -> switchFrame(wifiLayout, "wifi", R.anim.slide_in_right)
                        "wifi" -> switchFrame(configLayout, "config", R.anim.slide_in_right)
                    }
                }

                "right" -> {
                    when (currentFrame) {
                        "config" -> switchFrame(wifiLayout, "wifi", R.anim.slide_in_left)
                        "wifi" -> switchFrame(claveLayout, "clave", R.anim.slide_in_left)
                    }
                }
            }
        }

        // Configuración inicial para mostrar el layout de clave
        switchFrame(claveLayout, "clave", R.anim.slide_in_left)
    }

    private fun enviarClaveESP32(clave: String) {
        val ip = sharedPreferences.getString("ip_address", "192.168.0.56") // Valor por defecto
        val url = "http://$ip/recibir_clave"

        val body = FormBody.Builder()
            .add("clave", clave)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        Thread {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(
                            applicationContext,
                            "Clave enviada con éxito",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(
                            applicationContext,
                            "Error al enviar clave",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                runOnUiThread {
                    Toast.makeText(
                        applicationContext,
                        "Error en la conexión: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    Log.d("MiApp", "${e.message}")
                }
            }
        }.start()
    }

    private fun CerrarCajaESP32(estado: Boolean) {
        val ip = sharedPreferences.getString("ip_address", "192.168.0.56") // Valor por defecto
        val url = "http://$ip/recibir_estado"

        val body = FormBody.Builder()
            .add("estado", estado.toString()) // Convertimos el booleano a String
            .build()

        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        Thread {
            try {
                val response = client.newCall(request).execute()
                if (response.isSuccessful) {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Estado enviado con éxito", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Error al enviar estado", Toast.LENGTH_SHORT).show()
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

    private fun isValidIp(ip: String): Boolean {
        val regex =
            Regex("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")
        return regex.matches(ip)
    }

    private fun switchFrame(layout: View, frameName: String, animation: Int) {
        val frameContainer = findViewById<FrameLayout>(R.id.frame_container)
        frameContainer.removeAllViews()
        frameContainer.addView(layout)
        layout.startAnimation(AnimationUtils.loadAnimation(this, animation))
        currentFrame = frameName
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        event?.let { swipeListener.onTouch(it) }
        return super.onTouchEvent(event)
    }
}
