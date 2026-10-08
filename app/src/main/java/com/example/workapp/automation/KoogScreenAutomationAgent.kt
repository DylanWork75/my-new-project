package com.example.workapp.automation

import ai.koog.agents.core.agent.AIAgent
import ai.koog.agents.core.tools.ToolRegistry
import ai.koog.prompt.executor.clients.openai.OpenAILLMClient
import ai.koog.prompt.executor.clients.openai.OpenAIModels
import ai.koog.prompt.executor.llms.MultiLLMPromptExecutor
import com.example.workapp.utils.MobileTestTools

class KoogScreenAutomationAgent(openAiApiKey: String) {

    private val agent = AIAgent(
        promptExecutor = MultiLLMPromptExecutor(OpenAILLMClient(openAiApiKey)),
        systemPrompt = """
            You automate the WorkApp Android interface using the registered screen tools.
            Screen coordinates are absolute pixels from the top-left of the device display.
            Only use coordinates inside the WorkApp window. The tools reject gestures when
            another app is in front. A pinch scale factor above 1 spreads the fingers to zoom
            in; a factor below 1 brings them together to zoom out. Get the active window bounds
            before choosing coordinates. Keep actions deliberate and never guess if the target
            is ambiguous.
        """.trimIndent(),
        llmModel = OpenAIModels.Chat.GPT4o,
        toolRegistry = ToolRegistry {
            tools(MobileTestTools())
        }
    )

    suspend fun run(instruction: String): String = agent.run(instruction)
}
