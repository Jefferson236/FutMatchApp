package com.example.futmatchapp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private var bottomNavigationView: BottomNavigationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
        val navController = navHostFragment?.navController

        if (navController != null && bottomNavigationView != null) {
            bottomNavigationView?.setupWithNavController(navController)
        }
    }

    fun ocultarNavegacionSoporte() {
        bottomNavigationView?.visibility = View.GONE
    }

    fun mostrarNavegacionSoporte() {
        bottomNavigationView?.visibility = View.VISIBLE
    }

    fun ocultarBottomNavigation() {
        bottomNavigationView?.visibility = View.GONE
    }

    fun mostrarBottomNavigation() {
        bottomNavigationView?.visibility = View.VISIBLE
    }

    fun cambiarFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .addToBackStack(null)
            .commit()
    }
}
