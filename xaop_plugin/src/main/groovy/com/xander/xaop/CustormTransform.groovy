package com.xander.xaop

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager

public class CustormTransform extends Transform {

  CustormTransform() {
    super()
  }

  @Override
  String getName() {
    return "CustormTransform"
  }

  @Override
  Set<QualifiedContent.ContentType> getInputTypes() {
    return TransformManager.CONTENT_CLASS
  }

  @Override
  Set<? super QualifiedContent.Scope> getScopes() {
    return TransformManager.SCOPE_FULL_PROJECT
  }

  @Override
  boolean isIncremental() {
    return true
  }

  @Override
  Set<QualifiedContent.ContentType> getOutputTypes() {
    return super.getOutputTypes()
  }

  @Override
  void transform(TransformInvocation transformInvocation)
      throws TransformException, InterruptedException, IOException {
    super.transform(transformInvocation)
    // 这里是关键代码，就是如何处理字节码的，处理自己码我们需要 asm 工具
    //当前是否是增量编译
    boolean isIncremental = transformInvocation.isIncremental();
    //消费型输入，可以从中获取jar包和class文件夹路径。需要输出给下一个任务
    Collection<TransformInput> inputs = transformInvocation.getInputs();
    //引用型输入，无需输出。
    Collection<TransformInput> referencedInputs = transformInvocation.getReferencedInputs();
    //OutputProvider管理输出路径，如果消费型输入为空，你会发现OutputProvider == null
    TransformOutputProvider outputProvider = transformInvocation.getOutputProvider();
    for(TransformInput input : inputs) {
      for (JarInput jarInput : input.getJarInputs()) {
        File dest = outputProvider.getContentLocation(jarInput.getFile().getAbsolutePath(),
            jarInput.getContentTypes(),
            jarInput.getScopes(),
            Format.JAR);
        //将修改过的字节码copy到dest，就可以实现编译期间干预字节码的目的了
        FileUtils.copyFile(jarInput.getFile(), dest);
      }
      for (DirectoryInput directoryInput : input.getDirectoryInputs()) {
        File dest = outputProvider.getContentLocation(directoryInput.getName(),
            directoryInput.getContentTypes(), directoryInput.getScopes(),
            Format.DIRECTORY);
        //将修改过的字节码copy到dest，就可以实现编译期间干预字节码的目的了
        FileUtils.copyDirectory(directoryInput.getFile(), dest);
      }
    }
  }


}