package bg.crc.roamingapp.models.roamingzones

data class RoamingStatus(
    val isInRoaming: Boolean,
    val isRoamingSim1: Boolean,
    val isRoamingSim2: Boolean,
    val mccSim1: String?,
    val opMccSim1: String?,
    val mncSim1: String?,
    val opMncSim1: String?,
    val opNameSim1: String?,
    val mccSim2: String?,
    val opMccSim2: String?,
    val mncSim2: String?,
    val opMncSim2: String?,
    val opNameSim2: String?
)
