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

package org.jboss.errai.idea.plugin.ui.model;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiNameValuePair;
import com.intellij.psi.xml.XmlTag;
import org.jboss.errai.idea.plugin.ui.TemplateDataField;
import org.jboss.errai.idea.plugin.ui.TemplateUtil;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Map;

/**
 * @author Mike Brock
 */
public class TemplateMetaData {
  private final TemplateExpression templateExpression;
  private boolean defaultReference;
  private final PsiNameValuePair attribute;
  private final PsiClass templateClass;
  private final VirtualFile templateFile;
  private final XmlTag rootTag;
  private final Project project;

  public TemplateMetaData(TemplateExpression templateExpression,
                          boolean defaultReference,
                          PsiNameValuePair attribute,
                          PsiClass templateClass,
                          VirtualFile templateFile,
                          XmlTag rootTag,
                          Project project) {
    this.templateExpression = templateExpression;
    this.defaultReference = defaultReference;
    this.attribute = attribute;
    this.templateClass = templateClass;
    this.templateFile = templateFile;
    this.rootTag = rootTag;
    this.project = project;
  }

  public TemplateExpression getTemplateExpression() {
    return templateExpression;
  }

  public boolean isDefaultReference() {
    return defaultReference;
  }

  public PsiClass getTemplateClass() {
    return templateClass;
  }

  public VirtualFile getTemplateFile() {
    return templateFile;
  }

  public PsiNameValuePair getAttribute() {
    return attribute;
  }

  public XmlTag getRootTag() {
    return rootTag;
  }

  @NotNull
  public Map<String, TemplateDataField> getAllDataFieldsInTemplate(boolean includeRootTag) {
    return TemplateUtil.findAllDataFieldTags(this, project, includeRootTag);
  }

  public Map<String, ConsolidateDataFieldElementResult> getConsolidatedDataFields() {
    if (templateClass == null) {
      return Collections.emptyMap();
    }
    return TemplateUtil.getConsolidatedDataFields(templateClass, project);
  }
}
