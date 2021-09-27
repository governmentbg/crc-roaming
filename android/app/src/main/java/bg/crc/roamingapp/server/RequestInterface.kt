package bg.crc.roamingapp.server

import bg.crc.roamingapp.constant.Constants
import bg.crc.roamingapp.models.changepassword.ChangePasswordResponse
import bg.crc.roamingapp.models.common.CommonResponse
import bg.crc.roamingapp.models.emailregistration.EmailRegistrationResponse
import bg.crc.roamingapp.models.history.HistoryResponse
import bg.crc.roamingapp.models.login.LoginResponse
import bg.crc.roamingapp.models.roamingzones.RoamingZonesResponse
import bg.crc.roamingapp.models.roamingzones.RoamingZonesResponseEx
import bg.crc.roamingapp.models.telecomslist.TelecomsListResponse
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.QueryMap

/**
 * This interface holds all the server request functions.
 *
 * These are all functions hold prefixed by the [bg.crc.roamingapp.constant.Constants.BASE_URL].
 * functions hold which  'request-method is used for serer interaction' and what
 * is response of that request.
 *
 */
interface RequestInterface {

    @GET(Constants.GET_PART)
    fun registrationViaEmail(@QueryMap parameterName: HashMap<String, String>): Observable<EmailRegistrationResponse>

    @GET(Constants.GET_PART)
    fun loginViaEmail(@QueryMap parameterName: HashMap<String, String>): Observable<LoginResponse>

    @GET(Constants.GET_PART)
    fun changePassword(@QueryMap parameterName: HashMap<String, String>): Observable<ChangePasswordResponse>

    @GET(Constants.GET_PART)
    fun loginViaFacebook(@QueryMap parameterName: HashMap<String, String>): Observable<LoginResponse>

    @GET(Constants.GET_PART)
    fun loginViaGoogle(@QueryMap parameterName: HashMap<String, String>): Observable<LoginResponse>

    @GET(Constants.GET_PART)
    fun logout(@QueryMap parameterName: HashMap<String, String>): Observable<CommonResponse>

    @GET(Constants.GET_PART)
    fun forgottenPassword(@QueryMap parameterName: HashMap<String, String>): Observable<CommonResponse>

    @GET(Constants.GET_PART)
    fun telecomsList(@QueryMap parameterName: HashMap<String, String>): Observable<TelecomsListResponse>

    @GET(Constants.GET_PART)
    fun getRoamingZones(@QueryMap parameterName: HashMap<String, String>): Observable<RoamingZonesResponse>

    @GET(Constants.GET_PART)
    fun getRoamingZonesEx(@QueryMap parameterName: HashMap<String, String>): Observable<RoamingZonesResponseEx>

    @GET(Constants.GET_PART)
    fun reportRoamingZone(@QueryMap parameterName: HashMap<String, String>): Observable<CommonResponse>

    @GET(Constants.GET_PART)
    fun reportBlocking(@QueryMap parameterName: HashMap<String, String>): Observable<CommonResponse>

    @GET(Constants.GET_PART)
    fun reportRoamingDbgZone(@QueryMap parameterName: HashMap<String, String>): Observable<CommonResponse>

    @GET(Constants.GET_PART)
    fun historyList(@QueryMap parameterName: HashMap<String, String>): Observable<HistoryResponse>

}