package ai.adaskids.coastie.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import ai.adaskids.coastie.data.local.HistoryRepository
import ai.adaskids.coastie.data.local.entity.HistoryEntryEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HistoryViewModel(
    private val repo: HistoryRepository
) : ViewModel() {

    val entries: StateFlow<List<HistoryEntryEntity>> =
        repo.observeAll().stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun clearAll() {
        viewModelScope.launch { repo.clear() }
    }

    fun delete(id: Long) {
        viewModelScope.launch { repo.deleteById(id) }
    }
}
