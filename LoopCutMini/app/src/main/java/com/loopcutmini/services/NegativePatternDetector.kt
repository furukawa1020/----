package com.loopcutmini.services

import org.apache.commons.math3.stat.Frequency
import java.util.*
import kotlin.math.*

class NegativePatternDetector {
    // 負のパターンの辞書（重み付け付き）
    private val NEGATIVE_PATTERNS = mapOf(
        "でも" to 1.0,  // 接続詞：基本重み1.0
        "だって" to 1.0,  // 接続詞：基本重み1.0
        "〜ない" to 2.0,  // 否定表現：基本重み2.0
        "できない" to 2.5,  // 能力否定：重み2.5
        "無理" to 3.0,  // 絶望表現：重み3.0
        "どうしよう" to 2.0,  // 悩み表現：重み2.0
        "失敗" to 2.5,  // 消極表現：重み2.5
        "ダメ" to 2.5,  // 消極表現：重み2.5
        "ばか" to 3.0,  // 自己否定：重み3.0
        "馬鹿" to 3.0,  // 自己否定：重み3.0
        "バカ" to 3.0,  // 自己否定：重み3.0
        "バカ者" to 3.5,  // 自己否定強：重み3.5
        "バカ者だ" to 4.0,  // 自己否定最強：重み4.0
        "バカ者だよ" to 4.5,  // 自己否定最強：重み4.5
        "バカモノ" to 3.5,  // 自己否定強：重み3.5
        "バカモノだ" to 4.0,  // 自己否定最強：重み4.0
        "バカモノだよ" to 4.5  // 自己否定最強：重み4.5
    )

    // 文脈依存の否定表現
    private val CONTEXTUAL_NEGATIVES = listOf(
        "もう", "また", "いつもの", "いつも", "またしても", "いつものパターン"
    )

    // 連続使用の重み増加
    private val REPEAT_WEIGHT_MULTIPLIER = mapOf(
        2 to 1.5,  // 2回目は1.5倍
        3 to 2.0,  // 3回目は2.0倍
        4 to 2.5,  // 4回目は2.5倍
        5 to 3.0   // 5回目以上は3.0倍
    )

    // Count-Min Sketchの初期化
    private val frequencyCounter = Frequency()
    private val patternHistory = ArrayDeque<String>(100) // 最新100個のパターンを保持
    private val patternFrequency = mutableMapOf<String, Int>()
    private val MIN_PATTERN_COUNT = 3 // 連続する回数の閾値
    private val PATTERN_WINDOW = 90000L // 90秒のウィンドウ

    // 情報の保存期間（90秒）
    private val infoRetention = 90000L
    private var lastDetectionTime = System.currentTimeMillis()

    fun detectNegativePattern(text: String): Boolean {
        val words = text.split(" ", "\n", "\t", "\r").filter { it.isNotBlank() }
        var totalWeight = 0.0
        var detectedPatterns = mutableListOf<String>()
        val patternCounts = mutableMapOf<String, Int>()

        // パターンの検出と重み付け
        words.forEach { word ->
            NEGATIVE_PATTERNS.keys.forEach { pattern ->
                if (word.contains(pattern, ignoreCase = true)) {
                    detectedPatterns.add(word)
                    val baseWeight = NEGATIVE_PATTERNS[pattern] ?: 1.0
                    
                    // 連続使用による重み増加
                    val count = patternCounts.getOrDefault(pattern, 0) + 1
                    patternCounts[pattern] = count
                    
                    val multiplier = REPEAT_WEIGHT_MULTIPLIER[count] ?: 1.0
                    totalWeight += baseWeight * multiplier
                }
            }
        }

        // 文脈依存のパターン検出
        val contextualScore = detectContextualPatterns(words)
        totalWeight += contextualScore

        // パターンの履歴に追加
        updatePatternHistory(detectedPatterns)

        // 90秒以内のパターンの重みを計算
        val recentPatterns = getRecentPatterns()
        val recentWeight = recentPatterns.sumOf { pattern ->
            val baseWeight = NEGATIVE_PATTERNS[pattern] ?: 1.0
            val multiplier = REPEAT_WEIGHT_MULTIPLIER[patternFrequency[pattern] ?: 1] ?: 1.0
            baseWeight * multiplier
        }

        // 判定ロジック
        return when {
            // 重複パターンの検出
            hasRepeatingPatterns(recentPatterns) -> true
            
            // 重みの合計が閾値を超える
            recentWeight >= 10.0 -> true
            
            // 文脈依存の否定表現が複数回出現
            contextualScore >= 5.0 -> true
            
            // 接続詞の連続使用
            hasRepeatingConjunctions(patternCounts) -> true
            
            else -> false
        }
    }

    private fun detectContextualPatterns(words: List<String>): Double {
        var score = 0.0
        val contextualWords = CONTEXTUAL_NEGATIVES.toSet()
        
        // 文脈依存の否定表現の検出
        words.forEach { word ->
            if (contextualWords.contains(word.lowercase())) {
                score += 2.0 // 文脈依存の否定表現の重み
            }
        }
        
        // 文脈依存の否定表現の連続性チェック
        val contextualSequence = words.windowed(2)
        contextualSequence.forEach { (prev, current) ->
            if (contextualWords.contains(prev.lowercase()) && 
                NEGATIVE_PATTERNS.any { current.contains(it, ignoreCase = true) }) {
                score += 3.0 // 文脈依存と否定表現の連続
            }
        }
        
        return score
    }

    private fun updatePatternHistory(patterns: List<String>) {
        // 古いパターンの削除
        while (patternHistory.size > 0 && 
               patternHistory.first().toLongOrNull()?.let { System.currentTimeMillis() - it } ?: 0L > PATTERN_WINDOW) {
            patternHistory.removeFirst()
        }

        // 新しいパターンの追加
        patterns.forEach { pattern ->
            patternHistory.addLast("${pattern}_${System.currentTimeMillis()}")
            patternFrequency[pattern] = (patternFrequency[pattern] ?: 0) + 1
        }
    }

    private fun getRecentPatterns(): List<String> {
        return patternHistory
            .map { it.substringBefore('_') }
            .distinct()
        val currentTime = System.currentTimeMillis()
        return patternHistory.asSequence()
            .filter { entry ->
                val timestamp = entry.split(":")[0].toLong()
                currentTime - timestamp <= infoRetention
            }
            .map { it.split(":")[1] }
            .toList()
    }

    private fun hasRepeatingPatterns(patterns: List<String>): Boolean {
        val patternFrequency = mutableMapOf<String, Int>()
        patterns.forEach { pattern ->
            patternFrequency[pattern] = (patternFrequency[pattern] ?: 0) + 1
        }
        
        return patternFrequency.any { (_, count) -> count >= MIN_PATTERN_COUNT }
    }

    fun getMostFrequentPatterns(): List<String> {
        return patternFrequency.entries
            .sortedByDescending { it.value }
            .take(5)
            .map { it.key }
    }

    fun resetDetection() {
        patternHistory.clear()
        patternFrequency.clear()
        frequencyCounter.clear()
    }
}
