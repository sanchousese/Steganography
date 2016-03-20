package com.example.macroid.starter

import android.graphics.Bitmap
import android.util.Log

/**
  * Created by Sutula on 19.03.16
  */
object Steganography {

  val TAG = "Steganography"

  def encrypt(bitmap: Bitmap, text: String) = {
    val muttableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    val pixels: Array[Int] = new Array[Int](muttableBitmap.getHeight * muttableBitmap.getWidth)
    muttableBitmap.getPixels(pixels, 0, muttableBitmap.getWidth, 0, 0, muttableBitmap.getWidth, muttableBitmap.getHeight)

    val msg = text.getBytes.map(_.toInt)
    val len = msg.length

    Log.d(TAG, s"msg: ${msg.mkString(", ")}")
    Log.d(TAG, s"len: $len")

    encodeLen(pixels, len, 0)
    encodeText(pixels, msg, 32)

    muttableBitmap.setPixels(pixels, 0, muttableBitmap.getWidth, 0, 0, muttableBitmap.getWidth, muttableBitmap.getHeight)
    muttableBitmap
  }

  def decrypt(bitmap: Bitmap) = {
    val muttableBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
    val pixels: Array[Int] = new Array[Int](muttableBitmap.getHeight * muttableBitmap.getWidth)
    muttableBitmap.getPixels(pixels, 0, muttableBitmap.getWidth, 0, 0, muttableBitmap.getWidth, muttableBitmap.getHeight)

    val res: String = new Predef.String(decodeText(pixels))
    Log.d(TAG, s"res: $res")
    res
  }

  def encodeText(image: Array[Int], addition: Array[Int], offset: Int): Array[Int] = {
    if (addition.length * 8 + offset > image.length) {
      throw new IllegalArgumentException("File not long enough!")
    }

    val bits = for {
      add <- addition
      bit <- (0 until 8).reverse
      b <- Some((add >>> bit) & 1)
    } yield b

    bits.zipWithIndex.foreach {
      case (b, i) =>
        val currInd = offset + i
        image(currInd) = (image(currInd) & 0xFFFFFFFE) | b
    }

    image
  }

  def encodeLen(image: Array[Int], len: Int, offset: Int): Array[Int] = {
    val lenBit = for {
      bit <- (0 until 32).reverse
      b <- Some((len >>> bit) & 1)
    } yield b

    Log.d(TAG, s"lenBit: ${lenBit.mkString(", ")}")

    Log.d(TAG, s"imageBefore: ${image.take(32).mkString(", ")}")
    lenBit.zipWithIndex.foreach {
      case (b, i) =>
        val currInd = offset + i
        image(currInd) = (image(currInd) & 0xFFFFFFFE) | b
    }
    Log.d(TAG, s"imageAfter: ${image.take(32).mkString(", ")}")
    image
  }

  def decodeText(image: Array[Int]): Array[Byte] = {
    val offset = 32
    var length = 0

    Log.d(TAG, s"image.take: ${image.take(offset).mkString(", ")}")
    Log.d(TAG, s"image.take.map: ${image.take(offset).map(_ & 1).mkString(", ")}")
    image.take(offset)
      .map(_ & 1)
      .foreach(b => length = (length << 1) | b)

    Log.d(TAG, s"length: $length")

    val result = Array.fill(length)(0.toByte)
    val indexes = for {
      i <- 0 until length
      b <- 0 to 7
      currInd <- Some(i * 8 + b + offset)
    } yield (i, currInd)

    indexes.foreach {
      case (byte, ind) =>
        result.update(byte, ((result(byte) << 1) | (image(ind) & 1)).toByte)
    }

    result
  }
}
