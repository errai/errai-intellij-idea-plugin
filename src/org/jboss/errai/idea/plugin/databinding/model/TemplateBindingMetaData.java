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

package org.jboss.errai.idea.plugin.databinding.model;

import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiVariable;
import org.jboss.errai.idea.plugin.databinding.DataBindUtil;
import org.jboss.errai.idea.plugin.util.AnnotationSearchResult;
import org.jboss.errai.idea.plugin.util.ErraiVersion;
import org.jboss.errai.idea.plugin.util.Types;
import org.jboss.errai.idea.plugin.util.Util;
import org.jboss.errai.idea.plugin.util.VersionSpec;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

/**
 * @author Mike Brock
 */
public class TemplateBindingMetaData {
  private final VersionSpec versionSpec;
  private final BindingType bindingType;

  private final PsiClass templateClass;
  private final PsiClass boundClass;

  private final long templateClassModifyTime;

  private final Collection<AnnotationSearchResult> modelAnnotations;

  public TemplateBindingMetaData(PsiClass templateClass) {
    this.versionSpec = ErraiVersion.get(templateClass);

    this.templateClass = templateClass;
    templateClassModifyTime = templateClass.getContainingFile().getOriginalFile().getModificationStamp();

    /*
    This is so, if the user has specified more than one, we can detect and reference all of them for
    error highlighting;
   */
    modelAnnotations
        = new ArrayList<AnnotationSearchResult>(Util.findAllAnnotatedElements(templateClass, Types.AUTO_BOUND));

    if (modelAnnotations.size() == 1) {
      bindingType = BindingType.DATA_BINDER;
      AnnotationSearchResult result = modelAnnotations.iterator().next();
      boundClass = Util.getErasedTypeParam(
          templateClass.getProject(),
          ((PsiVariable) result.getOwningElement()).getType().getCanonicalText()
      );
    }
    else if (modelAnnotations.size() > 1) {
      bindingType = BindingType.DATA_BINDER;
      boundClass = null;
    }
    else if (versionSpec == VersionSpec.V3_0) {
      modelAnnotations.addAll(Util.findAllAnnotatedElements(templateClass, Types.MODEL));

      if (modelAnnotations.size() == 1) {
        bindingType = BindingType.RAW_MODEL;
        AnnotationSearchResult result = modelAnnotations.iterator().next();
        boundClass = Util.getTypeOfElement(result.getOwningElement());
      }
      else if (modelAnnotations.size() > 1) {
        bindingType = BindingType.RAW_MODEL;
        boundClass = null;
      }
      else {
        bindingType = BindingType.UNKNOWN;
        boundClass = null;
      }
    }
    else {
      bindingType = BindingType.UNKNOWN;
      boundClass = null;
    }
  }
//
//  public PsiClass getTemplateClass() {
//    return templateClass;
//  }


  public VersionSpec getVersionSpec() {
    return versionSpec;
  }

  public BindingType getBindingType() {
    return bindingType;
  }

  public Collection<AnnotationSearchResult> getModelAnnotations() {
    return modelAnnotations;
  }

  public boolean isValidBindableModel() {
    if (getModelAnnotations().size() > 1) {
      return false;
    }
    if (boundClass == null) {
      return false;
    }
    if (Util.elementIsAnnotated(boundClass, Types.BINDABLE)) {
      return true;
    }

    final Set<String> configuredBindableTypes = DataBindUtil.getConfiguredBindableTypes(boundClass.getProject());
    return configuredBindableTypes.contains(boundClass.getQualifiedName());
  }

  public boolean isCacheValid() {
    return boundClass != null && templateClassModifyTime == templateClass.getContainingFile().getOriginalFile().getModificationStamp();

  }

  public PsiClass getBoundClass() {
    return boundClass;
  }
}
