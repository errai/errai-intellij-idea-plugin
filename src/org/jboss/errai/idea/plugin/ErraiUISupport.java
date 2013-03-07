package org.jboss.errai.idea.plugin;

import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.lang.Language;
import com.intellij.lang.java.JavaLanguage;
import com.intellij.openapi.components.ProjectComponent;
import com.intellij.openapi.project.Project;
import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.ElementPatternCondition;
import com.intellij.patterns.InitialPatternCondition;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiLiteralExpression;
import com.intellij.psi.PsiReference;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import com.intellij.psi.impl.source.resolve.reference.ReferenceProvidersRegistry;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.util.PsiUtil;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * @author Mike Brock
 */
public class ErraiUISupport implements ProjectComponent {
  public static final String JAVAX_INJECT = "javax.inject.Inject";
  public static final String TEMPLATED_ANNOTATION_NAME = "org.jboss.errai.ui.shared.api.annotations.Templated";
  public static final String DATAFIELD_ANNOTATION_NAME = "org.jboss.errai.ui.shared.api.annotations.DataField";
  public static final String GWT_COMPOSITE_REF = "com.google.gwt.user.client.ui.Composite";
  public static final String WIDGET_TYPE = "com.google.gwt.user.client.ui.Widget";
  public static final String ELEMENT_TYPE = "com.google.gwt.dom.client.Element";

  public ErraiUISupport(final Project project, ReferenceProvidersRegistry registry) {
    final PsiReferenceRegistrar registrar = registry.getRegistrar(Language.findInstance(JavaLanguage.class));
    registrar.registerReferenceProvider(new AnnotationMatchingPattern(TEMPLATED_ANNOTATION_NAME),
        new PsiReferenceProvider() {
          @NotNull
          @Override
          public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext processingContext) {
            return new TemplateReference[]{new TemplateReference(project, (PsiLiteralExpression) element, false)};
          }
        }
    );

    registrar.registerReferenceProvider(new AnnotationMatchingPattern(DATAFIELD_ANNOTATION_NAME),
        new PsiReferenceProvider() {
          @NotNull
          @Override
          public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
            return new DatafieldReference[]{new DatafieldReference(project, (PsiLiteralExpression) element, false)};

          }
        });
  }

  public void initComponent() {
    // TODO: insert component initialization logic here
  }

  public void disposeComponent() {
    // TODO: insert component disposal logic here
  }

  @NotNull
  public String getComponentName() {
    return "ErraiUISupport";
  }

  public void projectOpened() {
    // called when project is opened
  }

  public void projectClosed() {
    // called when project is being closed
  }

  private static class DatafieldReference extends PsiReferenceBase<PsiLiteralExpression> {
    private final Project project;

    public DatafieldReference(Project project, PsiLiteralExpression element, boolean soft) {
      super(element, soft);
      this.project = project;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
      return null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
      final Util.TemplateMetaData templateFile = Util.getTemplateMetaData(getElement(), project);
      if (templateFile != null) {
        final ArrayList<Object> list = new ArrayList<Object>();
        final String rootNode = templateFile.getTemplateReference().getRootNode();
        for (final String value : Util.findAllDataFieldTags(templateFile, project, false).keySet()) {
          if (rootNode.equals(value)) continue;

          list.add(LookupElementBuilder.create(value));
        }
        return list.toArray();
      }
      else {
        return new Object[0];
      }
    }
  }

  private static class TemplateReference extends PsiReferenceBase<PsiLiteralExpression> {
    private final Project project;

    public TemplateReference(Project project, PsiLiteralExpression element, boolean soft) {
      super(element, soft);
      this.project = project;
    }

    private PsiDirectory getBaseDir() {
      final PsiClass psiClass = PsiUtil.getTopLevelClass(getElement());

      if (psiClass != null) {
        final PsiFile containingFile = psiClass.getContainingFile();

        if (containingFile != null) {
          return containingFile.getOriginalFile().getParent();
        }
      }
      return null;
    }

    @Nullable
    public PsiElement resolve() {
      final PsiDirectory baseDir = getBaseDir();

      if (baseDir != null) {
        final String inputValue = getValue();
        return baseDir.findFile(inputValue);
      }
      return null;
    }

    public Object[] getVariants() {
      List<Object> list = new ArrayList<Object>();

      final Util.TemplateMetaData templateMetaData = Util.getTemplateMetaData(getElement(), project);

      final PsiDirectory baseDir = getBaseDir();
      final String inputValue = getValue().replaceAll("IntellijIdeaRulezzz", "").trim();
      final Util.TemplateReference reference = Util.parseReference(inputValue);
      final PsiFile file = baseDir.findFile(reference.getFileName());

      final Collection<Util.DataFieldReference> allDataFieldTags;
      if (file != null) {
        allDataFieldTags = Util.findAllDataFieldTags(templateMetaData, project, true).values();
      }
      else {
        allDataFieldTags = Collections.emptyList();
      }

      if (allDataFieldTags.isEmpty()) {
        for (final PsiElement element : baseDir.getChildren()) {
          if (element.getContainingFile().getName().endsWith(".html")) {
            list.add(LookupElementBuilder.create((PsiFile) element));
          }
        }
      }
      else {
        for (Util.DataFieldReference dataFieldReference : allDataFieldTags) {
          list.add(LookupElementBuilder.create(reference.getFileName() + "#" + dataFieldReference.getDataFieldName()));
        }
      }

      return list.toArray();
    }
  }

  private static class AnnotationMatchingPattern implements ElementPattern<PsiLiteralExpression> {
    private final String type;
    private final ElementPatternCondition<PsiLiteralExpression> patternCondition =
        new ElementPatternCondition<PsiLiteralExpression>(new InitialPatternCondition<PsiLiteralExpression>(PsiLiteralExpression.class) {
          @Override
          public boolean accepts(@Nullable Object o, ProcessingContext context) {
            if (o instanceof PsiLiteralExpression) {
              final PsiAnnotation parentOfType = PsiTreeUtil.getParentOfType((PsiLiteralExpression) o, PsiAnnotation.class);
              return parentOfType != null && parentOfType.getQualifiedName().equals(type);
            }
            return false;
          }
        });

    private AnnotationMatchingPattern(String type) {
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
}
