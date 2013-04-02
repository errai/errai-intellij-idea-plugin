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

package org.jboss.errai.idea.plugin.actions;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.util.IconLoader;
import org.jboss.errai.idea.plugin.util.Util;

import javax.swing.*;

/**
 * @author Mike Brock
 */
public class ErraiActionGroup extends DefaultActionGroup {
  public static final Icon ERRAI_ICON = IconLoader.findIcon("/icons/errai_icon_16px.png");

  public ErraiActionGroup() {
    super("Errai", true);
    getTemplatePresentation().setDescription("Errai");
    getTemplatePresentation().setIcon(ERRAI_ICON);
  }

  @Override
  public void update(AnActionEvent e) {
    e.getPresentation().setVisible(Util.isInsideProjectSources(e));
  }
}
