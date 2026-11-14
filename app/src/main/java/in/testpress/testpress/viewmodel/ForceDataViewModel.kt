package `in`.testpress.testpress.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import android.util.Log
import `in`.testpress.testpress.repository.ForceDataRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class ForceDataViewModel(private val repository: ForceDataRepository) : ViewModel() {
    enum class State { CHECKING, COMPLETE, INCOMPLETE }
    private val mutableState = MutableLiveData<State>()
    val state: LiveData<State> = mutableState
    private var checkJob: Job? = null

    fun consumeIncompleteMessage() {
        if (mutableState.value == State.INCOMPLETE) mutableState.value = null
    }

    fun cancelVerification() {
        checkJob?.cancel()
    }

    fun verifyCompletion(token: String) {
        if (checkJob?.isActive == true || mutableState.value == State.COMPLETE) return
        checkJob = viewModelScope.launch(Dispatchers.IO) {
            mutableState.postValue(State.CHECKING)
            repeat(3) { attempt ->
                try {
                    if (repository.isDataCollected(token)) {
                        mutableState.postValue(State.COMPLETE)
                        return@launch
                    }
                } catch (exception: CancellationException) {
                    throw exception
                } catch (exception: Exception) {
                    Log.w("ForceDataViewModel", "Unable to verify mandatory details", exception)
                }
                if (attempt < 2) delay(3000)
            }
            mutableState.postValue(State.INCOMPLETE)
        }
    }
}
