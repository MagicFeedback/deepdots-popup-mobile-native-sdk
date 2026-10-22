package com.deepdots.reference

import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

/** Cada pestaña informa su ruta al SDK al hacerse visible, como haría un cliente. */
abstract class TrackedFragment(private val path: String) : Fragment() {
    override fun onResume() {
        super.onResume()
        ReferenceApp.sdk.setPath(path)
        HostLog.add("setPath $path")
    }
}

class HomeFragment : TrackedFragment(Catalog.HOME) {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_home, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val list = view.findViewById<RecyclerView>(R.id.product_list)
        list.layoutManager = LinearLayoutManager(requireContext())
        list.adapter = ProductAdapter { product ->
            startActivity(ProductDetailActivity.intent(requireContext(), product.id))
        }
    }
}

private class ProductAdapter(
    private val onTap: (Catalog.Product) -> Unit,
) : RecyclerView.Adapter<ProductAdapter.Holder>() {

    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.product_name)
        val note: TextView = view.findViewById(R.id.product_note)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        Holder(LayoutInflater.from(parent.context).inflate(R.layout.item_product, parent, false))

    override fun getItemCount() = Catalog.products.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val product = Catalog.products[position]
        holder.name.text = "${product.id}. ${product.name} · ${product.price}"
        holder.note.text = product.note
        holder.itemView.contentDescription = "product_${product.id}"
        holder.itemView.setOnClickListener { onTap(product) }
    }
}

class SearchFragment : TrackedFragment(Catalog.SEARCH) {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_search, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val input = view.findViewById<EditText>(R.id.search_input)
        val results = view.findViewById<TextView>(R.id.search_results)
        val search = {
            val query = input.text.toString().trim()
            val hits = Catalog.products.filter { it.name.contains(query, ignoreCase = true) }
            results.text = if (hits.isEmpty()) "No results" else hits.joinToString("\n") { it.name }
            ReferenceApp.sdk.trackSearch(query, hits.size)
            HostLog.add("trackSearch '$query' results=${hits.size}")
        }
        view.findViewById<Button>(R.id.search_button).setOnClickListener { search() }
        input.setOnEditorActionListener { _, actionId, _ ->
            (actionId == EditorInfo.IME_ACTION_SEARCH).also { if (it) search() }
        }
    }
}

class AccountFragment : TrackedFragment(Catalog.ACCOUNT) {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.fragment_account, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val user = view.findViewById<TextView>(R.id.account_user)
        val refresh = {
            user.text = "Signed in as: ${Session.userId(requireContext()) ?: "anonymous"}\nSDK user_id: ${ReferenceApp.sdk.getUserId()}"
        }
        refresh()

        view.findViewById<Button>(R.id.account_sign_in).setOnClickListener {
            Session.setUserId(requireContext(), "test-user-1")
            ReferenceApp.sdk.setUserId("test-user-1")
            HostLog.add("setUserId test-user-1")
            refresh()
        }
        view.findViewById<Button>(R.id.account_sign_out).setOnClickListener {
            Session.setUserId(requireContext(), null)
            ReferenceApp.sdk.setUserId(null)
            HostLog.add("setUserId null")
            refresh()
        }

        view.findViewById<Button>(R.id.open_dialog).setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Host dialog")
                .setMessage("A dialog owned by the app. A popup may open on top; closing the popup must leave this dialog open.")
                .setPositiveButton("Fire custom-event") { _, _ -> fireEvent() }
                .setNegativeButton("Close", null)
                .show()
            HostLog.add("host dialog opened")
        }
        view.findViewById<Button>(R.id.open_bottom_sheet).setOnClickListener {
            HostBottomSheet().show(childFragmentManager, "host-sheet")
            HostLog.add("host bottom sheet opened")
        }
        view.findViewById<Button>(R.id.open_date_picker).setOnClickListener {
            DatePickerDialog(requireContext()).show()
            HostLog.add("host date picker opened")
        }
        view.findViewById<Button>(R.id.open_checkout).setOnClickListener {
            startActivity(Intent(requireContext(), CheckoutActivity::class.java))
        }
        view.findViewById<Button>(R.id.open_diagnostics).setOnClickListener {
            startActivity(Intent(requireContext(), DiagnosticsActivity::class.java))
        }
    }
}

class HostBottomSheet : BottomSheetDialogFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View =
        inflater.inflate(R.layout.bottom_sheet_filters, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<Button>(R.id.sheet_fire_event).setOnClickListener { fireEvent() }
    }
}

fun fireEvent() {
    ReferenceApp.sdk.triggerEvent(Catalog.CUSTOM_EVENT)
    HostLog.add("triggerEvent ${Catalog.CUSTOM_EVENT}")
}
