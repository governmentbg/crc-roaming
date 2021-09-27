package bg.crc.roamingapp.models.telecomslist

data class TelecomsListResponse(
    val countries: List<Country>?,
    val ec: Int?,
    val vn: Int?
)