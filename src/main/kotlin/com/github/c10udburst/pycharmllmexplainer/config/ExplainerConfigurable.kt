package com.github.c10udburst.pycharmllmexplainer.config

import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.panel

class ExplainerConfigurable: BoundConfigurable("LLM Explainer") {
    override fun createPanel(): DialogPanel =
        panel {
            row {
                text("You can configure the LLM Explainer here.<br />" +
                        "This extension requires an <a href=\"https://ollama.ai/\">ollama.ai</a> server to be running.<br />" +
                        "You may need to install the selected model, consult ollama.ai for more information.")
            }
            row {
                label("Model")
                textField()
                    .bindText(ExplainerConfig.instance::model)
            }
            row {
                label("API endpoint")
                textField()
                    .bindText(ExplainerConfig.instance::endpoint)
                    .columns(25)
            }
            row {
                label("Prompt")
                textArea()
                    .bindText(ExplainerConfig.instance::prompt)
                    .columns(25)
            }
        }

}