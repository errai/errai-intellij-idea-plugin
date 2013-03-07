package org.jboss.errai.idea.plugin;

import com.intellij.codeInspection.InspectionToolProvider;

/**
 * @author Mike Brock
 */
public class ErraiUIInspectionToolProvider implements InspectionToolProvider {
  @Override
  public Class[] getInspectionClasses() {
    return new Class[] { ErraiUITemplateErrorInspections.class, ErraiUITemplateCodeSmellInspections.class };
  }
}
