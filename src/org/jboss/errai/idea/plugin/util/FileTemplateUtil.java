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

package org.jboss.errai.idea.plugin.util;

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;

import java.util.Map;
import java.util.Properties;

/**
 * @author Mike Brock
 */
public class FileTemplateUtil {
  public static PsiElement createFileFromTemplate(String templateName, String fileName, PsiDirectory directory, Map<String, String> properties) {
    final FileTemplate template = FileTemplateManager.getInstance().getJ2eeTemplate(templateName);
    final Properties props = FileTemplateManager.getInstance().getDefaultProperties();

    for (Map.Entry<String, String> entry : properties.entrySet()) {
      props.put(entry.getKey(), entry.getValue());
    }

    try {
      return com.intellij.ide.fileTemplates.FileTemplateUtil.createFromTemplate(template, fileName, props, directory);
    }
    catch (Exception e) {
      throw new RuntimeException("could not create template", e);
    }
  }
}
