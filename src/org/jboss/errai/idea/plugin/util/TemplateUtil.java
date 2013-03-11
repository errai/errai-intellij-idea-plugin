package org.jboss.errai.idea.plugin.util;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.ide.fileTemplates.FileTemplateUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;

import java.util.Map;
import java.util.Properties;

/**
 * @author Mike Brock
 */
public class TemplateUtil {
  public static PsiElement createFileFromTemplate(String templateName, String fileName, PsiDirectory directory, Map<String, String> properties) {
    final FileTemplate template = FileTemplateManager.getInstance().getJ2eeTemplate(templateName);
    final Properties props = FileTemplateManager.getInstance().getDefaultProperties();

    for (Map.Entry<String, String> entry : properties.entrySet()) {
      props.put(entry.getKey(), entry.getValue());
    }

    try {
      return FileTemplateUtil.createFromTemplate(template, fileName, props, directory);
    }
    catch (Exception e) {
      throw new RuntimeException("could not create template", e);
    }
  }
}
