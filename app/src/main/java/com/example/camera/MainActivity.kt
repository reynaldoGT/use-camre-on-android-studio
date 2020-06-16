package com.example.camera

import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.Image
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.FileProvider
import java.io.File
import java.lang.String.format
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {
    val SOLICITUD_TOMAR_FOTO = 1
    val permiso_camera = android.Manifest.permission.CAMERA
    val permisoWriteStorage = android.Manifest.permission.WRITE_EXTERNAL_STORAGE
    val permisoReadStorage = android.Manifest.permission.READ_EXTERNAL_STORAGE

    var ivFoto: ImageView? = null

    var urlFotoActual = ""

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnFoto = findViewById<Button>(R.id.btnTomarFoto)
        ivFoto = findViewById(R.id.ivFoto)
        btnFoto.setOnClickListener() {
            pedirPermisos()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun pedirPermisos() {
        val deboProverContext =
            ActivityCompat.shouldShowRequestPermissionRationale(this, permiso_camera)
        if (deboProverContext) {
            solicitudPermiso()
        } else {
            solicitudPermiso()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    fun solicitudPermiso() {
        requestPermissions(
            arrayOf(permiso_camera, permisoWriteStorage, permisoReadStorage),
            SOLICITUD_TOMAR_FOTO
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            SOLICITUD_TOMAR_FOTO -> {
                if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    desparaIntentTomarFoto()
                } else {
                    Toast.makeText(
                        this,
                        "No diste permiso para acceder a la cámara y almacenamiento",
                        Toast.LENGTH_LONG
                    ).show()

                }
            }

        }
    }

    fun desparaIntentTomarFoto() {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

        if (intent.resolveActivity(packageManager) != null) {
            var archivoFofo: File? = null
            archivoFofo = crearArchivoImagen()
            if (archivoFofo != null) {
                val urlFoto = FileProvider.getUriForFile(this, "com.example.camera", archivoFofo)

                intent.putExtra(MediaStore.EXTRA_OUTPUT, urlFoto)
                startActivityForResult(intent, SOLICITUD_TOMAR_FOTO)
            }


        }
    }

    // cuando terminos de interactuar con el acivty de la camara
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            SOLICITUD_TOMAR_FOTO -> {
                if (resultCode == Activity.RESULT_OK) {
                    //Obtener imagen
                    //val extras = data?.extras
                    //val imagenBitMap = extras!!.get("data") as Bitmap
                    val uri = Uri.parse(urlFotoActual)
                    val stream = contentResolver.openInputStream(uri)
                    val imagenBitMap = BitmapFactory.decodeStream(stream)

                    ivFoto!!.setImageBitmap(imagenBitMap)
                    addImageGalery()
                } else {
                    //cancelp ña captura
                }
            }
        }
    }

    fun crearArchivoImagen(): File {
        val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())

        val nombreArchivoImagen = "JPEG_" + timeStamp + "_"

        //val directorio = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        val directorio = Environment.getExternalStorageDirectory()
        val directorioPicture = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        val imagen = File.createTempFile(nombreArchivoImagen, ".jpg", directorioPicture)
        urlFotoActual = "file://" + imagen.absolutePath

        return imagen
    }

    fun addImageGalery() {
        val intent = Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE)
        val file = File(urlFotoActual)
        val uri = Uri.fromFile(file)
        intent.setData(uri)
        this.sendBroadcast(intent)

    }
}