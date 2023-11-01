import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
data class SensorData(var dataA: Float, var dataB: Float, var dataC: Float, var timestamp: Long ) {
}