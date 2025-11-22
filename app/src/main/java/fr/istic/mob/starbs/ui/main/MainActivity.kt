package fr.istic.mob.starbs.ui.main

import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.navigation.NavigationView
import fr.istic.mob.starbs.R
import fr.istic.mob.starbs.databinding.ActivityMainBinding
import fr.istic.mob.starbs.services.GTFSDownloaderService
import android.content.Intent
import androidx.drawerlayout.widget.DrawerLayout

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var drawerToggle: ActionBarDrawerToggle

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        setupDrawer()
        setupMenuActions()
    }

    private fun setupDrawer() {
        drawerToggle = ActionBarDrawerToggle(
            this,
            binding.drawerLayout,
            binding.toolbar,
            R.string.open_drawer,
            R.string.close_drawer
        )

        binding.drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
    }

    private fun setupMenuActions() {
        binding.navView.setNavigationItemSelectedListener { item ->
            when (item.itemId) {

                R.id.menu_download_gtfs -> {
                    viewModel.downloadGTFS(this)
                }

                R.id.menu_reset_db -> {
                    viewModel.resetDatabase()
                }

                R.id.menu_quit -> {
                    finish()
                }
            }

            binding.drawerLayout.closeDrawer(binding.navView)
            true
        }
    }
}
