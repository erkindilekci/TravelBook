package com.erkindilekci.travelbook

import android.content.Intent
import android.content.pm.PackageManager
import android.database.sqlite.SQLiteDatabase
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.erkindilekci.travelbook.databinding.ActivityDetailBinding
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream
import java.lang.reflect.Executable

class DetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityDetailBinding

    private lateinit var galleryResultLauncher: ActivityResultLauncher<Intent>
    private lateinit var permissionResultLauncher: ActivityResultLauncher<String>

    var selectedBitmap : Bitmap? = null

    private lateinit var database: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        database = this.openOrCreateDatabase("Places", MODE_PRIVATE, null)



        registerLauncher()

        val intent = intent
        val info = intent.getStringExtra("info")
        if (info.equals("new")){
            binding.cityText.setText("")
            binding.nameText.setText("")
            binding.countryText.setText("")
            binding.button.visibility = View.VISIBLE
            binding.deleteButton.visibility = View.INVISIBLE
            binding.imageView.setImageResource(R.drawable.selectimage)
        } else {
            binding.deleteButton.visibility = View.VISIBLE
            binding.button.visibility = View.INVISIBLE
            val selectedId = intent.getIntExtra("id",0)

            val cursor = database.rawQuery("SELECT * FROM places WHERE id = ?", arrayOf(selectedId.toString()))

            val placeNameIndex = cursor.getColumnIndex("placename")
            val cityNameIndex = cursor.getColumnIndex("cityname")
            val countryNameIndex = cursor.getColumnIndex("countryname")
            val imageIndex = cursor.getColumnIndex("image")

            while (cursor.moveToNext()){
                binding.nameText.setText(cursor.getString(placeNameIndex))
                binding.cityText.setText(cursor.getString(cityNameIndex))
                binding.countryText.setText(cursor.getString(countryNameIndex))

                val byteArray = cursor.getBlob(imageIndex)
                val bitmap = BitmapFactory.decodeByteArray(byteArray, 0,byteArray.size)
                binding.imageView.setImageBitmap(bitmap)
            }
            cursor.close()
        }

    }

    fun saveClicked(view: View){
        val countryName = binding.countryText.text.toString()
        val cityName = binding.cityText.text.toString()
        val placeName = binding.nameText.text.toString()


        if (selectedBitmap != null){

            val smallBitmap = makeSmallerBitmap(selectedBitmap!!, 300)
            val outputStream = ByteArrayOutputStream()
            smallBitmap.compress(Bitmap.CompressFormat.PNG, 50, outputStream)
            val byteArray = outputStream.toByteArray()

            try {
                database.execSQL("CREATE TABLE IF NOT EXISTS places (id INTEGER PRIMARY KEY, placename VARCHAR, cityname VARCHAR, countryname VARCHAR, image BLOB)")
                val sqlString = "INSERT INTO places (placename, cityname, countryname, image) VALUES (?, ?, ?, ?)"
                val statement = database.compileStatement(sqlString)
                statement.bindString(1, placeName)
                statement.bindString(2, cityName)
                statement.bindString(3, countryName)
                statement.bindBlob(4, byteArray)
                statement.execute()


            } catch (e: Exception){
                e.printStackTrace()
            }


            val intent = Intent(this, MainActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            startActivity(intent)


        }


    }

    fun delete(view: View){

        try {
            val selectedId = intent.getIntExtra("id",0)
            database.execSQL("DELETE FROM places WHERE id = ?", arrayOf(selectedId.toString()))


        } catch (e: Exception){
            e.printStackTrace()
        }
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }


    fun makeSmallerBitmap(selectedImage: Bitmap, maximumSize: Int) : Bitmap{
        var height = selectedImage.height
        var width = selectedImage.width

        val bitmapRatio : Double = width.toDouble() / height.toDouble()

        if(bitmapRatio>1){
            //landscape
            width = maximumSize
            val smallHeight = width/bitmapRatio
            height = smallHeight.toInt()
        } else{
            //portrait
            height = maximumSize
            val smallWidth = height*bitmapRatio
            width = smallWidth.toInt()
        }

        return Bitmap.createScaledBitmap(selectedImage,width,height,true)
    }


    fun imageClicked(view: View){
        if(ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)){
                // Rationale
                Snackbar.make(view, "Permission needed for gallery", Snackbar.LENGTH_INDEFINITE).setAction("Give Permission", View.OnClickListener {
                 // Request Permission
                    permissionResultLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                }).show()
            } else {
                // Request Permission
                permissionResultLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        } else{
            // Permission granted intent to gallery
            val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            galleryResultLauncher.launch(intentToGallery)
        }
    }

    private fun registerLauncher(){
        galleryResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == RESULT_OK){
                val intentFromResult = result.data
                if (intentFromResult != null){
                    val imageData = intentFromResult.data
                    //binding.imageView.setImageURI(imageData)
                    if (imageData != null) {
                        try {
                            if (Build.VERSION.SDK_INT >= 28){
                                val source = ImageDecoder.createSource(this@DetailActivity.contentResolver, imageData)
                                selectedBitmap = ImageDecoder.decodeBitmap(source)
                                binding.imageView.setImageBitmap(selectedBitmap)
                            } else {
                                selectedBitmap = MediaStore.Images.Media.getBitmap(contentResolver, imageData)
                                binding.imageView.setImageBitmap(selectedBitmap)
                            }
                        } catch (e: Exception){
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
        permissionResultLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ result ->
            if (result){
                //permission granted
                val intentToGallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                galleryResultLauncher.launch(intentToGallery)
            } else{
                //permission denied
                Toast.makeText(this@DetailActivity, "Permission Needed!", Toast.LENGTH_LONG).show()
            }
        }
    }
}