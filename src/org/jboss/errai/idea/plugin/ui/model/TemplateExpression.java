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

/**
* @author Mike Brock
*/
public class TemplateExpression {
  private final String fileName;
  private final String rootNode;

  public TemplateExpression(String fileName, String rootNode) {
    this.fileName = fileName;
    this.rootNode = rootNode;
  }

  public String getFileName() {
    return fileName;
  }

  public String getRootNode() {
    return rootNode;
  }

  public boolean hasRootNode() {
    return !"".equals(rootNode);
  }
}
