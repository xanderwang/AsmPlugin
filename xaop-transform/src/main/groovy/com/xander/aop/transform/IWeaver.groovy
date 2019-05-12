package com.xander.aop.transform

interface IWeaver {

  /**
   * Check a certain file is weavable*/
  boolean isWeavableClass(String filePath) throws IOException

  /**
   * Weave single class to byte array*/
  byte[] weaveSingleClassToByteArray(InputStream inputStream) throws IOException
}

