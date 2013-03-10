package org.jboss.errai.idea.plugin.util;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiReferenceBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * @author Mike Brock
 */
public class ExpressionErrorReference extends PsiReferenceBase<PsiLiteralExpression> {
  private final String errorElemement;
  private final TextRange textRange;

  public ExpressionErrorReference(@NotNull PsiLiteralExpression element, String errorElemement, TextRange textRange) {
    super(element);
    this.errorElemement = errorElemement;
    this.textRange = textRange;
  }

  @Override
  public TextRange getRangeInElement() {
    return textRange;
  }

  @NotNull
  @Override
  public String getCanonicalText() {
    return errorElemement;
  }

  @Nullable
  @Override
  public PsiElement resolve() {
    return null;
  }

  @NotNull
  @Override
  public Object[] getVariants() {
    return new Object[0];
  }
}
