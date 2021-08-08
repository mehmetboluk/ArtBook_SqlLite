package com.example.artbook_sqllite


import android.Manifest
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
import com.example.artbook_sqllite.databinding.ActivityDetailsBinding
import com.google.android.material.snackbar.Snackbar
import java.io.ByteArrayOutputStream
import java.lang.Exception


class DetailsActivity : AppCompatActivity() {

    private lateinit var binding : ActivityDetailsBinding
    private lateinit var activityResultLauncher : ActivityResultLauncher<Intent>
    private lateinit var permissionLauncher : ActivityResultLauncher<String>
    var selectedBitMap : Bitmap? = null
    private lateinit var database : SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailsBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)

        database = this.openOrCreateDatabase("Arts", MODE_PRIVATE, null)

        registerLauncher()

        val intent = intent
        val info = intent.getStringExtra("info")
        if(info.equals("new")){
            binding.etArtName.setText("")
            binding.etArtistName.setText("")
            binding.etYear.setText("")
            binding.btnSave.visibility = View.VISIBLE
            binding.imageView.setImageResource(R.drawable.save_icon)
        }else{
            binding.btnSave.visibility= View.INVISIBLE
            val selectedId = intent.getIntExtra("id", 1)
            val cursor = database.rawQuery("SELECT * FROM arts WHERE id = ?", arrayOf(selectedId.toString()))

            val artnameIx = cursor.getColumnIndex("artname")
            val artistNameIx = cursor.getColumnIndex("artistName")
            val yearIx = cursor.getColumnIndex("yearArt")
            val imageIx = cursor.getColumnIndex("image")

            while (cursor.moveToNext()){
                binding.etArtName.setText(cursor.getString(artnameIx))
                binding.etArtistName.setText(cursor.getString(artistNameIx))
                binding.etYear.setText(cursor.getString(yearIx))

                val byteArray = cursor.getBlob(imageIx)
                val bitmap = BitmapFactory.decodeByteArray(byteArray,0,byteArray.size)
                binding.imageView.setImageBitmap(bitmap)

            }
            cursor.close()
        }
    }

    fun saveBtnClicked(view: View){

        val nameArt = binding.etArtName.text.toString()
        val nameArtist = binding.etArtistName.text.toString()
        val yearArt = binding.etYear.text.toString()

        if(selectedBitMap != null){
            val smallBitMap = makeSmallerBitMap(selectedBitMap!!, 300)

            val outputStream = ByteArrayOutputStream()
            smallBitMap.compress(Bitmap.CompressFormat.PNG,50,outputStream)
            val byteArray = outputStream.toByteArray()

            try {
                database.execSQL("CREATE TABLE IF NOT EXISTS arts (id PRIMARY KEY, artname VARCHAR, artistName VARCHAR, yearArt VARCHAR, image BLOB)")
                val sqlString = "INSERT INTO arts (artname, artistName, yearArt, image) VALUES (?,?,?,?)"
                val statement = database.compileStatement(sqlString)
                statement.bindString(1,nameArt)
                statement.bindString(2,nameArtist)
                statement.bindString(3,yearArt)
                statement.bindBlob(4, byteArray)
                statement.execute()

            }catch (e: Exception){
                e.printStackTrace()
            }
        }

        val intent = Intent(this@DetailsActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)


    }

    private fun makeSmallerBitMap(image : Bitmap, maximumSize : Int) : Bitmap{
        var width = image.width
        var height = image.height

        val bitMapRatio : Double = width.toDouble() / height.toDouble()

        if (bitMapRatio > 1 ){
            //landscape
            width = maximumSize
            val scaledHeight = width / bitMapRatio
            height = scaledHeight.toInt()
        }else{
            //portrait
            height = maximumSize
            val scaledWidth = height * bitMapRatio
            width = scaledWidth.toInt()
        }
        return Bitmap.createScaledBitmap(image,width,height,true)
    }

    fun selectImage(view : View){

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)){
                //rational
                Snackbar.make(view, "Permission is needed to access Gallery",Snackbar.LENGTH_INDEFINITE).setAction("Give Permission",View.OnClickListener {
                    // request permission
                    permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
                }).show()
            }else{
                //request permission
                permissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            }
        }else{
            //intent
            val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            activityResultLauncher.launch(intentToGallery)
        }

    }

    private fun registerLauncher(){
    activityResultLauncher= registerForActivityResult(ActivityResultContracts.StartActivityForResult()){ result ->
        if(result.resultCode == RESULT_OK){
            val intentFromResult = result.data
            if(intentFromResult != null){
                val imageData = intentFromResult.data
                if(imageData != null) {
                    try {
                        if(Build.VERSION.SDK_INT >= 28) {
                            val source = ImageDecoder.createSource(this@DetailsActivity.contentResolver, imageData)
                            selectedBitMap = ImageDecoder.decodeBitmap(source)
                            binding.imageView.setImageBitmap(selectedBitMap)
                        }
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }
        }
    }
        permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ result ->
            if(result){
                //permission granted
                val intentToGallery = Intent(Intent.ACTION_PICK,MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                activityResultLauncher.launch(intentToGallery)
            }else{
                //permission denied
                Toast.makeText(this, "Permission Needed!", Toast.LENGTH_LONG).show()
            }

        }

    }
}