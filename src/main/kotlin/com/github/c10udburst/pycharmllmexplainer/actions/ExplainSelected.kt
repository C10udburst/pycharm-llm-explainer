package com.github.c10udburst.pycharmllmexplainer.actions

import com.github.c10udburst.pycharmllmexplainer.window.ExplainToolWindowFactory
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.wm.RegisterToolWindowTask
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import org.jetbrains.annotations.NotNull

class ExplainSelected: AnAction() {
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun update(@NotNull e: AnActionEvent) {
        val project = e.project
        val editor = e.getData(CommonDataKeys.EDITOR)
        e.presentation.isEnabledAndVisible = project != null && editor?.selectionModel?.hasSelection() ?: false
    }

    override fun actionPerformed(@NotNull e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR)
        val selectionModel = editor?.selectionModel
        val selectedText = selectionModel?.selectedText ?: return
        val language = e.getData(CommonDataKeys.PSI_FILE)?.language?.id ?: return
        ToolWindowManager.getInstance(e.project!!).getToolWindow("Explain")?.remove()
        ToolWindowManager.getInstance(e.project!!).registerToolWindow(
            RegisterToolWindowTask(
                id = "Explain",
                anchor = ToolWindowAnchor.LEFT,
                icon = null,
                contentFactory = ExplainToolWindowFactory(selectedText, language),
                canCloseContent = false
            )
        ).show()
    }
}