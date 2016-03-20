import android.util.Log
val image = Array(0, 1, 2, 3, 3, 3, 2, 3, 3, 3, 2, 3, 3, 3, 2, 3, 3, 3, 2, 3, 3, 3, 2, 3, 3, 3)
val addition = Array(87, 1)
val offset = 1
for {
  add <- addition
  bit <- (0 to 7).reverse
  b <- Some((add >>> bit) & 1)
} println(s"add: $add, bit: $bit, b: $b")
val bits =
  for {
    add <- addition
    bit <- (0 to 7).reverse
    b <- Some((add >>> bit) & 1)
  } yield b
bits.zipWithIndex.foreach{
  case (b, i) =>
    val currInd = offset + i
    println(s"currInd: $currInd, image(currInd): ${image(currInd)}, b: $b")
    image.update(currInd, ((image(currInd) & 0xFE) | b))
    println(s"image(currInd): ${image(currInd)}")
}
image.mkString(", ")
val gg = Array(-3223594, -2698282, -3223594, -2698282)
for {
  g <- Some(gg.head)
  bit <- (0 until 32).reverse
  b <- Some((g >>> bit) & 1)
}print(b)
println()
for {
  g <- Some(0xFFFFFFFE)
  bit <- (0 until 32).reverse
  b <- Some((g >>> bit) & 1)
}print(b)