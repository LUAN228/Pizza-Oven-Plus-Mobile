package com.pizza.oven

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.io.File
import java.io.FileOutputStream

class MainActivity : AppCompatActivity() {

    private var fileBaseUri: Uri? = null
    private var filePatchUri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Criar interface simples via código sem precisar de XML complexo
        val layout = android.widget.LinearLayout(this).apply {
            orientation = android.widget.LinearLayout.VERTICAL
            setPadding(50, 50, 50, 50)
        }

        val statusText = TextView(this).apply {
            text = "Pizza Oven Mobile - Selecione os arquivos"
            textSize = 18f
            setPadding(0, 0, 0, 40)
        }

        val btnSelectBase = Button(this).apply {
            text = "1. Selecionar Arquivo do Jogo (data.win)"
            setOnClickListener { openFilePicker(1001) }
        }

        val btnSelectPatch = Button(this).apply {
            text = "2. Selecionar Mod (.xdelta)"
            setOnClickListener { openFilePicker(1002) }
        }

        val btnApply = Button(this).apply {
            text = "3. Aplicar Mod!"
            setOnClickListener {
                if (fileBaseUri != null && filePatchUri != null) {
                    applyXdeltaPatch(statusText)
                } else {
                    Toast.makeText(this@MainActivity, "Selecione os dois arquivos antes!", Toast.LENGTH_SHORT).show()
                }
            }
        }

        layout.addView(statusText)
        layout.addView(btnSelectBase)
        layout.addView(btnSelectPatch)
        layout.addView(btnApply)

        setContentView(layout)
    }

    private fun openFilePicker(requestCode: Int) {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
        }
        startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && data != null) {
            when (requestCode) {
                1001 -> {
                    fileBaseUri = data.data
                    Toast.makeText(this, "Jogo Selecionado!", Toast.LENGTH_SHORT).show()
                }
                1002 -> {
                    filePatchUri = data.data
                    Toast.makeText(this, "Mod Selecionado!", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun applyXdeltaPatch(statusView: TextView) {
        statusView.text = "Aplicando mod, aguarde..."
        
        // A lógica do Xdelta roda em segundo plano para não travar a tela
        Thread {
            try {
                val fileBase = getFileFromUri(fileBaseUri!!)
                val filePatch = getFileFromUri(filePatchUri!!)
                val fileOut = File(cacheDir, "data_modificado.win")

                // Executa a aplicação do patch xdelta
                com.fuzziebrain.xdelta.XDelta.patch(
                    fileBase.absolutePath,
                    filePatch.absolutePath,
                    fileOut.absolutePath
                )

                runOnUiThread {
                    statusView.text = "Sucesso! Salvo em:\n${fileOut.absolutePath}"
                    Toast.makeText(this, "Mod aplicado com sucesso!", Toast.LENGTH_LONG).show()
                }
            } catch (e: Exception) {
                runOnUiThread {
                    statusView.text = "Erro ao aplicar: ${e.message}"
                    Toast.makeText(this, "Erro: ${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }.start()
    }

    private fun getFileFromUri(uri: Uri): File {
        val inputStream = contentResolver.openInputStream(uri)!!
        val tempFile = File.createTempFile("temp", null, cacheDir)
        val outputStream = FileOutputStream(tempFile)
        inputStream.copyTo(outputStream)
        inputStream.close()
        outputStream.close()
        return tempFile
    }
}
