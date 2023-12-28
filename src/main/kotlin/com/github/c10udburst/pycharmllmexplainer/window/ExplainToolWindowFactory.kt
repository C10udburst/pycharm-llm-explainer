package com.github.c10udburst.pycharmllmexplainer.window

import com.github.c10udburst.pycharmllmexplainer.ExplainerBundle
import com.github.c10udburst.pycharmllmexplainer.config.ExplainerConfig
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.components.htmlComponent
import io.github.amithkoujalgi.ollama4j.core.OllamaAPI
import org.intellij.markdown.flavours.gfm.GFMFlavourDescriptor
import org.intellij.markdown.html.HtmlGenerator
import org.intellij.markdown.parser.MarkdownParser
import kotlin.concurrent.thread

class ExplainToolWindowFactory(private val selection: String): ToolWindowFactory {

    override fun init(toolWindow: ToolWindow) {
        // as per {@link ToolWindowFactory} documentation, this should be done automatically, but it isn't
        toolWindow.stripeTitle = ExplainerBundle.message("toolwindow.stripe.com.github.c10udburst.pycharmllmexplainer.window")
    }

    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
        val content = htmlComponent(
            lineWrap = true
        )
        toolWindow.component.add(content)
        thread {
            val api = OllamaAPI(ExplainerConfig.instance.endpoint).apply {
                setVerbose(false)
                setRequestTimeoutSeconds(10)
            }
            val prompt = ExplainerConfig.instance.prompt
                .replace("{code}", selection)
            val result = api.askAsync(ExplainerConfig.instance.model, prompt)
            while (!result.isComplete) {
                content.text = result.stream.joinToString("\n").toHtml()
                Thread.sleep(250)
                if (toolWindow.isDisposed) {
                    result.interrupt()
                    return@thread
                }
            }
            content.text = result.response.toHtml()
        }
    }
}

private fun String.toHtml(): String {
    val flavour = GFMFlavourDescriptor()
    val parser = MarkdownParser(flavour)
    val tree = parser.buildMarkdownTreeFromString(this)
    return HtmlGenerator(this, tree, flavour, false).generateHtml()
}