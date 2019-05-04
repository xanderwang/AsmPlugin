package com.xander.xaop;

import com.android.build.api.transform.Context;
import com.android.build.api.transform.TransformException;
import com.android.build.api.transform.TransformInput;
import com.android.build.api.transform.TransformOutputProvider;
import com.quinn.hunter.transform.HunterTransform;
import com.quinn.hunter.transform.RunVariant;
import com.xander.xaop.test.TestWeaver;
import java.io.IOException;
import java.util.Collection;
import org.gradle.api.Project;

public class TestHunterTransform extends HunterTransform {
  private Project project;
  private TestHunterExtension testHunterExtension;

  public TestHunterTransform(Project project) {
    super(project);
    this.project = project;
    //project.getExtensions().create("testHunterExt", TestHunterExtension.class);
    project.getExtensions().add("testHunterExt",new TestHunterExtension());
    this.bytecodeWeaver = new TestWeaver();
  }

  @Override public void transform(Context context, Collection<TransformInput> inputs,
      Collection<TransformInput> referencedInputs, TransformOutputProvider outputProvider,
      boolean isIncremental) throws IOException, TransformException, InterruptedException {
    //testHunterExtension =
    //    (TestHunterExtension) project.getExtensions().getByName("testHunterExt");
    //bytecodeWeaver.setExtension(testHunterExtension);
    bytecodeWeaver.setExtension(new TestHunterExtension());
    super.transform(context, inputs, referencedInputs, outputProvider, isIncremental);
  }

  @Override protected RunVariant getRunVariant() {
    return testHunterExtension.runVariant;
  }

  @Override protected boolean inDuplcatedClassSafeMode() {
    return testHunterExtension.duplcatedClassSafeMode;
  }
}
