package com.github.c10udburst.pycharmllmexplainer.config

import com.github.c10udburst.pycharmllmexplainer.ExplainerBundle
import com.intellij.openapi.options.BoundConfigurable
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.panel

class ExplainerConfigurable: BoundConfigurable("LLM Explainer") {
    override fun createPanel(): DialogPanel =
        panel {
            row {
                text(ExplainerBundle.message("configurable.message"))
            }
            row {
                label(ExplainerBundle.message("configurable.model"))
                textField()
                    .bindText(ExplainerConfig.instance::model)
            }
            row {
                label(ExplainerBundle.message("configurable.api"))
                textField()
                    .bindText(ExplainerConfig.instance::endpoint)
                    .columns(25)
            }
            row {
                label(ExplainerBundle.message("configurable.prompt"))
                textArea()
                    .bindText(ExplainerConfig.instance::prompt)
                    .columns(25)
            }
        }

}