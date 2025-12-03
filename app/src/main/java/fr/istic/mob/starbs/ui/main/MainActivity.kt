package fr.istic.mob.starbs.ui.main

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.navigation.NavigationView
import fr.istic.mob.starbs.MainApp
import fr.istic.mob.starbs.R
import fr.istic.mob.starbs.databinding.ActivityMainBinding
import fr.istic.mob.starbs.services.GTFSDownloaderService
import fr.istic.mob.starbs.ui.loading.LoadingFragment
import fr.istic.mob.starbs.utils.GTFSConstants
import kotlinx.coroutines.launch
import java.io.File

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Toolbar
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener {
            binding.drawerLayout.openDrawer(GravityCompat.START)
        }

        // Menu du Drawer
        binding.navigationView.setNavigationItemSelectedListener(navListener)

        // Observer quand la base est prête
        viewModel.isReady.observe(this) {
            if (it) showMainFragment()
        }

        // Charger l’écran de chargement
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, LoadingFragment())
            .commit()

        // Démarrer les services
        startDownload()
    }

    private val navListener = NavigationView.OnNavigationItemSelectedListener { item ->
        when (item.itemId) {
            R.id.menu_reload -> {
                forceReload()
            }
        }
        binding.drawerLayout.closeDrawer(GravityCompat.START)
        true
    }

    private fun forceReload() {

        // 1. Supprimer le JSON local
        val jsonFile = File(filesDir, GTFSConstants.LOCAL_JSON_FILE)
        if (jsonFile.exists()) jsonFile.delete()

        // 2. Supprimer l'ancien ZIP si tu l'utilises
        val zipFile = File(filesDir, "gtfs.zip")
        if (zipFile.exists()) zipFile.delete()

        // 3. Vider la base
        lifecycleScope.launch {
            MainApp.repository.clearDatabase()

            // 4. Lancer un nouveau téléchargement
            showLoadingFragment()
            startDownload()
        }
    }


    private fun startDownload() {
        val intent = Intent(this, GTFSDownloaderService::class.java)
        startService(intent)
    }

    private fun showMainFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, MainFragment())
            .commit()
    }
    private fun showLoadingFragment() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, LoadingFragment())
            .commit()
    }

    override fun onStart() {
        super.onStart()
        viewModel.registerReceiver()
    }

    override fun onStop() {
        super.onStop()
        viewModel.unregisterReceiver()
    }
}
