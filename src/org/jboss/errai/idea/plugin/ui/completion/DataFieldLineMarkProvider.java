package org.jboss.errai.idea.plugin.ui.completion;

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo;
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider;
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.util.PsiTreeUtil;
import org.jboss.errai.idea.plugin.actions.ErraiActionGroup;
import org.jboss.errai.idea.plugin.util.Types;
import org.jboss.errai.idea.plugin.util.Util;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

import static org.jboss.errai.idea.plugin.ui.completion.DataFieldReference.getAvailableDataFields;

/**
 * @author edewit@redhat.com
 */
public class DataFieldLineMarkProvider extends RelatedItemLineMarkerProvider {
  @Override
  protected void collectNavigationMarkers(@NotNull PsiElement element, Collection<? super RelatedItemLineMarkerInfo> result) {
    if (element instanceof PsiField) {
      final Collection<PsiAnnotation> annotations = PsiTreeUtil.findChildrenOfType(element, PsiAnnotation.class);

      for (PsiAnnotation annotation : annotations) {
        if (annotation != null && Types.DATAFIELD.equals(annotation.getQualifiedName())) {
          createLineMark(element, result, annotation);
        }
      }
    }
  }

  private void createLineMark(PsiElement element, Collection<? super RelatedItemLineMarkerInfo> result, PsiAnnotation annotation) {
    final String text = Util.getValueStringFromAnnotationWithDefault(annotation).getValue();
    final PsiElement psiElement = getAvailableDataFields(false, annotation).get(text);
    // don't try to add a marker if there is no element
    if (psiElement != null) {
      NavigationGutterIconBuilder<PsiElement> builder =
          NavigationGutterIconBuilder.create(ErraiActionGroup.ERRAI_ICON).setTargets(psiElement)
              .setTooltipText("Navigate to '" + text + "' field");
      result.add(builder.createLineMarkerInfo(element));
    }
  }
}
