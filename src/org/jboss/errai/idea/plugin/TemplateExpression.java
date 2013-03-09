package org.jboss.errai.idea.plugin;

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
}
