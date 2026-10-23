package com.deepdots.reference

import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

/** Parte "clásica" de la app: AppCompat + Fragments con navegación inferior. */
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val nav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        nav.setOnItemSelectedListener { item ->
            show(
                when (item.itemId) {
                    R.id.nav_search -> SearchFragment()
                    R.id.nav_account -> AccountFragment()
                    else -> HomeFragment()
                }
            )
            true
        }
        if (savedInstanceState == null) show(HomeFragment())
    }

    override fun onResume() {
        super.onResume()
        findViewById<Button>(R.id.probe_button).bindProbe()
    }

    private fun show(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
