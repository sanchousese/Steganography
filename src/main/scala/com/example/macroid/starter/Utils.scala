package com.example.macroid.starter

import android.text.Editable

/**
  * Created by Sutula on 19.03.16
  */
object Utils {

  implicit def editableToString(e: Editable): String = e.toString

}
