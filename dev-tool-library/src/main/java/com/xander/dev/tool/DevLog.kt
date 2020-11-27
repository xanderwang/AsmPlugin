package com.xander.dev.tool

import android.util.Log
/**
 * @author Xander
 */

interface ILog {
  fun d(tag: String, msg: String)
  fun d(tag: String, msg: String, throwable: Throwable)
  fun e(tag: String, msg: String)
  fun e(tag: String, msg: String, throwable: Throwable)
}

object DevLog : ILog {

  override fun d(tag: String, msg: String) {
    Log.d(tag, msg)
  }

  override fun d(tag: String, msg: String, throwable: Throwable) {
    Log.d(tag, msg, throwable)
  }

  override fun e(tag: String, msg: String) {
    Log.e(tag, msg)
  }

  override fun e(tag: String, msg: String, throwable: Throwable) {
    Log.e(tag, msg, throwable)
  }

  fun cost(methodName: String, time: Long) {
    e(methodName, "cost time: $time ms")
  }
}