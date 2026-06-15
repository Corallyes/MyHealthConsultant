package com.example.myhealthconsultant.data.local.db

import com.example.myhealthconsultant.data.local.entity.*
import java.text.SimpleDateFormat
import java.util.*

/**
 * 为账号 13972293217 (test-user-001) 生成丰富的样例数据
 */
object SampleUserData {

    private const val USER_ID = "test-user-001"

    // 固定的处方ID
    private const val PRESCRIPTION_1 = "rx-001"
    private const val PRESCRIPTION_2 = "rx-002"

    // 固定的计划ID
    private const val PLAN_BLOOD_PRESSURE = "plan-bp-001"
    private const val PLAN_CHOLESTEROL = "plan-chol-001"
    private const val PLAN_VITAMIN = "plan-vit-001"
    private const val PLAN_GASTRIC = "plan-gas-001"
    private const val PLAN_CALCIUM = "plan-cal-001"

    fun getPrescriptions(): List<Prescription> = listOf(
        Prescription(
            id = PRESCRIPTION_1,
            userId = USER_ID,
            doctorName = "张明华",
            hospitalName = "市第一人民医院",
            diagnosis = "高血压病2级、高脂血症",
            notes = "1. 低盐低脂饮食\n2. 适量运动\n3. 定期监测血压\n4. 一个月后复查血脂",
            createdAt = daysAgo(30)
        ),
        Prescription(
            id = PRESCRIPTION_2,
            userId = USER_ID,
            doctorName = "李秀芬",
            hospitalName = "市中心医院",
            diagnosis = "慢性胃炎",
            notes = "1. 规律饮食，避免辛辣刺激\n2. 戒烟限酒\n3. 两周后复诊",
            createdAt = daysAgo(10)
        )
    )

    fun getMedicationPlans(): List<MedicationPlan> = listOf(
        // 长期降压药
        MedicationPlan(
            id = PLAN_BLOOD_PRESSURE,
            userId = USER_ID,
            drugId = "drug_063",  // 氨氯地平片
            drugName = "氨氯地平片",
            dosage = "1片",
            frequency = "每日1次",
            timeSlot = "morning",
            startDate = daysAgo(30),
            endDate = null,
            reminderEnabled = true,
            reminderHour = 8,
            reminderMinute = 0,
            mealBasedTime = "after_breakfast",
            notes = "张医生处方，长期服用",
            isActive = true,
            createdAt = daysAgo(30)
        ),
        // 降脂药
        MedicationPlan(
            id = PLAN_CHOLESTEROL,
            userId = USER_ID,
            drugId = "drug_059",  // 阿托伐他汀钙片
            drugName = "阿托伐他汀钙片",
            dosage = "1片",
            frequency = "每日1次",
            timeSlot = "evening",
            startDate = daysAgo(30),
            endDate = null,
            reminderEnabled = true,
            reminderHour = 21,
            reminderMinute = 0,
            mealBasedTime = "after_dinner",
            notes = "睡前服用效果更好",
            isActive = true,
            createdAt = daysAgo(30)
        ),
        // 维生素C
        MedicationPlan(
            id = PLAN_VITAMIN,
            userId = USER_ID,
            drugId = "drug_009",  // 维生素C片
            drugName = "维生素C片",
            dosage = "2片",
            frequency = "每日1次",
            timeSlot = "morning",
            startDate = daysAgo(60),
            endDate = null,
            reminderEnabled = false,
            reminderHour = null,
            reminderMinute = null,
            mealBasedTime = "after_breakfast",
            notes = "日常补充",
            isActive = true,
            createdAt = daysAgo(60)
        ),
        // 胃药（短期）
        MedicationPlan(
            id = PLAN_GASTRIC,
            userId = USER_ID,
            drugId = "drug_024",  // 奥美拉唑肠溶胶囊
            drugName = "奥美拉唑肠溶胶囊",
            dosage = "1粒",
            frequency = "每日1次",
            timeSlot = "morning",
            startDate = daysAgo(10),
            endDate = daysFromNow(18),
            reminderEnabled = true,
            reminderHour = 7,
            reminderMinute = 30,
            mealBasedTime = null,
            notes = "李医生处方，晨起空腹服用，疗程4周",
            isActive = true,
            createdAt = daysAgo(10)
        ),
        // 钙片
        MedicationPlan(
            id = PLAN_CALCIUM,
            userId = USER_ID,
            drugId = "drug_047",  // 碳酸钙D3片
            drugName = "碳酸钙D3片",
            dosage = "1片",
            frequency = "每日1次",
            timeSlot = "afternoon",
            startDate = daysAgo(45),
            endDate = null,
            reminderEnabled = true,
            reminderHour = 14,
            reminderMinute = 0,
            mealBasedTime = "after_lunch",
            notes = "咀嚼后咽下",
            isActive = true,
            createdAt = daysAgo(45)
        )
    )

    fun getMedicationRecords(): List<MedicationRecord> {
        val records = mutableListOf<MedicationRecord>()
        val calendar = Calendar.getInstance()

        // 为每个计划生成过去14天的服药记录（模拟约85%的服药率）
        val planRecords = mapOf(
            PLAN_BLOOD_PRESSURE to Pair("氨氯地平片", "1片"),
            PLAN_CHOLESTEROL to Pair("阿托伐他汀钙片", "1片"),
            PLAN_VITAMIN to Pair("维生素C片", "2片"),
            PLAN_GASTRIC to Pair("奥美拉唑肠溶胶囊", "1粒"),
            PLAN_CALCIUM to Pair("碳酸钙D3片", "1片")
        )

        // 模拟服药时间和偶尔漏服的模式
        val adherencePattern = listOf(
            true, true, true, true, true, true, false,  // 第1周：漏1天
            true, true, true, false, true, true, true,  // 第2周：漏1天
            true, true, true, true, false, true, true,  // 第3周：漏1天
            true, false, true, true, true, true, true   // 第4周：漏1天
        )

        for ((planId, info) in planRecords) {
            val (drugName, dosage) = info
            val planStartDay = when (planId) {
                PLAN_GASTRIC -> 10
                PLAN_VITAMIN -> 28
                PLAN_CALCIUM -> 14
                else -> 28
            }

            for (dayOffset in 0 until minOf(planStartDay, 28) - 1) {
                val taken = adherencePattern[dayOffset]
                calendar.timeInMillis = System.currentTimeMillis()
                calendar.add(Calendar.DAY_OF_YEAR, -(planStartDay - 1 - dayOffset))
                // 只取日期部分（零点）
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val dateTimestamp = calendar.timeInMillis

                val timeStr = when (planId) {
                    PLAN_BLOOD_PRESSURE, PLAN_VITAMIN -> "08:${"%02d".format(5 + (dayOffset % 20))}"
                    PLAN_CHOLESTEROL -> "21:${"%02d".format(10 + (dayOffset % 15))}"
                    PLAN_GASTRIC -> "07:${"%02d".format(25 + (dayOffset % 10))}"
                    PLAN_CALCIUM -> "14:${"%02d".format(5 + (dayOffset % 25))}"
                    else -> "08:00"
                }

                records.add(
                    MedicationRecord(
                        userId = USER_ID,
                        planId = planId,
                        drugName = drugName,
                        dosage = if (taken) dosage else dosage,
                        takenDate = dateTimestamp,
                        takenTime = if (taken) timeStr else null,
                        isTaken = taken,
                        notes = if (!taken) "忘记服用" else null,
                        createdAt = dateTimestamp + (if (taken) 3600000L else 0L)
                    )
                )
            }
        }
        return records
    }

    fun getCabinetMedicines(): List<CabinetMedicine> {
        val now = System.currentTimeMillis()
        return listOf(
            CabinetMedicine(
                userId = USER_ID,
                name = "氨氯地平片",
                genericName = "苯磺酸氨氯地平",
                category = "心血管用药",
                specification = "5mg×28片/盒",
                quantity = 2,
                unit = "盒",
                expiryDate = daysFromNow(365),
                storageLocation = "卧室药箱",
                prescriptionId = PRESCRIPTION_1,
                notes = "日常降压药",
                isActive = true,
                createdAt = daysAgo(30)
            ),
            CabinetMedicine(
                userId = USER_ID,
                name = "阿托伐他汀钙片",
                genericName = "阿托伐他汀钙",
                category = "心血管用药",
                specification = "20mg×7片/盒",
                quantity = 3,
                unit = "盒",
                expiryDate = daysFromNow(300),
                storageLocation = "卧室药箱",
                prescriptionId = PRESCRIPTION_1,
                notes = "降脂药",
                isActive = true,
                createdAt = daysAgo(30)
            ),
            CabinetMedicine(
                userId = USER_ID,
                name = "奥美拉唑肠溶胶囊",
                genericName = "奥美拉唑",
                category = "胃肠用药",
                specification = "20mg×14粒/盒",
                quantity = 2,
                unit = "盒",
                expiryDate = daysFromNow(200),
                storageLocation = "卧室药箱",
                prescriptionId = PRESCRIPTION_2,
                notes = "治胃病",
                isActive = true,
                createdAt = daysAgo(10)
            ),
            CabinetMedicine(
                userId = USER_ID,
                name = "维生素C片",
                genericName = "维生素C",
                category = "维生素补充",
                specification = "100mg×100片/瓶",
                quantity = 1,
                unit = "瓶",
                expiryDate = daysFromNow(500),
                storageLocation = "客厅药柜",
                notes = "日常补充维生素",
                isActive = true,
                createdAt = daysAgo(60)
            ),
            CabinetMedicine(
                userId = USER_ID,
                name = "碳酸钙D3片",
                genericName = "碳酸钙/维生素D3",
                category = "维生素补充",
                specification = "600mg×60片/瓶",
                quantity = 1,
                unit = "瓶",
                expiryDate = daysFromNow(400),
                storageLocation = "客厅药柜",
                notes = "补钙",
                isActive = true,
                createdAt = daysAgo(45)
            ),
            CabinetMedicine(
                userId = USER_ID,
                name = "布洛芬缓释胶囊",
                genericName = "布洛芬",
                category = "解热镇痛",
                specification = "0.3g×20粒/盒",
                quantity = 1,
                unit = "盒",
                expiryDate = daysFromNow(180),
                storageLocation = "客厅药柜",
                notes = "备用止痛药",
                isActive = true,
                createdAt = daysAgo(90)
            ),
            CabinetMedicine(
                userId = USER_ID,
                name = "蒙脱石散",
                genericName = "蒙脱石",
                category = "胃肠用药",
                specification = "3g×10袋/盒",
                quantity = 1,
                unit = "盒",
                expiryDate = daysFromNow(15),
                storageLocation = "客厅药柜",
                notes = "止泻药，快过期了",
                isActive = true,
                createdAt = daysAgo(350)
            ),
            CabinetMedicine(
                userId = USER_ID,
                name = "板蓝根颗粒",
                genericName = "板蓝根",
                category = "中药",
                specification = "10g×20袋/盒",
                quantity = 2,
                unit = "盒",
                expiryDate = daysAgo(5),
                storageLocation = "客厅药柜",
                notes = "已过期，需处理",
                isActive = true,
                createdAt = daysAgo(400)
            ),
            CabinetMedicine(
                userId = USER_ID,
                name = "氯雷他定片",
                genericName = "氯雷他定",
                category = "抗过敏药",
                specification = "10mg×6片/盒",
                quantity = 2,
                unit = "盒",
                expiryDate = daysFromNow(250),
                storageLocation = "卧室药箱",
                notes = "过敏季节备用",
                isActive = true,
                createdAt = daysAgo(120)
            ),
            CabinetMedicine(
                userId = USER_ID,
                name = "云南白药气雾剂",
                genericName = "云南白药",
                category = "外用药",
                specification = "60g+85g/盒",
                quantity = 1,
                unit = "盒",
                expiryDate = daysFromNow(600),
                storageLocation = "客厅药柜",
                notes = "跌打损伤",
                isActive = true,
                createdAt = daysAgo(200)
            )
        )
    }

    fun getChatHistory(): List<ChatHistory> {
        val sessionId1 = "session-ai-001"
        val sessionId2 = "session-ai-002"
        val sessionId3 = "session-ai-003"
        val base1 = daysAgo(5)
        val base2 = daysAgo(3)
        val base3 = daysAgo(1)

        return listOf(
            // 会话1：关于高血压用药咨询
            ChatHistory(userId = USER_ID, sessionId = sessionId1, role = "user",
                content = "我最近体检查出高血压，医生开了氨氯地平，这个药有什么需要注意的吗？",
                timestamp = base1),
            ChatHistory(userId = USER_ID, sessionId = sessionId1, role = "assistant",
                content = """氨氯地平是一种常用的钙通道阻滞剂，用于治疗高血压。以下是一些重要注意事项：

**服用方法：**
- 每日一次，建议早晨服用
- 可以空腹或随餐服用
- 整片吞服，不要掰开或嚼碎

**注意事项：**
1. **不可突然停药** - 可能导致血压反弹升高
2. **避免葡萄柚汁** - 可能影响药物代谢
3. **常见副作用** - 头痛、面部潮红、踝部水肿，通常会逐渐减轻
4. **定期监测血压** - 建议每天同一时间测量

**生活方式建议：**
- 低盐饮食（每日<6g盐）
- 适量运动
- 控制体重
- 戒烟限酒

如果有任何不适，及时咨询医生。""",
                timestamp = base1 + 30000),
            ChatHistory(userId = USER_ID, sessionId = sessionId1, role = "user",
                content = "好的谢谢，那阿托伐他汀晚上吃是不是更好？",
                timestamp = base1 + 60000),
            ChatHistory(userId = USER_ID, sessionId = sessionId1, role = "assistant",
                content = """是的，**阿托伐他汀晚上服用效果更好**。

这是因为：
- 人体胆固醇的合成主要在夜间进行（凌晨2-3点达到高峰）
- 他汀类药物在胆固醇合成活跃时服用，抑制效果最佳

不过阿托伐他汀相比其他他汀类药物（如辛伐他汀），半衰期较长（约14小时），所以即使偶尔白天服用，影响也不会太大。

**建议：**
- 固定在每天晚上睡前服用
- 可以和氨氯地平错开时间（一个早上，一个晚上）
- 服药期间避免大量饮酒
- 定期检查肝功能（初次服药后3个月复查）""",
                timestamp = base1 + 90000),

            // 会话2：感冒症状咨询
            ChatHistory(userId = USER_ID, sessionId = sessionId2, role = "user",
                content = "我这两天有点流鼻涕、打喷嚏，嗓子也有点疼，是不是感冒了？需要吃什么药？",
                timestamp = base2),
            ChatHistory(userId = USER_ID, sessionId = sessionId2, role = "assistant",
                content = """根据你的症状（流鼻涕、打喷嚏、咽痛），很可能是**普通感冒**（上呼吸道感染）。

**推荐用药（可选其中一种）：**
1. **复方氨酚烷胺胶囊** - 综合缓解感冒症状（发热、头痛、鼻塞、流涕）
2. **感冒灵颗粒** - 中西药结合，适合不想吃太多西药的情况
3. **对乙酰氨基酚片** - 如果主要是头痛发热

**注意事项：**
- ⚠️ 你正在服用降压药和降脂药，**避免使用含伪麻黄碱的感冒药**（如白加黑、新康泰克），因为它可能升高血压
- 多休息，多喝水
- 如果3-5天症状不缓解或加重，建议就医

**居家护理：**
- 温盐水漱口缓解咽痛
- 保持室内空气流通
- 饮食清淡""",
                timestamp = base2 + 30000),
            ChatHistory(userId = USER_ID, sessionId = sessionId2, role = "user",
                content = "那板蓝根可以喝吗？我家里有。",
                timestamp = base2 + 60000),
            ChatHistory(userId = USER_ID, sessionId = sessionId2, role = "assistant",
                content = """**板蓝根可以喝**，但需要注意几点：

**板蓝根的功效：**
- 清热解毒、凉血利咽
- 对风热感冒引起的咽喉肿痛有一定缓解作用

**注意事项：**
1. 板蓝根偏寒凉，**脾胃虚寒者慎用**（容易腹泻的人要注意）
2. 不宜长期大量服用（一般不超过3-5天）
3. 如果是风寒感冒（怕冷明显、流清鼻涕），板蓝根不太对症

**建议：**
- 板蓝根可以作为辅助，但如果症状明显，还是建议配合上面推荐的感冒药
- 注意你家里板蓝根的**有效期**，过期药品不要服用
- 服用期间忌辛辣、油腻食物

如果出现高热（>38.5°C）、剧烈头痛、呼吸困难等症状，请及时就医。""",
                timestamp = base2 + 90000),

            // 会话3：胃部不适咨询
            ChatHistory(userId = USER_ID, sessionId = sessionId3, role = "user",
                content = "最近吃完饭总觉得胃胀，有时候还有点反酸，是怎么回事？",
                timestamp = base3),
            ChatHistory(userId = USER_ID, sessionId = sessionId3, role = "assistant",
                content = """饭后胃胀伴反酸，常见原因包括：

**可能的原因：**
1. **功能性消化不良** - 最常见，与饮食习惯、压力有关
2. **慢性胃炎** - 需要胃镜检查确认
3. **胃食管反流** - 反酸明显时要考虑

**建议用药：**
- **奥美拉唑**（你已经在吃了）- 抑制胃酸分泌，坚持疗程
- **多潘立酮** - 饭前15-30分钟服用，促进胃动力，缓解胃胀
- **铝碳酸镁** - 偶尔反酸时嚼服，快速中和胃酸

**饮食建议：**
- 🍚 少食多餐，每餐七八分饱
- 🚫 避免：辛辣、油腻、咖啡、浓茶、碳酸饮料
- 🕐 晚饭不要太晚（睡前3小时不进食）
- 🛏️ 饭后不要立即躺下

**需要就医的情况：**
- 症状持续2周以上不缓解
- 出现黑便或呕血
- 体重明显下降
- 吞咽困难

你已经在看李医生了，建议按时复诊，必要时做胃镜检查。""",
                timestamp = base3 + 30000)
        )
    }

    fun getScanHistory(): List<ScanHistory> = listOf(
        ScanHistory(
            userId = USER_ID,
            imageUrl = "/storage/emulated/0/Pictures/scan_001.jpg",
            recognizedDrugName = "氨氯地平片",
            confidence = 0.95f,
            drugDetails = """{"name":"氨氯地平片","genericName":"苯磺酸氨氯地平","category":"心血管用药","type":"处方药(Rx)"}""",
            scannedAt = daysAgo(25)
        ),
        ScanHistory(
            userId = USER_ID,
            imageUrl = "/storage/emulated/0/Pictures/scan_002.jpg",
            recognizedDrugName = "阿托伐他汀钙片",
            confidence = 0.92f,
            drugDetails = """{"name":"阿托伐他汀钙片","genericName":"阿托伐他汀钙","category":"心血管用药","type":"处方药(Rx)"}""",
            scannedAt = daysAgo(25)
        ),
        ScanHistory(
            userId = USER_ID,
            imageUrl = "/storage/emulated/0/Pictures/scan_003.jpg",
            recognizedDrugName = "奥美拉唑肠溶胶囊",
            confidence = 0.88f,
            drugDetails = """{"name":"奥美拉唑肠溶胶囊","genericName":"奥美拉唑","category":"胃肠用药","type":"处方药(Rx)"}""",
            scannedAt = daysAgo(8)
        ),
        ScanHistory(
            userId = USER_ID,
            imageUrl = "/storage/emulated/0/Pictures/scan_004.jpg",
            recognizedDrugName = "蒙脱石散",
            confidence = 0.91f,
            drugDetails = """{"name":"蒙脱石散","genericName":"蒙脱石","category":"胃肠用药","type":"非处方药(OTC)"}""",
            scannedAt = daysAgo(3)
        )
    )

    // 标记收藏的药品ID列表（对应SampleDrugData中的药品）
    fun getFavoriteDrugIds(): List<String> = listOf(
        "drug_063",  // 氨氯地平片 - 日常用药
        "drug_059",  // 阿托伐他汀钙片 - 日常用药
        "drug_009",  // 维生素C片 - 日常补充
        "drug_002",  // 布洛芬缓释胶囊 - 常备止痛
        "drug_024",  // 奥美拉唑肠溶胶囊 - 胃药
        "drug_004",  // 复方氨酚烷胺胶囊 - 常备感冒药
        "drug_003",  // 板蓝根颗粒 - 常备中药
        "drug_007",  // 氯雷他定片 - 过敏季常备
        "drug_047",  // 碳酸钙D3片 - 补钙
        "drug_051",  // 连花清瘟胶囊 - 常备感冒药
        "drug_010"   // 速效救心丸 - 家中常备
    )

    // ===== 工具方法 =====
    private fun daysAgo(days: Int): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -days)
        return cal.timeInMillis
    }

    private fun daysFromNow(days: Int): Long {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, days)
        return cal.timeInMillis
    }
}
