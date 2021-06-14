package com.example.android.kidsdrawingapp

import android.app.Activity
import android.app.Dialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.media.MediaScannerConnection
import android.os.AsyncTask
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.get
import com.example.android.kidsdrawingapp.databinding.ActivityMainBinding
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.lang.Exception
import java.util.jar.Manifest


class MainActivity : AppCompatActivity() {
    private var mImageButtonCurrentPaint:ImageButton?=null

    private lateinit var binding :ActivityMainBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        binding= ActivityMainBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.drawingView.setBrushSize(10.toFloat())

        mImageButtonCurrentPaint=binding.llPaintColors[0] as ImageButton
        mImageButtonCurrentPaint!!.setImageDrawable(
                ContextCompat.getDrawable(this,R.drawable.pallet_selected)
        )

        binding.ibBrush.setOnClickListener{
            showBrushSizeChooserDialog()
        }
        binding.ibGallery.setOnClickListener {
            if (isReadStorageAllowed()){

                val pickPhotoIntent=Intent(Intent.ACTION_PICK,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

                startActivityForResult(pickPhotoIntent, GALLERY)

            }else{
                requestStoragePermission()
            }
        }

        binding.ibUndo.setOnClickListener {
            binding.drawingView.onClickUndo()
        }
        binding.ibRedo.setOnClickListener {
            binding.drawingView.onClickRedo()
        }
        binding.ibSave.setOnClickListener {
            if(isReadStorageAllowed()){
                BitmaoAsyncTask(getBitmapFromView(binding.flDrawingViewContainer)).execute()
            }else {
                requestStoragePermission()
            }
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode==Activity.RESULT_OK){
            if(requestCode== GALLERY){
                try {if (data!!.data!=null){
                    binding.ivBackground.visibility=View.VISIBLE
                    binding.ivBackground.setImageURI(data.data)
                }else{
                    Toast.makeText(this,"Error in parsing the image or its corrupted",
                    Toast.LENGTH_SHORT).show()
                }
                }catch (e:Exception){
                    e.printStackTrace()
                }
            }
        }
    }

    private fun getBitmapFromView(view:View):Bitmap{

        val returnedBitmap=Bitmap.createBitmap(view.width,view.height,Bitmap.Config.ARGB_8888)
        val canvas= Canvas(returnedBitmap)
        val bgDrawable=view.background

        if (bgDrawable!=null){
            bgDrawable.draw(canvas)
        }else{
            canvas.drawColor(Color.WHITE)
        }
        view.draw(canvas)
        return returnedBitmap
    }


    private fun showBrushSizeChooserDialog(){
        val brushDialog=Dialog(this)
        brushDialog.setTitle("Brush Size: ")
        brushDialog.setContentView(R.layout.layout_brush_dialog)

        val smallBtn=brushDialog.findViewById<ImageButton>(R.id.ib_small_brush)
        smallBtn.setOnClickListener{
            binding.drawingView.setBrushSize(10.toFloat())
            brushDialog.dismiss()
        }

        val mediumBtn=brushDialog.findViewById<ImageButton>(R.id.ib_medium_brush)
        mediumBtn.setOnClickListener{
            binding.drawingView.setBrushSize(20.toFloat())
            brushDialog.dismiss()
        }

        val largeBtn=brushDialog.findViewById<ImageButton>(R.id.ib_large_brush)
        largeBtn.setOnClickListener{
            binding.drawingView.setBrushSize(30.toFloat())
            brushDialog.dismiss()
        }
        brushDialog.show()
    }

    fun paintClicked(view: View){
        if (view!=mImageButtonCurrentPaint){
            val imageButton=view as ImageButton
            val colorTag=imageButton.tag.toString()
            binding.drawingView.setColor(colorTag)
            imageButton.setImageDrawable(
                    ContextCompat.getDrawable(this,R.drawable.pallet_selected)
            )
            mImageButtonCurrentPaint!!.setImageDrawable(
                    ContextCompat.getDrawable(this,R.drawable.pallet_normal)
            )
            mImageButtonCurrentPaint=view
        }
    }

   private fun requestStoragePermission(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                        arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE).toString())){
            Toast.makeText(this,"Need permission to add background",Toast.LENGTH_SHORT).show()
        }
       ActivityCompat.requestPermissions(this,arrayOf(android.Manifest.permission.READ_EXTERNAL_STORAGE,
               android.Manifest.permission.WRITE_EXTERNAL_STORAGE), STORAGE_PERMISSION_CODE)
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode== STORAGE_PERMISSION_CODE){
            if (grantResults.isNotEmpty()&&grantResults[0]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Now you can access the storage",
                        Toast.LENGTH_SHORT).show()

            }else{
                Toast.makeText(this,"Oops you just denied the permission",
                    Toast.LENGTH_SHORT).show()

            }
        }
    }


    private fun isReadStorageAllowed():Boolean{
        val permissionResult=ContextCompat.checkSelfPermission(this,android.Manifest.permission.READ_EXTERNAL_STORAGE)
        return permissionResult==PackageManager.PERMISSION_GRANTED
    }


    private inner class BitmaoAsyncTask(val mBitmap:Bitmap): AsyncTask<Any,Void,String>(){

        private lateinit var mProgressDialog:Dialog

        override fun onPreExecute() {
            super.onPreExecute()
            showProgressDialog()
        }

        override fun doInBackground(vararg params: Any?): String {
            var result=""

            if (mBitmap!=null){
                try{
                    val bytes=ByteArrayOutputStream()
                    mBitmap.compress(Bitmap.CompressFormat.PNG,90,bytes)
                    val f=File(externalCacheDir!!.absoluteFile.toString()
                            +File.separator+"KidDrawingApp_"
                            +System.currentTimeMillis()/1000+".png")
                    val fos=FileOutputStream(f)
                    fos.write(bytes.toByteArray())
                    fos.close()
                    return f.absolutePath

                }catch (e:Exception){
                    result=""
                    e.printStackTrace()
                }
            }
            return result
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            cancelProgressDialog()
            if (result!!.isNotEmpty()){
                Toast.makeText(this@MainActivity,
                "File saved successfully",Toast.LENGTH_SHORT).show()
            }else{
                Toast.makeText(this@MainActivity,
                  "Something happened while saving the file",Toast.LENGTH_SHORT)
                   .show()
            }
            MediaScannerConnection.scanFile(this@MainActivity, arrayOf(result),null){
                path,uri->val shareIntent=Intent()
                shareIntent.action=Intent.ACTION_SEND
                shareIntent.putExtra(Intent.EXTRA_STREAM,uri)
                shareIntent.type="image/png"

                startActivity(
                        Intent.createChooser(
                                shareIntent,"Share"
                        )
                )
            }

        }

        private fun showProgressDialog(){
            mProgressDialog= Dialog(this@MainActivity)
            mProgressDialog.setContentView(R.layout.dialog_custom_progress)
            mProgressDialog.show()
        }

        private fun cancelProgressDialog(){
            mProgressDialog.dismiss()
        }
    }


    companion object{
        private const val STORAGE_PERMISSION_CODE=1
        private const val GALLERY=2
    }
}