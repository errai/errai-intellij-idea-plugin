package org.jboss.errai.idea.plugin.actions;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.DataKeys;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.ui.popup.PopupChooserBuilder;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiJavaFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.ui.ListCellRendererWrapper;
import com.intellij.ui.components.JBList;
import org.jboss.errai.idea.plugin.ui.TemplateUtil;
import org.jboss.errai.idea.plugin.ui.model.TemplateMetaData;

import javax.swing.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Mike Brock
 */
public class TemplateToggleAction extends AnAction {
  @Override
  public void actionPerformed(AnActionEvent e) {
    final PsiFile data = e.getData(DataKeys.PSI_FILE);

    final Editor editor = e.getData(DataKeys.EDITOR);
    if (editor == null) {
      return;
    }

    if (data instanceof XmlFile) {
      final List<TemplateMetaData> owners = new ArrayList<TemplateMetaData>(TemplateUtil.getTemplateOwners(data));
      if (owners.isEmpty()) {
        HintManager.getInstance().showInformationHint(editor, "Could not find an owning backing bean for template");
      }
      else if (owners.size() > 1) {
        final JBList list = new JBList();
        DefaultListModel model = new DefaultListModel();
        model.setSize(owners.size());

        list.setCellRenderer(new ListCellRendererWrapper<TemplateMetaData>() {
          @Override
          public void customize(JList list, TemplateMetaData md, int index, boolean selected, boolean hasFocus) {
            if (md != null) {
              setIcon(AllIcons.Nodes.Class);
              setText(md.getTemplateClass().getQualifiedName());

              final String rootNode = md.getTemplateExpression().getRootNode();
              if (rootNode.length() > 0) {
                if (hasFocus) {
                  HintManager.getInstance().showInformationHint(editor,
                      md.getTemplateClass().getName() + " is rooted at: #" + rootNode);
                }
              }
            }
          }
        });

        int i = 0;
        for (TemplateMetaData metaData : owners) {
          model.set(i++, metaData);
        }

        list.setModel(model);

        final PopupChooserBuilder listPopupBuilder = JBPopupFactory.getInstance().createListPopupBuilder(list);
        listPopupBuilder.setTitle("Select a template class");
        listPopupBuilder.setItemChoosenCallback(new Runnable() {
          @Override
          public void run() {
            owners.get(list.getSelectedIndex()).getTemplateClass().navigate(true);
          }
        }).createPopup().showInBestPositionFor(editor);
      }
      else {
        owners.iterator().next().getTemplateClass().navigate(true);
      }
    }
    else if (data instanceof PsiJavaFile) {
      for (PsiClass cls : ((PsiJavaFile) data).getClasses()) {
        final TemplateMetaData templateMetaData = TemplateUtil.getTemplateMetaData(cls);
        if (templateMetaData != null) {
          templateMetaData.getRootTag().getContainingFile().navigate(true);
          break;
        }
      }
    }
  }
}
