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
public class BeanBindingMetaData {
  private final VersionSpec versionSpec;
  private final BindingType bindingType;

  private final PsiClass templateClass;
  private final PsiClass boundClass;

  private final long templateClassModifyTime;

  private final Collection<AnnotationSearchResult> modelAnnotations;

  public BeanBindingMetaData(PsiClass templateClass) {
    this.versionSpec = ErraiVersion.get(templateClass);

    this.templateClass = templateClass;
    templateClassModifyTime = templateClass.getContainingFile().getOriginalFile().getModificationStamp();

    /*
    This is so, if the user has specified more than one, we can detect and reference all of them for
    error highlighting;
   */
    modelAnnotations
        = new ArrayList<AnnotationSearchResult>(Util.findAllAnnotatedElements(templateClass, Types.AUTO_BOUND));
    modelAnnotations.addAll(Util.findAllAnnotatedElements(templateClass, Types.MODEL));

    if (modelAnnotations.size() == 1) {
      AnnotationSearchResult result = modelAnnotations.iterator().next();
      bindingType = getBindingType(result);
      boundClass = getTypeByBindingType(result, templateClass);
    }
    else if (modelAnnotations.size() > 1) {
      bindingType = BindingType.DATA_BINDER;
      boundClass = null;
    }
    else {
      bindingType = BindingType.UNKNOWN;
      boundClass = null;
    }
  }

  private BindingType getBindingType(AnnotationSearchResult searchResult) {
    final String qualifiedName = searchResult.getAnnotation().getQualifiedName();
    if (qualifiedName == null) {
      return BindingType.UNKNOWN;
    }
    if (versionSpec == VersionSpec.V3_0 && qualifiedName.equals(Types.MODEL)) {
      return BindingType.RAW_MODEL;
    }
    else if (qualifiedName.equals(Types.AUTO_BOUND)) {
      return BindingType.DATA_BINDER;
    }
    return BindingType.UNKNOWN;
  }

  private PsiClass getTypeByBindingType(AnnotationSearchResult result, PsiClass templateClass) {
    final BindingType bindType = getBindingType(result);
    if (bindType == BindingType.UNKNOWN) {
      return null;
    }

    if (versionSpec == VersionSpec.V3_0 && bindType == BindingType.RAW_MODEL) {
      return Util.getTypeOfElement(result.getOwningElement());
    }

    if (bindType == BindingType.DATA_BINDER) {
      return Util.getErasedTypeParam(
          templateClass.getProject(),
          ((PsiVariable) result.getOwningElement()).getType().getCanonicalText()
      );
    }

    return null;
  }

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
