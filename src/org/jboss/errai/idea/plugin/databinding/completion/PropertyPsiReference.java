package org.jboss.errai.idea.plugin.databinding.completion;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.util.IncorrectOperationException;
import org.jboss.errai.idea.plugin.databinding.DataBindUtil;
import org.jboss.errai.idea.plugin.databinding.model.PropertyInfo;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
* @author Mike Brock
*/
class PropertyPsiReference extends PsiReferenceBase<PsiLiteralExpression> {
  private final PsiClass parentType;
  private final String propertyName;
  private final TextRange range;

  public PropertyPsiReference(PsiLiteralExpression literalExpression, PsiClass parentType, String propertyName, TextRange range) {
    super(literalExpression, false);
    this.parentType = parentType;
    this.propertyName = propertyName;
    this.range = range;
  }

  public Map<String, PropertyInfo> getCompletions() {
    return DataBindUtil.getAllProperties(parentType, propertyName.trim());
  }

  @Override
  public TextRange getRangeInElement() {
    return range;
  }

  @Nullable
  @Override
  public PsiElement resolve() {
    for (PsiField psiField : parentType.getAllFields()) {
      if (psiField.getName().equals(propertyName)) {
        return psiField;
      }
    }
    return null;
  }

  @NotNull
  @Override
  public String getCanonicalText() {
    return propertyName;
  }

  @Override
  public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
    return null;
  }

  @Override
  public PsiElement bindToElement(@NotNull PsiElement element) throws IncorrectOperationException {
    return null;
  }

  @Override
  public boolean isReferenceTo(PsiElement element) {
    return element.equals(resolve());
  }

  @NotNull
  @Override
  public Object[] getVariants() {
    List<Object> variants = new ArrayList<Object>();
    for (Map.Entry<String, PropertyInfo> entry : getCompletions().entrySet()) {
      final PsiClass propertyType = entry.getValue().getPropertyType();
      if (propertyType == null) {
        continue;
      }
      variants.add(LookupElementBuilder.create(entry.getKey())
          .withTypeText(propertyType.getQualifiedName(), true));
    }

    return variants.toArray();
  }
}
