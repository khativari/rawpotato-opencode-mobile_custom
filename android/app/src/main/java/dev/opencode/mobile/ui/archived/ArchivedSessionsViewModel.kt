package dev.opencode.mobile.ui.archived

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.opencode.mobile.domain.event.ForegroundEventStream
import dev.opencode.mobile.domain.home.HomeProjectsSnapshot
import dev.opencode.mobile.domain.preferences.HomeDisplayPreferences
import dev.opencode.mobile.domain.session.SessionDomainAdapter
import dev.opencode.mobile.domain.session.SessionRepository
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject

/** Screen Model of the Archived sessions screen; the logic lives in [ArchivedSessionsModel]. */
@HiltViewModel
class ArchivedSessionsViewModel
    @Inject
    constructor(
        sessionRepository: SessionRepository,
        sessionDomainAdapter: SessionDomainAdapter,
        homeProjectsSnapshot: HomeProjectsSnapshot,
        foregroundEventStream: ForegroundEventStream,
    ) : ViewModel() {
        private val model =
            ArchivedSessionsModel(
                sessionRepository = sessionRepository,
                sessionDomainAdapter = sessionDomainAdapter,
                projects = homeProjectsSnapshot.projects,
                events = foregroundEventStream.events,
                scope = viewModelScope,
                worktreesEnabled = { HomeDisplayPreferences.showsWorktrees },
            )
        val uiState: StateFlow<ArchivedSessionsUiState> = model.state

        fun bind(scopeId: String) = model.bind(scopeId)

        // custom: массовое выделение архивных сессий.
        private val _selection = kotlinx.coroutines.flow.MutableStateFlow<Set<String>>(emptySet())
        val selection: kotlinx.coroutines.flow.StateFlow<Set<String>> = _selection

        /** custom: из шторки действий войти в режим выделения с этой сессией. */
        fun selectFromActions() {
            val id = model.state.value.actionTarget?.id ?: return
            model.dismissActions()
            _selection.value = setOf(id)
        }

        fun toggleSelection(sessionId: String) {
            _selection.value =
                if (sessionId in _selection.value) _selection.value - sessionId else _selection.value + sessionId
        }

        fun clearSelection() {
            _selection.value = emptySet()
        }

        fun unarchiveSelected() {
            val ids = _selection.value
            if (ids.isEmpty()) return
            model.unarchiveMany(ids)
            _selection.value = emptySet()
        }

        fun deleteSelected() {
            val ids = _selection.value
            if (ids.isEmpty()) return
            model.deleteMany(ids)
            _selection.value = emptySet()
        }

        fun retry() = model.retry()

        fun showAllProjects() = model.showAllProjects()

        fun openActions(row: ArchivedSessionRow) = model.openActions(row)

        fun dismissActions() = model.dismissActions()

        fun chooseOpen(): ArchivedSessionRow? = model.chooseOpen()

        fun chooseUnarchive() = model.chooseUnarchive()

        fun chooseDelete() = model.chooseDelete()

        fun dismissDeleteConfirm() = model.dismissDeleteConfirm()

        fun confirmDelete() = model.confirmDelete()

        fun undoUnarchive(sessionId: String) = model.undoUnarchive(sessionId)

        fun consumeSnackbar() = model.consumeSnackbar()
    }
