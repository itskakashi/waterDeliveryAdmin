package com.example.authenticationn.DI


import AnalyticsManager
import BillingAndPaymentManager
import CanesManager
import OrderManager
import PendingPaymentManager
import UserManager
import com.example.admin.data.FireStoreDatabase.managers.AdminManager
import com.example.admin.domain.FireBaseRepository
import com.example.admin.presentation.FireBaseViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val appModule = module {





//      Repositories
single { OrderManager() }
    singleOf(:: UserManager)  // <--- Use the correct UserManager
    singleOf(:: OrderManager)


    singleOf(:: BillingAndPaymentManager)
    singleOf(:: AnalyticsManager)
    singleOf(:: CanesManager)
    singleOf(:: PendingPaymentManager)
    singleOf(:: AdminManager)

    singleOf(:: FireBaseRepository)






    viewModelOf(:: FireBaseViewModel)
//
//    // ViewModels
//    viewModel { OrderViewModel(get()) }
//    viewModel { BillViewModel(get()) }
//    viewModel { AnalyticsViewModel(get()) }



}