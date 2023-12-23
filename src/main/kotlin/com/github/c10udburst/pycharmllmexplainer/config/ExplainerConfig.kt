package com.github.c10udburst.pycharmllmexplainer.config

import com.intellij.openapi.util.registry.RegistryManager

class ExplainerConfig {
    companion object {
        private const val REGISTRY_KEY = "com.github.c10udburst.pycharmllmexplainer"
        private const val MODEL_KEY = "${REGISTRY_KEY}.model"
        private const val ENDPOINT_KEY = "${REGISTRY_KEY}.endpoint"
        private const val PROMPT_KEY = "${REGISTRY_KEY}.prompt"
        val instance = ExplainerConfig()
    }

    var model: String = RegistryManager.getInstance().get(MODEL_KEY).asString()
        set(value) {
            field = value
            // save to registry
            RegistryManager.getInstance().get(MODEL_KEY).setValue(value)
        }
    var endpoint: String = RegistryManager.getInstance().get(ENDPOINT_KEY).asString()
        set(value) {
            field = value
            // save to registry
            RegistryManager.getInstance().get(ENDPOINT_KEY).setValue(value)
        }

    var prompt: String = RegistryManager.getInstance().get(PROMPT_KEY).asString()
        set(value) {
            field = value
            // save to registry
            RegistryManager.getInstance().get(PROMPT_KEY).setValue(value)
        }
}