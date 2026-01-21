package com.maker.data.callapi

import com.maker.data.model.CharacterResponse
import retrofit2.http.GET

interface ApiMermaid {
    @GET("api/ST203_OCMakerFullBody")
    suspend fun getAllData(): CharacterResponse
}