package com.xander.aop.transform

public class XaopConfig {
  boolean log = false
  boolean debugSkip = false
  boolean releaseSkip = true
  boolean duplcatedClassSafeMode = false
  ArrayList<String> whiteList = new ArrayList<>()
  ArrayList<String> blackList = new ArrayList<>()

  @Override
  String toString() {
    return "debugSkip:${debugSkip},releaseSkip:${releaseSkip}"
  }
}