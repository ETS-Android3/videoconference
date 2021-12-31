package io.agora.api;

import io.agora.requestmodels.CheckCodeRequest;
import io.agora.requestmodels.DeviceSettingModel;
import io.agora.requestmodels.GetRoomsRequest;
import io.agora.requestmodels.LoginRequest;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Url;


public interface APIInterface {



    @GET
    Call<ResponseBody> getApi(@Header("Authorization") String Authorization,
                              @Url String url);


    @POST("app/Api/company_login")
    Call<ResponseBody> login(@Body LoginRequest request);

    @POST("app/Api/reload")
    Call<ResponseBody> getRooms(@Body GetRoomsRequest request);

    @POST("app/Api/check_code")
    Call<ResponseBody> checkCode(@Body CheckCodeRequest request);

    @POST("app/Api/change_availability")
    Call<ResponseBody> changeAvailability(@Body GetRoomsRequest request);

    @POST("app/Api/send_notification")
    Call<ResponseBody> sendNotification(@Body GetRoomsRequest request);

    @POST("app/Api/invite_user")
    Call<ResponseBody> inviteApi(@Body GetRoomsRequest request);

    @POST("app/Api/get_notification")
    Call<ResponseBody> getNotification(@Body LoginRequest request);

    @POST("app/Api/update_token")
    Call<ResponseBody> updateToken(@Body CheckCodeRequest request);

    @POST("app/Api/invite_user_list")
    Call<ResponseBody> getInviteUserList(@Body GetRoomsRequest request);

    @POST("app/Api/update_device_name")
    Call<ResponseBody> updateDeviceName(@Body CheckCodeRequest request);

    @POST("app/Api/update_device_connection_status")
    Call<ResponseBody> updateDeviceSettings(@Body DeviceSettingModel request);




}
