package io.github.dracula101.jetscan.data.platform.datasource.network.interceptors

import javax.inject.Inject
import javax.inject.Singleton

/**
 * An overall container for various [BaseUrlInterceptor] implementations for different API groups.
 */
@Singleton
class BaseUrlInterceptors @Inject constructor() {

    val apiInterceptor: BaseUrlInterceptor = BaseUrlInterceptor()

}