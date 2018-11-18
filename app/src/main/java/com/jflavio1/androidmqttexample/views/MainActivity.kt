package com.jflavio1.androidmqttexample.views

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.support.annotation.RequiresApi
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.widget.Toast
import com.jflavio1.androidmqttexample.R
import com.jflavio1.androidmqttexample.model.CustomLightSensor
import com.jflavio1.androidmqttexample.presenters.SensorsListPresenter
import com.jflavio1.androidmqttexample.presenters.SensorsListPresenterImpl
import kotlinx.android.synthetic.main.activity_main.*

/**
 * MainActivity
 *
 * @author Jose Flavio - jflavio90@gmail.com
 * @since  6/5/17
 */
class MainActivity : AppCompatActivity(), SensorsListView {

    lateinit var presenter: SensorsListPresenter
    lateinit var sensorsAdapter: SensorsAdapter

    companion object {
        const val MISCELLANEOUS_CHANNEL_NAME = "Miscellaneous"
        const val MISCELLANEOUS_CHANNEL_ID = "com.Miscellaneous"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        SensorsListPresenterImpl(this)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannels()
        }
        sensorsAdapter = SensorsAdapter(object : SensorsAdapter.SensorsAdapterListener {
            override fun onSensorLightClick(sensor: CustomLightSensor) {
                presenter.changeLightState(sensor, !sensor.lightOn)
            }
        })

        mainActivity_rv.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            setHasFixedSize(true)
            adapter = sensorsAdapter
        }

    }

    override fun onDestroy() {
        this.presenter.stopMqttService()
        super.onDestroy()
    }

    override fun setSensorPresenter(presenter: SensorsListPresenter) {
        this.presenter = presenter
        this.presenter.initMqttService()
    }

    override fun onMqttConnected() {
        this.presenter.getTemperatures()
    }

    override fun onMqttError(errorMessage: String) {
        Toast.makeText(this, "Error on connection: $errorMessage", Toast.LENGTH_SHORT).show()
    }

    override fun onMqttDisconnected() {
        Toast.makeText(this, "Disconnected from server...", Toast.LENGTH_SHORT).show()
    }

    override fun onMqttStopped() {
    }

    override fun setSensorsTemperature(sensors: ArrayList<CustomLightSensor>) {
        sensorsAdapter.updateAllList(sensors)
    }

    override fun getViewContext() = this

    @RequiresApi(api = Build.VERSION_CODES.O)
    fun createNotificationChannels() {
        val miscChannel = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            NotificationChannel(
                MISCELLANEOUS_CHANNEL_ID, MISCELLANEOUS_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW
            )
        } else {
            NotificationChannel(
                MISCELLANEOUS_CHANNEL_ID, MISCELLANEOUS_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_NONE
            )
        }
        miscChannel.enableLights(false)
        miscChannel.enableVibration(false)
        miscChannel.setSound(null, null)
        miscChannel.setShowBadge(false)
        val notificationManager: NotificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(miscChannel)
    }

}
