package com.example.myhealthconsultant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.myhealthconsultant.data.local.entity.User
import com.example.myhealthconsultant.presentation.ai.AiChatScreen
import com.example.myhealthconsultant.presentation.auth.AuthViewModel
import com.example.myhealthconsultant.presentation.auth.LoginScreen
import com.example.myhealthconsultant.presentation.auth.RegisterScreen
import com.example.myhealthconsultant.presentation.calendar.CalendarScreen
import com.example.myhealthconsultant.presentation.camera.CameraScreen
import com.example.myhealthconsultant.presentation.drugs.DrugDatabaseScreen
import com.example.myhealthconsultant.presentation.navigation.AppDestinations
import com.example.myhealthconsultant.presentation.profile.ProfileScreen
import com.example.myhealthconsultant.presentation.splash.SplashScreen
import com.example.myhealthconsultant.ui.theme.*
import com.example.myhealthconsultant.util.DataStoreManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var dataStoreManager: DataStoreManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by dataStoreManager.isDarkMode.collectAsState(initial = false)
            var showSplash by remember { mutableStateOf(true) }

            MyHealthConsultantTheme(darkTheme = isDarkMode) {
                if (showSplash) {
                    // 启动页/闪屏
                    SplashScreen(
                        onSplashFinished = { showSplash = false }
                    )
                } else {
                    // 根据登录状态显示不同页面
                    val authViewModel: AuthViewModel = hiltViewModel()
                    val authState by authViewModel.uiState.collectAsState()

                    if (authState.isLoggedIn) {
                        MainAppContent()
                    } else {
                        AuthNavGraph(
                            onLoginSuccess = { }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun AuthNavGraph(
    onLoginSuccess: () -> Unit
) {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "login"
    ) {
        composable("login") {
            LoginScreen(
                onLoginSuccess = onLoginSuccess,
                onNavigateToRegister = { navController.navigate("register") }
            )
        }
        composable("register") {
            RegisterScreen(
                onRegisterSuccess = onLoginSuccess,
                onNavigateToLogin = { navController.popBackStack() }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainAppContent() {
    var currentDestination by remember { mutableStateOf(AppDestinations.AI_CHAT) }
    var showProfile by remember { mutableStateOf(false) }
    var showMedicineCabinet by remember { mutableStateOf(false) }
    var showScanHistory by remember { mutableStateOf(false) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val authViewModel: AuthViewModel = hiltViewModel()

    // 获取当前用户信息
    val mainViewModel: MainViewModel = hiltViewModel()
    val currentUser by mainViewModel.currentUser.collectAsState()

    if (showMedicineCabinet) {
        com.example.myhealthconsultant.presentation.cabinet.MedicineCabinetScreen(
            onBack = { showMedicineCabinet = false }
        )
        return
    }

    if (showScanHistory) {
        com.example.myhealthconsultant.presentation.camera.ScanHistoryScreen(
            onBack = { showScanHistory = false }
        )
        return
    }

    if (showProfile) {
        ProfileScreen(
            onBack = { showProfile = false }
        )
        return
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawerContent(
                currentUser = currentUser,
                currentDestination = currentDestination,
                onNavigate = { destination ->
                    currentDestination = destination
                    scope.launch { drawerState.close() }
                },
                onNavigateToProfile = {
                    showProfile = true
                    scope.launch { drawerState.close() }
                },
                onLogout = { authViewModel.logout() }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Image(
                                painter = painterResource(id = R.drawable.logo_transparent),
                                contentDescription = "青囊",
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(RoundedCornerShape(0.dp)),
                                contentScale = ContentScale.Fit
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                when (currentDestination) {
                                    AppDestinations.AI_CHAT -> "AI健康助手"
                                    AppDestinations.CAMERA -> "拍照识药"
                                    AppDestinations.DRUGS -> "药品库"
                                    AppDestinations.CALENDAR -> "我的"
                                },
                                style = MaterialTheme.typography.titleLarge
                            )
                        }
                    },
                    navigationIcon = {
                        IconButton(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(
                                Icons.Default.Menu,
                                contentDescription = "菜单",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = { showMedicineCabinet = true }) {
                            Icon(
                                Icons.Default.MedicalServices,
                                contentDescription = "医药箱",
                                tint = MaterialTheme.colorScheme.onBackground
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = MaterialTheme.colorScheme.background,
                        titleContentColor = MaterialTheme.colorScheme.onBackground,
                        navigationIconContentColor = MaterialTheme.colorScheme.onBackground,
                        actionIconContentColor = MaterialTheme.colorScheme.onBackground
                    )
                )
            },
            bottomBar = {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface,
                    tonalElevation = 0.dp
                ) {
                    AppDestinations.entries.forEach { destination ->
                        NavigationBarItem(
                            icon = {
                                Icon(
                                    destination.icon,
                                    contentDescription = destination.label,
                                    modifier = Modifier.size(24.dp)
                                )
                            },
                            label = {
                                Text(
                                    destination.label,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            },
                            selected = currentDestination == destination,
                            onClick = { currentDestination = destination },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = MaterialTheme.colorScheme.primary,
                                selectedTextColor = MaterialTheme.colorScheme.primary,
                                unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                indicatorColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        )
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                when (currentDestination) {
                    AppDestinations.AI_CHAT -> AiChatScreen()
                    AppDestinations.CAMERA -> CameraScreen(
                        onNavigateToHistory = { showScanHistory = true }
                    )
                    AppDestinations.DRUGS -> DrugDatabaseScreen()
                    AppDestinations.CALENDAR -> CalendarScreen(
                        onNavigateToProfile = { showProfile = true }
                    )
                }
            }
        }
    }
}

@Composable
fun AppDrawerContent(
    currentUser: User?,
    currentDestination: AppDestinations,
    onNavigate: (AppDestinations) -> Unit,
    onNavigateToProfile: () -> Unit,
    onLogout: () -> Unit
) {
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showAboutDialog by remember { mutableStateOf(false) }
    var showPrivacyDialog by remember { mutableStateOf(false) }

    ModalDrawerSheet(
        modifier = Modifier.width(300.dp),
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        drawerContentColor = MaterialTheme.colorScheme.onSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // 用户信息头部 - 动态显示用户数据
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateToProfile() }
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(64.dp),
                            shape = MaterialTheme.shapes.extraLarge,
                            color = MaterialTheme.colorScheme.primary
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Person,
                                    contentDescription = null,
                                    modifier = Modifier.size(32.dp),
                                    tint = MaterialTheme.colorScheme.surface
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(16.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                currentUser?.nickname ?: "未设置昵称",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                currentUser?.phone?.let {
                                    if (it.length >= 7) it.take(3) + "****" + it.takeLast(4)
                                    else it
                                } ?: "未绑定手机",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Icon(
                            Icons.Default.ChevronRight,
                            contentDescription = "进入设置",
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "主要功能",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp)
            )

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.SmartToy, contentDescription = null, modifier = Modifier.size(20.dp)) },
                label = { Text("AI问答") },
                selected = currentDestination == AppDestinations.AI_CHAT,
                onClick = { onNavigate(AppDestinations.AI_CHAT) },
                modifier = Modifier.padding(horizontal = 12.dp),
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer, unselectedContainerColor = MaterialTheme.colorScheme.surface,
                    selectedIconColor = MaterialTheme.colorScheme.primary, unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedTextColor = MaterialTheme.colorScheme.primary, unselectedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(20.dp)) },
                label = { Text("拍照识药") },
                selected = currentDestination == AppDestinations.CAMERA,
                onClick = { onNavigate(AppDestinations.CAMERA) },
                modifier = Modifier.padding(horizontal = 12.dp),
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer, unselectedContainerColor = MaterialTheme.colorScheme.surface,
                    selectedIconColor = MaterialTheme.colorScheme.primary, unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedTextColor = MaterialTheme.colorScheme.primary, unselectedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Medication, contentDescription = null, modifier = Modifier.size(20.dp)) },
                label = { Text("药品库") },
                selected = currentDestination == AppDestinations.DRUGS,
                onClick = { onNavigate(AppDestinations.DRUGS) },
                modifier = Modifier.padding(horizontal = 12.dp),
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer, unselectedContainerColor = MaterialTheme.colorScheme.surface,
                    selectedIconColor = MaterialTheme.colorScheme.primary, unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedTextColor = MaterialTheme.colorScheme.primary, unselectedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.CalendarMonth, contentDescription = null, modifier = Modifier.size(20.dp)) },
                label = { Text("用药日历") },
                selected = currentDestination == AppDestinations.CALENDAR,
                onClick = { onNavigate(AppDestinations.CALENDAR) },
                modifier = Modifier.padding(horizontal = 12.dp),
                colors = NavigationDrawerItemDefaults.colors(
                    selectedContainerColor = MaterialTheme.colorScheme.primaryContainer, unselectedContainerColor = MaterialTheme.colorScheme.surface,
                    selectedIconColor = MaterialTheme.colorScheme.primary, unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                    selectedTextColor = MaterialTheme.colorScheme.primary, unselectedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)

            Text(
                "快捷设置",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 28.dp, vertical = 8.dp)
            )

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.ManageAccounts, contentDescription = null, modifier = Modifier.size(20.dp)) },
                label = { Text("账户与安全") },
                selected = false,
                onClick = onNavigateToProfile,
                modifier = Modifier.padding(horizontal = 12.dp),
                colors = NavigationDrawerItemDefaults.colors(
                    unselectedContainerColor = MaterialTheme.colorScheme.surface, unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant, unselectedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )

            HorizontalDivider(modifier = Modifier.padding(horizontal = 28.dp, vertical = 16.dp), color = MaterialTheme.colorScheme.outlineVariant)

            NavigationDrawerItem(
                icon = { Icon(Icons.Default.Info, contentDescription = null, modifier = Modifier.size(20.dp)) },
                label = { Text("关于APP") },
                selected = false, onClick = { showAboutDialog = true },
                modifier = Modifier.padding(horizontal = 12.dp),
                colors = NavigationDrawerItemDefaults.colors(
                    unselectedContainerColor = MaterialTheme.colorScheme.surface, unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant, unselectedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )
            NavigationDrawerItem(
                icon = { Icon(Icons.Default.PrivacyTip, contentDescription = null, modifier = Modifier.size(20.dp)) },
                label = { Text("隐私政策") },
                selected = false, onClick = { showPrivacyDialog = true },
                modifier = Modifier.padding(horizontal = 12.dp),
                colors = NavigationDrawerItemDefaults.colors(
                    unselectedContainerColor = MaterialTheme.colorScheme.surface, unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant, unselectedTextColor = MaterialTheme.colorScheme.onSurface
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            HorizontalDivider(modifier = Modifier.padding(horizontal = 28.dp), color = MaterialTheme.colorScheme.outlineVariant)
            Spacer(modifier = Modifier.height(16.dp))
            NavigationDrawerItem(
                icon = { Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, modifier = Modifier.size(20.dp), tint = MaterialTheme.colorScheme.error) },
                label = { Text("退出登录", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodyMedium) },
                selected = false,
                onClick = { showLogoutDialog = true },
                modifier = Modifier.padding(horizontal = 12.dp),
                colors = NavigationDrawerItemDefaults.colors(
                    unselectedContainerColor = MaterialTheme.colorScheme.surface, unselectedIconColor = MaterialTheme.colorScheme.error, unselectedTextColor = MaterialTheme.colorScheme.error
                )
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }

    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text("确认退出", style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurface) },
            text = { Text("确定要退出登录吗？", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) },
            confirmButton = {
                TextButton(onClick = { showLogoutDialog = false; onLogout() }) {
                    Text("确定", color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelLarge)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text("取消", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelLarge)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface
        )
    }

    if (showAboutDialog) {
        AlertDialog(
            onDismissRequest = { showAboutDialog = false },
            title = { Text("关于青囊", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("青囊 · Capsula", style = MaterialTheme.typography.titleSmall, color = MaterialTheme.colorScheme.primary)
                    Text("版本：1.0.0", style = MaterialTheme.typography.bodyMedium)
                    Text("一款个人健康管理助手，提供用药提醒、药品查询、拍照识药和AI健康咨询等功能，帮助用户科学管理日常用药与健康。", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("本应用提供的健康信息仅供参考，不构成医疗建议。如有不适请及时就医。", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f))
                }
            },
            confirmButton = {
                TextButton(onClick = { showAboutDialog = false }) {
                    Text("确定", color = MaterialTheme.colorScheme.primary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(0.dp)
        )
    }

    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = { Text("隐私政策", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold) },
            text = {
                Column(
                    modifier = Modifier.verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("一、数据收集", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    Text("本应用仅收集您主动提供的个人信息（如昵称、手机号），用于账户管理和个性化服务。", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("二、数据存储", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    Text("您的所有健康数据（用药记录、健康日志等）均存储在本地设备中，不会上传至任何服务器。", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("三、权限使用", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    Text("• 相机权限：仅用于拍照识药功能\n• 通知权限：仅用于用药提醒\n• 网络权限：仅用于AI咨询服务", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Text("四、第三方服务", fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyMedium)
                    Text("本应用使用的AI服务可能涉及数据传输，我们将严格遵守相关法律法规保护您的隐私。", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            },
            confirmButton = {
                TextButton(onClick = { showPrivacyDialog = false }) {
                    Text("我已阅读", color = MaterialTheme.colorScheme.primary)
                }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(0.dp)
        )
    }
}
