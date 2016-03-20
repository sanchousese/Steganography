package com.example.macroid.starter

import java.io._
import java.nio.ByteBuffer

import android.content.{ContentValues, Intent}
import android.net.Uri
import android.provider.MediaStore
import android.provider.MediaStore.Images.Media
import android.provider.MediaStore.{Images, MediaColumns}
import android.text.Editable
import android.util.Log
import android.view.View.OnClickListener

import scala.language.postfixOps

import android.os.{Environment, Bundle}
import android.widget._
import android.view.ViewGroup.LayoutParams._
import android.view.{Gravity, View}
import android.app.Activity
import android.graphics.{Bitmap, BitmapFactory, Color}

// import macroid stuff
import macroid._
import macroid.FullDsl._
import macroid.contrib._
import macroid.viewable._

import scala.concurrent.ExecutionContext.Implicits.global

import scala.collection.JavaConversions._
import Utils._
import Steganography._

class MainActivity extends Activity with Contexts[Activity] {
  implicit val ctx = this

  val TAG = "MainActivity"

  val PICK_PHOTO_GALLERY = 123
  val PICK_PHOTO_CAMERA = 234

  lazy val etEncryptText = findViewById(R.id.etEncryptText).asInstanceOf[EditText]
  lazy val etDecryptText = findViewById(R.id.etDecryptedText).asInstanceOf[EditText]

  lazy val btnEncrypt = findViewById(R.id.btnEncrypt).asInstanceOf[Button]
  lazy val btnDecrypt = findViewById(R.id.btnDecrypt).asInstanceOf[Button]

  lazy val imageView = findViewById(R.id.imageView).asInstanceOf[ImageView]
  lazy val imageViewOut = findViewById(R.id.imageViewOut).asInstanceOf[ImageView]

  lazy val btnSave = findViewById(R.id.btnSave).asInstanceOf[Button]

  var maybeBitmap: Option[Bitmap] = _
  var dest: File = _

  override def onCreate(savedInstanceState: Bundle) = {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    runUi(imageView <~ On.click(
      dialog("Choose Image")
        <~ positive("Camera")(Ui {
        val intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        startActivityForResult(intent, PICK_PHOTO_CAMERA)
      })
        <~ negative("Gallery")(Ui {
        val intent = new Intent(Intent.ACTION_GET_CONTENT)
        intent.setType("image/*")
        startActivityForResult(intent, PICK_PHOTO_GALLERY)
      })
        <~ speak)
    )

    runUi(btnEncrypt <~ On.click(Ui {
      if (etEncryptText.getText.toString.nonEmpty) {
        maybeBitmap match {
          case Some(bitmap) =>
            val resBitmap: Bitmap = encrypt(bitmap, etEncryptText.getText)
            maybeBitmap = Some(resBitmap)
            imageViewOut.setImageBitmap(resBitmap)
          case _ =>
            runUi(
              dialog("Please add image")
                <~ negativeCancel(Ui {})
                <~ speak
            )
        }
      } else {
        runUi(
          dialog("Please add the text")
            <~ negativeCancel(Ui {})
            <~ speak
        )
      }
    }))

    runUi(btnDecrypt <~ On.click(Ui {
      maybeBitmap match {
        case Some(bitmap) =>
          etDecryptText.setText(decrypt(bitmap))
        case _ =>
          runUi(
            dialog("Please add image")
              <~ negativeCancel(Ui {})
              <~ speak
          )
      }
    }))

    runUi(btnSave <~ On.click(Ui{
      maybeBitmap match {
        case Some(bitmap) =>
          val destination = new File(Environment.getExternalStorageDirectory, System.currentTimeMillis() + ".png")
          val fos = new FileOutputStream(destination)

          bitmap.compress(Bitmap.CompressFormat.PNG, 100, fos)
          fos.flush()
          fos.close()
          MediaStore.Images.Media.insertImage(getContentResolver,
            destination.getAbsolutePath, destination.getName, destination.getName)

          val options = new BitmapFactory.Options()
          options.inPreferredConfig = Bitmap.Config.ARGB_8888
          Log.d(TAG, s"destination: ${destination.getAbsolutePath} : ${destination.getCanonicalPath}")
          val btmD = BitmapFactory.decodeStream(new FileInputStream(destination), null, options)
          etDecryptText.setText(decrypt(btmD))

          dest = destination
        case _ =>
          runUi(
            dialog("Please add image")
              <~ negativeCancel(Ui {})
              <~ speak
          )
      }
    }))
  }

  override def onActivityResult(requestCode: Int, resultCode: Int, data: Intent): Unit = {

    super.onActivityResult(requestCode, resultCode, data)

    if (resultCode == Activity.RESULT_OK) {
      if (data == null) {
        Log.d(TAG, "Error")
        return
      }
      requestCode match {
        case PICK_PHOTO_GALLERY =>
          maybeBitmap match {
            case Some(bitmap) =>
              val options = new BitmapFactory.Options()
              options.inPreferredConfig = Bitmap.Config.ARGB_8888
              Log.d(TAG, s"destination: ${dest.getAbsolutePath} : ${dest.getCanonicalPath}")
              val btmD = BitmapFactory.decodeStream(new FileInputStream(dest), null, options)
              maybeBitmap = Some(btmD)
            case _ =>
              val selectedImage = data.getData
              val filePathColumn: Array[String] = Array(MediaColumns.DATA)

              val cursor = getContentResolver.query(selectedImage, filePathColumn, null, null, null)
              cursor.moveToFirst

              val columnIndex: Int = cursor.getColumnIndex(filePathColumn(0))
              val picturePath: String = cursor.getString(columnIndex)
              cursor.close

              val options = new BitmapFactory.Options()
              options.inPreferredConfig = Bitmap.Config.ARGB_8888
              maybeBitmap = Some(BitmapFactory.decodeFile(picturePath, options))
              Log.d(TAG, s"destination: ${picturePath} : ${picturePath}")

          }

          imageView.setImageBitmap(maybeBitmap.get)
        case PICK_PHOTO_CAMERA =>
          maybeBitmap = Some(data.getExtras.get("data").asInstanceOf[Bitmap])
          imageView.setImageBitmap(maybeBitmap.get)
      }
    }

  }
}
