package org.jboss.errai.idea.plugin;

import com.intellij.codeInsight.daemon.DaemonCodeAnalyzer;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceBase;
import com.intellij.psi.impl.source.xml.XmlAttributeImpl;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Mike Brock
 */
public class XmlDatafieldReference extends PsiReferenceBase<PsiElement> {
  private final Project project;

  public XmlDatafieldReference(Project project, XmlAttribute element, boolean soft) {
    super(element, soft);
    this.project = project;
  }

  public static class DataFieldRef {
    private final String name;
    private final String fileName;

    public DataFieldRef(String name, String fileName) {
      this.name = name;
      this.fileName = fileName;
    }

    public String getName() {
      return name;
    }

    public String getFileName() {
      return fileName;
    }
  }

  private List<DataFieldRef> findLinkedTemplateAndDataFields() {
    DaemonCodeAnalyzer.getInstance(project).restart();

    PsiElement el = getElement();
    while (!(el instanceof XmlFile) && el != null) {
      el = el.getParent();
    }

    if (el == null) {
      return Collections.emptyList();
    }

    final List<DataFieldRef> dataFieldRefs = new ArrayList<DataFieldRef>();

    final XmlFile xmlFile = (XmlFile) el;
    final Map<String, Util.DataFieldReference> allDataFieldTags
        = Util.findAllDataFieldTags(xmlFile, xmlFile.getRootTag(), true);

    final Set<PsiClass> owners = Util.getOwners(xmlFile, project);
    for (PsiClass psiClass : owners) {
      final Collection<Util.AnnotationSearchResult> allAnnotatedElements
          = Util.findAllAnnotatedElements(psiClass, ErraiFrameworkSupport.DATAFIELD_ANNOTATION_NAME);

      final Collection<String> strings = Util.extractDataFieldList(allAnnotatedElements);

      for (String dataField : strings) {
        if (allDataFieldTags.containsKey(dataField)) continue;

        dataFieldRefs.add(new DataFieldRef(dataField, psiClass.getQualifiedName()));
      }
    }

    return dataFieldRefs;
  }

  @Override
  public PsiElement handleElementRename(String newElementName) throws IncorrectOperationException {
    final PsiElement element = getElement();
    ((XmlAttributeImpl) element).setValue(newElementName);
    return element;
  }

  @Nullable
  @Override
  public PsiElement resolve() {
    return ((XmlAttribute) getElement()).getValueElement();
  }

  @NotNull
  @Override
  public Object[] getVariants() {
    List<Object> results = new ArrayList<Object>();
    for (DataFieldRef ref : findLinkedTemplateAndDataFields()) {
      results.add(LookupElementBuilder.create(ref.getName()).withTypeText(ref.getFileName(), true));
    }
    return results.toArray();
  }
}
