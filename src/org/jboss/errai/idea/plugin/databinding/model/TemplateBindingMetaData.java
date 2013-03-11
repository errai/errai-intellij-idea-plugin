package org.jboss.errai.idea.plugin.databinding.model;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiVariable;
import org.jboss.errai.idea.plugin.databinding.DataBindUtil;
import org.jboss.errai.idea.plugin.util.AnnotationSearchResult;
import org.jboss.errai.idea.plugin.ui.TemplateUtil;
import org.jboss.errai.idea.plugin.util.Types;
import org.jboss.errai.idea.plugin.util.Util;

import java.util.Collection;
import java.util.Set;

/**
 * @author Mike Brock
 */
public class TemplateBindingMetaData {
  private final PsiClass templateClass;
  private final PsiClass boundClass;

  private final long templateClassModifyTime;
  //private final long boundClassModifyTime;


  /**
   * This is so, if the user has specified more than one, we can detect and reference all of them for
   * error highlighting;
   */
  private final Collection<AnnotationSearchResult> autoBoundAnnotations;

  public TemplateBindingMetaData(PsiClass templateClass) {
    this.templateClass = templateClass;
    templateClassModifyTime = templateClass.getContainingFile().getOriginalFile().getModificationStamp();

    autoBoundAnnotations = Util.findAllAnnotatedElements(templateClass, Types.AUTO_BOUND);

    if (autoBoundAnnotations.size() == 1) {
      AnnotationSearchResult result = autoBoundAnnotations.iterator().next();
      boundClass = Util.getErasedTypeParam(templateClass.getProject(), ((PsiVariable) result.getOwningElement()).getType().getCanonicalText());

      if (boundClass != null) {
        TemplateUtil.declareOwner(boundClass.getContainingFile(), templateClass);
      }
    }
    else {
      boundClass = null;
    }
  }

  public PsiClass getTemplateClass() {
    return templateClass;
  }

  public PsiAnnotation getAutoboundAnnotation() {
    if (autoBoundAnnotations.size() == 1) {
      return autoBoundAnnotations.iterator().next().getAnnotation();
    }
    else {
      return null;
    }
  }

  public PsiElement getDataBinderElement() {
    if (autoBoundAnnotations.size() == 1) {
      return autoBoundAnnotations.iterator().next().getOwningElement();
    }
    else {
      return null;
    }
  }

  public boolean isValidBindableModel() {
    if (boundClass == null) {
      return false;
    }
    if (Util.elementIsAnnotated(boundClass, Types.BINDABLE)) {
      return true;
    }

    final Set<String> configuredBindableTypes = DataBindUtil.getConfiguredBindableTypes(boundClass.getProject());
    return configuredBindableTypes.contains(boundClass.getQualifiedName());
  }

  public boolean isCacheValid() {
    if (boundClass == null) {
      return false;
    }

    return templateClassModifyTime == templateClass.getContainingFile().getOriginalFile().getModificationStamp();
  }

  public PsiClass getBoundClass() {
    return boundClass;
  }
}
