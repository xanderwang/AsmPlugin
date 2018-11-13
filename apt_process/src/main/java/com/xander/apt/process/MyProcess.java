package com.xander.apt.process;

import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import com.xander.apt.lib.AutoCreate;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

@AutoService(MyProcess.class) public class MyProcess extends AbstractProcessor {

  @Override public Set<String> getSupportedAnnotationTypes() {
    return Collections.singleton(AutoCreate.class.getCanonicalName());
  }

  @Override
  public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

    // 定义一个 method
    MethodSpec mainMethod = MethodSpec.methodBuilder("main")
        .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
        .returns(void.class)
        .addParameter(String[].class, "args")
        .addStatement("$T.out.println($s)", System.class, "Hello apt!!!")
        .build();

    // 定义一个类
    TypeSpec testSpec = TypeSpec.classBuilder("TestApt")
        .addModifiers(Modifier.STATIC, Modifier.PUBLIC)
        .addMethod(mainMethod)
        .build();

    // 写入到磁盘
    JavaFile testFile = JavaFile.builder("com.xander.apt", testSpec).build();

    try {
      testFile.writeTo(processingEnv.getFiler());
    } catch (IOException e) {
      e.printStackTrace();
    }

    return false;
  }
}
