package bg.crc.roamingapp.models.reportromaningzone

data class RealRoamingZone(
    var ts: Long,
    var mcc: String,
    var mnc: String,
    var opMcc: String,
    var opMnc: String,
    var opName: String,
    var latVa: Double,
    var lngVa: Double,
    var latVb: Double,
    var lngVb: Double
)