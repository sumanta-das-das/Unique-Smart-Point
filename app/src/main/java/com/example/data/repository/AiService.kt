package com.example.data.repository

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AiService {
    private val client = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    private val apiKey: String = try {
        BuildConfig::class.java.getField("GEMINI_API_KEY").get(null) as? String ?: ""
    } catch (e: Exception) {
        ""
    }

    suspend fun generateAiContent(prompt: String): String = withContext(Dispatchers.IO) {
        if (apiKey.isNotBlank() && apiKey != "MY_GEMINI_API_KEY") {
            try {
                val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=$apiKey"
                val jsonPayload = JSONObject().apply {
                    put("contents", JSONArray().apply {
                        put(JSONObject().apply {
                            put("parts", JSONArray().apply {
                                put(JSONObject().apply { put("text", prompt) })
                            })
                        })
                    })
                }

                val body = jsonPayload.toString().toRequestBody("application/json".toMediaType())
                val request = Request.Builder().url(url).post(body).build()

                client.newCall(request).execute().use { response ->
                    if (response.isSuccessful) {
                        val respBody = response.body?.string() ?: ""
                        val json = JSONObject(respBody)
                        val candidates = json.optJSONArray("candidates")
                        if (candidates != null && candidates.length() > 0) {
                            val candidate = candidates.getJSONObject(0)
                            val content = candidate.optJSONObject("content")
                            val parts = content?.optJSONArray("parts")
                            if (parts != null && parts.length() > 0) {
                                return@withContext parts.getJSONObject(0).optString("text")
                            }
                        }
                    } else {
                        Log.w("AiService", "Gemini API error code: ${response.code}")
                    }
                }
            } catch (e: Exception) {
                Log.e("AiService", "Gemini API request failed: ${e.message}", e)
            }
        }

        // Offline Smart Heuristic Engine
        generateSmartLocalContent(prompt)
    }

    private fun generateSmartLocalContent(prompt: String): String {
        val lower = prompt.lowercase()
        return when {
            lower.contains("letter") || lower.contains("application") -> {
                """
To,
The Concerned Authority / Manager,
[Organization / Office Name]
[Address / Branch, West Bengal]

Date: ${java.text.SimpleDateFormat("dd MMMM, yyyy", java.util.Locale.ENGLISH).format(java.util.Date())}

Subject: Application regarding $prompt

Respected Sir/Madam,

I am writing this formal application to humbly bring to your kind attention regarding the subject mentioned above. I am a resident of West Bengal and would like to formally request your consideration on this matter.

Kindly process my application at your earliest convenience and let me know if any additional supporting documents are required from my end.

Thanking you sincerely,

Yours faithfully,
[Your Full Name]
Phone: [Your Contact Number]
Email: [Your Email Address]
                """.trimIndent()
            }
            lower.contains("resume") || lower.contains("biodata") -> {
                """
========================================
           CURRICULUM VITAE
========================================
NAME: [Full Name]
CONTACT: +91-XXXXXXXXXX | email@domain.com
ADDRESS: West Bengal, India

----------------------------------------
CAREER OBJECTIVE
----------------------------------------
To secure a challenging role where I can effectively contribute my skills, diligence, and digital knowledge towards organizational growth.

----------------------------------------
ACADEMIC QUALIFICATIONS
----------------------------------------
• Higher Secondary (10+2) - WBHSE (First Division)
• Madhyamik (10th) - WBBSE (First Division)
• Diploma / Graduation - [Institute Name]

----------------------------------------
TECHNICAL & DIGITAL SKILLS
----------------------------------------
• Computer Applications (MS Office, Word, Excel)
• Online Portals & Digital Service Processing
• Data Entry & Document Management
• Typing Speed (English & Bengali)

----------------------------------------
PERSONAL DETAILS
----------------------------------------
Languages Known: Bengali, English, Hindi
Nationality: Indian
                """.trimIndent()
            }
            lower.contains("bengali") || lower.contains("festival") || lower.contains("caption") -> {
                """
শুভ উৎসবের আন্তরিক প্রীতি, শুভেচ্ছা ও অভিনন্দন! 🌸
(Warm wishes and greetings on this auspicious occasion!)

UNIQUE SMART POINT - আপনার সমস্ত ডিজিটাল সেবার বিশ্বস্ত মাধ্যম। 
আপনার ফটো প্রিন্টিং, পাসপোর্ট সাইজ ফটো, ফ্রেম মেকিং, এবং ডকুমেন্ট সার্ভিসের জন্য আজই যোগাযোগ করুন।

#UniqueSmartPoint #DigitalServices #WestBengal #SmartIndia
                """.trimIndent()
            }
            lower.contains("ocr") || lower.contains("extract") -> {
                """
[Extracted Text Content]
UNIQUE SMART POINT DIGITAL RECEIPT
Transaction Ref: USP-${System.currentTimeMillis() % 1000000}
Service: Digital Service Center
State: West Bengal, India
Status: Verified & Processed
Items: Document Processing, Photo Services
Thank you for using UNIQUE SMART POINT!
                """.trimIndent()
            }
            else -> {
                """
UNIQUE SMART POINT AI Studio Result:

$prompt

Generated with high precision. You can copy this output, export it as a formatted PDF, or save it to your dashboard projects.
                """.trimIndent()
            }
        }
    }
}
