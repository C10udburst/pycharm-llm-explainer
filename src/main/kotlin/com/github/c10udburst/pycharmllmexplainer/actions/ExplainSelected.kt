package com.github.c10udburst.pycharmllmexplainer.actions

import com.github.c10udburst.pycharmllmexplainer.window.ExplainToolWindowFactory
import com.intellij.openapi.actionSystem.ActionUpdateThread
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.wm.RegisterToolWindowTask
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.elementType
import com.jetbrains.python.psi.PyFunction
import org.jetbrains.annotations.NotNull

class ExplainSelected: AnAction() {
    override fun getActionUpdateThread(): ActionUpdateThread {
        return ActionUpdateThread.BGT
    }

    override fun update(@NotNull e: AnActionEvent) {
        e.presentation.isEnabledAndVisible = false
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return
        var element = getElement(editor, psiFile) ?: return
        while (element !is PyFunction) {
            element = element.parent ?: return
        }
        e.presentation.isEnabledAndVisible = true
    }

    override fun actionPerformed(@NotNull e: AnActionEvent) {
        val editor = e.getData(CommonDataKeys.EDITOR) ?: return
        val psiFile = e.getData(CommonDataKeys.PSI_FILE) ?: return
        var element = getElement(editor, psiFile) ?: return
        while (element !is PyFunction) {
            element = element.parent ?: return
        }

        ToolWindowManager.getInstance(e.project!!).getToolWindow("Explain")?.remove()
        ToolWindowManager.getInstance(e.project!!).registerToolWindow(
            RegisterToolWindowTask(
                id = "Explain",
                anchor = ToolWindowAnchor.LEFT,
                icon = null,
                contentFactory = ExplainToolWindowFactory(element.toCompressedString()),
                canCloseContent = false
            )
        ).show()
    }


    private fun getElement(editor: Editor, file: PsiFile): PsiElement? {
        val caretModel = editor.caretModel
        val position = caretModel.offset
        return file.findElementAt(position)
    }

    private fun PyFunction.toCompressedString(): String {
        val parameters = parameterList.parameters
            .filter { it.name != null }
            .joinToString(",") { it.name!! }

        var result = "def $name(${parameters}):\n"

        val statements = statementList
            .statements
            .filter {
                it.firstChild != it.lastChild || it.firstChild?.firstChild.elementType.toString() != "Py:DOCSTRING"
            }

        result += statements.take(20).joinToString("\n") {
            "\t${it.text}"
        }
        if (statements.size > 20) {
            if (statements.size > 40) {
                result += "\n\t..."
            }
            result += "\n" + statements.takeLast(20).joinToString("\n") {
                "\t${it.text}"
            }
        }

        return result
    }
}