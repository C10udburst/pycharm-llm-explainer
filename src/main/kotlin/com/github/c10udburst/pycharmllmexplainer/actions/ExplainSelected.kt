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
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.psi.util.childrenOfType
import com.intellij.psi.util.elementType
import com.jetbrains.python.psi.PyAnnotation
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
        val whitespaceWidth = editor.settings.getTabSize(e.project)
        while (element !is PyFunction) {
            element = element.parent ?: return
        }

        ToolWindowManager.getInstance(e.project!!).getToolWindow("Explain")?.remove()
        ToolWindowManager.getInstance(e.project!!).registerToolWindow(
            RegisterToolWindowTask(
                id = "Explain",
                anchor = ToolWindowAnchor.LEFT,
                icon = null,
                contentFactory = ExplainToolWindowFactory(element.toCompressedString(whitespaceWidth)),
                canCloseContent = false
            )
        ).show()
    }


    private fun getElement(editor: Editor, file: PsiFile): PsiElement? {
        val caretModel = editor.caretModel
        val position = caretModel.offset
        return file.findElementAt(position)
    }

    private fun PsiElement.removeComments() {
        childrenOfType<PsiComment>().forEach { it.delete() }
        children.forEach { it.removeComments() }
    }

    private fun PsiElement.removeTypeAnnotations() {
        childrenOfType<PyAnnotation>().forEach { it.delete() }
        children.forEach { it.removeTypeAnnotations() }
    }

    private fun PyFunction.toCompressedString(whitespaceWidth: Int): String {
        val parameters = parameterList.parameters // we can remove default values and the LLM can infer the types
            .filter { it.name != null }
            .joinToString(",") { it.name!! }

        var result = "def $name(${parameters}):\n"

        var statements = statementList // we can filter out comments and docstrings
            .statements
            .filter {
                it.isValid // filter out invalid statements
            }
            .filter { // filter out docstrings
                it.firstChild != it.lastChild || it.firstChild?.firstChild.elementType.toString() != "Py:DOCSTRING"
            }
            .map { // make PsiElements editable
                it.copy()
            }

        statements.forEach { // filter out stuff LLM should not need
            it.removeComments()
            it.removeTypeAnnotations()
        }

        statements = statements.filter { // filter out empty statements
            it != null && it.text.isNotBlank()
        }

        result += statements.take(25).joinToString("\n") {
            "\t${it.text}"
        }
        if (statements.size > 25) {
            if (statements.size > 50) {
                result += "\n\t..."
            }
            result += "\n" + statements.takeLast(25).joinToString("\n") {
                "\t${it.text}"
            }
        }

        return result
            .replace(" ".repeat(whitespaceWidth), "\t") // replace spaces with tabs as tab is only one token
    }
}