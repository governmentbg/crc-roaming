package bg.crc.roamingapp.models.history

data class Z(
    val mcc: String?,
    val mnc: String?,
    val ts: Int?,
    val va: List<Double>?,
    val vb: List<Double>?
)