package org.jboss.errai.idea.plugin.util;

import java.util.List;

/**
 * @author Mike Brock
 */
public class SuperTypeInfo {
  private final List<String> typeParms;

  public SuperTypeInfo(List<String> typeParms) {
    this.typeParms = typeParms;
  }

  public List<String> getTypeParms() {
    return typeParms;
  }
}
