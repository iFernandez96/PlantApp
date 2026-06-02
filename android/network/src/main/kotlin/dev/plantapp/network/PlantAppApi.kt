package dev.plantapp.network

import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

/** Retrofit API for the Slice 1 backend endpoints. All routes require a bearer token,
 *  supplied by the [AuthTokenProvider] interceptor in [PlantAppApiFactory]. */
interface PlantAppApi {
    @POST("garden-spaces")
    suspend fun createGardenSpace(@Body body: CreateGardenSpaceRequest): GardenSpaceDto

    @POST("containers")
    suspend fun createContainer(@Body body: CreateContainerRequest): ContainerDto

    @POST("plants")
    suspend fun addPlant(@Body body: AddPlantRequest): AddPlantResponse

    @GET("plants")
    suspend fun listPlants(): List<PlantInstanceDto>

    @GET("plants/{id}")
    suspend fun getPlant(@Path("id") id: String): PlantInstanceDto

    @GET("plants/{id}/tasks")
    suspend fun getPlantTasks(@Path("id") id: String): List<CareTaskDto>

    @DELETE("plants/{id}")
    suspend fun deletePlant(@Path("id") id: String): retrofit2.Response<Unit>
}
