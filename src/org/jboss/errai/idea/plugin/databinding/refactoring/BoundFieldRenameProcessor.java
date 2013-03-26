/*
 * Copyright 2013 Red Hat, Inc.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package org.jboss.errai.idea.plugin.databinding.refactoring;

import com.intellij.psi.PsiAnnotation;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiVariable;
import com.intellij.psi.util.PsiUtil;
import com.intellij.refactoring.rename.RenameJavaVariableProcessor;
import org.jboss.errai.idea.plugin.databinding.DataBindUtil;
import org.jboss.errai.idea.plugin.databinding.model.BeanBindingMetaData;
import org.jboss.errai.idea.plugin.databinding.model.PropertyInfo;
import org.jboss.errai.idea.plugin.util.DefaultPolicy;
import org.jboss.errai.idea.plugin.util.Types;
import org.jboss.errai.idea.plugin.util.Util;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

/**
 * @author Mike Brock
 */
public class BoundFieldRenameProcessor extends RenameJavaVariableProcessor {
  @Override
  public boolean canProcessElement(@NotNull PsiElement element) {
    if (element instanceof PsiVariable) {
      if (Util.elementIsAnnotated(element, Types.BOUND)) {
        final PsiAnnotation psiAnnotation = Util.getAnnotationFromElement(element, Types.BOUND);
        final String value = Util.getAttributeValue(psiAnnotation, "property", DefaultPolicy.NULL);
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

    final BeanBindingMetaData dataBindingMetaData = DataBindUtil.getDataBindingMetaData(topLevelClass);
    if (dataBindingMetaData == null) {
      return;
    }
    final Map<String, PropertyInfo> allProperties
        = DataBindUtil.getAllProperties(dataBindingMetaData.getBoundClass(), "");

    final PropertyInfo propertyInfo = allProperties.get(oldName);
    if (propertyInfo == null) {
      return;
    }

    final PsiField associatedField = propertyInfo.getAssociatedField();
    if (associatedField == null) {
      return;
    }

    super.prepareRenaming(associatedField, newName, allRenames);
  }
}
