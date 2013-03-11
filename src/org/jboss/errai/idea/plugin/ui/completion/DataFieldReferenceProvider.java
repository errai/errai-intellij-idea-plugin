package org.jboss.errai.idea.plugin.ui.completion;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import org.jboss.errai.idea.plugin.util.Util;
import org.jetbrains.annotations.NotNull;

/**
 * @author Mike Brock
 */
public class DataFieldReferenceProvider extends PsiReferenceProvider {
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

    return new PsiReference[] {
       new DataFieldReference(false, literalExpression, TextRange.create(1, text.length() + 1))
    };
  }
}
