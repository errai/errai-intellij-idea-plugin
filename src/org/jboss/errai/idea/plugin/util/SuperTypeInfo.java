package org.jboss.errai.idea.plugin.util;

import java.util.List;

/**
 * @author Mike Brock
 */
public class SuperTypeInfo {
  private final String className;
  private final List<String> typeParms;

  public SuperTypeInfo(String className, List<String> typeParms) {
    this.className = className;
    this.typeParms = typeParms;
  }

  public String getClassName() {
    return className;
  }

  public List<String> getTypeParms() {
    return typeParms;
  }
}
