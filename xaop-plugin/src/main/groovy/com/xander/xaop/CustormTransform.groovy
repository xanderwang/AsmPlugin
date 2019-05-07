package com.xander.xaop

import com.android.build.api.transform.DirectoryInput
import com.android.build.api.transform.Format
import com.android.build.api.transform.JarInput
import com.android.build.api.transform.QualifiedContent
import com.android.build.api.transform.Transform
import com.android.build.api.transform.TransformException
import com.android.build.api.transform.TransformInput
import com.android.build.api.transform.TransformInvocation
import com.android.build.api.transform.TransformOutputProvider
import com.android.build.gradle.internal.pipeline.TransformManager
import com.google.gson.Gson
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

public class CustormTransform extends Transform {

  private static final Set<QualifiedContent.Scope> SCOPES = new HashSet<>()

  static {
    SCOPES.add(QualifiedContent.Scope.PROJECT)
    SCOPES.add(QualifiedContent.Scope.SUB_PROJECTS)
    SCOPES.add(QualifiedContent.Scope.EXTERNAL_LIBRARIES)
  }

  Project project

  CustormTransform(Project project) {
    this.project = project
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
    return SCOPES
  }

  @Override
  boolean isIncremental() {
    // 开启增量编译
    return true
  }

  @Override
  boolean isCacheable() {
    return true
  }

  @Override
  Set<QualifiedContent.ContentType> getOutputTypes() {
    return super.getOutputTypes()
  }

  @Override
  Set<? super QualifiedContent.Scope> getReferencedScopes() {
    return TransformManager.EMPTY_SCOPES
  }

  @Override
  Map<String, Object> getParameterInputs() {
    return super.getParameterInputs()
  }

  @Override
  void transform(TransformInvocation transformInvocation)
      throws TransformException, InterruptedException, IOException {
    super.transform(transformInvocation)
    println(":----------------------------------------:start")
//    println("transform:" + new Gson().toJson(transformInvocation.context))
    // 这里是关键代码，就是如何处理字节码的，处理自己码我们需要 asm 工具
    // 当前是否是增量编译
    boolean isIncremental = transformInvocation.isIncremental();
    // 消费型输入，可以从中获取jar包和class文件夹路径。需要输出给下一个任务
    Collection<TransformInput> inputs = transformInvocation.getInputs();
    // 引用型输入，无需输出。
    Collection<TransformInput> referencedInputs = transformInvocation.getReferencedInputs();
    // OutputProvider管理输出路径，如果消费型输入为空，你会发现OutputProvider == null
    TransformOutputProvider outputProvider = transformInvocation.getOutputProvider();
    println("outputProvider:" + outputProvider.toString())
    if (!isIncremental) {
      // 如果不是增量编译，那么就清空旧的输出内容
      outputProvider.deleteAll()
    }
    for (TransformInput input : inputs) {
      for (JarInput jarInput : input.getJarInputs()) {
        File dest = outputProvider.getContentLocation(jarInput.getFile().getAbsolutePath(),
            jarInput.getContentTypes(),
            jarInput.getScopes(),
            Format.JAR)
        println("dest:${dest.getAbsolutePath()}")
        // 将修改过的字节码copy到dest，就可以实现编译期间干预字节码的目的了
        FileUtils.copyFile(jarInput.getFile(), dest);
      }
      for (DirectoryInput directoryInput : input.getDirectoryInputs()) {
        File dest = outputProvider.getContentLocation(directoryInput.getName(),
            directoryInput.getContentTypes(),
            directoryInput.getScopes(),
            Format.DIRECTORY)
        println("dest:${dest.getAbsolutePath()}")
        //将修改过的字节码copy到dest，就可以实现编译期间干预字节码的目的了
        FileUtils.copyDirectory(directoryInput.getFile(), dest)
      }
    }
    println(":----------------------------------------:end")
  }
}