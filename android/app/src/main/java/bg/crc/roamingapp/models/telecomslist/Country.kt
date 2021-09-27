package bg.crc.roamingapp.models.telecomslist

import androidx.annotation.StringRes


data class Country(
    val bgName: String?,
    val dname: String?,
    val phoneCode: String?,
    val mcc: String?,
    val isEU: Int?,
    val telcos: List<Telco>?
) {
    var countryFlag: Int? = null
    var isCheckCountry: Boolean? = true
}

