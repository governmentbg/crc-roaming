package bg.crc.roamingapp.constant

import android.location.Location
import kotlin.math.absoluteValue

object LocationConverter {
    fun latitudeAsDMS(latitude: Double, decimalPlace: Int): String {
        val direction = if (latitude > 0) "N" else "S"
        var strLatitude = Location.convert(latitude.absoluteValue, Location.FORMAT_DEGREES)
        strLatitude = replaceDelimiters(strLatitude, decimalPlace)
        strLatitude += " $direction"
        return strLatitude
    }

    fun longitudeAsDMS(longitude: Double, decimalPlace: Int): String {
        val direction = if (longitude > 0) "E" else "W"
        var strLongitude = Location.convert(longitude.absoluteValue, Location.FORMAT_DEGREES)
        strLongitude = replaceDelimiters(strLongitude, decimalPlace)
        strLongitude += " $direction"
        return strLongitude
    }

    /**
     * convert location to the human readable format.
     *
     * @param strLocation : String - Coordinate of location
     * @param decimalPlace : Int - digit after decimal points
     * Note: Coordinate need into the string
     * */
    private fun replaceDelimiters(strLocation: String, decimalPlace: Int): String {
        var str = strLocation
        str = str.replaceFirst(":".toRegex(), "°")
        str = str.replaceFirst(":".toRegex(), "'")
        var pointIndex = str.indexOf(".")
        if(pointIndex == -1)
            pointIndex = str.indexOf(",")

        val endIndex = pointIndex + 1 + decimalPlace
        if (endIndex < str.length) {
            str = str.substring(0, endIndex)
        }
        str += "\""
        return str
    }

}