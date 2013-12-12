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

package org.jboss.errai.idea.plugin.ui;

import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;

/**
 * @author Mike Brock
 */
public class TemplateDataField {
  private final XmlTag tag;
  private final String dataFieldName;

  public TemplateDataField(XmlTag tag, String dataFieldName) {
    this.tag = tag;
    this.dataFieldName = dataFieldName;
  }

  public XmlTag getTag() {
    return tag;
  }

  public void setDataField(String text) {
    if (tag != null) {
      final XmlAttribute value = tag.getAttribute("value");
      if (value != null) {
        value.setValue(text);
      }
    }
  }

  public XmlAttribute getDataFieldAttribute() {
    if (tag != null) {
      if (tag.getAttribute("data-field") != null) {
        return tag.getAttribute("data-field");
      } else {
        return tag.getAttribute("id");
      }
    }
    return null;
  }

  public String getDataFieldName() {
    return dataFieldName;
  }

  @Override
  public String toString() {
    return "TemplateDataField{" +
        "tag=" + tag +
        ", dataFieldName='" + dataFieldName + '\'' +
        '}';
  }
}
