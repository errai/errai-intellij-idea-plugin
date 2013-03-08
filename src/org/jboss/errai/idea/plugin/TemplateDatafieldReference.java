package org.jboss.errai.idea.plugin;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.xml.XmlAttribute;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mike Brock
 */
class TemplateDatafieldReference extends PsiReferenceBase<PsiLiteralExpression> {

  public TemplateDatafieldReference(PsiLiteralExpression element, boolean soft) {
    super(element, soft);
  }

  private Map<String, PsiElement> getAvailableDataFields() {
    Map<String, PsiElement> map = new HashMap<String, PsiElement>();
    final Project project = getElement().getProject();
    final Util.TemplateMetaData templateFile = Util.getTemplateMetaData(getElement(), project);
    if (templateFile != null) {
      final String rootNode = templateFile.getTemplateReference().getRootNode();
      final Map<String, Util.DataFieldReference> dataFieldTags = Util.findAllDataFieldTags(templateFile, project, false);

      for (Map.Entry<String, Util.DataFieldReference> entry : dataFieldTags.entrySet()) {
        if (rootNode.equals(entry.getKey())) continue;

        final XmlAttribute attribute = entry.getValue().getTag().getAttribute("data-field");

        if (attribute != null) {
          map.put(entry.getKey(), attribute.getValueElement());
        }
      }
    }
    return map;
  }


  @Nullable
  @Override
  public PsiElement resolve() {
    return getAvailableDataFields().get(getValue());
  }

  @NotNull
  @Override
  public Object[] getVariants() {
    final Map<String, PsiElement> dataFields = getAvailableDataFields();
    final ArrayList<Object> list = new ArrayList<Object>();
    for (final String value : dataFields.keySet()) {

      list.add(LookupElementBuilder.create(value));
    }
    return list.toArray();
  }
}
