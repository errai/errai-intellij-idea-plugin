package org.jboss.errai.idea.plugin.ui.completion;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.xml.XmlAttribute;
import org.jboss.errai.idea.plugin.ui.TemplateDataField;
import org.jboss.errai.idea.plugin.ui.TemplateUtil;
import org.jboss.errai.idea.plugin.ui.model.TemplateMetaData;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Mike Brock
 */
public class DataFieldReference extends PsiReferenceBase<PsiLiteralExpression> {
  private final TextRange range;
  private final boolean considerRoot;

  public DataFieldReference(boolean considerRoot, PsiLiteralExpression element, TextRange range) {
    super(element, false);
    this.range = range;
    this.considerRoot = considerRoot;
  }


  private Map<String, PsiElement> getAvailableDataFields() {
    Map<String, PsiElement> map = new HashMap<String, PsiElement>();
    final TemplateMetaData metaData = TemplateUtil.getTemplateMetaData(getElement());
    if (metaData != null) {
      final String rootNode = metaData.getTemplateExpression().getRootNode();
      final Map<String, TemplateDataField> dataFieldTags = metaData.getAllDataFieldsInTemplate(considerRoot);

      for (Map.Entry<String, TemplateDataField> entry : dataFieldTags.entrySet()) {
        if (!considerRoot && rootNode.equals(entry.getKey())) continue;

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

  @Override
  public boolean isReferenceTo(PsiElement element) {
    return element.equals(resolve());
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

  @Override
  public TextRange getRangeInElement() {
    return range;
  }
}
