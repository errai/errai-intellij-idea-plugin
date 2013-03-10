package org.jboss.errai.idea.plugin.ui.completion;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.xml.XmlAttribute;
import org.jboss.errai.idea.plugin.ui.TemplateDataField;
import org.jboss.errai.idea.plugin.ui.model.TemplateMetaData;
import org.jboss.errai.idea.plugin.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mike Brock
 */
public class TemplateDatafieldReference extends PsiReferenceBase<PsiLiteralExpression> {

  public TemplateDatafieldReference(PsiLiteralExpression element, boolean soft) {
    super(element, soft);
  }

  private Map<String, PsiElement> getAvailableDataFields() {
    Map<String, PsiElement> map = new HashMap<String, PsiElement>();
    final TemplateMetaData metaData = Util.getTemplateMetaData(getElement());
    if (metaData != null) {
      final String rootNode = metaData.getTemplateExpression().getRootNode();
      final Map<String, TemplateDataField> dataFieldTags = metaData.getAllDataFieldsInTemplate(false);

      for (Map.Entry<String, TemplateDataField> entry : dataFieldTags.entrySet()) {
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
