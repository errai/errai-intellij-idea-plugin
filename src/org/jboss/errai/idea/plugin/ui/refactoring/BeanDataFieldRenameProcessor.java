package org.jboss.errai.idea.plugin.ui.refactoring;

import com.google.common.collect.Multimap;
import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiVariable;
import com.intellij.psi.util.PsiUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.refactoring.rename.RenameJavaVariableProcessor;
import com.intellij.util.IncorrectOperationException;
import org.jboss.errai.idea.plugin.ui.TemplateDataField;
import org.jboss.errai.idea.plugin.ui.TemplateUtil;
import org.jboss.errai.idea.plugin.ui.model.TemplateMetaData;
import org.jboss.errai.idea.plugin.util.DefaultPolicy;
import org.jboss.errai.idea.plugin.util.FakeNamedPsi;
import org.jboss.errai.idea.plugin.util.Types;
import org.jboss.errai.idea.plugin.util.Util;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Map;

/**
 * @author Mike Brock
 */
public class BeanDataFieldRenameProcessor extends RenameJavaVariableProcessor {
  @Override
  public boolean canProcessElement(@NotNull PsiElement element) {
    if (element instanceof PsiVariable) {
      if (Util.elementIsAnnotated(element, Types.DATAFIELD)) {
        final PsiAnnotation psiAnnotation = Util.getAnnotationFromElement(element, Types.DATAFIELD);
        final String value = Util.getAttributeValue(psiAnnotation, "value", DefaultPolicy.NULL);
        return value == null;
      }
    }
    return false;
  }

  @Override
  public void prepareRenaming(PsiElement element, String newName, Map<PsiElement, String> allRenames) {
    final PsiClass topLevelClass = PsiUtil.getTopLevelClass(element);
    if (topLevelClass == null) {
      return;
    }

    final PsiVariable psiVariable = (PsiVariable) element;
    final String oldName = psiVariable.getName();

    final TemplateMetaData templateMetaData = TemplateUtil.getTemplateMetaData(topLevelClass);

    final Multimap<String, TemplateDataField> dataFields = templateMetaData.getAllDataFieldsInTemplate(false);
    final Collection<TemplateDataField> dataField = dataFields.get(oldName);

    if (dataField.size() != 1) {
      return;
    }

    final XmlAttribute dataFieldAttribute = dataField.iterator().next().getDataFieldAttribute();
    if (dataFieldAttribute != null && dataFieldAttribute.getValueElement() != null) {
      FakeNamedPsi fakeNamedPsi = new FakeNamedPsi(dataFieldAttribute) {
        @Override
        public PsiElement setName(@NonNls @NotNull String name) throws IncorrectOperationException {
          dataFieldAttribute.setValue(name);
          return dataFieldAttribute;
        }

        @Override
        public String getName() {
          return dataFieldAttribute.getValue();
        }
      };

      allRenames.put(fakeNamedPsi, newName);
    }
  }
}
