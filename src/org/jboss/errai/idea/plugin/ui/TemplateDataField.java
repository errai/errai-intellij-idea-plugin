package org.jboss.errai.idea.plugin.ui;

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
