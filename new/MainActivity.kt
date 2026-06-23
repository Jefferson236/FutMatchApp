package com.example.futmatchapp

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.futmatchapp.vista.ProfileFragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    private lateinit var bottomNavigationView: BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigationView = findViewById(R.id.bottomNavigationView)

        // Gestión básica del Bottom Navigation Bar para comunicarse con las vistas
        bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_explore -> {
                    // Acción para el feed deslizable (Fase posterior)
                    true
                }
                R.id.nav_profile -> {
                    // Navegar al Fragment de Perfil
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.nav_host_fragment, ProfileFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }
    }

    fun ocultarBottomNavigation() {
        bottomNavigationView.visibility = View.GONE
    }

    fun mostrarBottomNavigation() {
        bottomNavigationView.visibility = View.VISIBLE
    }

    fun cambiarFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment, fragment)
            .addToBackStack(null)
            .commit()
    }
}
