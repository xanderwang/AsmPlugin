package com.xander.xaop;

import com.quinn.hunter.transform.RunVariant;
import java.util.ArrayList;
import java.util.List;

public class TestHunterExtension {
  public RunVariant runVariant = RunVariant.ALWAYS;
  public List<String> whitelist = new ArrayList<>();
  public List<String> blacklist = new ArrayList<>();
  public boolean duplcatedClassSafeMode = false;

  @Override
  public String toString() {
    return "TimingHunterExtension{" +
        "runVariant=" + runVariant +
        ", whitelist=" + whitelist +
        ", blacklist=" + blacklist +
        ", duplcatedClassSafeMode=" + duplcatedClassSafeMode +
        '}';
  }
}
