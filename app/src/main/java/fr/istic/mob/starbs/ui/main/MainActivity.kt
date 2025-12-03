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
import fr.istic.mob.starbs.MainApp
import fr.istic.mob.starbs.R
import fr.istic.mob.starbs.databinding.ActivityMainBinding
import fr.istic.mob.starbs.services.GTFSParserService
import fr.istic.mob.starbs.ui.loading.LoadingFragment
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

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

        if (Build.VERSION.SDK_INT >= 33 &&
            checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.POST_NOTIFICATIONS), 10)
        }

        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        viewModel = ViewModelProvider(this)[MainViewModel::class.java]

        // -------------------------------
        //   CHOIX DU PREMIER ÉCRAN
        // -------------------------------
        val prefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        val firstLaunch = prefs.getBoolean("first_launch", true)

        if (firstLaunch) {

            // ➜ On montre l'écran de chargement
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, LoadingFragment())
                .commit()

            // ➜ On télécharge automatiquement
            viewModel.downloadGTFS(this)

            prefs.edit().putBoolean("first_launch", false).apply()

        } else {

            // Vérifier si la base est remplie
            if (viewModel.isDatabaseReady()) {

                // ✔ Base OK → directement écran principal
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, MainFragment())
                    .commit()

                // Vérifier si mise à jour nécessaire
                viewModel.autoUpdateGTFS(this)

            } else {

                // Base vide → écran de chargement
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, LoadingFragment())
                    .commit()

                viewModel.downloadGTFS(this)
            }
        }

        // Drawer/menu après avoir choisi l'écran
        setupDrawer()
        setupMenuActions()

        // Message éventuel
        viewModel.progress.observe(this) { (percent, msg) ->
            if (msg.startsWith("Base réinitialisée")) {
                android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_SHORT).show()
            }
        }
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

                R.id.menu_update_gtfs -> {
                    // Télécharger / mettre à jour GTFS
                    viewModel.downloadGTFS(this)
                }

                R.id.menu_reset_db -> {
                    // Réinitialiser la base
                    viewModel.resetDatabase()
                }

                R.id.menu_quit -> {
                    // NE FERMER L'APP QUE ICI
                    finish()
                }
            }
            binding.drawerLayout.closeDrawer(binding.navView)
            true
        }
    }


    private fun setupFragment() {
        if (supportFragmentManager.findFragmentById(R.id.fragment_container) == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, MainFragment())
                .commit()
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
//        updateMenuLabel()
    }

    override fun onPause() {
        unregisterReceiver(gtfsReceiver)
        super.onPause()
    }

//    private fun updateMenuLabel() {
//        lifecycleScope.launch {
//            val isEmpty = MainApp.repository.isDatabaseEmpty()
//            val item = binding.navView.menu.findItem(R.id.menu_update_gtfs)
//            item.title = if (isEmpty) "Télécharger GTFS" else "Mettre à jour GTFS"
//        }
//    }
}
