package com.xander.aop.transform

public class XaopConfig {
  boolean debugSkip = false
  boolean releaseSkip = true
  boolean duplcatedClassSafeMode = false;
  ArrayList<String> whitePackageList = new ArrayList<>()
  ArrayList<String> whiteMethodList = new ArrayList<>()

  @Override
  String toString() {
    return "debugSkip:${debugSkip},releaseSkip:${releaseSkip}"
  }
}