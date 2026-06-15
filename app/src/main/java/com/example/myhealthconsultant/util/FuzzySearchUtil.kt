package com.example.myhealthconsultant.util

import kotlin.math.max
import kotlin.math.min

/**
 * 模糊搜索工具类
 * 支持编辑距离相似度、字符重叠度、子串匹配等
 */
object FuzzySearchUtil {

    /**
     * 计算两个字符串的模糊匹配分数
     * @param query 用户输入的搜索词
     * @param target 目标字符串（药品名称、成分等）
     * @return 相似度分数 0.0-1.0，越高越相似
     */
    fun calculateSimilarity(query: String, target: String): Double {
        if (query.isEmpty() || target.isEmpty()) return 0.0

        // 精确包含，最高分
        if (target.contains(query, ignoreCase = true)) {
            return 1.0
        }

        // 计算多种相似度
        val editDistanceScore = editDistanceSimilarity(query, target)
        val charOverlapScore = charOverlapSimilarity(query, target)
        val subsequenceScore = subsequenceSimilarity(query, target)

        // 综合评分，取最高分
        return maxOf(editDistanceScore, charOverlapScore, subsequenceScore)
    }

    /**
     * 判断是否匹配（阈值0.6）
     */
    fun isMatch(query: String, target: String, threshold: Double = 0.6): Boolean {
        return calculateSimilarity(query, target) >= threshold
    }

    /**
     * 编辑距离相似度
     * 适用于：字序错误、多字、少字
     * 如："阿西莫林" vs "阿莫西林"
     */
    private fun editDistanceSimilarity(query: String, target: String): Double {
        val distance = levenshteinDistance(query, target)
        val maxLen = max(query.length, target.length)
        if (maxLen == 0) return 1.0
        return 1.0 - (distance.toDouble() / maxLen)
    }

    /**
     * 字符重叠相似度
     * 适用于：字符相同但顺序不同
     * 如："阿嬷西邻" vs "阿莫西林"（部分字符匹配）
     */
    private fun charOverlapSimilarity(query: String, target: String): Double {
        val queryChars = query.toSet()
        val targetChars = target.toSet()
        val intersection = queryChars.intersect(targetChars)
        val union = queryChars.union(targetChars)
        if (union.isEmpty()) return 0.0
        return intersection.size.toDouble() / union.size.toDouble()
    }

    /**
     * 子序列相似度
     * 适用于：字符按顺序出现但不连续
     * 如："阿林" 可以匹配 "阿莫西林"
     */
    private fun subsequenceSimilarity(query: String, target: String): Double {
        var queryIdx = 0
        var matchCount = 0

        for (char in target) {
            if (queryIdx < query.length && char == query[queryIdx]) {
                queryIdx++
                matchCount++
            }
        }

        return matchCount.toDouble() / query.length.toDouble()
    }

    /**
     * 计算Levenshtein编辑距离
     */
    private fun levenshteinDistance(s1: String, s2: String): Int {
        val len1 = s1.length
        val len2 = s2.length

        val dp = Array(len1 + 1) { IntArray(len2 + 1) }

        for (i in 0..len1) dp[i][0] = i
        for (j in 0..len2) dp[0][j] = j

        for (i in 1..len1) {
            for (j in 1..len2) {
                val cost = if (s1[i - 1] == s2[j - 1]) 0 else 1
                dp[i][j] = minOf(
                    dp[i - 1][j] + 1,      // 删除
                    dp[i][j - 1] + 1,      // 插入
                    dp[i - 1][j - 1] + cost // 替换
                )
            }
        }

        return dp[len1][len2]
    }

    /**
     * 扩展搜索：对Drug的多个字段进行模糊匹配
     * 返回综合得分最高的结果
     */
    fun <T> fuzzyFilter(
        query: String,
        items: List<T>,
        vararg selectors: (T) -> String?,
        threshold: Double = 0.6
    ): List<Pair<T, Double>> {
        if (query.isEmpty()) return items.map { it to 1.0 }

        return items.mapNotNull { item ->
            var maxScore = 0.0
            for (selector in selectors) {
                val field = selector(item) ?: continue
                val score = calculateSimilarity(query, field)
                if (score > maxScore) maxScore = score
            }
            if (maxScore >= threshold) item to maxScore else null
        }.sortedByDescending { it.second }
    }
}
