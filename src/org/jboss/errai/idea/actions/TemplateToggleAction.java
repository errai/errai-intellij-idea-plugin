package org.jboss.errai.idea.actions;

import com.intellij.codeInsight.hint.HintManager;
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
      final List<PsiClass> owners = new ArrayList<PsiClass>(TemplateUtil.getTemplateOwners(data));
      if (owners.isEmpty()) {
        HintManager.getInstance().showInformationHint(editor, "Could not find an owning backing bean for template");
      }
      else if (owners.size() > 1) {
        ;
        final JBList list = new JBList();
        DefaultListModel model = new DefaultListModel();
        model.setSize(owners.size());

        int i = 0;
        for (PsiClass psiClass : owners) {
          model.set(i++, psiClass.getQualifiedName());
        }

        list.setModel(model);

        final PopupChooserBuilder listPopupBuilder = JBPopupFactory.getInstance().createListPopupBuilder(list);
        listPopupBuilder.setTitle("Select a template class");
        listPopupBuilder.setItemChoosenCallback(new Runnable() {
          @Override
          public void run() {
            owners.get(list.getSelectedIndex()).navigate(true);
          }
        }).createPopup().showInBestPositionFor(editor);
      }
      else {
        owners.iterator().next().navigate(true);
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
