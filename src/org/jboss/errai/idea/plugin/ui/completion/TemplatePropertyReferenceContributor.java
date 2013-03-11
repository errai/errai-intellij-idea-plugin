package org.jboss.errai.idea.plugin.ui.completion;

import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import org.jboss.errai.idea.plugin.util.AnnotationMatchingPattern;
import org.jboss.errai.idea.plugin.util.Types;

/**
 * @author Mike Brock
 */
public class TemplatePropertyReferenceContributor extends PsiReferenceContributor {
  @Override
  public void registerReferenceProviders(PsiReferenceRegistrar registrar) {
    registrar.registerReferenceProvider(new AnnotationMatchingPattern(Types.TEMPLATED_ANNOTATION_NAME), new TemplatedReferenceProvider());
    registrar.registerReferenceProvider(new AnnotationMatchingPattern(Types.DATAFIELD_ANNOTATION_NAME), new DataFieldReferenceProvider());
  }
}
