package com.example.planit.service;

import java.util.List;

import model.TeamDTO;
import model.TeamMemebershipDTO;
import model.TeamSyncDTO;
import model.UserInfoDTO;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TeamService {

    @Headers({
            "User-Agent: Mobile-Android",
            "Content-Type:application/json"
    })

    @POST("team/create")
    Call<List<TeamMemebershipDTO>> createTeam(@Body TeamDTO teamDTO);

    @GET("team/checkMember")
    Call<UserInfoDTO> checkMember(@Query("email") String email);

    @DELETE("team/{teamId}")
    Call<ResponseBody> deleteTeam(@Path("teamId") Integer teamId);

    @PUT("team/{teamId}")
    Call<ResponseBody> updateTeam(@Path("teamId") Integer teamId, @Body TeamDTO teamDTO);

    @PUT("team/members/{teamId}")
    Call<List<TeamMemebershipDTO>> updateTeamMembers(@Path("teamId") Integer teamId, @Body TeamDTO teamDTO);

    @GET("team/sync")
    Call<TeamSyncDTO> synchronizationTeam(@Query("email") String email, @Query("date") Long date);

}
