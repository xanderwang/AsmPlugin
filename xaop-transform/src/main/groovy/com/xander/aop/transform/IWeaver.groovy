package com.xander.aop.transform

public interface IWeaver {

  /**
   * Check a certain file is weavable*/
  public boolean isWeavableClass(String filePath) throws IOException

  /**
   * Weave single class to byte array*/
  public byte[] weaveSingleClassToByteArray(InputStream inputStream) throws IOException
}

