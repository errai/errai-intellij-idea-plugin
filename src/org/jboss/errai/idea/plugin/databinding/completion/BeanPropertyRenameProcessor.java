package org.jboss.errai.idea.plugin.databinding.completion;

import com.intellij.psi.JavaPsiFacade;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiAnnotationMemberValue;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiExpression;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiUtil;
import com.intellij.refactoring.rename.RenamePsiElementProcessor;
import com.intellij.util.IncorrectOperationException;
import org.jboss.errai.idea.plugin.databinding.model.BoundMetaData;
import org.jboss.errai.idea.plugin.databinding.model.PropertyInfo;
import org.jboss.errai.idea.plugin.databinding.model.TemplateBindingMetaData;
import org.jboss.errai.idea.plugin.util.FakeNamedPsi;
import org.jboss.errai.idea.plugin.databinding.DataBindUtil;
import org.jboss.errai.idea.plugin.util.DefaultPolicy;
import org.jboss.errai.idea.plugin.util.Types;
import org.jboss.errai.idea.plugin.util.Util;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * @author Mike Brock
 */
public class BeanPropertyRenameProcessor extends RenamePsiElementProcessor {
  @Override
  public boolean canProcessElement(@NotNull PsiElement element) {
    if (element instanceof PsiField) {
      final PsiClass topLevelClass = PsiUtil.getTopLevelClass(element);

      return topLevelClass != null && Util.elementIsAnnotated(topLevelClass, Types.BINDABLE);
    }
    return false;
  }

  @Override
  public void prepareRenaming(final PsiElement element, String replacementStr, Map<PsiElement, String> psiElementStringMap) {
    final PsiFile containingFile = PsiUtil.getTopLevelClass(element).getContainingFile();
    final Set<PsiClass> owners = Util.getOwners(containingFile);
    for (PsiClass psiClass : owners) {
      final TemplateBindingMetaData metaData = DataBindUtil.getTemplateBindingMetaData(psiClass);
      final Collection<BoundMetaData> allBoundMetaDataFromClass = DataBindUtil.getAllBoundMetaDataFromClass(psiClass);

      for (BoundMetaData md : allBoundMetaDataFromClass) {
        final PsiAnnotation boundAnnotation = md.getPsiAnnotation();
        final String property = Util.getAttributeValue(boundAnnotation, "property", DefaultPolicy.NULL);

        final StringBuilder sb = new StringBuilder();
        boolean first = true;
        PsiClass cls = metaData.getBoundClass();
        for (String prop : property.split("\\.")) {
          if (!first) {
            sb.append('.');
          }
          first = false;

          final PropertyInfo beanPropertyInfo = DataBindUtil.getBeanPropertyInfo(cls, prop.trim());

          if (beanPropertyInfo == null) {
            return;
          }

          if (element.equals(beanPropertyInfo.getAssociatedField())) {
            sb.append(replacementStr);
          }
          else {
            sb.append(prop);
          }

          cls = beanPropertyInfo.getPropertyType();
        }

        final PsiAnnotationMemberValue property1 = Util.getAnnotationMemberValue(boundAnnotation, "property");

        FakeNamedPsi dummy = new FakeNamedPsi(property1) {

          @Override
          public PsiElement setName(@NotNull final String name) throws IncorrectOperationException {
            final PsiExpression expressionFromText = JavaPsiFacade.getInstance(element.getProject()).getElementFactory()
                .createExpressionFromText("\"" + sb.toString() + "\"", property1);
            boundAnnotation.setDeclaredAttributeValue("property", expressionFromText);

            return expressionFromText;
          }

          @NotNull
          @Override
          public String getName() {
            final String text = Util.getAnnotationMemberValue(boundAnnotation, "property").getText();
            return text.substring(1, text.length() - 1);
          }

          @Override
          public boolean isPhysical() {
            return false;
          }
        };

        psiElementStringMap.put(dummy, sb.toString());
      }
    }
  }
}
