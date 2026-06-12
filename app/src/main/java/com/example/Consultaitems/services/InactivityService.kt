import android.app.Activity
import android.content.Intent
import android.os.Handler
import com.example.Consultaitems.ui.activities.frmLogin
import java.lang.ref.WeakReference

object SessionManager {
    private const val INACTIVITY_TIMEOUT: Long =  60 * 60 * 1000 // 30 minutos en milisegundos
    private lateinit var activityReference: WeakReference<Activity>
    private var lastInteractionTime = 0L
    private var logoutHandler = Handler()
    private val logoutRunnable = Runnable {
        val activity = activityReference.get()
        if (activity != null && System.currentTimeMillis() - lastInteractionTime >= INACTIVITY_TIMEOUT) {
            // Redirigir al usuario al login
            val intent = Intent(activity, frmLogin::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            activity.startActivity(intent)
            activity.finish()
        }
    }

    fun onActivityResumed(activity: Activity) {
        activityReference = WeakReference(activity)
        resetLogoutTimer()
    }

    fun onUserInteraction() {
        resetLogoutTimer()
    }

    private fun resetLogoutTimer() {
        logoutHandler.removeCallbacks(logoutRunnable)
        logoutHandler.postDelayed(logoutRunnable, INACTIVITY_TIMEOUT)
        lastInteractionTime = System.currentTimeMillis()
    }
}
