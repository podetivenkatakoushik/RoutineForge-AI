package com.routineforge.app.ai

import com.routineforge.app.domain.model.RoutineProposal
import com.routineforge.app.recorder.model.WorkflowCandidate
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class GeminiAIProvider(
    private val apiKey: String = AIConfig.geminiApiKey,
    private val mockFallback: AIProvider = MockAIProvider()
) : AIProvider {

    override val name: String = "Google Gemini AI Engine"
    override val isAvailable: Boolean = apiKey.isNotBlank()

    override suspend fun generateRoutine(candidate: WorkflowCandidate): Result<RoutineProposal> {
        if (!isAvailable) {
            return mockFallback.generateRoutine(candidate)
        }

        return withContext(Dispatchers.IO) {
            try {
                val systemPrompt = AIPromptBuilder.buildSystemPrompt()
                val userPrompt = AIPromptBuilder.buildUserPrompt(candidate)
                val combinedPrompt = "$systemPrompt\n\nUSER INPUT:\n$userPrompt"

                val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"
                val url = URL(endpoint)
                val conn = url.openConnection() as HttpURLConnection
                conn.requestMethod = "POST"
                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                conn.doOutput = true
                conn.connectTimeout = 10000
                conn.readTimeout = 10000

                val requestBody = JSONObject().apply {
                    val contents = JSONArray().apply {
                        val contentObj = JSONObject().apply {
                            val parts = JSONArray().apply {
                                val part = JSONObject().apply {
                                    put("text", combinedPrompt)
                                }
                                put(part)
                            }
                            put("parts", parts)
                        }
                        put(contentObj)
                    }
                    put("contents", contents)

                    val genConfig = JSONObject().apply {
                        put("responseMimeType", "application/json")
                    }
                    put("generationConfig", genConfig)
                }

                OutputStreamWriter(conn.outputStream, "UTF-8").use { writer ->
                    writer.write(requestBody.toString())
                    writer.flush()
                }

                val responseCode = conn.responseCode
                if (responseCode == 200) {
                    val responseText = BufferedReader(InputStreamReader(conn.inputStream, "UTF-8")).use { reader ->
                        reader.readText()
                    }

                    val jsonResp = JSONObject(responseText)
                    val candidatesArray = jsonResp.optJSONArray("candidates")
                    val firstCandidate = candidatesArray?.optJSONObject(0)
                    val contentObj = firstCandidate?.optJSONObject("content")
                    val partsArray = contentObj?.optJSONArray("parts")
                    val rawAiJson = partsArray?.optJSONObject(0)?.optString("text") ?: ""

                    when (val validResult = RoutineProposalValidator.parseAndValidate(rawAiJson)) {
                        is RoutineProposalValidationResult.Success -> Result.success(validResult.proposal)
                        is RoutineProposalValidationResult.Error -> mockFallback.generateRoutine(candidate)
                    }
                } else {
                    mockFallback.generateRoutine(candidate)
                }
            } catch (e: Exception) {
                mockFallback.generateRoutine(candidate)
            }
        }
    }
}

object AIConfig {
    val geminiApiKey: String
        get() = System.getProperty("GEMINI_API_KEY")
            ?: System.getenv("GEMINI_API_KEY")
            ?: ""
}
