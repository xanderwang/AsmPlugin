package com.xander.aop.transform

import com.android.build.api.transform.*
import com.android.build.gradle.internal.pipeline.TransformManager
import com.android.ide.common.internal.WaitableExecutor
import com.google.common.io.Files
import org.apache.commons.io.FileUtils
import org.gradle.api.Project

public class XaopTransform extends Transform {

  private static final Set<QualifiedContent.Scope> SCOPES = new HashSet<>()

  static {
    SCOPES.add(QualifiedContent.Scope.PROJECT)
    SCOPES.add(QualifiedContent.Scope.SUB_PROJECTS)
    SCOPES.add(QualifiedContent.Scope.EXTERNAL_LIBRARIES)
  }

  private Project project
  // 配置
  protected XaopConfig config = new XaopConfig()
  // 用来编辑字节码
  protected BaseWeaver weaver
  // 多任务
  private WaitableExecutor waitableExecutor

  public XaopTransform(Project project) {
    this.project = project
    this.waitableExecutor = WaitableExecutor.useGlobalSharedThreadPool()
  }

  @Override
  public String getName() {
    return this.getClass().getSimpleName()
  }

  @Override
  public Set<QualifiedContent.ContentType> getInputTypes() {
    return TransformManager.CONTENT_CLASS
  }

  @Override
  public Set<QualifiedContent.Scope> getScopes() {
    return SCOPES
  }

  @Override
  public boolean isIncremental() {
    return true
  }

  @Override
  public boolean isCacheable() {
    return true
  }

  @Override
  void transform(TransformInvocation transformInvocation)
      throws TransformException, InterruptedException, IOException {
    //super.transform(transformInvocation)
    boolean skip = false
    XaopConfig config = getXaopConfig()
    println "config:${config}"
    String variantName = transformInvocation.context.variantName
    if ("debug".equals(variantName)) {
      skip = config.debugSkip
    } else if ("release".equals(variantName)) {
      skip = config.releaseSkip
    }
    println "variant:${variantName},skip:${skip}"
    long startTime = System.currentTimeMillis()
    TransformOutputProvider outputProvider = transformInvocation.getOutputProvider()
    if (!transformInvocation.incremental) {
      outputProvider.deleteAll()
    }
    Collection<TransformInput> inputs = transformInvocation.inputs
    Collection<TransformInput> referencedInputs = transformInvocation.referencedInputs
    URLClassLoader urlClassLoader = ClassLoaderHelper.getClassLoader(inputs, referencedInputs,
        project)
    this.weaver.setClassLoader(urlClassLoader)
    boolean flagForCleanDexBuilderFolder = false

    for (TransformInput input : inputs) {
      for (JarInput jarInput : input.getJarInputs()) {
        File dest = outputProvider.getContentLocation(jarInput.getFile().getAbsolutePath(),
            jarInput.getContentTypes(),
            jarInput.getScopes(),
            Format.JAR)
        println "jarInput:${jarInput.getFile().getAbsolutePath()},dest:${dest.getAbsolutePath()}"
        if (skip) {
          FileUtils.copyFile(jarInput, dest)
          continue
        }
        Status status = jarInput.getStatus()
        if (transformInvocation.incremental) {
          switch (status) {
            case NOTCHANGED:
              break
            case ADDED:
            case CHANGED:
              transformJar(jarInput.getFile(), dest, status)
              break
            case REMOVED:
              if (dest.exists()) {
                FileUtils.forceDelete(dest)
              }
              break
          }
        } else {
          //Forgive me!, Some project will store 3rd-party aar for serveral copies in dexbuilder folder,,unknown issue.
          if (inDuplcatedClassSafeMode() && !flagForCleanDexBuilderFolder) {
            cleanDexBuilderFolder(dest)
            flagForCleanDexBuilderFolder = true
          }
          transformJar(jarInput.getFile(), dest, status)
        }
      }
    }

    for (DirectoryInput directoryInput : input.getDirectoryInputs()) {
      File dest = outputProvider.getContentLocation(directoryInput.getName(),
          directoryInput.getContentTypes(), directoryInput.getScopes(),
          Format.DIRECTORY)
      println "directoryInput:${directoryInput.getFile().getAbsolutePath()},dest:${dest.getAbsolutePath()}"
      FileUtils.forceMkdir(dest)
      if (skip) {
        FileUtils.copyDirectory(directoryInput.getFile(), dest)
        continue
      }
      if (transformInvocation.incremental) {
        String srcDirPath = directoryInput.getFile().getAbsolutePath()
        String destDirPath = dest.getAbsolutePath()
        Map<File, Status> fileStatusMap = directoryInput.getChangedFiles()
        for (Map.Entry<File, Status> changedFile : fileStatusMap.entrySet()) {
          Status status = changedFile.getValue()
          File inputFile = changedFile.getKey()
          String destFilePath = inputFile.getAbsolutePath().replace(srcDirPath, destDirPath)
          File destFile = new File(destFilePath)
          switch (status) {
            case NOTCHANGED:
              break
            case REMOVED:
              if (destFile.exists()) {
                //noinspection ResultOfMethodCallIgnored
                destFile.delete()
              }
              break
            case ADDED:
            case CHANGED:
              try {
                FileUtils.touch(destFile)
              } catch (IOException e) {
                //maybe mkdirs fail for some strange reason, try again.
                Files.createParentDirs(destFile)
              }
              transformSingleFile(inputFile, destFile, srcDirPath)
              break
          }
        }
      } else {
        transformDir(directoryInput.getFile(), directoryInput.getFile().getAbsolutePath(),
            dest.getAbsolutePath())
      }
    }

    waitableExecutor.waitForTasksWithQuickFail(true)
    long costTime = System.currentTimeMillis() - startTime
    println "${getName()} costed:${costTime}ms"
  }

  protected void transformSingleFile(final File inputFile, final File outputFile,
      final String srcBaseDir) {
    println "transformSingleFile inputFile:${inputFile.getAbsolutePath()}" +
        ",outputFile:${outputFile.getAbsolutePath()},srcBaseDir:${srcBaseDir}"
    waitableExecutor.execute({
      weaver.weaveSingleClassToFile(inputFile, outputFile, srcBaseDir)
      return null
    })
  }

  protected void transformDir(final File sourceDir, final String inputDirPath,
      final String outputDirPath) throws IOException {
    println "transformDir sourceDir:${sourceDir.getAbsolutePath()},inputDirPath:${inputDirPath}" +
        ",outputDirPath:${outputDirPath}"
    if (null != sourceDir && sourceDir.isDirectory()) {
      // 一次处理一个文件夹下面的文件，防止创建的任务过多
      waitableExecutor.execute({
        for (File sourceFile : sourceDir.listFiles()) {
          if (sourceFile.isDirectory()) {
            transformDir(file, inputDirPath, outputDirPath)
          } else {
            String filePath = sourceFile.getAbsolutePath()
            File outputFile = new File(filePath.replace(inputDirPath, outputDirPath))
            weaver.weaveSingleClassToFile(file, outputFile, inputDirPath)
          }
        }
      })
    }
  }

  protected void transformJar(final File srcJar, final File destJar) {
    //FileUtils.copyFile(srcJar, destJar)
    println "transformJar srcJar:${srcJar.getAbsolutePath()},destJar:${destJar.getAbsolutePath()}"
    waitableExecutor.execute({
      weaver.weaveJar(srcJar, destJar)
    })
  }

  protected void cleanDexBuilderFolder(File dest) {
    waitableExecutor.execute({
      try {
        String dexBuilderDir = replaceLastPart(dest.getAbsolutePath(), getName(), "dexBuilder")
        //intermediates/transforms/dexBuilder/debug
        File file = new File(dexBuilderDir).getParentFile()
        println("clean dexBuilder folder = " + file.getAbsolutePath())
        if (file.exists() && file.isDirectory()) {
          FileUtils.deleteDirectory(file)
          //          com.android.utils.FileUtils.deleteDirectoryContents(file)
        }
      } catch (Exception e) {
        e.printStackTrace()
      }
      return null
    })
  }

  protected String replaceLastPart(String originString, String replacement, String toreplace) {
    int start = originString.lastIndexOf(replacement)
    StringBuilder builder = new StringBuilder()
    builder.append(originString.substring(0, start))
    builder.append(toreplace)
    builder.append(originString.substring(start + replacement.length()))
    return builder.toString()
  }

  protected XaopConfig getXaopConfig() {
    return config
  }

  protected boolean inDuplcatedClassSafeMode() {
    return false
  }
}
