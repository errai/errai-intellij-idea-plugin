package org.jboss.errai.idea.plugin.util;

import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.ElementPatternCondition;
import com.intellij.patterns.InitialPatternCondition;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.Nullable;

/**
* @author Mike Brock
*/
public class AnnotationMatchingPattern implements ElementPattern<PsiLiteralExpression> {
  private final String type;
  private final ElementPatternCondition<PsiLiteralExpression> patternCondition =
      new ElementPatternCondition<PsiLiteralExpression>(new InitialPatternCondition<PsiLiteralExpression>(PsiLiteralExpression.class) {
        @Override
        public boolean accepts(@Nullable Object o, ProcessingContext context) {
          if (o instanceof PsiLiteralExpression) {
            final PsiAnnotation parentOfType = PsiTreeUtil.getParentOfType((PsiLiteralExpression) o, PsiAnnotation.class);
            return parentOfType != null && type.equals(parentOfType.getQualifiedName());
          }
          return false;
        }
      });

  public AnnotationMatchingPattern(String type) {
    this.type = type;
  }

  @Override
  public boolean accepts(@Nullable Object o) {
    return false;
  }

  @Override
  public boolean accepts(@Nullable Object o, ProcessingContext processingContext) {
    return getCondition().accepts(o, processingContext);
  }

  @Override
  public ElementPatternCondition<PsiLiteralExpression> getCondition() {
    return patternCondition;
  }
}
