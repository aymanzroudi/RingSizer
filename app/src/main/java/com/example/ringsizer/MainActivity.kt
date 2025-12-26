package com.example.ringsizer

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts.OpenDocument
import androidx.activity.result.contract.ActivityResultContracts
import android.content.Intent
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavType
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import coil.compose.AsyncImage
import com.example.ringsizer.ui.AuthViewModel
import com.example.ringsizer.ui.CartViewModel
import com.example.ringsizer.ui.CreateProductViewModel
import com.example.ringsizer.ui.GoldPriceViewModel
import com.example.ringsizer.ui.ManageProductViewModel
import com.example.ringsizer.ui.ProductsViewModel
import com.example.ringsizer.ui.UserDataViewModel
import com.example.ringsizer.ui.theme.RingSizerTheme
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.PI
import kotlin.math.roundToInt

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            RingSizerTheme {
                RingSizerRoot()
            }
        }
    }
}

@Composable
private fun CreateProductScreen(
    navController: NavHostController,
    productsVm: ProductsViewModel,
    vm: CreateProductViewModel = hiltViewModel()
) {
    val goldVm: GoldPriceViewModel = hiltViewModel()
    val context = LocalContext.current

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("ring") }
    var sizeMinMmText by remember { mutableStateOf("") }
    var sizeMaxMmText by remember { mutableStateOf("") }
    var karat by remember { mutableStateOf("18") }
    var weightG by remember { mutableStateOf("") }
    var priceText by remember { mutableStateOf("") }
    var stock by remember { mutableStateOf("1") }
    var status by remember { mutableStateOf("published") }
    var autoPrice by remember { mutableStateOf(true) }

    var imageUri by remember { mutableStateOf<String?>(null) }
    val imagePicker = rememberLauncherForActivityResult(OpenDocument()) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
                // Some providers don't support persistable permissions; ignore.
            }
        }
        imageUri = uri?.toString()
    }

    val pricePerGram = goldVm.price?.price_per_gram_mad ?: goldVm.price?.price_per_gram

    val suggestedPrice by remember(pricePerGram, weightG, karat) {
        derivedStateOf {
            val grams = weightG.toDoubleOrNull()
            val k = karat.toIntOrNull()
            if (pricePerGram == null || grams == null || k == null) return@derivedStateOf null
            val ratio = (k.toDouble() / 24.0)
            (grams * pricePerGram * ratio)
        }
    }

    LaunchedEffect(suggestedPrice, autoPrice) {
        if (autoPrice) {
            priceText = suggestedPrice?.let { ((it * 100).roundToInt() / 100.0).toString() } ?: ""
        }
    }

    val loading = vm.loading
    val error = vm.error
    val createdId = vm.createdId

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Ajouter un produit", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.size(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Type de produit", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.size(8.dp))
                Button(
                    onClick = { category = "ring" },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (category == "ring") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (category == "ring") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                ) { Text("Bague") }
                Button(
                    onClick = { category = "bracelet" },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (category == "bracelet") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (category == "bracelet") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                ) { Text("Bracelet") }
            }
        }

        Spacer(Modifier.size(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Photo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.size(8.dp))

                Button(
                    onClick = { imagePicker.launch(arrayOf("image/*")) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) { Text(if (imageUri == null) "Choisir une image" else "Changer l'image") }

                imageUri?.let {
                    Spacer(Modifier.size(12.dp))
                    AsyncImage(
                        model = it,
                        contentDescription = "Image produit",
                        modifier = Modifier.fillMaxWidth().heightIn(min = 180.dp)
                    )
                }
            }
        }

        Spacer(Modifier.size(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Détails", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.size(8.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titre") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

                val sizeLabel = if (category == "ring") "Diamètre" else "Taille"
                OutlinedTextField(
                    value = sizeMinMmText,
                    onValueChange = { sizeMinMmText = it },
                    label = { Text("$sizeLabel min (mm)") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = sizeMaxMmText,
                    onValueChange = { sizeMaxMmText = it },
                    label = { Text("$sizeLabel max (mm)") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = karat,
                    onValueChange = { karat = it },
                    label = { Text("Karat (ex: 18)") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = weightG,
                    onValueChange = { weightG = it },
                    label = { Text("Poids (g)") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

                Text("Prix auto")
                Switch(checked = autoPrice, onCheckedChange = { autoPrice = it })

                if (pricePerGram != null) {
                    Text("Or: ${pricePerGram} / g (base 24k)", style = MaterialTheme.typography.bodySmall)
                } else {
                    Text("Or: indisponible (ouvre l'onglet Prix or)", style = MaterialTheme.typography.bodySmall)
                }

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Prix (MAD)") },
                    enabled = !autoPrice,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp)
                )

                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = { Text("Stock") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = status,
                    onValueChange = { status = it },
                    label = { Text("Status (draft/published/archived)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(Modifier.size(12.dp))

        if (loading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    val karatVal = karat.toIntOrNull() ?: 18
                    val weightVal = weightG.toDoubleOrNull()
                    val priceVal = priceText.toDoubleOrNull() ?: 0.0
                    val stockVal = stock.toIntOrNull() ?: 0
                    val sizeMinVal = sizeMinMmText.toDoubleOrNull()
                    val sizeMaxVal = sizeMaxMmText.toDoubleOrNull()
                    vm.create(
                        title = title,
                        description = description.ifBlank { null },
                        category = category,
                        sizeMinMm = sizeMinVal,
                        sizeMaxMm = sizeMaxVal,
                        karat = karatVal,
                        weightG = weightVal,
                        price = priceVal,
                        stock = stockVal,
                        imagePath = imageUri
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Publier")
            }
        }

        error?.let {
            Spacer(Modifier.size(8.dp))
            Text("Erreur: $it", color = MaterialTheme.colorScheme.error)
        }

        if (createdId != null) {
            Spacer(Modifier.size(12.dp))
            Text("Produit créé (id=$createdId)")
            Spacer(Modifier.size(8.dp))
            Button(
                onClick = {
                    productsVm.load()
                    navController.popBackStack(Screen.Seller.route, inclusive = false)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Retour à mes produits")
            }
        }
    }
}

private sealed class Screen(val route: String, val label: String) {
    object Home : Screen("home", "Accueil")
    object Measure : Screen("measure", "Mesure")
    object Gold : Screen("gold", "Prix or")
    object Catalog : Screen("catalog", "Catalogue")
    object Cart : Screen("cart", "Panier")
    object Seller : Screen("seller", "Vendre")
    object CreateProduct : Screen("seller/create", "Ajouter")
    object EditProduct : Screen("seller/edit/{productId}", "Modifier")
    object ProductDetail : Screen("catalog/{productId}", "Détail")
    object Profile : Screen("profile", "Profil")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun RingSizerRoot() {
    val navController = rememberNavController()
    val authVm: AuthViewModel = hiltViewModel()
    val productsVm: ProductsViewModel = hiltViewModel()
    val goldVm: GoldPriceViewModel = hiltViewModel()
    val userDataVm: UserDataViewModel = hiltViewModel()
    val cartVm: CartViewModel = hiltViewModel()
    val showSeller = authVm.token != null && authVm.userRole?.lowercase() == "seller"

    val isClientLoggedIn = authVm.token != null && authVm.userRole?.lowercase() != "seller"
    val items = if (showSeller) {
        listOf(Screen.Home, Screen.Gold, Screen.Catalog, Screen.Seller, Screen.Profile)
    } else if (isClientLoggedIn) {
        listOf(Screen.Home, Screen.Measure, Screen.Gold, Screen.Catalog, Screen.Cart, Screen.Profile)
    } else {
        listOf(Screen.Home, Screen.Measure, Screen.Gold, Screen.Catalog, Screen.Profile)
    }

    LaunchedEffect(authVm.token, authVm.userRole) {
        if (isClientLoggedIn) {
            userDataVm.load()
            cartVm.load()
        }
    }
    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text("RingSizer", color = MaterialTheme.colorScheme.onSurface) },
                colors = TopAppBarDefaults.smallTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.onSurface
                )
            )
        },
        bottomBar = {
            val currentRoute = navController.currentBackStackEntryAsState().value?.destination?.route
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurface
            ) {
                items.forEach { screen ->
                    NavigationBarItem(
                        selected = currentRoute == screen.route,
                        onClick = {
                            navController.navigate(screen.route) {
                                popUpTo(navController.graph.startDestinationId) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        icon = { },
                        label = { Text(screen.label) },
                        colors = androidx.compose.material3.NavigationBarItemDefaults.colors(
                            selectedIconColor = MaterialTheme.colorScheme.onPrimary,
                            selectedTextColor = MaterialTheme.colorScheme.onPrimary,
                            indicatorColor = MaterialTheme.colorScheme.primary,
                            unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                            unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                }
            }
        }
    ) { padding ->
        RingSizerNavHost(navController, padding, authVm, productsVm, goldVm, userDataVm, cartVm)
    }
}

@Composable
private fun RingSizerNavHost(
    navController: NavHostController,
    padding: PaddingValues,
    authVm: AuthViewModel,
    productsVm: ProductsViewModel,
    goldVm: GoldPriceViewModel,
    userDataVm: UserDataViewModel,
    cartVm: CartViewModel
) {
    NavHost(navController = navController, startDestination = Screen.Home.route, modifier = Modifier.padding(padding)) {
        composable(Screen.Home.route) { HomeScreen(navController, authVm, productsVm, goldVm) }
        composable(Screen.Measure.route) {
            val showMeasure = authVm.token.isNullOrBlank() || authVm.userRole?.lowercase() != "seller"
            if (showMeasure) {
                MeasureScreen(navController = navController, authVm = authVm, userDataVm = userDataVm)
            } else {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("La mesure est disponible uniquement pour les clients")
                }
            }
        }
        composable(Screen.Gold.route) { GoldPriceScreen(vm = goldVm) }
        composable(Screen.Catalog.route) { CatalogScreen(navController = navController, authVm = authVm, userDataVm = userDataVm, vm = productsVm) }
        composable(Screen.Cart.route) { CartScreen(navController, authVm, cartVm) }
        composable(Screen.Seller.route) { SellerSpaceScreen(navController, authVm, productsVm) }
        composable(Screen.CreateProduct.route) { CreateProductScreen(navController, productsVm = productsVm) }
        composable(
            Screen.EditProduct.route,
            arguments = listOf(navArgument("productId") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("productId") ?: -1
            EditProductScreen(navController = navController, productId = id, productsVm = productsVm)
        }
        composable(
            Screen.ProductDetail.route,
            arguments = listOf(navArgument("productId") { type = NavType.LongType })
        ) { backStackEntry ->
            val id = backStackEntry.arguments?.getLong("productId") ?: -1
            ProductDetailScreen(navController = navController, authVm = authVm, userDataVm = userDataVm, cartVm = cartVm, productId = id, vm = productsVm)
        }
        composable(Screen.Profile.route) { ProfileScreen(navController = navController, authVm = authVm, userDataVm = userDataVm, cartVm = cartVm) }
    }
}

@Composable
private fun SellerSpaceScreen(navController: NavHostController, authVm: AuthViewModel, vm: ProductsViewModel) {
    val token = authVm.token
    val role = authVm.userRole
    val sellerId = authVm.userId

    LaunchedEffect(token) {
        if (!token.isNullOrBlank()) {
            vm.load()
        }
    }

    if (token.isNullOrBlank()) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Connecte-toi pour accéder à l'espace vendeur")
        }
        return
    }

    if (role != null && role.lowercase() != "seller") {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Ton compte n'est pas vendeur")
        }
        return
    }

    var query by remember { mutableStateOf("") }
    var typeFilter by remember { mutableStateOf("all") } // all | ring | bracelet
    var statusFilter by remember { mutableStateOf("all") } // all | draft | published | archived
    var sort by remember { mutableStateOf("default") } // default | price_asc | price_desc | title

    val filteredProducts by remember(vm.items, sellerId, query, typeFilter, statusFilter, sort) {
        derivedStateOf {
            if (sellerId == null) return@derivedStateOf emptyList()
            val base = vm.items.filter { it.seller_id == sellerId }
            val q = query.trim()
            val searched = if (q.isBlank()) {
                base
            } else {
                base.filter {
                    it.title.contains(q, ignoreCase = true) ||
                        (it.description?.contains(q, ignoreCase = true) == true)
                }
            }
            val typed = if (typeFilter == "all") searched else searched.filter { (it.category ?: "").lowercase() == typeFilter }
            val statused = if (statusFilter == "all") typed else typed.filter { it.status.lowercase() == statusFilter }

            when (sort) {
                "price_asc" -> statused.sortedBy { it.price }
                "price_desc" -> statused.sortedByDescending { it.price }
                "title" -> statused.sortedBy { it.title.lowercase() }
                else -> statused
            }
        }
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text("Espace vendeur", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.size(12.dp))
        }

        item {
            Button(
                onClick = { navController.navigate(Screen.CreateProduct.route) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Ajouter un produit")
            }
            Spacer(Modifier.size(8.dp))
            Button(
                onClick = { vm.load() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Text("Rafraîchir")
            }
            Spacer(Modifier.size(12.dp))
            Text("Mes produits", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.size(8.dp))
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Recherche", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.size(6.dp))
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        label = { Text("Titre ou description") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(Modifier.size(12.dp))
                    Text("Type", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.size(6.dp))
                    Button(
                        onClick = { typeFilter = "all" },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (typeFilter == "all") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (typeFilter == "all") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    ) { Text("Tous") }
                    Button(
                        onClick = { typeFilter = "ring" },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (typeFilter == "ring") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (typeFilter == "ring") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    ) { Text("Bague") }
                    Button(
                        onClick = { typeFilter = "bracelet" },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (typeFilter == "bracelet") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (typeFilter == "bracelet") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    ) { Text("Bracelet") }

                    Spacer(Modifier.size(12.dp))
                    Text("Status", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.size(6.dp))
                    Button(
                        onClick = { statusFilter = "all" },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (statusFilter == "all") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (statusFilter == "all") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    ) { Text("Tous") }
                    Button(
                        onClick = { statusFilter = "published" },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (statusFilter == "published") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (statusFilter == "published") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    ) { Text("Publié") }
                    Button(
                        onClick = { statusFilter = "draft" },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (statusFilter == "draft") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (statusFilter == "draft") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    ) { Text("Brouillon") }
                    Button(
                        onClick = { statusFilter = "archived" },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (statusFilter == "archived") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (statusFilter == "archived") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    ) { Text("Archivé") }

                    Spacer(Modifier.size(12.dp))
                    Text("Tri", style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.size(6.dp))
                    Button(
                        onClick = { sort = "default" },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (sort == "default") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (sort == "default") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    ) { Text("Par défaut") }
                    Button(
                        onClick = { sort = "price_asc" },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (sort == "price_asc") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (sort == "price_asc") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    ) { Text("Prix croissant") }
                    Button(
                        onClick = { sort = "price_desc" },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (sort == "price_desc") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (sort == "price_desc") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    ) { Text("Prix décroissant") }
                    Button(
                        onClick = { sort = "title" },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (sort == "title") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (sort == "title") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    ) { Text("Nom") }
                }
            }
            Spacer(Modifier.size(12.dp))
        }

        when {
            vm.loading -> item {
                Box(Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            vm.error != null -> item { Text("Erreur: ${vm.error}") }
            sellerId == null -> item { Text("Impossible de déterminer ton identifiant vendeur") }
            filteredProducts.isEmpty() -> item { Text("Aucun produit") }
            else -> {
                item {
                    Text("Résultats: ${filteredProducts.size}", style = MaterialTheme.typography.bodySmall)
                    Spacer(Modifier.size(8.dp))
                }
                items(filteredProducts) { p ->
                    val imageUrl = chooseImage(p)
                    Card(
                        modifier = Modifier
                            .padding(vertical = 8.dp)
                            .fillMaxWidth(),
                        colors = androidx.compose.material3.CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            if (imageUrl.isBlank()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 140.dp)
                                        .padding(bottom = 8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text("Aucune image")
                                }
                            } else {
                                AsyncImage(
                                    model = imageUrl,
                                    contentDescription = p.title,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = 140.dp)
                                        .padding(bottom = 8.dp)
                                )
                            }
                            Text(p.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                            Spacer(Modifier.size(4.dp))
                            Text("Prix: ${p.price} MAD")
                            Text("Karat: ${p.karat}")
                            Text("Status: ${p.status}")
                            Spacer(Modifier.size(10.dp))
                            Button(
                                onClick = { navController.navigate("seller/edit/${p.id}") },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) { Text("Modifier / Supprimer") }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EditProductScreen(
    navController: NavHostController,
    productId: Long,
    productsVm: ProductsViewModel,
    vm: ManageProductViewModel = hiltViewModel()
) {
    val product = productsVm.items.find { it.id == productId }

    if (product == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Produit introuvable")
        }
        return
    }

    val goldVm: GoldPriceViewModel = hiltViewModel()
    val context = LocalContext.current

    var title by remember { mutableStateOf(product.title) }
    var description by remember { mutableStateOf(product.description ?: "") }
    var category by remember { mutableStateOf(product.category ?: "ring") }
    var sizeMinMmText by remember { mutableStateOf(product.size_min_mm?.toString() ?: "") }
    var sizeMaxMmText by remember { mutableStateOf(product.size_max_mm?.toString() ?: "") }
    var karat by remember { mutableStateOf(product.karat.toString()) }
    var weightG by remember { mutableStateOf(product.weight_g?.toString() ?: "") }
    var priceText by remember { mutableStateOf(product.price.toString()) }
    var stock by remember { mutableStateOf(product.stock.toString()) }
    var status by remember { mutableStateOf(product.status) }
    var autoPrice by remember { mutableStateOf(false) }

    var imageUri by remember { mutableStateOf<String?>(null) }
    val imagePicker = rememberLauncherForActivityResult(OpenDocument()) { uri ->
        if (uri != null) {
            try {
                context.contentResolver.takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION
                )
            } catch (_: SecurityException) {
            }
        }
        imageUri = uri?.toString()
    }

    val pricePerGram = goldVm.price?.price_per_gram_mad ?: goldVm.price?.price_per_gram
    val suggestedPrice by remember(pricePerGram, weightG, karat) {
        derivedStateOf {
            val grams = weightG.toDoubleOrNull()
            val k = karat.toIntOrNull()
            if (pricePerGram == null || grams == null || k == null) return@derivedStateOf null
            val ratio = (k.toDouble() / 24.0)
            (grams * pricePerGram * ratio)
        }
    }

    LaunchedEffect(suggestedPrice, autoPrice) {
        if (autoPrice) {
            priceText = suggestedPrice?.let { ((it * 100).roundToInt() / 100.0).toString() } ?: ""
        }
    }

    val loading = vm.loading
    val error = vm.error
    val updated = vm.updatedProduct
    val deleted = vm.deleted

    LaunchedEffect(updated, deleted) {
        if (updated != null || deleted) {
            if (deleted) {
                productsVm.removeLocal(productId)
            }
            if (updated != null) {
                productsVm.upsertLocal(updated)
            }
            productsVm.load()
            navController.popBackStack(Screen.Seller.route, inclusive = false)
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Modifier produit", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.size(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Type de produit", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.size(8.dp))
                Button(
                    onClick = { category = "ring" },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (category == "ring") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (category == "ring") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                ) { Text("Bague") }
                Button(
                    onClick = { category = "bracelet" },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (category == "bracelet") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (category == "bracelet") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                ) { Text("Bracelet") }
            }
        }

        Spacer(Modifier.size(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Photo", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.size(8.dp))

                Button(
                    onClick = { imagePicker.launch(arrayOf("image/*")) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) { Text(if (imageUri == null) "Choisir une nouvelle image" else "Changer l'image") }

                Spacer(Modifier.size(12.dp))
                AsyncImage(
                    model = imageUri ?: chooseImage(product),
                    contentDescription = "Image produit",
                    modifier = Modifier.fillMaxWidth().heightIn(min = 180.dp)
                )
            }
        }

        Spacer(Modifier.size(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Détails", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.size(8.dp))

                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Titre") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

                val sizeLabel = if (category == "ring") "Diamètre" else "Taille"
                OutlinedTextField(
                    value = sizeMinMmText,
                    onValueChange = { sizeMinMmText = it },
                    label = { Text("$sizeLabel min (mm)") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = sizeMaxMmText,
                    onValueChange = { sizeMaxMmText = it },
                    label = { Text("$sizeLabel max (mm)") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = karat,
                    onValueChange = { karat = it },
                    label = { Text("Karat (ex: 18)") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )
                OutlinedTextField(
                    value = weightG,
                    onValueChange = { weightG = it },
                    label = { Text("Poids (g)") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

                Text("Prix auto")
                Switch(checked = autoPrice, onCheckedChange = { autoPrice = it })

                if (pricePerGram != null) {
                    Text("Or: ${pricePerGram} / g (base 24k)", style = MaterialTheme.typography.bodySmall)
                } else {
                    Text("Or: indisponible (ouvre l'onglet Prix or)", style = MaterialTheme.typography.bodySmall)
                }

                OutlinedTextField(
                    value = priceText,
                    onValueChange = { priceText = it },
                    label = { Text("Prix (MAD)") },
                    enabled = !autoPrice,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 8.dp)
                )

                OutlinedTextField(
                    value = stock,
                    onValueChange = { stock = it },
                    label = { Text("Stock") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

                OutlinedTextField(
                    value = status,
                    onValueChange = { status = it },
                    label = { Text("Status (draft/published/archived)") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        Spacer(Modifier.size(12.dp))

        if (loading) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    val sizeMinVal = sizeMinMmText.toDoubleOrNull()
                    val sizeMaxVal = sizeMaxMmText.toDoubleOrNull()
                    vm.update(
                        productId = productId,
                        title = title,
                        description = description.ifBlank { null },
                        category = category,
                        sizeMinMm = sizeMinVal,
                        sizeMaxMm = sizeMaxVal,
                        karat = karat.toIntOrNull() ?: product.karat,
                        weightG = weightG.toDoubleOrNull(),
                        price = priceText.toDoubleOrNull() ?: product.price,
                        stock = stock.toIntOrNull() ?: product.stock,
                        status = status.ifBlank { product.status },
                        imagePath = imageUri
                    )
                },
                modifier = Modifier.fillMaxWidth()
            ) { Text("Enregistrer") }

            Spacer(Modifier.size(8.dp))

            Button(
                onClick = { vm.delete(productId) },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) { Text("Supprimer") }
        }

        error?.let {
            Spacer(Modifier.size(8.dp))
            Text("Erreur: $it", color = MaterialTheme.colorScheme.error)
        }

        if (updated != null || deleted) {
            Spacer(Modifier.size(12.dp))
            Text(if (deleted) "Produit supprimé" else "Produit mis à jour")
        }
    }
}

@Composable
private fun HomeScreen(
    navController: NavHostController,
    authVm: AuthViewModel,
    productsVm: ProductsViewModel,
    goldVm: GoldPriceViewModel
) {
    val latest = goldVm.latestValueMad()
    val historyValues = goldVm.historyValuesMad()
    val (minV, maxV, changeV) = goldVm.stats()
    val featured = remember(productsVm.items) {
        productsVm.items
            .filter { it.status.lowercase() == "published" }
            .sortedByDescending { it.id }
            .take(8)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                Text(
                    "RingSizer",
                    style = MaterialTheme.typography.headlineLarge,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.size(16.dp))
        }

        if (featured.isNotEmpty()) {
            item {
                Text("Produits en vedette", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.size(10.dp))
            }

            items(featured.chunked(2)) { row ->
                Row(Modifier.fillMaxWidth()) {
                    FeaturedProductTile(
                        product = row[0],
                        onClick = { navController.navigate("catalog/${row[0].id}") },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(Modifier.width(12.dp))
                    if (row.size > 1) {
                        FeaturedProductTile(
                            product = row[1],
                            onClick = { navController.navigate("catalog/${row[1].id}") },
                            modifier = Modifier.weight(1f)
                        )
                    } else {
                        Spacer(Modifier.weight(1f))
                    }
                }
                Spacer(Modifier.size(12.dp))
            }
            item { Spacer(Modifier.size(6.dp)) }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Prix de l'or (live)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.size(8.dp))
                    val latestText = latest?.let { ((it * 100).roundToInt() / 100.0).toString() } ?: "-"
                    Text("${latestText} MAD / g", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.size(6.dp))
                    val changeText = changeV?.let { ((it * 100).roundToInt() / 100.0) } ?: 0.0
                    val changeColor = if (changeText >= 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    Text("Variation (${goldVm.selectedDays}j): ${changeText} MAD", color = changeColor)
                    Spacer(Modifier.size(12.dp))
                    GoldHistoryChart(values = historyValues)
                    Spacer(Modifier.size(12.dp))
                    Button(
                        onClick = { navController.navigate(Screen.Gold.route) },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) { Text("Voir le suivi") }
                }
            }
            Spacer(Modifier.size(12.dp))
        }
    }
}

@Composable
private fun FeaturedProductTile(
    product: com.example.ringsizer.data.remote.ProductDto,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val imageUrl = chooseImage(product)
    Card(
        modifier = modifier,
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        onClick = onClick
    ) {
        Column(Modifier.padding(12.dp)) {
            if (imageUrl.isBlank()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Aucune image", style = MaterialTheme.typography.bodySmall)
                }
            } else {
                AsyncImage(
                    model = imageUrl,
                    contentDescription = product.title,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f),
                    contentScale = ContentScale.Crop
                )
            }
            Spacer(Modifier.size(10.dp))
            Text(product.title, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Spacer(Modifier.size(2.dp))
            Text("${product.price} MAD", style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun MeasureScreen(navController: NavHostController, authVm: AuthViewModel, userDataVm: UserDataViewModel) {
    var mode by remember { mutableStateOf("ring") }

    var diameterText by remember { mutableStateOf("") }
    var circumferenceText by remember { mutableStateOf("") }
    val diameter = diameterText.toDoubleOrNull()
    val circumference = circumferenceText.toDoubleOrNull() ?: diameter?.let { it * PI }
    val sizeFr = circumference
    val sizeUs = diameter?.let { (it - 11.54) / 0.8128 }

    var wristText by remember { mutableStateOf("") }
    val wrist = wristText.toDoubleOrNull()
    val recommended = wrist?.let { (it + 1.5).let { v -> (v * 10).roundToInt() / 10.0 } }
    val recommendedMm = recommended?.let { it * 10.0 }

    val isClientLoggedIn = authVm.token != null && authVm.userRole?.lowercase() != "seller"

    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text("Mesure", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.size(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = androidx.compose.material3.CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Choisir", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.size(8.dp))
                Button(
                    onClick = { mode = "ring" },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (mode == "ring") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (mode == "ring") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                ) { Text("Bague") }
                Button(
                    onClick = { mode = "bracelet" },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (mode == "bracelet") MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                        contentColor = if (mode == "bracelet") MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                    )
                ) { Text("Bracelet") }
            }
        }

        Spacer(Modifier.size(12.dp))

        if (mode == "ring") {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Bague", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.size(8.dp))
                    OutlinedTextField(
                        value = diameterText,
                        onValueChange = { diameterText = it },
                        label = { Text("Diamètre (mm)") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = circumferenceText,
                        onValueChange = { circumferenceText = it },
                        label = { Text("Circonférence (mm)") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    )
                    Text("Résultats", fontWeight = FontWeight.SemiBold)
                    Text("FR/EU (approx) : ${sizeFr?.roundToInt() ?: "-"}")
                    Text("US (approx) : ${sizeUs?.let { (it * 10).roundToInt() / 10.0 } ?: "-"}")

                    Spacer(Modifier.size(12.dp))
                    Button(
                        onClick = {
                            if (!isClientLoggedIn) {
                                navController.navigate(Screen.Profile.route)
                            } else {
                                userDataVm.upsertSize(
                                    "ring",
                                    com.example.ringsizer.data.remote.SavedSizeUpsertRequest(
                                        diameter_mm = diameter,
                                        circumference_mm = circumference,
                                        standard = "MM",
                                        label = "Ma bague"
                                    )
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) { Text(if (!isClientLoggedIn) "Se connecter pour enregistrer" else "Enregistrer") }
                }
            }
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Bracelet", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.size(8.dp))
                    OutlinedTextField(
                        value = wristText,
                        onValueChange = { wristText = it },
                        label = { Text("Tour de poignet (cm)") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
                    )
                    Text("Longueur recommandée : ${recommended ?: "-"} cm")

                    Spacer(Modifier.size(12.dp))
                    Button(
                        onClick = {
                            if (!isClientLoggedIn) {
                                navController.navigate(Screen.Profile.route)
                            } else {
                                userDataVm.upsertSize(
                                    "bracelet",
                                    com.example.ringsizer.data.remote.SavedSizeUpsertRequest(
                                        circumference_mm = recommendedMm,
                                        standard = "MM",
                                        label = "Mon bracelet"
                                    )
                                )
                            }
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        )
                    ) { Text(if (!isClientLoggedIn) "Se connecter pour enregistrer" else "Enregistrer") }
                }
            }
        }
    }
}

@Composable
private fun GoldPriceScreen(vm: GoldPriceViewModel = hiltViewModel()) {
    val loading = vm.loading
    val error = vm.error
    val price = vm.price
    val history = vm.history
    val (minV, maxV, changeV) = vm.stats()

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text("Prix de l'or", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.size(12.dp))
        }

        if (loading) {
            item {
                Box(Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }

        if (error != null) {
            item {
                Text("Erreur: $error", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.size(12.dp))
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Column(Modifier.padding(16.dp)) {
                    val latest = vm.latestValueMad()
                    val latestText = latest?.let { ((it * 100).roundToInt() / 100.0).toString() } ?: "-"
                    Text("Dernier prix", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.size(6.dp))
                    Text("${latestText} MAD / g", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.size(6.dp))
                    price?.let {
                        Text("Source: ${it.source} • ${it.karat}k")
                        Text("Mise à jour: ${it.collected_at}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Spacer(Modifier.size(12.dp))
        }

        item {
            Text("Période", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.size(8.dp))
            LazyRow {
                items(listOf(7, 30, 90)) { d ->
                    val selected = vm.selectedDays == d
                    Button(
                        onClick = { vm.setDays(d) },
                        modifier = Modifier.padding(end = 10.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    ) { Text("${d}j") }
                }
                item {
                    Button(
                        onClick = { vm.load() },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant,
                            contentColor = MaterialTheme.colorScheme.onSurface
                        )
                    ) { Text("Rafraîchir") }
                }
            }
            Spacer(Modifier.size(12.dp))
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Historique", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                    Spacer(Modifier.size(10.dp))
                    GoldHistoryChart(values = vm.historyValuesMad())
                    Spacer(Modifier.size(12.dp))
                    val minText = minV?.let { ((it * 100).roundToInt() / 100.0).toString() } ?: "-"
                    val maxText = maxV?.let { ((it * 100).roundToInt() / 100.0).toString() } ?: "-"
                    val changeText = changeV?.let { ((it * 100).roundToInt() / 100.0) } ?: 0.0
                    GoldStatRow(label = "Min", value = "$minText MAD")
                    GoldStatRow(label = "Max", value = "$maxText MAD")
                    GoldStatRow(label = "Variation", value = "${changeText} MAD")
                    if (history.isEmpty() && !loading && error == null) {
                        Spacer(Modifier.size(8.dp))
                        Text("Aucun historique (attends la collecte automatique)", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}

@Composable
private fun GoldStatRow(label: String, value: String) {
    Column(Modifier.padding(bottom = 6.dp)) {
        Text(label, style = MaterialTheme.typography.bodySmall)
        Text(value, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun GoldHistoryChart(values: List<Double>) {
    val lineColor = MaterialTheme.colorScheme.primary
    val axisColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.25f)
    val bg = MaterialTheme.colorScheme.surface

    if (values.size < 2) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Historique insuffisant")
        }
        return
    }

    val min = values.minOrNull() ?: 0.0
    val max = values.maxOrNull() ?: 0.0
    val range = (max - min).takeIf { it > 0 } ?: 1.0

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
    ) {
        drawRect(color = bg)

        val w = size.width
        val h = size.height

        drawLine(axisColor, start = Offset(0f, h), end = Offset(w, h), strokeWidth = 2f)
        drawLine(axisColor, start = Offset(0f, 0f), end = Offset(0f, h), strokeWidth = 2f)

        val stepX = w / (values.size - 1)
        val path = Path()
        values.forEachIndexed { index, v ->
            val x = stepX * index
            val y = h - (((v - min) / range).toFloat() * h)
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }

        drawPath(
            path = path,
            color = lineColor,
            style = Stroke(width = 5f)
        )
    }
}

@Composable
private fun CatalogScreen(
    navController: NavHostController,
    authVm: AuthViewModel,
    userDataVm: UserDataViewModel,
    vm: ProductsViewModel = hiltViewModel()
) {
    var priceMin by remember { mutableStateOf("") }
    var priceMax by remember { mutableStateOf("") }
    var carat by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("") }
    var onlyMySize by remember { mutableStateOf(false) }

    val priceMinVal = priceMin.toDoubleOrNull()
    val priceMaxVal = priceMax.toDoubleOrNull()
    val caratVal = carat.toIntOrNull()

    val items = vm.items
    val loading = vm.loading
    val error = vm.error

    val isClientLoggedIn = authVm.token != null && authVm.userRole?.lowercase() != "seller"
    val ringMm = userDataVm.ringSize()?.diameter_mm
    val braceletMm = userDataVm.braceletSize()?.circumference_mm

    val filtered = items.filter { p ->
        val cat = (p.category ?: "").lowercase()
        val sizeOk = if (onlyMySize && isClientLoggedIn) {
            when (cat) {
                "ring" -> {
                    if (ringMm == null) true
                    else {
                        val min = p.size_min_mm
                        val max = p.size_max_mm
                        min != null && max != null && ringMm >= min && ringMm <= max
                    }
                }
                "bracelet" -> {
                    if (braceletMm == null) true
                    else {
                        val min = p.size_min_mm
                        val max = p.size_max_mm
                        min != null && max != null && braceletMm >= min && braceletMm <= max
                    }
                }
                else -> true
            }
        } else {
            true
        }
        (priceMinVal == null || p.price >= priceMinVal) &&
        (priceMaxVal == null || p.price <= priceMaxVal) &&
        (caratVal == null || p.karat == caratVal) &&
        (category.isBlank() || p.category?.contains(category, ignoreCase = true) == true) &&
        sizeOk
    }

    when {
        loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Erreur: $error") }
        else -> Column(Modifier.fillMaxSize()) {
            FilterBar(
                priceMin = priceMin,
                priceMax = priceMax,
                carat = carat,
                category = category,
                onlyMySize = onlyMySize,
                showMySizeToggle = isClientLoggedIn,
                onToggleMySize = { onlyMySize = !onlyMySize },
                onPriceMin = { priceMin = it },
                onPriceMax = { priceMax = it },
                onCarat = { carat = it },
                onCategory = { category = it },
                onReset = {
                    priceMin = ""; priceMax = ""; carat = ""; category = ""; onlyMySize = false
                }
            )
            LazyColumn {
                items(filtered) { p ->
                    ProductCard(p, onClick = { navController.navigate("catalog/${p.id}") })
                }
            }
        }
    }
}

@Composable
private fun FilterBar(
    priceMin: String,
    priceMax: String,
    carat: String,
    category: String,
    onlyMySize: Boolean,
    showMySizeToggle: Boolean,
    onToggleMySize: () -> Unit,
    onPriceMin: (String) -> Unit,
    onPriceMax: (String) -> Unit,
    onCarat: (String) -> Unit,
    onCategory: (String) -> Unit,
    onReset: () -> Unit
) {
    Column(Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
        Text("Filtres", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.size(8.dp))

        if (showMySizeToggle) {
            Button(
                onClick = onToggleMySize,
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (onlyMySize) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = if (onlyMySize) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            ) { Text("Ma taille") }
        }

        OutlinedTextField(
            value = priceMin,
            onValueChange = onPriceMin,
            label = { Text("Prix min") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = priceMax,
            onValueChange = onPriceMax,
            label = { Text("Prix max") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = carat,
            onValueChange = onCarat,
            label = { Text("Carat exact (ex: 18)") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        OutlinedTextField(
            value = category,
            onValueChange = onCategory,
            label = { Text("Catégorie") },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
        )
        Button(
            onClick = onReset,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                contentColor = MaterialTheme.colorScheme.onSurface
            )
        ) { Text("Réinitialiser") }
    }
}

@Composable
private fun ProductCard(p: com.example.ringsizer.data.remote.ProductDto, onClick: () -> Unit) {
    val imageUrl = chooseImage(p)
    val shop = p.seller?.seller_profile?.shop_name ?: p.seller?.name
    val sizeText = if (p.size_min_mm != null && p.size_max_mm != null) {
        "${p.size_min_mm} - ${p.size_max_mm} mm"
    } else {
        null
    }
    Card(
        modifier = Modifier
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .fillMaxWidth(),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        onClick = onClick
    ) {
        Column(Modifier.padding(16.dp)) {
            AsyncImage(
                model = imageUrl,
                contentDescription = p.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
                    .heightIn(min = 160.dp)
            )
            Text(p.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.size(4.dp))
            Text("Prix: ${p.price} MAD", style = MaterialTheme.typography.bodyMedium)
            Text("Karat: ${p.karat}", style = MaterialTheme.typography.bodySmall)
            shop?.let { Text("Boutique: $it", style = MaterialTheme.typography.bodySmall) }
            sizeText?.let { Text("Taille: $it", style = MaterialTheme.typography.bodySmall) }
            p.category?.let { Text("Catégorie: $it", style = MaterialTheme.typography.bodySmall) }
        }
    }
}

@Composable
private fun ProductDetailScreen(
    navController: NavHostController,
    authVm: AuthViewModel,
    userDataVm: UserDataViewModel,
    cartVm: CartViewModel,
    productId: Long,
    vm: ProductsViewModel
) {
    val product = vm.items.find { it.id == productId }
    when {
        vm.loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
        vm.error != null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Erreur: ${vm.error}") }
        product == null -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Produit introuvable") }
        else -> {
            val imageUrl = chooseImage(product)
            val isClientLoggedIn = authVm.token != null && authVm.userRole?.lowercase() != "seller"
            val isFav = userDataVm.isFavorite(product.id)
            Card(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxSize(),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Column(
                    Modifier
                        .padding(16.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    AsyncImage(
                        model = imageUrl,
                        contentDescription = product.title,
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(min = 220.dp)
                            .padding(bottom = 12.dp)
                    )
                    Text(product.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.size(6.dp))
                    Text("Prix: ${product.price} MAD", style = MaterialTheme.typography.bodyLarge)
                    Text("Karat: ${product.karat}", style = MaterialTheme.typography.bodyMedium)
                    val shop = product.seller?.seller_profile?.shop_name ?: product.seller?.name
                    shop?.let { Text("Boutique: $it", style = MaterialTheme.typography.bodyMedium) }
                    if (product.size_min_mm != null && product.size_max_mm != null) {
                        Text("Taille: ${product.size_min_mm} - ${product.size_max_mm} mm", style = MaterialTheme.typography.bodyMedium)
                    }
                    product.category?.let { Text("Catégorie: $it", style = MaterialTheme.typography.bodyMedium) }
                    product.weight_g?.let { Text("Poids: $it g", style = MaterialTheme.typography.bodyMedium) }
                    Text("Statut: ${product.status}", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.size(10.dp))
                    product.description?.let { Text(it, style = MaterialTheme.typography.bodyMedium) }

                    Spacer(Modifier.size(14.dp))

                    if (!isClientLoggedIn) {
                        Button(
                            onClick = { navController.navigate(Screen.Profile.route) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.primary,
                                contentColor = MaterialTheme.colorScheme.onPrimary
                            )
                        ) { Text("Se connecter pour acheter") }
                    } else {
                        Row(Modifier.fillMaxWidth()) {
                            Button(
                                onClick = {
                                    if (isFav) userDataVm.removeFavorite(product.id) else userDataVm.addFavorite(product.id)
                                },
                                modifier = Modifier.weight(1f).padding(end = 8.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) { Text(if (isFav) "Retirer favori" else "Ajouter favori") }

                            Button(
                                onClick = { cartVm.add(product.id, 1) },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) { Text("Ajouter au panier") }
                        }

                        Spacer(Modifier.size(10.dp))

                        Button(
                            onClick = { navController.navigate(Screen.Cart.route) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) { Text("Voir mon panier") }
                    }
                }
            }
        }
    }
}

@Composable
private fun CatalogScreen(vm: ProductsViewModel = hiltViewModel()) {
    val navController = rememberNavController()
    val authVm: AuthViewModel = hiltViewModel()
    val userDataVm: UserDataViewModel = hiltViewModel()
    CatalogScreen(navController = navController, authVm = authVm, userDataVm = userDataVm, vm = vm)
}

@Composable
private fun ProfileScreen(
    navController: NavHostController,
    authVm: AuthViewModel,
    userDataVm: UserDataViewModel,
    cartVm: CartViewModel
) {
    var isLogin by remember { mutableStateOf(true) }
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var registerRole by remember { mutableStateOf("buyer") }
    var shopName by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }

    val token = authVm.token
    val loading = authVm.loading
    val error = authVm.error

    val isClientLoggedIn = token != null && authVm.userRole?.lowercase() != "seller"

    LaunchedEffect(token, authVm.userRole) {
        if (isClientLoggedIn) {
            userDataVm.load()
            cartVm.load()
        }
    }

    Column(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Profil", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(Modifier.size(12.dp))

        if (token != null) {
            Text("Connecté", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.size(8.dp))
            Button(onClick = { authVm.logout() }) { Text("Se déconnecter") }

            if (isClientLoggedIn) {
                Spacer(Modifier.size(16.dp))

                val ring = userDataVm.ringSize()
                val bracelet = userDataVm.braceletSize()

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Mes mesures", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.size(10.dp))

                        if (ring == null && bracelet == null) {
                            Text("Aucune mesure enregistrée")
                            Spacer(Modifier.size(10.dp))
                            Button(
                                onClick = { navController.navigate(Screen.Measure.route) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.primary,
                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                )
                            ) { Text("Aller à Mesure") }
                        } else {
                            ring?.let {
                                Text("Bague", fontWeight = FontWeight.SemiBold)
                                Text("Diamètre: ${it.diameter_mm ?: "-"} mm")
                                Text("Circonférence: ${it.circumference_mm ?: "-"} mm")
                                Spacer(Modifier.size(10.dp))
                            }
                            bracelet?.let {
                                Text("Bracelet", fontWeight = FontWeight.SemiBold)
                                Text("Taille: ${it.circumference_mm ?: "-"} mm")
                                Spacer(Modifier.size(10.dp))
                            }
                            Button(
                                onClick = { navController.navigate(Screen.Measure.route) },
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                )
                            ) { Text("Modifier via Mesure") }
                        }
                    }
                }

                Spacer(Modifier.size(16.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = androidx.compose.material3.CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        contentColor = MaterialTheme.colorScheme.onSurface
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Mes favoris", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.size(10.dp))

                        if (userDataVm.favorites.isEmpty()) {
                            Text("Aucun favori")
                        } else {
                            userDataVm.favorites.forEach { fav ->
                                val p = fav.product
                                val title = p?.title ?: "Produit #${fav.product_id}"
                                Card(
                                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                                    colors = androidx.compose.material3.CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                        contentColor = MaterialTheme.colorScheme.onSurface
                                    )
                                ) {
                                    Column(Modifier.padding(12.dp)) {
                                        Text(title, fontWeight = FontWeight.SemiBold)
                                        p?.let { Text("${it.price} MAD", style = MaterialTheme.typography.bodySmall) }
                                        Spacer(Modifier.size(8.dp))
                                        Row(Modifier.fillMaxWidth()) {
                                            Button(
                                                onClick = { navController.navigate("catalog/${fav.product_id}") },
                                                modifier = Modifier.weight(1f).padding(end = 8.dp),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.primary,
                                                    contentColor = MaterialTheme.colorScheme.onPrimary
                                                )
                                            ) { Text("Voir") }
                                            Button(
                                                onClick = { userDataVm.removeFavorite(fav.product_id) },
                                                modifier = Modifier.weight(1f),
                                                colors = ButtonDefaults.buttonColors(
                                                    containerColor = MaterialTheme.colorScheme.error,
                                                    contentColor = MaterialTheme.colorScheme.onError
                                                )
                                            ) { Text("Retirer") }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } else {
            RowToggle(isLogin = isLogin, onToggle = { isLogin = it })
            Spacer(Modifier.size(12.dp))
            if (!isLogin) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nom") },
                    modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                )

                Text("Type de compte", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.size(8.dp))
                LazyRow {
                    item {
                        val selected = registerRole == "buyer"
                        Button(
                            onClick = { registerRole = "buyer" },
                            modifier = Modifier.padding(end = 10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        ) { Text("Client") }
                    }
                    item {
                        val selected = registerRole == "seller"
                        Button(
                            onClick = { registerRole = "seller" },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (selected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = if (selected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        ) { Text("Vendeur") }
                    }
                }
                Spacer(Modifier.size(12.dp))

                if (registerRole == "seller") {
                    OutlinedTextField(
                        value = shopName,
                        onValueChange = { shopName = it },
                        label = { Text("Nom boutique") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = phone,
                        onValueChange = { phone = it },
                        label = { Text("Téléphone") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = city,
                        onValueChange = { city = it },
                        label = { Text("Ville") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Adresse") },
                        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
                    )
                }
            }
            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp)
            )
            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Mot de passe") },
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp)
            )

            if (loading) {
                CircularProgressIndicator()
            } else {
                Button(
                    onClick = {
                        if (isLogin) {
                            authVm.login(email, password)
                        } else {
                            authVm.register(
                                name = name,
                                email = email,
                                password = password,
                                role = registerRole,
                                shopName = shopName.ifBlank { null },
                                phone = phone.ifBlank { null },
                                city = city.ifBlank { null },
                                address = address.ifBlank { null }
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(if (isLogin) "Se connecter" else "Créer un compte")
                }
            }
            error?.let {
                Spacer(Modifier.size(8.dp))
                Text("Erreur: $it", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}

@Composable
private fun CartScreen(navController: NavHostController, authVm: AuthViewModel, cartVm: CartViewModel) {
    val isClientLoggedIn = authVm.token != null && authVm.userRole?.lowercase() != "seller"

    LaunchedEffect(authVm.token, authVm.userRole) {
        if (isClientLoggedIn) cartVm.load()
    }

    if (!isClientLoggedIn) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("Connecte-toi pour accéder au panier")
            Spacer(Modifier.size(12.dp))
            Button(
                onClick = { navController.navigate(Screen.Profile.route) },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
            ) { Text("Aller au profil") }
        }
        return
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            Text("Panier", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.size(12.dp))
        }

        if (cartVm.loading) {
            item {
                Box(Modifier.fillMaxWidth().padding(vertical = 24.dp), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }

        cartVm.error?.let { err ->
            item {
                Text("Erreur: $err", color = MaterialTheme.colorScheme.error)
                Spacer(Modifier.size(12.dp))
            }
        }

        if (cartVm.items.isEmpty() && !cartVm.loading) {
            item {
                Text("Ton panier est vide")
                Spacer(Modifier.size(12.dp))
                Button(
                    onClick = { navController.navigate(Screen.Catalog.route) },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    )
                ) { Text("Aller au catalogue") }
            }
        }

        items(cartVm.items) { item ->
            val p = item.product
            val imageUrl = p?.let { chooseImage(it) } ?: ""
            val shop = p?.seller?.seller_profile?.shop_name ?: p?.seller?.name
            val subtotal = ((p?.price ?: 0.0) * item.quantity * 100).roundToInt() / 100.0
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                colors = androidx.compose.material3.CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) {
                Row(Modifier.padding(12.dp)) {
                    if (imageUrl.isNotBlank()) {
                        AsyncImage(
                            model = imageUrl,
                            contentDescription = p?.title ?: "Produit",
                            modifier = Modifier
                                .size(72.dp)
                                .padding(end = 12.dp)
                        )
                    }

                    Column(Modifier.weight(1f)) {
                        Text(p?.title ?: "Produit #${item.product_id}", fontWeight = FontWeight.SemiBold)
                        shop?.let { Text("Boutique: $it", style = MaterialTheme.typography.bodySmall) }
                        p?.let { Text("${it.price} MAD", style = MaterialTheme.typography.bodySmall) }
                        Text("Sous-total: $subtotal MAD", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.size(8.dp))

                        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = { if (item.quantity > 1) cartVm.update(item.product_id, item.quantity - 1) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) { Text("-") }

                        Text("  ${item.quantity}  ", fontWeight = FontWeight.SemiBold)

                        Button(
                            onClick = { cartVm.update(item.product_id, item.quantity + 1) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                contentColor = MaterialTheme.colorScheme.onSurface
                            )
                        ) { Text("+") }

                        Spacer(Modifier.size(12.dp))

                        Button(
                            onClick = { cartVm.remove(item.product_id) },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = MaterialTheme.colorScheme.error,
                                contentColor = MaterialTheme.colorScheme.onError
                            )
                        ) { Text("Supprimer") }
                        }
                    }
                }
            }
        }

        item {
            Spacer(Modifier.size(8.dp))
            val total = (cartVm.totalMad() * 100).roundToInt() / 100.0
            Text("Total: $total MAD", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.size(10.dp))
            Button(
                onClick = { cartVm.clear() },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
            ) { Text("Vider le panier") }
        }
    }
}

@Composable
private fun RowToggle(isLogin: Boolean, onToggle: (Boolean) -> Unit) {
    Column(Modifier.fillMaxWidth()) {
        Button(
            onClick = { onToggle(true) },
            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (isLogin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (isLogin) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )
        ) { Text("Connexion") }
        Button(
            onClick = { onToggle(false) },
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (!isLogin) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant,
                contentColor = if (!isLogin) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )
        ) { Text("Inscription") }
    }
}

private fun chooseImage(p: com.example.ringsizer.data.remote.ProductDto): String {
    val firstRemoteImage = p.images
        ?.firstOrNull { img ->
            val path = img.path ?: return@firstOrNull false
            val trimmed = path.trim()
            trimmed.isNotBlank() &&
                !trimmed.startsWith("content://") &&
                !trimmed.startsWith("file://") &&
                !trimmed.startsWith("android.resource://")
        }
        ?.path

    val candidate = when {
        !p.cover_image_path.isNullOrBlank() -> p.cover_image_path
        !firstRemoteImage.isNullOrBlank() -> firstRemoteImage
        else -> ""
    }
    return toFullUrl(candidate)
}

private fun toFullUrl(path: String): String {
    val trimmed = path.trim()
    val full = when {
        trimmed.isBlank() -> ""
        trimmed.startsWith("http") -> trimmed
        trimmed.startsWith("content://") -> trimmed
        trimmed.startsWith("file://") -> trimmed
        trimmed.startsWith("android.resource://") -> trimmed
        else -> "http://10.0.2.2:8000/${trimmed.trimStart('/')}"
    }
    Log.d("RingSizer", "cover_image_path: '$path' -> '$full'")
    return full
}

@Composable
private fun HomeButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.primary,
            contentColor = MaterialTheme.colorScheme.onPrimary
        )
    ) {
        Text(text, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
    }
}