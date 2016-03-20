import android.util.Log

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
      image(currInd) = (image(currInd) & 0xFE) | b
  }

  image
}

def encodeLen(image: Array[Int], len: Int, offset: Int): Array[Int] = {
  val lenBit = for {
    bit <- (0 until 32).reverse
    b <- Some((len >>> bit) & 1)
  } yield b

  lenBit.zipWithIndex.foreach {
    case (b, i) =>
      val currInd = offset + i
      image(currInd) = (image(currInd) & 0xFE) | b
  }
  image
}

def decodeText(image: Array[Int]): Array[Byte] = {
  val offset = 32
  var length = 0
  image.take(offset)
    .map(_ & 1)
    .foreach(b => length = (length << 1) | b)

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

val image = Array(0, 1, 2, 3, 3, 3, 2, 3, 3, 3, 2, 3, 3, 3, 2, 3, 3, 3, 2, 3, 3, 3, 2, 3, 3, 3, 0, 1, 2, 3, 3, 3, 2, 3, 3, 3, 2, 3, 3, 3, 2, 3, 3, 3, 2, 3, 3, 3, 2, 3, 3, 3, 0, 1, 2, 3, 3, 3, 2, 3, 3, 3, 2, 3, 3, 3, 2, 3, 3, 3, 2, 3, 3, 3, 2, 3, 3, 3, 0, 1, 2, 3, 3, 3, 2, 3, 3, 3, 2, 3, 3, 3, 2, 3, 3, 3, 2, 3, 3, 3, 2, 3, 3, 3)
val msg = "gogi".getBytes.map(_.toInt)
val len = msg.length


encodeLen(image, len, 0)
encodeText(image, msg, 32)

new String(decodeText(image))

