package com.surrus.galwaybus.domain.interactor

import com.surrus.galwaybus.domain.repository.GalwayBusRepository
import com.surrus.galwaybus.model.BusStop
import com.surrus.galwaybus.model.Location
import kotlinx.coroutines.Deferred


open class GetNearestBusStopsUseCase constructor(val galwayRepository: GalwayBusRepository) {

    suspend fun getNearestBusStops(location: Location): Deferred<List<BusStop>> {
        return galwayRepository.getNearestBusStops(location)

//                .repeatWhen { completed -> completed.delay(30, TimeUnit.SECONDS) }
//                .retry(3)
    }
}