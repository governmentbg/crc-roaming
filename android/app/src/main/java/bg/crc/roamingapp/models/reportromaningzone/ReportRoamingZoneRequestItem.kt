package bg.crc.roamingapp.models.reportromaningzone

data class ReportRoamingZoneRequestItem(
    val ts: Long?,
    val mcc: String?,
    val mnc: String?,
    val va: ArrayList<Double>?,
    val vb: ArrayList<Double>?
)