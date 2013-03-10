package org.jboss.errai.idea.plugin.ui.model;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNameValuePair;
import com.intellij.psi.xml.XmlTag;
import org.jboss.errai.idea.plugin.ui.TemplateDataField;
import org.jboss.errai.idea.plugin.util.AnnotationSearchResult;
import org.jboss.errai.idea.plugin.util.Types;
import org.jboss.errai.idea.plugin.util.Util;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Mike Brock
 */
public class TemplateMetaData {
  private final TemplateExpression templateExpression;
  private boolean defaultReference;
  private final PsiNameValuePair attribute;
  private final PsiClass templateClass;
  private final VirtualFile templateFile;
  private final XmlTag rootTag;
  private final Project project;

  public TemplateMetaData(TemplateExpression templateExpression,
                          boolean defaultReference,
                          PsiNameValuePair attribute,
                          PsiClass templateClass,
                          VirtualFile templateFile,
                          XmlTag rootTag,
                          Project project) {
    this.templateExpression = templateExpression;
    this.defaultReference = defaultReference;
    this.attribute = attribute;
    this.templateClass = templateClass;
    this.templateFile = templateFile;
    this.rootTag = rootTag;
    this.project = project;
  }

  public TemplateExpression getTemplateExpression() {
    return templateExpression;
  }

  public boolean isDefaultReference() {
    return defaultReference;
  }

  public PsiClass getTemplateClass() {
    return templateClass;
  }

  public VirtualFile getTemplateFile() {
    return templateFile;
  }

  public PsiNameValuePair getAttribute() {
    return attribute;
  }

  public XmlTag getRootTag() {
    return rootTag;
  }

  public Project getProject() {
    return project;
  }

  public Map<String, TemplateDataField> getAllDataFieldsInTemplate(boolean includeRootTag) {
    return Util.findAllDataFieldTags(this, project, includeRootTag);
  }

  public Map<String, ClassDataField> getAllDataFieldsInClass() {
    final Collection<AnnotationSearchResult> allInjectionPoints
        = Util.findAllAnnotatedElements(templateClass, Types.DATAFIELD_ANNOTATION_NAME);

    final Map<String, ClassDataField> map = new LinkedHashMap<String, ClassDataField>();
    for (AnnotationSearchResult injectionPoint : allInjectionPoints) {
      PsiElement owningElement = injectionPoint.getOwningElement();
      PsiAnnotation psiAnnotation = injectionPoint.getAnnotation();

      final String value = Util.getValueStringFromAnnotationWithDefault(psiAnnotation).getValue();

      map.put(value, new ClassDataField(value, owningElement));
    }
    return map;
  }


  public Map<String, ConsolidateDataFieldElementResult> getConsolidatedDataFields() {
    if (templateClass == null) {
      return Collections.emptyMap();
    }
    return Util.getConsolidatedDataFields(templateClass, project);
  }


}
