package org.jboss.errai.idea.plugin;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiReferenceBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Mike Brock
 */
public class BoundReference extends PsiReferenceBase<PsiLiteralExpression> {
  public BoundReference(PsiLiteralExpression element, boolean soft) {
    super(element, soft);
  }

  public Map<String, DataBindUtil.PropertyInfo> getCompletions() {
    return DataBindUtil.getAllProperties(DataBindUtil.getTemplateBindingMetaData(getElement()).getBoundClass(),
        getValue().replace(Util.INTELLIJ_MAGIC_STRING, ""));
  }

  @Nullable
  @Override
  public PsiElement resolve() {
    final String value = getValue();
    final Map<String, DataBindUtil.PropertyInfo> completions = getCompletions();
    final DataBindUtil.PropertyInfo info = completions.get(value);
    if (info == null) {
      return null;
    }
    else {
      return info.getAccessorElement();
    }
  }

  @NotNull
  @Override
  public Object[] getVariants() {
    List<Object> variants = new ArrayList<Object>();
    for (Map.Entry<String, DataBindUtil.PropertyInfo> entry : getCompletions().entrySet()) {
      variants.add(LookupElementBuilder.create(entry.getKey())
          .withTypeText(entry.getValue().getPropertyType().getQualifiedName(), true));
    }
    return variants.toArray();
  }
}
