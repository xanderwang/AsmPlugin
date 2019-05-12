package com.xander.aop.transform

class XaopConfig {
  boolean log = false
  boolean skipJar = true
  boolean debugSkip = false
  boolean releaseSkip = true
  boolean duplcatedClassSafeMode = false
  ArrayList<String> whiteList = new ArrayList<>()
  ArrayList<String> blackList = new ArrayList<>()

  @Override
  String toString() {
    return "{log:${log},debugSkip:${debugSkip},releaseSkip:${releaseSkip}},skipJar:${skipJar}," +
      ",duplcatedClassSafeMode:${duplcatedClassSafeMode},whiteList:${whiteList},blackList:${blackList}}"
  }
}