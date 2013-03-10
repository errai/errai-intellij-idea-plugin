package org.jboss.errai.idea.plugin.databinding.model;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiVariable;
import org.jboss.errai.idea.plugin.databinding.DataBindUtil;
import org.jboss.errai.idea.plugin.util.DefaultPolicy;
import org.jboss.errai.idea.plugin.util.Types;
import org.jboss.errai.idea.plugin.util.Util;

/**
* @author Mike Brock
*/
public class BoundMetaData {
  private final TemplateBindingMetaData templateBindingMetaData;
  private final PsiElement owner;
  private final PsiAnnotation psiAnnotation;
  private final String property;
  private final String bindableConverter;

  public BoundMetaData(PsiElement owner) {
    this.templateBindingMetaData = DataBindUtil.getTemplateBindingMetaData(owner);
    this.owner = owner;
    this.psiAnnotation = Util.getAnnotationFromElement(owner, Types.BOUND);

    if (psiAnnotation != null) {
      property = Util.getAttributeValue(psiAnnotation, "property", DefaultPolicy.OWNER_IDENTIFIER_NAME);
      bindableConverter = Util.getAttributeValue(psiAnnotation, "converter", DefaultPolicy.NULL);
    }
    else {
      property = null;
      bindableConverter = null;
    }
  }

  public TemplateBindingMetaData getBindingMetaData() {
    return templateBindingMetaData;
  }

  public PsiAnnotation getPsiAnnotation() {
    return psiAnnotation;
  }

  public String getProperty() {
    return property;
  }

  public PsiElement getOwner() {
    return owner;
  }

  public String getBindableConverter() {
    return bindableConverter;
  }

  public PropertyValidation validateProperty() {
    final PsiClass boundClass = getBindingMetaData().getBoundClass();

    if (property != null && boundClass != null) {
      PsiClass cls = boundClass;
      for (String token : property.split("\\.")) {
        if (!Util.elementIsAnnotated(cls, Types.BINDABLE)) {
          PropertyValidation validation = new PropertyValidation();
          validation.setParentBindable(false);
          validation.setUnresolvedParent(cls);
          validation.setUnresolvedPropertyElement(token);
          return validation;
        }
        PsiClass result = DataBindUtil.getBeanPropertyType(cls, token.trim());
        if (result == null) {
          PropertyValidation validation = new PropertyValidation();
          validation.setParentBindable(true);
          validation.setUnresolvedParent(cls);
          validation.setUnresolvedPropertyElement(token);
          return validation;
        }
        cls = result;
      }

      final PropertyValidation validation = new PropertyValidation(true);

      PsiVariable variable = (PsiVariable) owner;
      final PsiClass widgetType = DataBindUtil.getPsiClassFromType(owner.getProject(), variable.getType());
      if (widgetType != null) {
        final ConvertibilityMetaData convertibilityMetaData = DataBindUtil.getConvertibilityMetaData(psiAnnotation);

        validation.setBindabilityValidation(DataBindUtil.typeIsBindableToWidget(cls, widgetType, convertibilityMetaData));
      }

      validation.setBoundType(cls);

      return validation;
    }
    else {
      return new PropertyValidation(false);
    }
  }
}
