package com.xander.plugin.asm.lib

open class PluginConfig {
  var log = true
  var classLog = false
  var methodLog = false
  var debugSkip = false
  var releaseSkip = false
  var skipJar = true
  var timeAnnotation: String = "com.xander.dev.tool.DevTime"
  var useExecutor = false


  companion object {
    val debug = PluginConfig()
  }

  override fun toString(): String {
    return "PluginConfig(log=$log, classLog=$classLog, methodLog=$methodLog, debugSkip=$debugSkip, releaseSkip=$releaseSkip, skipJar=$skipJar, timeAnnotation='$timeAnnotation', useExecutor=$useExecutor)"
  }

}