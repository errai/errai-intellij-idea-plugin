/*
 * Copyright 2013 Red Hat, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.jboss.errai.idea.plugin.databinding.completion;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
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

  public PropertyPsiReference(PsiLiteralExpression literalExpression,
                              PsiClass parentType,
                              String propertyName,
                              TextRange range) {
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
      final LookupElementBuilder lookupElementBuilder = LookupElementBuilder.create(entry.getKey());
      final LookupElementBuilder e;

      if (DataBindUtil.typeIsBindable(entry.getValue().getPropertyType())) {
        e = lookupElementBuilder.withIcon(AllIcons.Nodes.Class);
      }
      else {
        e = lookupElementBuilder.withIcon(AllIcons.Nodes.Property);
      }

      variants.add(e.withTypeText(propertyType.getQualifiedName(), true));
    }

    return variants.toArray();
  }
}
