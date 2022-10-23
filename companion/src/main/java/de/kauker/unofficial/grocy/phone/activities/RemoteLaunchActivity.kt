package de.kauker.unofficial.grocy.phone.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import de.kauker.unofficial.grocy.phone.MainActivity

@SuppressLint("CustomSplashScreen")
class RemoteLaunchActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startActivity(Intent(this, MainActivity().javaClass))
        finish()
    }
}
