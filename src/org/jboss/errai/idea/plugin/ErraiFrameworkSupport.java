package org.jboss.errai.idea.plugin;

import com.intellij.lang.Language;
import com.intellij.lang.html.HTMLLanguage;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

/**
 * @author Mike Brock
 */
public class ErraiFrameworkSupport implements ProjectComponent {
  public static final String JAVAX_INJECT = "javax.inject.Inject";

  public static final String TEMPLATED_ANNOTATION_NAME = "org.jboss.errai.ui.shared.api.annotations.Templated";
  public static final String DATAFIELD_ANNOTATION_NAME = "org.jboss.errai.ui.shared.api.annotations.DataField";
  public static final String EVENTHANDLER_ANNOTATION_NAME = "org.jboss.errai.ui.shared.api.annotations.EventHandler";
  public static final String SINKNATIVE_ANNOTATION_NAME = "org.jboss.errai.ui.shared.api.annotations.SinkNative";

  public static final String GWT_COMPOSITE_REF = "com.google.gwt.user.client.ui.Composite";
  public static final String GWT_WIDGET_TYPE = "com.google.gwt.user.client.ui.Widget";
  public static final String GWT_ELEMENT_TYPE = "com.google.gwt.dom.client.Element";
  public static final String GWT_DOM_EVENT_TYPE = "com.google.gwt.user.client.Event";
  public static final String GWT_EVENT_TYPE = "com.google.gwt.event.shared.GwtEvent";

  public ErraiFrameworkSupport(final Project project, ReferenceProvidersRegistry registry) {
    final PsiReferenceRegistrar javaRegistrar = registry.getRegistrar(Language.findInstance(JavaLanguage.class));
    javaRegistrar.registerReferenceProvider(new AnnotationMatchingPattern(TEMPLATED_ANNOTATION_NAME),
        new PsiReferenceProvider() {
          @NotNull
          @Override
          public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext processingContext) {
            return new TemplateReference[]{new TemplateReference(project, (PsiLiteralExpression) element, false)};
          }
        }
    );

    javaRegistrar.registerReferenceProvider(new AnnotationMatchingPattern(DATAFIELD_ANNOTATION_NAME),
        new PsiReferenceProvider() {
          @NotNull
          @Override
          public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
            return new TemplateDatafieldReference[]{new TemplateDatafieldReference(project, (PsiLiteralExpression) element, false)};

          }
        });

    javaRegistrar.registerReferenceProvider(new AnnotationMatchingPattern(EVENTHANDLER_ANNOTATION_NAME),
        new PsiReferenceProvider() {
          @NotNull
          @Override
          public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
            return new BeanDatafieldReference[]{new BeanDatafieldReference(project, (PsiLiteralExpression) element, false)};
          }
        });


    final PsiReferenceRegistrar xmlRegistrar = registry.getRegistrar(Language.findInstance(HTMLLanguage.class));

    xmlRegistrar.registerReferenceProvider(new XmlAttributeMatchingPattern("data-field"),
        new PsiReferenceProvider() {
          @NotNull
          @Override
          public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
            return new XmlDatafieldReference[]{new XmlDatafieldReference(project, (XmlAttribute) element, false)};
          }
        });
  }

  public void initComponent() {
  }

  public void disposeComponent() {
  }

  @NotNull
  public String getComponentName() {
    return "ErraiFrameworkTools";
  }

  public void projectOpened() {
  }

  public void projectClosed() {
  }
}
