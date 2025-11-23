package ca.gbc.comp3074.uiprototype.data.supabase

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

object CoroutineScopeHelper {
    @JvmStatic
    val ioScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
}
