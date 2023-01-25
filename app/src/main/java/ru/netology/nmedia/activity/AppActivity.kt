package ru.netology.nmedia.activity

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import ru.netology.nmedia.R
import ru.netology.nmedia.activity.NewPostFragment.Companion.textArg

import ru.netology.nmedia.di.DependencyContainer
import ru.netology.nmedia.viewmodel.AuthViewModel
import ru.netology.nmedia.viewmodel.ViewModelFactory
import javax.inject.Inject

class AppActivity : AppCompatActivity(R.layout.activity_app) {
    private val dependencyContainer = DependencyContainer.getInstance()
    private val viewModel: AuthViewModel by viewModels(
        factoryProducer = {
            ViewModelFactory(dependencyContainer.repository, dependencyContainer.appAuth)
        }
    )

    @Inject
    lateinit var firebaseMessaging: FirebaseMessaging

    @Inject
    lateinit var googleApiAvailability: GoogleApiAvailability

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        intent?.let {
            if (it.action != Intent.ACTION_SEND) {
                return@let
            }

            val text = it.getStringExtra(Intent.EXTRA_TEXT)
            if (text?.isNotBlank() != true) {
                return@let
            }

            intent.removeExtra(Intent.EXTRA_TEXT)
            findNavController(R.id.nav_host_fragment)
                .navigate(
                    R.id.action_feedFragment_to_newPostFragment,
                    Bundle().apply {
                        textArg = text
                    }
                )
        }

        viewModel.data.observe(this) {
            invalidateOptionsMenu()
        }

//        FirebaseInstallations.getInstance().id.addOnCompleteListener { task ->
//            if (!task.isSuccessful) {
//                println("some stuff happened: ${task.exception}")
//                return@addOnCompleteListener
//            }
//
//            val token = task.result
//            println(token)
//        }

        checkGoogleApiAvailability()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)

        menu.let {
            it.setGroupVisible(R.id.unauthenticated, !viewModel.authenticated)
            it.setGroupVisible(R.id.authenticated, viewModel.authenticated)
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.signin -> {
                // TODO: just hardcode it, implementation must be in homework
                dependencyContainer.appAuth.setAuth(5, "x-token")
                true
            }
            R.id.signup -> {
                // TODO: just hardcode it, implementation must be in homework
                dependencyContainer.appAuth.setAuth(5, "x-token")
                true
            }
            R.id.signout -> {
                // TODO: just hardcode it, implementation must be in homework
                dependencyContainer.appAuth.removeAuth()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun checkGoogleApiAvailability() {
        with(googleApiAvailability) {
            val code = isGooglePlayServicesAvailable(this@AppActivity)
            if (code == ConnectionResult.SUCCESS) {
                return@with
            }
            if (isUserResolvableError(code)) {
                getErrorDialog(this@AppActivity, code, 9000)?.show()
                return
            }
            Toast.makeText(this@AppActivity, "Google Api Unavailable", Toast.LENGTH_LONG)
                .show()
        }

        firebaseMessaging.token.addOnSuccessListener {
            println(it)
        }
    }
}