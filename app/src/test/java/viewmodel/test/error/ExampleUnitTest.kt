package viewmodel.test.error

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.cash.turbine.test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.withContext
import org.junit.Assert.assertEquals
import org.junit.Test

@Suppress("OPT_IN_USAGE")
class ExampleUnitTest {

    init { Dispatchers.setMain(UnconfinedTestDispatcher()) }

    @Test
    fun `tests if success is emitted`() = runTest {
        repeat(2000) {
            SimpleViewModel().stateFlow.test {
                assertEquals("Run count: $it", "Loading", awaitItem())
                assertEquals("Run count: $it", "Success", awaitItem())
                cancelAndIgnoreRemainingEvents()
            }
        }
    }
}

suspend fun read3rdPartyData(): String = withContext(Dispatchers.IO) { "Success" }

internal class SimpleViewModel : ViewModel() {

    val stateFlow = flow {
        emit("Loading")
        emit(read3rdPartyData())
    }.catch {
        emit("Error")
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Loading"
    )
}