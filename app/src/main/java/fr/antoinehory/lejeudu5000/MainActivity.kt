package fr.antoinehory.lejeudu5000

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.hilt.android.AndroidEntryPoint
import fr.antoinehory.lejeudu5000.ui.navigation.AppNavigation
import fr.antoinehory.lejeudu5000.ui.theme.LeJeuDu5000Theme

@AndroidEntryPoint // Assurez-vous que Hilt est configuré si vous l'utilisez
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Vous pouvez conserver ceci si vous gérez les insets
        setContent {
            LeJeuDu5000Theme {
                Surface( // Utiliser Surface pour définir la couleur de fond globale
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppNavigation()
                }
            }
        }
    }
}