package bg.crc.roamingapp.activity

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Vibrator
import android.view.View
import android.widget.CompoundButton
import bg.crc.roamingapp.R
import bg.crc.roamingapp.app.ApplicationSession
import bg.crc.roamingapp.app.RoamingProtect
import bg.crc.roamingapp.constant.AppUtils
import bg.crc.roamingapp.constant.ReportRoamingZone
import bg.crc.roamingapp.models.BlockingEntry
import bg.crc.roamingapp.service.AlarmReceiver
import kotlinx.android.synthetic.main.activity_settings.*
import kotlinx.android.synthetic.main.header.*
import kotlinx.android.synthetic.main.header.view.*
import kotlinx.android.synthetic.main.internet_connectivity_view.*
import java.util.*


/**
 * This screen display Settings UI.
 * User can enable disable mobile data, data roaming and notification.
 */
class SettingsActivity : BaseActivity(), CompoundButton.OnCheckedChangeListener {
    private var lastRoamingState : Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        setListeners()
        init()

        header_layout.title.text = getString(R.string.title_settings)
        header_layout.ivSettings.visibility = View.GONE
        header_layout.ivBack.visibility = View.VISIBLE
    }

    /**
     *This function update [swDataRoaming], [swMobileData], [swNotifications] state.
     * This function also for Listener, Permission.
     * */
    private fun init() {
        //switch click listener
        swDataRoaming.setOnCheckedChangeListener(this)
        swMobileData.setOnCheckedChangeListener(this)
        swNotifications.setOnCheckedChangeListener(this)

        val settingNotification = ApplicationSession.getUserSettingNotification()
        swNotifications.isChecked = settingNotification

        setMobileDataOnOff()
        setRoamingDataOnOff()
    }

    /**
     *set listeners
     **/
    private fun setListeners() {
        swMobileData.setOnClickListener {
            val v = this@SettingsActivity.applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
            v?.cancel()

            ApplicationSession.mMediaPlayer?.stop()
        }

        swDataRoaming.setOnClickListener {
            val v = this@SettingsActivity.applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
            v?.cancel()

            ApplicationSession.mMediaPlayer?.stop()
        }

        swNotifications.setOnClickListener {
            RoamingProtect.isNotificationRunning = false
            val v = this@SettingsActivity.applicationContext.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator?
            v?.cancel()

            ApplicationSession.mMediaPlayer?.stop()
        }

        ivBack.setOnClickListener {
            this.startActivity(Intent(this@SettingsActivity, HomeActivity::class.java))
        }
    }

    /**
    * This override function for change switch state
    * */
    override fun onCheckedChanged(buttonView: CompoundButton, isChecked: Boolean) {
        when (buttonView.id) {
            R.id.swMobileData -> {
                startActivityForResult(Intent(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS), 0)
            }

            R.id.swDataRoaming -> {
                startActivityForResult(Intent(android.provider.Settings.ACTION_DATA_ROAMING_SETTINGS), 0)
            }

            R.id.swNotifications -> {
                ApplicationSession.setUserSettingNotification(isChecked)

                if(!isChecked) {
                    val alarmIntent = Intent(this@SettingsActivity, AlarmReceiver::class.java)
                    val pendingIntent = PendingIntent.getBroadcast(this@SettingsActivity, 0, alarmIntent, 0)

                    val manager = this@SettingsActivity.getSystemService(Context.ALARM_SERVICE) as AlarmManager

                    val calendar: Calendar = Calendar.getInstance()
                    calendar.timeInMillis = System.currentTimeMillis()
                    calendar.add(Calendar.DAY_OF_MONTH, 1)

                    manager.setRepeating(
                        AlarmManager.RTC_WAKEUP, calendar.timeInMillis,
                        AlarmManager.INTERVAL_DAY, pendingIntent
                    )
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setMobileDataOnOff()
        setRoamingDataOnOff()
    }

    /**
     * This function checks device settings for 'Mobile data' and
     * updates switch state according to current device settings.
     */
    private fun setMobileDataOnOff() {
        swMobileData.setOnCheckedChangeListener(null)
        swMobileData.isChecked = AppUtils.isMobileDataOn()
        swMobileData.setOnCheckedChangeListener(this)
    }

    /**
     * This function checks device settings for 'Data roaming' and
     * updates switch state according to current device settings.
     */
    private fun setRoamingDataOnOff() {
        if(AppUtils.isMiUi()) {
            tvwDataRoaming.visibility = View.VISIBLE
            swDataRoaming.isEnabled = false
        } else {
            tvwDataRoaming.visibility = View.GONE
            swDataRoaming.isEnabled = true

            swDataRoaming.setOnCheckedChangeListener(null)
            val roamingState = AppUtils.isDataRoamingOn()
            swDataRoaming.isChecked = roamingState
            swDataRoaming.setOnCheckedChangeListener(this)

            if (lastRoamingState && !roamingState) {
                checkSimRoamingStatus()
            }
            lastRoamingState = roamingState
        }
    }

    private fun checkSimRoamingStatus() {
        val sim1RoamingStatus = ReportRoamingZone.isSim1InRoaming(this@SettingsActivity)
        val sim2RoamingStatus = ReportRoamingZone.isSim2InRoaming(this@SettingsActivity)

        if(sim1RoamingStatus.first)
            reportBlocking(sim1RoamingStatus.second!!)

        if(sim2RoamingStatus.first)
            reportBlocking(sim2RoamingStatus.second!!)
    }

    private fun reportBlocking(simData: Pair<String?, String?>) {
        if(ApplicationSession.getBlockingData() == null && RoamingProtect.isNotificationRunning) {
            RoamingProtect.isNotificationRunning = false
            ApplicationSession.putBlockingData(BlockingEntry(Calendar.getInstance().timeInMillis / 1000, simData.first!!, simData.second!!))
        }
    }

    /**
     * This override function notify to user about internet connectivity.
     */
    override fun onInternetConnectivityChange() {
        tvInternetConnection?.visibility = if (isInternetAvailable) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

}