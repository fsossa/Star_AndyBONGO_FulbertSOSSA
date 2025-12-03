package fr.istic.mob.starbs.ui.main

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import fr.istic.mob.starbs.R
import fr.istic.mob.starbs.databinding.ActivityMainBinding
import fr.istic.mob.starbs.services.GTFSParserService
import fr.istic.mob.starbs.ui.loading.LoadingFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var viewModel: MainViewModel
    private lateinit var drawerToggle: ActionBarDrawerToggle

    private val gtfsReceiver = object : android.content.BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val percent = intent.getIntExtra(GTFSParserService.EXTRA_PERCENT, 0)
            val msg = intent.getStringExtra(GTFSParserService.EXTRA_MESSAGE) ?: ""
            viewModel.updateProgress(percent, msg)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {

        // Permission notifications
        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 10)
        }

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        setupDrawer()
        setupMenuActions()

        // -------------------------------
        //   LANCEMENT ASYNCHRONE
        // -------------------------------
        lifecycleScope.launch {
            val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
            val firstLaunch = prefs.getBoolean("first_launch", true)

            if (firstLaunch) {
                showLoadingScreen()
                viewModel.downloadGTFS(this@MainActivity)
                prefs.edit().putBoolean("first_launch", false).apply()
                return@launch
            }

            // Vérifie la base hors UI thread
            val baseReady = withContext(Dispatchers.IO) {
                viewModel.isDatabaseReady()
            }

            if (!baseReady) {
                showLoadingScreen()
                viewModel.downloadGTFS(this@MainActivity)
                return@launch
            }

            // Base prête → affichage direct
            showMainScreen()

            // Vérifier si mise à jour nécessaire
            viewModel.autoUpdateGTFS(this@MainActivity)
        }
    }

    private fun showLoadingScreen() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, LoadingFragment())
            .commitAllowingStateLoss()
    }

    private fun showMainScreen() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, MainFragment())
            .commitAllowingStateLoss()
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

//                R.id.menu_update_gtfs -> {
//                    showLoadingScreen()
//                    viewModel.downloadGTFS(this)
//                }

                R.id.menu_reset_db -> {
                    lifecycleScope.launch {
                        viewModel.resetDatabase()
                        showLoadingScreen()
                        viewModel.downloadGTFS(this@MainActivity)
                    }
                }

                R.id.menu_quit -> finish()
            }

            binding.drawerLayout.closeDrawer(binding.navView)
            true
        }
    }

    override fun onResume() {
        super.onResume()
        val filter = IntentFilter(GTFSParserService.ACTION_PROGRESS)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(
                gtfsReceiver,
                filter,
                Context.RECEIVER_NOT_EXPORTED
            )
        } else {
            registerReceiver(gtfsReceiver, filter)
        }
    }

    override fun onPause() {
        unregisterReceiver(gtfsReceiver)
        super.onPause()
    }
}
