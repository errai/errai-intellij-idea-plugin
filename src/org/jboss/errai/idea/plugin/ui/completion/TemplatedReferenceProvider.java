package org.jboss.errai.idea.plugin.ui.completion;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.util.ProcessingContext;
import org.jboss.errai.idea.plugin.ui.TemplateUtil;
import org.jboss.errai.idea.plugin.ui.model.TemplateExpression;
import org.jboss.errai.idea.plugin.util.Util;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Brock
 */
public class TemplatedReferenceProvider extends PsiReferenceProvider {
  @NotNull
  @Override
  public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
    final List<PsiReference> referenceList = new ArrayList<PsiReference>();

    final PsiLiteralExpression literalExpression = (PsiLiteralExpression) element;
    final String value = (String) literalExpression.getValue();

    final String text;
    if (value == null) {
      text = "";
    }
    else {
      text = value.replace(Util.INTELLIJ_MAGIC_STRING, "");
    }

    final TemplateExpression templateExpression = TemplateUtil.parseReference(text);

    final int start = text.indexOf(templateExpression.getFileName());
    final TextRange textRange = TextRange.create(start + 1, start + templateExpression.getFileName().length() + 1);

    final int hashIdx = text.indexOf('#');
    referenceList.add(new TemplateFileReference(literalExpression, textRange));

    if (hashIdx != -1) {
      final int nodeStart = hashIdx + 2;
      final TextRange range = TextRange.create(nodeStart, text.length() + 1);
      referenceList.add(new DataFieldReference(true, literalExpression, range));
    }

    return referenceList.toArray(new PsiReference[referenceList.size()]);
  }
}
