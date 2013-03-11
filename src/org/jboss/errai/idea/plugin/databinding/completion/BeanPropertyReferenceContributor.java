package org.jboss.errai.idea.plugin.databinding.completion;

import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceRegistrar;
import org.jboss.errai.idea.plugin.util.AnnotationMatchingPattern;
import org.jboss.errai.idea.plugin.util.Types;

/**
 * @author Mike Brock
 */
public class BeanPropertyReferenceContributor extends PsiReferenceContributor {
  @Override
  public void registerReferenceProviders(PsiReferenceRegistrar registrar) {
    registrar.registerReferenceProvider(new AnnotationMatchingPattern(Types.BOUND), new BeanPropertyReferenceProvider());
  }

}
