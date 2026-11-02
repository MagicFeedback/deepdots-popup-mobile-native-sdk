package com.deepdots.testapp

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShoppingBag
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.deepdots.sdk.platform.PlatformContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val Ink = Color(0xFF111827)
private val LabBg = Color(0xFF0F1115)
private val LabText = Color(0xFFC9CCD3)

data class Product(val id: String, val name: String, val detail: String, val price: Int)

val PRODUCTS = listOf(
    Product("fjell-jacket", "Fjell Jacket", "Waterproof · M · Green", 89),
    Product("wool-beanie", "Wool Beanie", "One size · Grey", 24),
    Product("ull-socks", "Ull Socks", "39–42", 12),
)

/** Screens. `path` is what the app passes to setPath(); segments.path in DEV matches on it. */
sealed class Route(val path: String) {
    data object Home : Route("/")
    data object Shop : Route("/shop")
    data class ProductDetail(val product: Product) : Route("/product/${product.id}")
    data object Cart : Route("/cart")
    data object Checkout : Route("/checkout")
    data object Account : Route("/account")
}

class MainActivity : ComponentActivity() {
    private val app get() = application as TestApp

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!app.initialized) app.config = LabConfig.resolve(this, intent)
        app.sdk.attachContext(PlatformContext(this))
        val config = app.config
        val consent = LabConfig.prefs(this).getString("consent", null)
        if (config.load == "immediate" || consent == "all") app.initSdk()
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(primary = Ink)) {
                NordlyApp(app, askConsent = consent == null)
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
private fun NordlyApp(app: TestApp, askConsent: Boolean) {
    val config = app.config
    val context = androidx.compose.ui.platform.LocalContext.current
    var stack by remember { mutableStateOf(startStack(config.start)) }
    val current = stack.last()
    val cart = remember { mutableStateListOf(PRODUCTS[0], PRODUCTS[1]) }
    var showLab by remember { mutableStateOf(false) }
    var showConsent by remember { mutableStateOf(askConsent) }
    var showConfirm by remember { mutableStateOf(false) }
    var rateProduct by remember { mutableStateOf<Product?>(null) }
    var previousPath by remember { mutableStateOf<String?>(null) }
    val scope = rememberCoroutineScope()

    fun push(route: Route) { stack = stack + route }
    fun tab(route: Route) { stack = listOf(route) }

    LaunchedEffect(current.path) {
        Lab.log("nav", "${previousPath ?: "launch"} → ${current.path}${if (config.nav == "silent") " (silent: no setPath)" else ""}")
        if (config.nav != "silent") app.sdk.setPath(current.path)
        previousPath = current.path
    }
    BackHandler(enabled = stack.size > 1) { stack = stack.dropLast(1) }

    Surface(
        modifier = Modifier.fillMaxSize().semantics { testTagsAsResourceId = true },
        color = Color(0xFFFAFAF9),
    ) {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            LabBar(config, onOpen = { showLab = true })
            TopAppBar(
                title = { Text(title(current), fontWeight = FontWeight.SemiBold) },
                navigationIcon = {
                    if (stack.size > 1) IconButton(onClick = { stack = stack.dropLast(1) }, modifier = Modifier.testTag("back")) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
            Box(Modifier.weight(1f).fillMaxWidth()) {
                when (current) {
                    Route.Home -> HomeScreen(onShop = { tab(Route.Shop) }, onProduct = { push(Route.ProductDetail(it)) })
                    Route.Shop -> ShopScreen(onProduct = { push(Route.ProductDetail(it)) })
                    is Route.ProductDetail -> ProductScreen(
                        current.product,
                        onAdd = {
                            cart.add(current.product)
                            Lab.log("host", "add to cart ${current.product.id} (cart ${cart.size})")
                        },
                        onRate = { rateProduct = current.product },
                    )
                    Route.Cart -> CartScreen(cart, onCheckout = { push(Route.Checkout) })
                    Route.Checkout -> CheckoutScreen(cart, onPlaceOrder = { showConfirm = true })
                    Route.Account -> AccountScreen(
                        userId = if (app.initialized) app.sdk.getUserId() else null,
                        onHelp = {
                            Lab.log("host", "click help-button")
                            app.sdk.triggerClick("help-button")
                        },
                    )
                }
            }
            LabLog()
            if (current != Route.Checkout) {
                NavigationBar {
                    TabItem("Home", Icons.Filled.Home, "tab-home", stack.first() == Route.Home) { tab(Route.Home) }
                    TabItem("Shop", Icons.Filled.ShoppingBag, "tab-shop", stack.first() == Route.Shop) { tab(Route.Shop) }
                    TabItem("Cart (${cart.size})", Icons.Filled.ShoppingCart, "tab-cart", stack.first() == Route.Cart) { tab(Route.Cart) }
                    TabItem("Account", Icons.Filled.AccountCircle, "tab-account", stack.first() == Route.Account) { tab(Route.Account) }
                }
            }
        }
    }

    rateProduct?.let { product ->
        ModalBottomSheet(onDismissRequest = { rateProduct = null }, modifier = Modifier.testIds().testTag("rate-sheet")) {
            RateSheet(product, onSubmit = { stars ->
                Lab.log("host", "rate ${product.id} stars=$stars → triggerEvent(rate_product)")
                rateProduct = null
                app.sdk.triggerEvent("rate_product")
            })
        }
    }

    if (showConsent) {
        AlertDialog(
            onDismissRequest = {},
            modifier = Modifier.testIds().testTag("cookie-dialog"),
            title = { Text("Cookies") },
            text = { Text("We use cookies to improve your experience.") },
            confirmButton = {
                Button(onClick = {
                    LabConfig.prefs(context).edit().putString("consent", "all").apply()
                    showConsent = false
                    Lab.log("host", "consent accepted")
                    app.initSdk()
                    if (config.nav != "silent") app.sdk.setPath(current.path)
                }, modifier = Modifier.testTag("consent-accept")) { Text("Accept all") }
            },
            dismissButton = {
                TextButton(onClick = {
                    LabConfig.prefs(context).edit().putString("consent", "necessary").apply()
                    showConsent = false
                    Lab.log("host", "consent necessary only")
                }, modifier = Modifier.testTag("consent-necessary")) { Text("Necessary only") }
            },
        )
    }

    if (showConfirm) {
        AlertDialog(
            onDismissRequest = { showConfirm = false },
            modifier = Modifier.testIds().testTag("confirm-dialog"),
            title = { Text("Place order?") },
            text = { Text("Total ${cart.sumOf { it.price }},00 €") },
            confirmButton = {
                Button(onClick = {
                    showConfirm = false
                    cart.clear()
                    Lab.log("host", "order placed")
                    Toast.makeText(context, "Order placed", Toast.LENGTH_SHORT).show()
                    tab(Route.Home)
                }, modifier = Modifier.testTag("confirm-order")) { Text("Confirm") }
            },
            dismissButton = { TextButton(onClick = { showConfirm = false }) { Text("Cancel") } },
        )
    }

    if (showLab) LabPanel(app, onClose = { showLab = false }, onRapidCheckout = {
        showLab = false
        // Rapid navigation (faster than Maestro can tap): Cart → Checkout → Cart, three times,
        // 250 ms per screen, ending on Cart.
        scope.launch {
            Lab.log("host", "rapid nav: cart ⇄ checkout x3")
            stack = listOf(Route.Cart)
            repeat(3) {
                delay(250); stack = listOf(Route.Cart, Route.Checkout)
                delay(250); stack = listOf(Route.Cart)
            }
        }
    })
}

private fun startStack(start: String): List<Route> = when (start) {
    "shop" -> listOf(Route.Shop)
    "cart" -> listOf(Route.Cart)
    "checkout" -> listOf(Route.Cart, Route.Checkout)
    "account" -> listOf(Route.Account)
    else -> listOf(Route.Home)
}

private fun title(route: Route) = when (route) {
    Route.Home -> "NORDLY"
    Route.Shop -> "Shop"
    is Route.ProductDetail -> route.product.name
    Route.Cart -> "Cart"
    Route.Checkout -> "Checkout"
    Route.Account -> "Account"
}

@Composable
private fun androidx.compose.foundation.layout.RowScope.TabItem(
    label: String, icon: androidx.compose.ui.graphics.vector.ImageVector, tag: String, selected: Boolean, onClick: () -> Unit,
) {
    NavigationBarItem(
        selected = selected, onClick = onClick, modifier = Modifier.testTag(tag),
        icon = { Icon(icon, contentDescription = null) }, label = { Text(label) },
    )
}

@Composable
private fun LabBar(config: LabConfig, onOpen: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().background(LabBg).padding(horizontal = 12.dp).height(40.dp).testTag("lab-bar"),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Text("SDK Lab", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        Text("${BuildConfig.SDK_VERSION} · ${if (BuildConfig.SDK_VERSION == "local") "local" else "Maven"}",
            color = LabText, fontSize = 12.sp, modifier = Modifier.testTag("lab-sdk-version"))
        Text("DEV", color = Color(0xFF6EE7A8), fontSize = 12.sp, modifier = Modifier.testTag("lab-env"))
        Text(config.scenario, color = Color(0xFF9CA3AF), fontSize = 12.sp, modifier = Modifier.testTag("lab-scenario"))
        Text("shown ${Lab.shown} · done ${Lab.completed}", color = LabText, fontSize = 12.sp, modifier = Modifier.testTag("lab-counts"))
        Spacer(Modifier.weight(1f))
        IconButton(onClick = onOpen, modifier = Modifier.testTag("lab-open")) {
            Icon(Icons.Filled.Settings, contentDescription = "Open SDK Lab", tint = LabText)
        }
    }
}

@Composable
private fun LabLog() {
    Column(
        Modifier.fillMaxWidth().background(LabBg).padding(horizontal = 12.dp, vertical = 6.dp).height(64.dp).testTag("lab-log"),
    ) {
        Lab.lines.takeLast(3).forEach {
            Text(it, color = LabText, fontSize = 10.sp, fontFamily = FontFamily.Monospace, maxLines = 1)
        }
    }
}

@Composable
private fun HomeScreen(onShop: () -> Unit, onProduct: (Product) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("New season, same rain.", fontSize = 24.sp, fontWeight = FontWeight.SemiBold)
        Text("Free shipping over 60 € · 30-day returns", color = Color(0xFF4B5563))
        Button(onClick = onShop, modifier = Modifier.fillMaxWidth().height(48.dp).testTag("home-shop")) { Text("Shop new arrivals") }
        Text("Featured", fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(top = 8.dp))
        PRODUCTS.take(2).forEach { ProductRow(it, onClick = { onProduct(it) }) }
    }
}

@Composable
private fun ShopScreen(onProduct: (Product) -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        PRODUCTS.forEach { ProductRow(it, onClick = { onProduct(it) }) }
    }
}

@Composable
private fun ProductRow(product: Product, onClick: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 10.dp).testTag("product-${product.id}"),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically,
    ) {
        Box(Modifier.size(56.dp).background(Color(0xFFD6D3C9), RoundedCornerShape(8.dp)))
        Column(Modifier.weight(1f)) {
            Text(product.name, fontSize = 16.sp)
            Text(product.detail, color = Color(0xFF4B5563), fontSize = 13.sp)
        }
        Text("${product.price},00 €")
    }
}

@Composable
private fun ProductScreen(product: Product, onAdd: () -> Unit, onRate: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Box(Modifier.fillMaxWidth().height(240.dp).background(Color(0xFFD6D3C9)))
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(product.name, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
            Text("${product.price},00 €", fontSize = 16.sp)
            Text(product.detail, color = Color(0xFF4B5563))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.padding(top = 8.dp)) {
                Button(onClick = onAdd, modifier = Modifier.weight(1f).height(48.dp).testTag("add-to-cart")) { Text("Add to cart") }
                OutlinedButton(onClick = onRate, modifier = Modifier.weight(1f).height(48.dp).testTag("rate-product")) { Text("Rate") }
            }
        }
    }
}

@Composable
private fun RateSheet(product: Product, onSubmit: (Int) -> Unit) {
    var stars by remember { mutableIntStateOf(4) }
    Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Rate the ${product.name}", fontSize = 18.sp, fontWeight = FontWeight.SemiBold)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            (1..5).forEach { n ->
                val selected = n == stars
                OutlinedButton(onClick = { stars = n }, modifier = Modifier.size(52.dp).testTag("star-$n"),
                    colors = if (selected) androidx.compose.material3.ButtonDefaults.outlinedButtonColors(containerColor = Ink, contentColor = Color.White)
                    else androidx.compose.material3.ButtonDefaults.outlinedButtonColors(),
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) { Text("$n") }
            }
        }
        Button(onClick = { onSubmit(stars) }, modifier = Modifier.fillMaxWidth().height(48.dp).testTag("submit-rating")) { Text("Submit rating") }
        Spacer(Modifier.height(16.dp))
    }
}

@Composable
private fun CartScreen(cart: List<Product>, onCheckout: () -> Unit) {
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text("${cart.size} items", color = Color(0xFF4B5563), modifier = Modifier.testTag("cart-count"))
        cart.forEachIndexed { i, p -> ProductRow(p, onClick = {}); if (i < cart.lastIndex) HorizontalDivider() }
        Button(onClick = onCheckout, enabled = cart.isNotEmpty(), modifier = Modifier.fillMaxWidth().padding(top = 12.dp).height(48.dp).testTag("go-checkout")) {
            Text("Go to checkout")
        }
    }
}

@Composable
private fun CheckoutScreen(cart: List<Product>, onPlaceOrder: () -> Unit) {
    var email by remember { mutableStateOf("qa+android@deepdots.dev") }
    var address by remember { mutableStateOf("1 Test Street") }
    Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Cart › Shipping › Payment", color = Color(0xFF6B7280), fontSize = 12.sp)
        OutlinedTextField(email, { email = it }, label = { Text("Email") }, modifier = Modifier.fillMaxWidth().testTag("checkout-email"))
        OutlinedTextField(address, { address = it }, label = { Text("Address") }, modifier = Modifier.fillMaxWidth())
        Row(Modifier.fillMaxWidth()) {
            Text("Total", fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text("${cart.sumOf { it.price }},00 €", fontWeight = FontWeight.SemiBold)
        }
        Button(onClick = onPlaceOrder, modifier = Modifier.fillMaxWidth().height(48.dp).testTag("place-order")) { Text("Place order") }
    }
}

@Composable
private fun AccountScreen(userId: String?, onHelp: () -> Unit) {
    Column(Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text("Test User", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        Text("SDK user id: ${userId ?: "—"}", color = Color(0xFF4B5563), fontSize = 13.sp, modifier = Modifier.testTag("account-user-id"))
        HorizontalDivider()
        Text("Orders", fontSize = 16.sp)
        Text("Addresses", fontSize = 16.sp)
        HorizontalDivider()
        OutlinedButton(onClick = onHelp, modifier = Modifier.fillMaxWidth().height(48.dp).testTag("help-button")) { Text("Help") }
    }
}
