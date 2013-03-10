package org.jboss.errai.idea.plugin.databinding.completion;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.ProcessingContext;
import org.jboss.errai.idea.plugin.databinding.DataBindUtil;
import org.jboss.errai.idea.plugin.databinding.model.PropertyInfo;
import org.jboss.errai.idea.plugin.databinding.model.TemplateBindingMetaData;
import org.jboss.errai.idea.plugin.util.AnnotationMatchingPattern;
import org.jboss.errai.idea.plugin.util.ExpressionErrorReference;
import org.jboss.errai.idea.plugin.util.Types;
import org.jboss.errai.idea.plugin.util.Util;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Mike Brock
 */
public class BeanPropertyReferenceContributor extends PsiReferenceContributor {
  @Override
  public void registerReferenceProviders(PsiReferenceRegistrar registrar) {
    registrar.registerReferenceProvider(new AnnotationMatchingPattern(Types.BOUND),
        new PsiReferenceProvider() {
          @NotNull
          @Override
          public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
            final PsiLiteralExpression literalExpression = (PsiLiteralExpression) element;
            final String value = (String) literalExpression.getValue();

            final String text;
            if (value == null) {
              text = "";
            }
            else {
              text = value.replace(Util.INTELLIJ_MAGIC_STRING, "");
            }

            final TemplateBindingMetaData metaData = DataBindUtil.getTemplateBindingMetaData(element);
            PsiClass cls = metaData.getBoundClass();

            final List<PsiReference> references = new ArrayList<PsiReference>();
            int cursor = 1;
            for (final String propertyName : text.split("\\.")) {


              final int rangeStart = cursor;
              final int rangeEnd = cursor + propertyName.length();

              final TextRange range = TextRange.create(rangeStart, rangeEnd);

              cursor = rangeEnd + 1;

              if (cls == null) {
                references.add(new ExpressionErrorReference(literalExpression, propertyName, range));
                break;
              }

              final PsiClass parentType = cls;
              final PsiClass propPsiClass
                  = DataBindUtil.getBeanPropertyType(cls, propertyName.trim());

              if (propPsiClass != null) {
                Util.declareOwner(propPsiClass.getContainingFile(), metaData.getTemplateClass());
              }
              else {
                references.add(new ExpressionErrorReference(literalExpression, propertyName, range));
              }

              references.add(new PsiReferenceBase<PsiLiteralExpression>(literalExpression, false) {
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
                  boolean ref = element.equals(resolve());
                  if (ref) {
                    return true;
                  }
                  else {
                    return false;
                  }
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
              });

              cls = propPsiClass;
            }

            return references.toArray(new PsiReference[references.size()]);
          }
        });
  }
}
