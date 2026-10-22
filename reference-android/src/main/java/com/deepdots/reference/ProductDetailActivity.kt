package com.deepdots.reference

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.roundToInt

/**
 * Ficha de producto en Compose (otra Activity). Informa la ruta al volver a primer plano,
 * reporta el scroll y dispara click/evento. También se abre por deep link
 * (`deepdots-ref://product/{id}`), sin la Activity principal debajo.
 */
class ProductDetailActivity : ComponentActivity() {

    private var productId = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        productId = intent.data?.lastPathSegment?.toIntOrNull()
            ?: intent.getIntExtra(EXTRA_ID, 1)
        val product = Catalog.product(productId)

        setContent {
            MaterialTheme {
                ProductScreen(product, onBack = { finish() }, onCheckout = {
                    startActivity(Intent(this, CheckoutActivity::class.java))
                })
            }
        }
    }

    override fun onResume() {
        super.onResume()
        val path = Catalog.productPath(productId)
        ReferenceApp.sdk.setPath(path)
        HostLog.add("setPath $path")
    }

    companion object {
        private const val EXTRA_ID = "product_id"

        fun intent(context: Context, id: Int) =
            Intent(context, ProductDetailActivity::class.java).putExtra(EXTRA_ID, id)
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
private fun ProductScreen(product: Catalog.Product, onBack: () -> Unit, onCheckout: () -> Unit) {
    val listState = rememberLazyListState()
    ReportScroll(listState)
    var probe by remember { mutableIntStateOf(Probe.taps) }

    Scaffold(
        // Ids de Compose visibles para los E2E como resource-id.
        modifier = Modifier.semantics { testTagsAsResourceId = true },
        topBar = {
            TopAppBar(
                title = { Text(product.name) },
                navigationIcon = {
                    OutlinedButton(onClick = onBack, modifier = Modifier.testTag("detail_back")) { Text("Back") }
                },
            )
        },
    ) { padding ->
        LazyColumn(
            state = listState,
            modifier = Modifier.fillMaxSize().padding(padding).testTag("detail_list"),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilledTonalButton(
                        onClick = { probe = Probe.tap() },
                        modifier = Modifier.fillMaxWidth().testTag("probe_button"),
                    ) { Text("Probe taps: $probe") }
                    Text("${product.price} · ${product.note}", style = MaterialTheme.typography.bodyMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                ReferenceApp.sdk.triggerClick(Catalog.ADD_TO_CART_CLICK)
                                ReferenceApp.sdk.trackFunnelStep("purchase", "add_to_cart", "order-${product.id}")
                                HostLog.add("triggerClick ${Catalog.ADD_TO_CART_CLICK}")
                            },
                            modifier = Modifier.testTag("detail_add_to_cart"),
                        ) { Text("Add to cart") }
                        OutlinedButton(onClick = { fireEvent() }, modifier = Modifier.testTag("detail_fire_event")) {
                            Text("Fire event")
                        }
                    }
                    OutlinedButton(onClick = onCheckout, modifier = Modifier.testTag("detail_checkout")) {
                        Text("Go to checkout")
                    }
                }
            }
            items(30) { index ->
                Text(
                    "Section ${index + 1}. Lorem ipsum dolor sit amet, consectetur adipiscing elit. " +
                        "Sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad " +
                        "minim veniam, quis nostrud exercitation ullamco laboris.",
                    modifier = Modifier.padding(horizontal = 16.dp),
                )
            }
        }
    }
}

@Composable
private fun ReportScroll(listState: LazyListState) {
    LaunchedEffect(listState) {
        snapshotFlow {
            val layout = listState.layoutInfo
            val total = layout.totalItemsCount.coerceAtLeast(1)
            val last = layout.visibleItemsInfo.lastOrNull()?.index ?: 0
            (((last + 1).toFloat() / total) * 100f).roundToInt().coerceIn(0, 100)
        }
            .distinctUntilChanged()
            .collect { pct -> ReferenceApp.sdk.onScroll(pct) }
    }
}
