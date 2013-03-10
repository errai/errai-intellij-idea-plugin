package org.jboss.errai.idea.plugin;

import com.intellij.codeInspection.InspectionToolProvider;
import org.jboss.errai.idea.plugin.databinding.inspection.DataBindingErrorInspections;
import org.jboss.errai.idea.plugin.ui.inspection.UITemplateErrorInspections;
import org.jboss.errai.idea.plugin.ui.inspection.UITemplateCodeSmellInspections;

/**
 * @author Mike Brock
 */
public class ErraiUIInspectionToolProvider implements InspectionToolProvider {
  @Override
  public Class[] getInspectionClasses() {
    return new Class[]{
        UITemplateErrorInspections.class,
        UITemplateCodeSmellInspections.class,
        DataBindingErrorInspections.class
    };
  }
}
