package org.jboss.errai.idea.plugin;

import com.intellij.lang.Language;
import com.intellij.lang.html.HTMLLanguage;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.components.ApplicationComponent;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.util.ProcessingContext;
import org.jboss.errai.idea.plugin.ui.completion.TemplateDatafieldReference;
import org.jboss.errai.idea.plugin.ui.model.TemplatedReference;
import org.jboss.errai.idea.plugin.util.XmlAttributeMatchingPattern;
import org.jboss.errai.idea.plugin.ui.completion.XmlDatafieldReference;
import org.jboss.errai.idea.plugin.ui.completion.BeanDataFieldReference;
import org.jboss.errai.idea.plugin.util.AnnotationMatchingPattern;
import org.jboss.errai.idea.plugin.util.Types;
import org.jetbrains.annotations.NotNull;

/**
 * @author Mike Brock
 */
public class ErraiFrameworkSupport implements ApplicationComponent {

  private final ReferenceProvidersRegistry registry;

  public ErraiFrameworkSupport(ReferenceProvidersRegistry registry) {
    this.registry = registry;
  }

  public void initComponent() {
    final PsiReferenceRegistrar javaRegistrar = registry.getRegistrar(Language.findInstance(JavaLanguage.class));

    javaRegistrar.registerReferenceProvider(new AnnotationMatchingPattern(Types.TEMPLATED_ANNOTATION_NAME),
        new PsiReferenceProvider() {
          @NotNull
          @Override
          public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext processingContext) {
            return new TemplatedReference[]{new TemplatedReference((PsiLiteralExpression) element, false)};
          }
        }
    );

    javaRegistrar.registerReferenceProvider(new AnnotationMatchingPattern(Types.DATAFIELD_ANNOTATION_NAME),
        new PsiReferenceProvider() {
          @NotNull
          @Override
          public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
            return new TemplateDatafieldReference[]{new TemplateDatafieldReference((PsiLiteralExpression) element, false)};

          }
        });

    javaRegistrar.registerReferenceProvider(new AnnotationMatchingPattern(Types.EVENTHANDLER_ANNOTATION_NAME),
        new PsiReferenceProvider() {
          @NotNull
          @Override
          public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
            return new BeanDataFieldReference[]{new BeanDataFieldReference((PsiLiteralExpression) element, false)};
          }
        });

//    javaRegistrar.registerReferenceProvider(new AnnotationMatchingPattern(Types.BOUND),
//         new PsiReferenceProvider() {
//           @NotNull
//           @Override
//           public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
//             return new BoundReference[]{new BoundReference((PsiLiteralExpression) element, false)};
//           }
//         });


    final PsiReferenceRegistrar xmlRegistrar = registry.getRegistrar(Language.findInstance(HTMLLanguage.class));

    xmlRegistrar.registerReferenceProvider(new XmlAttributeMatchingPattern("data-field"),
        new PsiReferenceProvider() {
          @NotNull
          @Override
          public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
            return new XmlDatafieldReference[]{new XmlDatafieldReference((XmlAttribute) element, false)};
          }
        });
  }

  public void disposeComponent() {
  }

  @NotNull
  public String getComponentName() {
    return "ErraiFrameworkTools";
  }
}
