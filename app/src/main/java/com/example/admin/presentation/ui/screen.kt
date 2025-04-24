package com.example.admin.presentation.ui


import Customer
import kotlinx.serialization.Serializable
import kotlinx.serialization.Serializer

sealed class route(){

    @Serializable
    data object loginScreen: route()


    @Serializable
    data object dashBoardScreen: route()

    @Serializable
    data object orderPlaceScreen: route()
    @Serializable
    data object customerScreen: route()

    @Serializable
    data class customerDetailScreen( val userId: String?): route()

    @Serializable
    data object orderScreen: route()

    @Serializable
    data object paymentScreen: route()



}


